package com.marand.thinkmed.medications.titration.impl;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.time.Intervals;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.administration.AdministrationProvider;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.api.internal.dto.TitrationType;
import com.marand.thinkmed.medications.config.MedsProperties;
import com.marand.thinkmed.medications.connector.MedicationsConnector;
import com.marand.thinkmed.medications.connector.data.object.BolusQuantityWithTimeDto;
import com.marand.thinkmed.medications.connector.data.object.QuantityWithTimeDto;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.MedicationActionEnum;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.TherapyForTitrationDto;
import com.marand.thinkmed.medications.dto.TitrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationResultEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.administration.DoseAdministration;
import com.marand.thinkmed.medications.dto.overview.RateTherapyRowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyRowDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.overview.OverviewContentProvider;
import com.marand.thinkmed.medications.titration.TitrationDataProvider;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */
@Component
public class TitrationDataProviderImpl implements TitrationDataProvider
{
  private MedicationsConnector medicationsConnector;
  private MedicationsValueHolderProvider medicationsValueHolderProvider;
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private AdministrationProvider administrationProvider;
  private OverviewContentProvider overviewContentProvider;
  private MedsProperties medsProperties;

  @Autowired
  public void setMedicationsConnector(final MedicationsConnector medicationsConnector)
  {
    this.medicationsConnector = medicationsConnector;
  }

  @Autowired
  public void setMedicationsValueHolderProvider(final MedicationsValueHolderProvider medicationsValueHolderProvider)
  {
    this.medicationsValueHolderProvider = medicationsValueHolderProvider;
  }

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Autowired
  public void setAdministrationProvider(final AdministrationProvider administrationProvider)
  {
    this.administrationProvider = administrationProvider;
  }

  @Autowired
  public void setOverviewContentProvider(final OverviewContentProvider overviewContentProvider)
  {
    this.overviewContentProvider = overviewContentProvider;
  }

  @Autowired
  public void setMedsProperties(final MedsProperties medsProperties)
  {
    this.medsProperties = medsProperties;
  }

  @Override
  public TitrationDto getDataForTitration(
      final @NonNull String patientId,
      final @NonNull String therapyId,
      final @NonNull TitrationType titrationType,
      final @NonNull DateTime searchStart,
      final @NonNull DateTime searchEnd,
      final @NonNull DateTime when,
      final @NonNull Locale locale)
  {
    final TitrationDto titrationDto = new TitrationDto();
    titrationDto.setTitrationType(titrationType);
    titrationDto.setName(Dictionary.getEntry(titrationType.name(), locale));
    titrationDto.setUnit(titrationType.getUnit());

    final Interval searchInterval = new Interval(searchStart, searchEnd);

    fillTherapiesAndMedicationData(patientId, therapyId, titrationType, titrationDto, searchInterval, when, locale);
    titrationDto.setResults(getObservationResults(patientId, titrationDto.getTitrationType(), searchInterval));

    return titrationDto;
  }

  private void fillTherapiesAndMedicationData(
      final String patientId,
      final String administrationTherapyId,
      final TitrationType titrationType,
      final TitrationDto titrationDto,
      final Interval interval,
      final DateTime when,
      final Locale locale)
  {
    final List<InpatientPrescription> inpatientPrescriptions =
        medicationsOpenEhrDao.findInpatientPrescriptions(patientId, Intervals.infiniteFrom(interval.getStart()))
            .stream()
            .filter(p -> !isTherapyCanceled(p))
            .collect(Collectors.toList());

    final List<AdministrationDto> administrations = administrationProvider.getPrescriptionsAdministrations(
        patientId,
        inpatientPrescriptions,
        null,
        true);

    final List<TherapyRowDto> therapyRows = overviewContentProvider.buildTherapyRows(
        patientId,
        inpatientPrescriptions,
        administrations,
        Collections.emptyList(),
        TherapySortTypeEnum.DESCRIPTION_ASC,
        false,
        Collections.emptyList(),
        null,
        interval,
        null,
        locale,
        when);

    for (final TherapyRowDto therapyRow : therapyRows)
    {
      final Long mainMedicationId = therapyRow.getTherapy().getMainMedicationId();

      if (mainMedicationId != null)
      {
        final MedicationDataDto medicationData = medicationsValueHolderProvider.getMedicationData(mainMedicationId);

        if (medicationData.getTitration() == titrationType)
        {
          final TherapyForTitrationDto therapyForTitration = buildTherapyForTitration(therapyRow);
          titrationDto.getTherapies().add(therapyForTitration);
        }

        if (therapyRow.getTherapyId().equals(administrationTherapyId))
        {
          titrationDto.setMedicationData(medicationData);
        }
      }
    }
  }

  private boolean isTherapyCanceled(final InpatientPrescription inpatientPrescription)
  {
    return inpatientPrescription.getActions().stream()
        .map(MedicationActionEnum::getActionEnum)
        .anyMatch(e -> e == MedicationActionEnum.CANCEL);
  }

  private TherapyForTitrationDto buildTherapyForTitration(final TherapyRowDto therapyRow)
  {
    final TherapyForTitrationDto therapyForTitration = new TherapyForTitrationDto();
    therapyForTitration.setTherapy(therapyRow.getTherapy());

    therapyRow.getAdministrations()
        .stream()
        .filter(administration -> AdministrationTypeEnum.MEDICATION_ADMINISTRATION.contains(administration.getAdministrationType()))
        .filter(administration -> AdministrationResultEnum.ADMINISTERED.contains(administration.getAdministrationResult()))
        .forEach(administration -> addAdministrationToTherapyForTitration(administration, therapyForTitration));

    if (therapyRow instanceof RateTherapyRowDto)
    {
      therapyForTitration.setInfusionFormulaAtIntervalStart(((RateTherapyRowDto)therapyRow).getInfusionFormulaAtIntervalStart());
      if (therapyForTitration.getDoseUnit() == null)
      {
        therapyForTitration.setDoseUnit(((RateTherapyRowDto)therapyRow).getFormulaUnit());
      }
    }

    return therapyForTitration;
  }

  private void addAdministrationToTherapyForTitration(
      final AdministrationDto administration,
      final TherapyForTitrationDto therapyForTitration)
  {
    final QuantityWithTimeDto quantityWithTimeDto;
    String doseUnit = null;

    final DateTime administrationTime = administration.getAdministrationTime();
    final String comment = administration.getComment();

    if (administration instanceof DoseAdministration)
    {
      final TherapyDoseDto administeredDose = ((DoseAdministration)administration).getAdministeredDose();
      if (therapyForTitration.getTherapy().isWithRate())
      {
        if (administration.getAdministrationType() == AdministrationTypeEnum.BOLUS)
        {
          quantityWithTimeDto = new BolusQuantityWithTimeDto(
              administrationTime,
              null,
              comment,
              administeredDose.getNumerator(),
              administeredDose.getNumeratorUnit());
        }
        else
        {
          quantityWithTimeDto = new QuantityWithTimeDto(administrationTime, administeredDose.getDenominator(), comment);
          doseUnit = administeredDose.getDenominatorUnit();
        }
      }
      else
      {
        quantityWithTimeDto = new QuantityWithTimeDto(administrationTime, administeredDose.getNumerator(), comment);
        doseUnit = administeredDose.getNumeratorUnit();
      }
    }
    else
    {
      quantityWithTimeDto = new QuantityWithTimeDto(administrationTime, null, comment);
    }

    if (therapyForTitration.getDoseUnit() == null)
    {
      therapyForTitration.setDoseUnit(doseUnit);
    }

    therapyForTitration.getAdministrations().add(quantityWithTimeDto);
  }

  @Override
  public List<QuantityWithTimeDto> getObservationResults(
      final @NonNull String patientId,
      final @NonNull TitrationType titrationType,
      final @NonNull Interval interval)
  {
    if (titrationType == TitrationType.BLOOD_SUGAR)
    {
      return medicationsConnector.getBloodSugarObservations(patientId, interval);
    }
    else if (titrationType == TitrationType.MAP)
    {
      return medicationsConnector.findMeanArterialPressureMeasurements(patientId, interval);
    }
    else if (titrationType == TitrationType.INR)
    {
      return medicationsConnector.getLabResults(patientId, medsProperties.getInrResultCode(), interval);
    }
    else if (titrationType == TitrationType.APTTR)
    {
      return medicationsConnector.getLabResults(patientId, medsProperties.getApttrResultCode(), interval);

    }
    else
    {
      throw new IllegalArgumentException("Titration type " + titrationType.name() + " not supported!");
    }
  }
}
