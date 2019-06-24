package com.marand.thinkmed.medications.warnings.internal;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.marand.ispek.common.Dictionary;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.property.MedicationPropertyType;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import com.marand.thinkmed.medications.mentalhealth.impl.ConsentFormFromEhrProvider;
import com.marand.thinkmed.medications.service.WarningSeverity;
import com.marand.thinkmed.medications.service.WarningType;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider;
import com.marand.thinkmed.request.time.RequestDateTimeHolder;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @author Nejc Korasa
 */

@SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
@Component
public class AntipsychoticMaxDoseWarningsHandler
{
  private MedicationsValueHolderProvider medicationsValueHolderProvider;
  private ConsentFormFromEhrProvider consentFormFromEhrProvider;
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private RequestDateTimeHolder requestDateTimeHolder;
  private MedicationsBo medicationsBo;

  @Autowired
  public void setMedicationsValueHolderProvider(final MedicationsValueHolderProvider medicationsValueHolderProvider)
  {
    this.medicationsValueHolderProvider = medicationsValueHolderProvider;
  }

  @Autowired
  public void setConsentFormFromEhrProvider(final ConsentFormFromEhrProvider consentFormFromEhrProvider)
  {
    this.consentFormFromEhrProvider = consentFormFromEhrProvider;
  }

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Autowired
  public void setRequestDateTimeHolder(final RequestDateTimeHolder requestDateTimeHolder)
  {
    this.requestDateTimeHolder = requestDateTimeHolder;
  }

  @Autowired
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  public List<MedicationsWarningDto> getWarnings(final @NonNull String patientId, final @NonNull List<TherapyDto> therapies)
  {
    return buildMaxDoseWarnings(
        getAntipsychotics(therapies),
        getCumulativeAntipsychoticPercentage(therapies),
        getLastConsentFormMaxDosePercentage(patientId));
  }

  public Integer getPatientsCumulativeAntipsychoticPercentage(final @NonNull String patientId)
  {
    final List<TherapyDto> therapies = medicationsBo.convertInpatientPrescriptionsToTherapies(
        medicationsOpenEhrDao.getPrescriptionsWithMaxDosePercentage(requestDateTimeHolder.getRequestTimestamp(), patientId),
        null,
        null,
        null);

    return getCumulativeAntipsychoticPercentage(therapies);
  }

  private Integer getCumulativeAntipsychoticPercentage(final List<TherapyDto> therapies)
  {
    return therapies.stream()
        .filter(t -> t.getMaxDosePercentage() != null)
        .filter(this::antipsychoticsExist)
        .mapToInt(TherapyDto::getMaxDosePercentage)
        .sum();
  }

  public List<NamedExternalDto> getAntipsychotics(final @NonNull List<TherapyDto> therapies)
  {
    return therapies.stream()
        .filter(t -> t.getMaxDosePercentage() != null)
        .flatMap(t -> extractAntipsychotics(t).stream())
        .collect(Collectors.toList());
  }

  public boolean hasAntipsychotics(final @NonNull List<TherapyDto> therapies)
  {
    return !getAntipsychotics(therapies).isEmpty();
  }

  private Integer getLastConsentFormMaxDosePercentage(final String patientId)
  {
    return consentFormFromEhrProvider.getLatestMentalHealthDocument(patientId)
        .filter(d -> d.getMaxDosePercentage() != null)
        .map(MentalHealthDocumentDto::getMaxDosePercentage)
        .orElse(null);
  }

  List<MedicationsWarningDto> buildMaxDoseWarnings(
      final List<NamedExternalDto> medications,
      final int percentage,
      final Integer overrideMaxPercentage)
  {
    //noinspection IfMayBeConditional
    if (percentage <= 100 || medications.isEmpty())
    {
      return Collections.emptyList();
    }
    else
    {
      return Collections.singletonList(new MedicationsWarningDto(
          buildWarningDescription(percentage, overrideMaxPercentage),
          (overrideMaxPercentage == null || percentage > overrideMaxPercentage) ? WarningSeverity.HIGH_OVERRIDE : WarningSeverity.HIGH,
          WarningType.MAX_DOSE,
          medications));
    }
  }

  private String buildWarningDescription(final int percentage, final Integer overrideMaxPercentage)
  {
    final StringBuilder sb = new StringBuilder(
        Dictionary.getEntry("cumulative.dose.of.antipsychotic.is")
            + " "
            + percentage + Dictionary.getEntry("of.maximum.recommended.dose")
            + ".");

    if (overrideMaxPercentage != null)
    {
      sb.append(" " + Dictionary.getEntry("patient.cumulative.upper.limit.set.to") + " " + overrideMaxPercentage + "%.");
    }

    return sb.toString();
  }

  private List<NamedExternalDto> extractAntipsychotics(final TherapyDto therapy)
  {
    return therapy.getMedicationIds()
        .stream()
        .map(id -> medicationsValueHolderProvider.getMedicationData(id))
        .filter(m -> m.hasProperty(MedicationPropertyType.ANTIPSYCHOTIC_TAG))
        .map(m ->  new NamedExternalDto(String.valueOf(m.getMedication().getId()), m.getMedication().getName()))
        .collect(Collectors.toList());
  }

  private boolean antipsychoticsExist(final TherapyDto therapy)
  {
    return therapy.getMedicationIds()
        .stream()
        .map(id -> medicationsValueHolderProvider.getMedicationData(id))
        .anyMatch(m -> m.hasProperty(MedicationPropertyType.ANTIPSYCHOTIC_TAG));
  }
}
