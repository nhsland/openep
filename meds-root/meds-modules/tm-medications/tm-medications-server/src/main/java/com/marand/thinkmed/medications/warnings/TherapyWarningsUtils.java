package com.marand.thinkmed.medications.warnings;

import java.util.Collections;
import java.util.List;

import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.api.internal.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.OxygenTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.service.dto.WarningScreenMedicationDto;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.toList;

/**
 * @author Nejc Korasa
 */

@Component
public class TherapyWarningsUtils
{
  private MedicationsValueHolderProvider medicationsValueHolderProvider;

  @Autowired
  public void setMedicationsValueHolderProvider(final MedicationsValueHolderProvider medicationsValueHolderProvider)
  {
    this.medicationsValueHolderProvider = medicationsValueHolderProvider;
  }

  public List<WarningScreenMedicationDto> extractWarningScreenMedicationDtos(final @NonNull List<TherapyDto> therapies)
  {
    return therapies.stream().flatMap(t -> buildWarningScreenMedicationDtos(t).stream()).collect(toList());
  }

  private List<WarningScreenMedicationDto> buildWarningScreenMedicationDtos(final TherapyDto therapy)
  {
    if (therapy instanceof SimpleTherapyDto)
    {
      final MedicationDto medication = ((SimpleTherapyDto)therapy).getMedication();
      if (medication.getId() != null)
      {
        return Collections.singletonList(buildWarningsScreenMedicationDto(medication, therapy));
      }
    }
    if (therapy instanceof ComplexTherapyDto)
    {
      return ((ComplexTherapyDto)therapy).getIngredientsList().stream()
          .map(InfusionIngredientDto::getMedication)
          .filter(m -> m.getId() != null)
          .map(m -> buildWarningsScreenMedicationDto(m, therapy))
          .collect(toList());
    }
    if (therapy instanceof OxygenTherapyDto)
    {
      final MedicationDto medication = ((OxygenTherapyDto)therapy).getMedication();
      if (medication.getId() != null)
      {
        return Collections.singletonList(buildWarningsScreenMedicationDto(medication, therapy));
      }
    }
    return Collections.emptyList();
  }

  private WarningScreenMedicationDto buildWarningsScreenMedicationDto(
      final MedicationDto medication,
      final TherapyDto therapy)
  {
    final WarningScreenMedicationDto warningScreenMedication = new WarningScreenMedicationDto();
    warningScreenMedication.setId(medication.getId());
    warningScreenMedication.setName(medication.getName());
    warningScreenMedication.setRouteId(therapy.getRoutes().isEmpty() ? null : therapy.getRoutes().get(0).getId());

    /*This combination is appropriate only for english production, where there is used both FDB and SNOMED code. In any
    other combination (ie. slo production + FDB) this will not work. We need to refactor this in this way:
    1.) add routes to medication_external_cross_tab table and pair them with external codes which different warning provider
     use.
    2.) add property on server, which will tell if openep maps route id to this external id (from table) before sending it
    to warning provider. In another scenario, code will be sent to external warning provider. */
    warningScreenMedication.setRouteExternalId(therapy.getRoutes().isEmpty() ? null : therapy.getRoutes().get(0).getCode());


    warningScreenMedication.setProduct(medicationsValueHolderProvider.getMedicationData(medication.getId()).getMedicationLevel() != MedicationLevelEnum.VTM);
    return warningScreenMedication;
  }
}
