package com.marand.thinkmed.medications.therapy.converter.fromehr;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.marand.maf.core.Opt;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.api.internal.dto.DosageJustificationEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicalDeviceEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRole;
import com.marand.thinkmed.medications.api.internal.dto.MedicationTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.TitrationType;
import com.marand.thinkmed.medications.api.internal.dto.DoseFormDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.OxygenStartingDevice;
import com.marand.thinkmed.medications.api.internal.dto.dose.ComplexDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.DoseRangeDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.SimpleDoseElementDto;
import com.marand.thinkmed.medications.ehr.model.Dosage;
import com.marand.thinkmed.medications.ehr.model.MedicalDevice;
import com.marand.thinkmed.medications.ehr.model.Medication;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.utils.EhrValueUtils;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider;
import org.openehr.jaxb.rm.DataValue;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvInterval;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.DvTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */

@Component
public class TherapyFromEhrUtils
{
  private MedicationsValueHolderProvider medicationsValueHolderProvider;

  @Autowired
  public void setMedicationsValueHolderProvider(final MedicationsValueHolderProvider medicationsValueHolderProvider)
  {
    this.medicationsValueHolderProvider = medicationsValueHolderProvider;
  }

  /**
   * Builds medication from constituent. If medication is prescribed from universal form and medication is not in db, map
   * medication type data from constituent.
   *
   * @param constituent medication
   *
   * @return MedicationDto
   */
  public MedicationDto buildMedication(final Medication constituent)
  {
    final DvText medicationItem = constituent.getComponentName();

    if (medicationItem instanceof DvCodedText) // if DvCodedText then medication exists in database
    {
      final String medicationId = ((DvCodedText)medicationItem).getDefiningCode().getCodeString();
      return medicationsValueHolderProvider.getMedication(Long.parseLong(medicationId));
    }
    else
    {
      final MedicationDto medication = new MedicationDto();
      medication.setName(medicationItem.getValue());

      if (constituent.getRole() != null)
      {
        final MedicationRole medicationRole = MedicationRole.valueOf(constituent.getRole());
        medication.setMedicationType(MedicationTypeEnum.fromMedicationRole(medicationRole));
      }
      else
      {
        medication.setMedicationType(MedicationTypeEnum.MEDICATION);
      }

      return medication;
    }
  }

  /**
   * Builds medication from constituent. If medication is prescribed from universal form and medication is not in db,
   * medication type data is NOT filled - use {@link #buildMedication(Medication)}
   *
   * @param medicationItem medication item
   *
   * @return MedicationDto
   */
  public MedicationDto buildMedication(final DvText medicationItem)
  {
    if (medicationItem instanceof DvCodedText) // if DvCodedText then medication exists in database
    {
      final String medicationId = ((DvCodedText)medicationItem).getDefiningCode().getCodeString();
      return medicationsValueHolderProvider.getMedication(Long.parseLong(medicationId));
    }
    else
    {
      final MedicationDto medication = new MedicationDto();
      medication.setName(medicationItem.getValue());
      return medication;
    }
  }

  public DoseFormDto buildDoseForm(final DvText doseForm)
  {
    if (doseForm instanceof DvCodedText)
    {
      final long doseFormId = Long.parseLong(((DvCodedText)doseForm).getDefiningCode().getCodeString());
      final DoseFormDto dbDoseFormDto = medicationsValueHolderProvider.getDoseForm(doseFormId);
      if (dbDoseFormDto != null)
      {
        return dbDoseFormDto;
      }
      final DoseFormDto doseFormDto = new DoseFormDto();
      doseFormDto.setId(doseFormId);
      doseFormDto.setName(doseForm.getValue());
      return doseFormDto;
    }
    return null;
  }

  public List<HourMinuteDto> extractDoseTimes(final MedicationOrder medicationOrder)
  {
    final Opt<List<DvTime>> times = Opt.resolve(
        () -> medicationOrder.getStructuredDoseAndTimingDirections().get(0)
            .getDosage().get(0).getTiming().getSpecificTime());
    if (times.isPresent())
    {
      return times.get().stream()
          .map(EhrValueUtils::getTime)
          .collect(Collectors.toList());
    }
    return new ArrayList<>();
  }

  public TitrationType extractTitration(final MedicationOrder medicationOrder)
  {
    return medicationOrder.getDosageJustification().stream()
        .map(DosageJustificationEnum::valueOf)
        .map(TitrationType::valueOf)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  public OxygenStartingDevice buildOxygenDevice(final MedicalDevice medicalDevice)
  {
    if (medicalDevice == null)
    {
      return null;
    }

    final OxygenStartingDevice oxygenStartingDevice = new OxygenStartingDevice(MedicalDeviceEnum.valueOf(medicalDevice.getName()));
    oxygenStartingDevice.setRouteType(EhrValueUtils.getText(medicalDevice.getType()));
    return oxygenStartingDevice;
  }

  public SimpleDoseElementDto extractSimpleDoseElement(final Dosage dosage)
  {
    final SimpleDoseElementDto doseElement = new SimpleDoseElementDto();
    final DataValue doseAmount = dosage.getDoseAmount();
    final DataValue alternateDoseAmount = dosage.getAlternateDoseAmount();

    if (doseAmount instanceof DvQuantity)
    {
      doseElement.setQuantity(((DvQuantity)doseAmount).getMagnitude());
    }
    else if (doseAmount instanceof DvInterval)
    {
      final DoseRangeDto doseRange = new DoseRangeDto();
      final DvInterval doseInterval = (DvInterval)doseAmount;
      doseRange.setMinNumerator(Opt.resolve(() -> ((DvQuantity)doseInterval.getLower()).getMagnitude()).orElse(null));
      doseRange.setMaxNumerator(Opt.resolve(() -> ((DvQuantity)doseInterval.getUpper()).getMagnitude()).orElse(null));

      if (alternateDoseAmount instanceof DvInterval)
      {
        final DvInterval alternateDoseInterval = (DvInterval)alternateDoseAmount;
        doseRange.setMinDenominator(Opt.resolve(
            () -> ((DvQuantity)alternateDoseInterval.getLower()).getMagnitude()).orElse(null));
        doseRange.setMaxDenominator(Opt.resolve(
            () -> ((DvQuantity)alternateDoseInterval.getUpper()).getMagnitude()).orElse(null));
      }
      doseElement.setDoseRange(doseRange);
    }

    if (dosage.getAlternateDoseAmount() instanceof DvQuantity)
    {
      doseElement.setQuantityDenominator(((DvQuantity)dosage.getAlternateDoseAmount()).getMagnitude());
    }
    if (dosage.getDoseDescription() != null)
    {
      doseElement.setDoseDescription(dosage.getDoseDescription().getValue());
    }
    return doseElement;
  }

  public ComplexDoseElementDto extractComplexDoseElement(final Dosage dosage)
  {
    final ComplexDoseElementDto doseElement = new ComplexDoseElementDto();
    final Double rate = extractRate(dosage);
    if (rate == null)
    {
      return null;
    }

    doseElement.setRate(rate);
    doseElement.setRateUnit(extractRateUnit(dosage));
    doseElement.setDuration(extractDuration(dosage));

    final String doseFormula = EhrValueUtils.getText(dosage.getDoseFormula());
    doseElement.setRateFormula(extractDoseFormulaValue(doseFormula));
    doseElement.setRateFormulaUnit(extractDoseFormulaUnit(doseFormula));

    return doseElement;
  }

  // Dose formula value and unit are stored as a space comma separated String ("5.0 mg/kg/h")
  public Double extractDoseFormulaValue(final String doseFormula)
  {
    if (doseFormula != null)
    {
      final int splitIndex = doseFormula.indexOf(' ');
      if (splitIndex > 0)
      {
        try
        {
          return Double.valueOf(doseFormula.substring(0, splitIndex));
        }
        catch (final NumberFormatException ignored)
        {
        }
      }
    }
    return null;
  }

  // Dose formula value and unit are stored as a space comma separated String ("5.0 mg/kg/h")
  public String extractDoseFormulaUnit(final String doseFormula)
  {
    if (doseFormula != null)
    {
      final int splitIndex = doseFormula.indexOf(' ');
      final String doseFormulaUnit = doseFormula.substring(splitIndex + 1, doseFormula.length());
      if (!doseFormulaUnit.isEmpty())
      {
        return doseFormulaUnit;
      }
    }
    return null;
  }

  public Double extractRate(final Dosage dosage)
  {
    if (dosage != null && dosage.getAdministrationRate() instanceof DvQuantity)
    {
      return ((DvQuantity)dosage.getAdministrationRate()).getMagnitude();
    }
    return null;
  }

  public String extractRateUnit(final Dosage dosage)
  {
    if (dosage != null && dosage.getAdministrationRate() instanceof DvQuantity)
    {
      return ((DvQuantity)dosage.getAdministrationRate()).getUnits();
    }
    return null;
  }

  private Integer extractDuration(final Dosage dosage)
  {
    return Opt.resolve(() -> DataValueUtils.getPeriod(dosage.getAdministrationDuration()).getMinutes()).orElse(null);
  }

  public boolean isBaselineInfusion(final MedicationOrder medicationOrder)
  {
    return Opt.resolve(() -> medicationOrder.getAdditionalDetails().getBaselineInfusion().isValue()).orElse(false);
  }
}