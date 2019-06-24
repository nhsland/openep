package com.marand.thinkmed.medications.warnings;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.api.internal.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.OxygenTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.service.dto.WarningScreenMedicationDto;
import com.marand.thinkmed.medications.test.MedicationsTestUtils;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;

/**
 * @author Nejc Korasa
 */

@SuppressWarnings({"TooBroadScope"})
@RunWith(SpringJUnit4ClassRunner.class)
public class TherapyWarningsUtilsTest
{
  @InjectMocks
  private TherapyWarningsUtils therapyWarningsUtils;

  @Mock
  private MedicationsValueHolderProvider medicationsValueHolderProvider;

  @Test
  public void testExtractWarningsSearchDtos()
  {
    final MedicationDataDto medication = new MedicationDataDto();
    medication.setMedicationLevel(MedicationLevelEnum.VTM);
    Mockito
        .when(medicationsValueHolderProvider.getMedicationData(anyLong()))
        .thenReturn(medication);

    final List<TherapyDto> therapies = new ArrayList<>();

    final ConstantSimpleTherapyDto simpleTherapy = new ConstantSimpleTherapyDto();
    simpleTherapy.setMedication(MedicationsTestUtils.buildMedicationDto(10L, "Aspirin"));
    simpleTherapy.getRoutes().add(MedicationsTestUtils.buildRoute(100L, "Oral", MedicationRouteTypeEnum.ORAL));
    therapies.add(simpleTherapy);

    final ConstantComplexTherapyDto complexTherapy = new ConstantComplexTherapyDto();
    complexTherapy.getRoutes().add(MedicationsTestUtils.buildRoute(200L, "IV", MedicationRouteTypeEnum.IV));
    complexTherapy.getIngredientsList().add(
        MedicationsTestUtils.buildInfusionIngredientDto(
            MedicationsTestUtils.buildMedicationDto(20L, "Dopamin"),
            10.0,
            "mg",
            5.0,
            "ml"));
    complexTherapy.getIngredientsList().add(
        MedicationsTestUtils.buildInfusionIngredientDto(
            MedicationsTestUtils.buildMedicationDto(30L, "Glocose"),
            5.0,
            "mg",
            100.0,
            "ml"));
    therapies.add(complexTherapy);

    final OxygenTherapyDto oxygenTherapy = new OxygenTherapyDto();
    oxygenTherapy.setMedication(MedicationsTestUtils.buildMedicationDto(40L, "Oxygen"));
    oxygenTherapy.getRoutes().add(MedicationsTestUtils.buildRoute(100L, "Oral", MedicationRouteTypeEnum.ORAL));
    therapies.add(oxygenTherapy);

    final List<WarningScreenMedicationDto> warningsSearchDtos = therapyWarningsUtils.extractWarningScreenMedicationDtos(therapies);

    assertEquals(4, warningsSearchDtos.size());

    assertEquals(10L, warningsSearchDtos.get(0).getId());
    assertEquals("Aspirin", warningsSearchDtos.get(0).getName());
    assertEquals(100L, (long)warningsSearchDtos.get(0).getRouteId());

    assertEquals(20L, warningsSearchDtos.get(1).getId());
    assertEquals("Dopamin", warningsSearchDtos.get(1).getName());
    assertEquals(200L, (long)warningsSearchDtos.get(1).getRouteId());

    assertEquals(30L, warningsSearchDtos.get(2).getId());
    assertEquals("Glocose", warningsSearchDtos.get(2).getName());
    assertEquals(200L, (long)warningsSearchDtos.get(2).getRouteId());

    assertEquals(40L, warningsSearchDtos.get(3).getId());
    assertEquals("Oxygen", warningsSearchDtos.get(3).getName());
    assertEquals(100L, (long)warningsSearchDtos.get(3).getRouteId());
  }
}