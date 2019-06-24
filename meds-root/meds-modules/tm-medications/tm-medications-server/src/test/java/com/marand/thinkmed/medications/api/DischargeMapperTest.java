package com.marand.thinkmed.medications.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Lists;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.maf.core.data.object.NamedIdDto;
import com.marand.maf.core.dictionary.MafDictionary;
import com.marand.maf.core.server.util.DefinedLocaleHolder;
import com.marand.thinkmed.medications.api.external.dto.DischargeDetailsDto;
import com.marand.thinkmed.medications.api.external.dto.DischargeListDto;
import com.marand.thinkmed.medications.api.external.dto.DischargeListItemDto;
import com.marand.thinkmed.medications.api.external.dto.DischargeSummaryDto;
import com.marand.thinkmed.medications.api.external.dto.DischargeSummaryItemDto;
import com.marand.thinkmed.medications.api.external.dto.DischargeSummaryStatusEnum;
import com.marand.thinkmed.medications.api.external.dto.PrescriptionDto;
import com.marand.thinkmed.medications.api.internal.dto.CodedNameDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.IndicationDto;
import com.marand.thinkmed.medications.api.internal.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseType;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.discharge.ControlledDrugSupplyDto;
import com.marand.thinkmed.medications.api.internal.dto.DispenseDetailsDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.TimedSimpleDoseElementDto;
import com.marand.thinkmed.medications.dto.property.MedicationPropertyDto;
import com.marand.thinkmed.medications.api.internal.dto.property.MedicationPropertyType;
import com.marand.thinkmed.medications.dto.reconsiliation.ReconciliationRowDto;
import com.marand.thinkmed.medications.dto.reconsiliation.ReconciliationRowGroupEnum;
import com.marand.thinkmed.medications.dto.reconsiliation.ReconciliationSummaryDto;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolder;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Mitja Lapajne
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class DischargeMapperTest
{
  @InjectMocks
  private final DischargeMapper dischargeMapper = new DischargeMapper();

  @Mock
  private MedicationsValueHolder medicationsValueHolder;

  @Before
  public void setup()
  {
    Dictionary.setDelegate(new MafDictionary("Dictionary", "", new Locale("en")));
    DefinedLocaleHolder.INSTANCE.setLocale(new Locale("en"));

    final MedicationDataDto data = new MedicationDataDto();
    data.setProperty(new MedicationPropertyDto(1L, MedicationPropertyType.CONTROLLED_DRUG, "Controlled drug"));
    final Map<Long, MedicationDataDto> medications = new HashMap<>();
    medications.put(1L, data);
    Mockito.when(medicationsValueHolder.getMedications())
        .thenReturn(medications);
  }

  @Test
  public void testMapDischargeList()
  {
    final List<MedicationOnDischargeDto> medicationsOnDischarge = buildMedicationsOnDischarge();
    final DischargeListDto summary = dischargeMapper.mapDischargeList(medicationsOnDischarge);
    assertEquals(2, summary.getItems().size());

    final DischargeListItemDto item0 = summary.getItems().get(0);
    assertConstantSimplePrescription(item0.getPrescription());
    assertEquals("abcd|Medication instruction", item0.getId());
    assertEquals("Pharmacy", item0.getDischarge().getDispenseSource().getDisplay());
    assertNull(item0.getDischarge().getDuration());
    assertEquals(1, item0.getDischarge().getQuantities().getItems().size());
    assertEquals("1 pack", item0.getDischarge().getQuantities().getItems().get(0).getDisplay());
    assertEquals(1, item0.getDischarge().getAdditionalData().size());
    assertEquals("targetInr", item0.getDischarge().getAdditionalData().get(0).getKey());
    assertEquals("5.0", item0.getDischarge().getAdditionalData().get(0).getValue());
    assertEquals("MR 24", item0.getPrescription().getReleaseCharacteristics().getDisplay());

    final DischargeListItemDto item1 = summary.getItems().get(1);
    assertConstantComplexPrescription(item1.getPrescription());
    assertEquals("abcd|Medication instruction", item1.getId());
    assertTrue(item1.getDischarge().getAdditionalData().isEmpty());
    assertEquals("Pharmacy", item1.getDischarge().getDispenseSource().getDisplay());
    assertEquals("15 days", item1.getDischarge().getDuration().getDisplay());
    assertNull(item1.getDischarge().getQuantities());
  }

  @Test
  public void testMapDischargeSummary()
  {
    final ReconciliationSummaryDto reconciliationSummaryDto = buildReconciliationSummaryDto();
    final DischargeSummaryDto summary = dischargeMapper.mapDischargeSummary(reconciliationSummaryDto);

    assertEquals(2, summary.getItems().size());

    final DischargeSummaryItemDto item0 = summary.getItems().get(0);
    assertConstantSimplePrescription(item0.getPrescription());
    assertEquals("abcd|Medication instruction", item0.getId());
    assertEquals(DischargeSummaryStatusEnum.STOPPED, item0.getStatus());
    assertEquals("Stopped due to a reaction - comment1", item0.getStatusReason().getDisplay());
    assertEquals("Pharmacy", item0.getDischarge().getDispenseSource().getDisplay());
    assertNull(item0.getDischarge().getDuration());
    assertEquals(1, item0.getDischarge().getQuantities().getItems().size());
    assertEquals("1 pack", item0.getDischarge().getQuantities().getItems().get(0).getDisplay());
    assertEquals(1, item0.getDischarge().getAdditionalData().size());
    assertEquals("targetInr", item0.getDischarge().getAdditionalData().get(0).getKey());
    assertEquals("5.0", item0.getDischarge().getAdditionalData().get(0).getValue());
    assertEquals("MR 24", item0.getPrescription().getReleaseCharacteristics().getDisplay());

    final DischargeSummaryItemDto item1 = summary.getItems().get(1);
    assertConstantComplexPrescription(item1.getPrescription());
    assertEquals("abcd|Medication instruction", item1.getId());
    assertEquals(DischargeSummaryStatusEnum.NEW, item1.getStatus());
    assertNull(item1.getStatusReason());
    assertTrue(item1.getDischarge().getAdditionalData().isEmpty());
    assertEquals("Pharmacy", item1.getDischarge().getDispenseSource().getDisplay());
    assertEquals("15 days", item1.getDischarge().getDuration().getDisplay());
    assertNull(item1.getDischarge().getQuantities());
  }

  @Test
  public void testMapControlledDrugDischarge()
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    final DispenseDetailsDto dispenseDetails = new DispenseDetailsDto();

    final ControlledDrugSupplyDto supply1 = new ControlledDrugSupplyDto();
    supply1.setMedication(new NamedIdDto(1L, "Morphine 10mg"));
    supply1.setQuantity(20);
    supply1.setUnit("tbl");
    dispenseDetails.getControlledDrugSupply().add(supply1);

    final ControlledDrugSupplyDto supply2 = new ControlledDrugSupplyDto();
    supply2.setMedication(new NamedIdDto(1L, "Morphine 5mg"));
    supply2.setQuantity(15);
    supply2.setUnit("tbl");
    dispenseDetails.getControlledDrugSupply().add(supply2);

    therapy.setDispenseDetails(dispenseDetails);
    final DischargeDetailsDto dischargeDetails = dischargeMapper.mapDischarge(therapy);

    assertNotNull(dischargeDetails.getQuantities());
    assertEquals(2, dischargeDetails.getQuantities().getItems().size());

    assertEquals("Morphine 10mg - 20 tbl", dischargeDetails.getQuantities().getItems().get(0).getDisplay());
    assertEquals(20, (long)dischargeDetails.getQuantities().getItems().get(0).getQuantity().getValue());
    assertEquals("tbl", dischargeDetails.getQuantities().getItems().get(0).getQuantity().getUnit());
    assertEquals("20 tbl", dischargeDetails.getQuantities().getItems().get(0).getQuantity().getDisplay());
    assertEquals("twenty", dischargeDetails.getQuantities().getItems().get(0).getQuantity().getTextValue());
    assertEquals("Morphine 10mg", dischargeDetails.getQuantities().getItems().get(0).getMedication().getDisplay());

    assertEquals("Morphine 5mg - 15 tbl", dischargeDetails.getQuantities().getItems().get(1).getDisplay());
    assertEquals(15, (long)dischargeDetails.getQuantities().getItems().get(1).getQuantity().getValue());
    assertEquals("tbl", dischargeDetails.getQuantities().getItems().get(1).getQuantity().getUnit());
    assertEquals("15 tbl", dischargeDetails.getQuantities().getItems().get(1).getQuantity().getDisplay());
    assertEquals("fifteen", dischargeDetails.getQuantities().getItems().get(1).getQuantity().getTextValue());
    assertEquals("Morphine 5mg", dischargeDetails.getQuantities().getItems().get(1).getMedication().getDisplay());
  }

  @Test
  public void testMapDischargeProtocol()
  {
    final VariableSimpleTherapyDto therapyDto = new VariableSimpleTherapyDto();
    therapyDto.setCompositionUid("ccc");
    therapyDto.setEhrOrderName("Medication instruction");
    therapyDto.setTherapyDescription("Paracetamol - VARIABLE DOSE - Oral");
    therapyDto.setMedication(buildMedicationDto(1L, "Paracetamol"));
    final MedicationRouteDto route = new MedicationRouteDto();
    route.setName("Oral");
    therapyDto.getRoutes().add(route);
    therapyDto.setQuantityDisplay("VARIABLE DOSE");

    final TimedSimpleDoseElementDto element1 = new TimedSimpleDoseElementDto();
    element1.setQuantityDisplay("500 mg");
    element1.setTimingDescription("one week");
    element1.setTimeDisplay("one week");
    therapyDto.getTimedDoseElements().add(element1);

    final TimedSimpleDoseElementDto element2 = new TimedSimpleDoseElementDto();
    element2.setQuantityDisplay("200 mg");
    element2.setTimingDescription("until next visit");
    element2.setTimeDisplay("until next visit");
    therapyDto.getTimedDoseElements().add(element2);

    final PrescriptionDto prescription = dischargeMapper.mapPrescription(therapyDto);

    assertEquals("Oral", prescription.getRoutes().getDisplay());
    assertEquals("500 mg - one week", prescription.getDose().getItems().get(0).getDisplay());
    assertEquals("500 mg", prescription.getDose().getItems().get(0).getQuantity().getDisplay());
    assertEquals("one week", prescription.getDose().getItems().get(0).getTiming().getDisplay());
    assertEquals("one week", prescription.getDose().getItems().get(0).getTiming().getDescriptiveTiming());
    assertEquals("200 mg - until next visit", prescription.getDose().getItems().get(1).getDisplay());
    assertEquals("200 mg", prescription.getDose().getItems().get(1).getQuantity().getDisplay());
    assertEquals("until next visit", prescription.getDose().getItems().get(1).getTiming().getDisplay());
    assertEquals("until next visit", prescription.getDose().getItems().get(1).getTiming().getDescriptiveTiming());
  }

  @Test
  public void testMapSimpleVariable()
  {
    final VariableSimpleTherapyDto therapyDto = new VariableSimpleTherapyDto();
    therapyDto.setCompositionUid("abcd");
    therapyDto.setEhrOrderName("Medication instruction");
    therapyDto.setTherapyDescription("Paracetamol - Variable dose - Oral");
    therapyDto.setMedication(buildMedicationDto(1L, "Paracetamol"));

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setName("Oral");
    therapyDto.getRoutes().add(route);

    therapyDto.setQuantityDisplay("Variable dose");

    final TimedSimpleDoseElementDto doseElement1 = new TimedSimpleDoseElementDto();
    doseElement1.setQuantityDisplay("500 mg");
    doseElement1.setTimeDisplay("8:10");
    doseElement1.setDoseTime(new HourMinuteDto(8, 10));
    therapyDto.getTimedDoseElements().add(doseElement1);

    final TimedSimpleDoseElementDto doseElement2 = new TimedSimpleDoseElementDto();
    doseElement2.setQuantityDisplay("1000 mg");
    doseElement2.setTimeDisplay("16:00");
    doseElement2.setDoseTime(new HourMinuteDto(16, 0));
    therapyDto.getTimedDoseElements().add(doseElement2);

    therapyDto.setDispenseDetails(new DispenseDetailsDto());

    final PrescriptionDto prescription = dischargeMapper.mapPrescription(therapyDto);

    assertEquals("Oral", prescription.getRoutes().getDisplay());
    assertEquals("500 mg - 8:10", prescription.getDose().getItems().get(0).getDisplay());
    assertEquals("500 mg", prescription.getDose().getItems().get(0).getQuantity().getDisplay());
    assertEquals("8:10", prescription.getDose().getItems().get(0).getTiming().getDisplay());
    assertNull(prescription.getDose().getItems().get(0).getTiming().getDescriptiveTiming());
    assertEquals(8, prescription.getDose().getItems().get(0).getTiming().getLocalTime().getHour());
    assertEquals(10, prescription.getDose().getItems().get(0).getTiming().getLocalTime().getMinute());
    assertEquals("1000 mg - 16:00", prescription.getDose().getItems().get(1).getDisplay());
    assertEquals("1000 mg", prescription.getDose().getItems().get(1).getQuantity().getDisplay());
    assertEquals("16:00", prescription.getDose().getItems().get(1).getTiming().getDisplay());
    assertNull(prescription.getDose().getItems().get(1).getTiming().getDescriptiveTiming());
    assertEquals(16, prescription.getDose().getItems().get(1).getTiming().getLocalTime().getHour());
    assertEquals(0, prescription.getDose().getItems().get(1).getTiming().getLocalTime().getMinute());
  }

  private void assertConstantSimplePrescription(final PrescriptionDto prescription)
  {
    assertEquals("Paracetamol - 500 mg - Every 2 days - Oral", prescription.getDisplay());
    assertEquals("1", prescription.getMedication().getId());
    assertEquals("Paracetamol", prescription.getMedication().getName());
    assertNull(prescription.getDoseForm());
    assertEquals("Oral", prescription.getRoutes().getDisplay());
    assertEquals(1, prescription.getRoutes().getItems().size());
    assertEquals("Oral", prescription.getRoutes().getItems().get(0).getName());
    assertEquals(1, prescription.getDose().getItems().size());
    assertEquals("500 mg", prescription.getDose().getItems().get(0).getDisplay());
    assertEquals("1X per day - Every 2 days", prescription.getTimingDirections().getDisplay());
    assertEquals("Comme", prescription.getComment().getDisplay());
    assertEquals("Indi", prescription.getIndication().getDisplay());
    assertEquals("Before meal", prescription.getAdditionalInstructions().getDisplay());
    assertEquals(1, prescription.getAdditionalInstructions().getItems().size());
    assertEquals("Before meal", prescription.getAdditionalInstructions().getItems().get(0).getDisplay());
  }

  private void assertConstantComplexPrescription(final PrescriptionDto prescription)
  {
    assertEquals("Dopamin 5 mg/10 ml, Glucose 90ml - 100 ml - Every 2 hours - IV", prescription.getDisplay());
    assertNull(prescription.getMedication().getId());
    assertEquals("Dopamin - Glucose", prescription.getMedication().getName());
    assertNull(prescription.getDoseForm());
    assertEquals("IV", prescription.getRoutes().getDisplay());
    assertEquals(1, prescription.getRoutes().getItems().size());
    assertEquals("IV", prescription.getRoutes().getItems().get(0).getName());
    assertEquals(1, prescription.getDose().getItems().size());
    assertEquals("100 ml", prescription.getDose().getItems().get(0).getDisplay());
    assertEquals("Every 2 hours", prescription.getTimingDirections().getDisplay());
    assertNull(prescription.getComment());
    assertNull(prescription.getIndication());
    assertNull(prescription.getAdditionalInstructions());
  }

  private List<MedicationOnDischargeDto> buildMedicationsOnDischarge()
  {
    final MedicationOnDischargeDto constantSimple = new MedicationOnDischargeDto();
    constantSimple.setTherapy(buildConstantSimpleTherapyDto());

    final MedicationOnDischargeDto constantComplex = new MedicationOnDischargeDto();
    constantComplex.setTherapy(buildConstantComplexTherapyDto());

    final List<MedicationOnDischargeDto> medicationsOnDischarge = new ArrayList<>();
    medicationsOnDischarge.add(constantSimple);
    medicationsOnDischarge.add(constantComplex);
    return medicationsOnDischarge;
  }

  private ReconciliationSummaryDto buildReconciliationSummaryDto()
  {
    final List<ReconciliationRowDto> rows = Lists.newArrayList(
        buildReconciliationRowDto(
            buildConstantSimpleTherapyDto(),
            ReconciliationRowGroupEnum.ONLY_ON_ADMISSION,
            "Stopped due to a reaction",
            "comment1"),
        buildReconciliationRowDto(
            buildConstantComplexTherapyDto(),
            ReconciliationRowGroupEnum.ONLY_ON_DISCHARGE,
            null,
            null)
    );
    return new ReconciliationSummaryDto(rows, new DateTime(2019, 4, 10, 12, 0), true, true, null, null);
  }

  private TherapyDto buildConstantSimpleTherapyDto()
  {
    final ConstantSimpleTherapyDto therapyDto = new ConstantSimpleTherapyDto();
    therapyDto.setCompositionUid("abcd");
    therapyDto.setEhrOrderName("Medication instruction");
    therapyDto.setTherapyDescription("Paracetamol - 500 mg - Every 2 days - Oral");
    therapyDto.setMedication(buildMedicationDto(1L, "Paracetamol"));
    final MedicationRouteDto route = new MedicationRouteDto();
    route.setName("Oral");
    therapyDto.getRoutes().add(route);
    therapyDto.setQuantityDisplay("500 mg");
    therapyDto.setFrequencyDisplay("1X per day");
    therapyDto.setDaysFrequencyDisplay("Every 2 days");
    therapyDto.setClinicalIndication(new IndicationDto("1", "Indi"));
    therapyDto.setComment("Comme");
    therapyDto.setApplicationPreconditionDisplay("Before meal");
    therapyDto.setTargetInr(5.0);
    final ReleaseDetailsDto releaseDetails = new ReleaseDetailsDto(ReleaseType.MODIFIED_RELEASE, 24);
    releaseDetails.setDisplay("MR 24");
    therapyDto.setReleaseDetails(releaseDetails);

    final DispenseDetailsDto dispenseDetails = new DispenseDetailsDto();
    dispenseDetails.setQuantity(1);
    dispenseDetails.setUnit("pack");

    final NamedIdDto dispenseSource = new NamedIdDto();
    dispenseSource.setId(1L);
    dispenseSource.setName("Pharmacy");
    dispenseDetails.setDispenseSource(dispenseSource);

    therapyDto.setDispenseDetails(dispenseDetails);
    return therapyDto;
  }

  private TherapyDto buildConstantComplexTherapyDto()
  {
    final ConstantComplexTherapyDto therapyDto = new ConstantComplexTherapyDto();
    therapyDto.setCompositionUid("abcd");
    therapyDto.setEhrOrderName("Medication instruction");
    therapyDto.setTherapyDescription("Dopamin 5 mg/10 ml, Glucose 90ml - 100 ml - Every 2 hours - IV");
    therapyDto.getIngredientsList().add(buildInfusionIngredientMedication());
    therapyDto.getIngredientsList().add(buildInfusionIngredientDiluent());
    final MedicationRouteDto route = new MedicationRouteDto();
    route.setName("IV");
    therapyDto.getRoutes().add(route);
    therapyDto.setVolumeSumDisplay("100 ml");
    therapyDto.setFrequencyDisplay("Every 2 hours");

    final DispenseDetailsDto dispenseDetails = new DispenseDetailsDto();
    dispenseDetails.setDaysDuration(15);

    final NamedIdDto dispenseSource = new NamedIdDto();
    dispenseSource.setId(1L);
    dispenseSource.setName("Pharmacy");
    dispenseDetails.setDispenseSource(dispenseSource);

    therapyDto.setDispenseDetails(dispenseDetails);
    return therapyDto;
  }

  private MedicationDto buildMedicationDto(final Long id, final String displayName)
  {
    final MedicationDto medicationDto = new MedicationDto();
    medicationDto.setId(id);
    medicationDto.setDisplayName(displayName);
    return medicationDto;
  }

  private InfusionIngredientDto buildInfusionIngredientMedication()
  {
    final InfusionIngredientDto infusionIngredientDto = new InfusionIngredientDto();
    infusionIngredientDto.setMedication(buildMedicationDto(1L, "Dopamin"));
    infusionIngredientDto.setQuantityDisplay("5 mg/10 ml");
    return infusionIngredientDto;
  }

  private InfusionIngredientDto buildInfusionIngredientDiluent()
  {
    final InfusionIngredientDto infusionIngredientDto = new InfusionIngredientDto();
    infusionIngredientDto.setMedication(buildMedicationDto(1L, "Glucose"));
    infusionIngredientDto.setQuantityDisplay("90 ml");
    return infusionIngredientDto;
  }

  private ReconciliationRowDto buildReconciliationRowDto(
      final TherapyDto therapyDto,
      final ReconciliationRowGroupEnum groupEnum,
      final String statusChangeReason,
      final String statusChangeReasonComment)
  {
    final ReconciliationRowDto row = new ReconciliationRowDto();
    row.setGroupEnum(groupEnum);
    if (statusChangeReason != null)
    {
      final TherapyChangeReasonDto changeReasonDto = new TherapyChangeReasonDto();
      changeReasonDto.setChangeReason(new CodedNameDto("a", statusChangeReason));
      changeReasonDto.setComment(statusChangeReasonComment);
      row.setChangeReasonDto(changeReasonDto);
    }
    if (groupEnum == ReconciliationRowGroupEnum.ONLY_ON_ADMISSION)
    {
      row.setTherapyOnAdmission(therapyDto);
    }
    else
    {
      row.setTherapyOnDischarge(therapyDto);
    }
    return row;
  }
}