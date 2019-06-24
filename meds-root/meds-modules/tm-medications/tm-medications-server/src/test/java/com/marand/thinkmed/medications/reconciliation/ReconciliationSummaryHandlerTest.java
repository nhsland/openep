package com.marand.thinkmed.medications.reconciliation;

import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;
import com.marand.thinkmed.medications.api.internal.dto.CodedNameDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.change.TherapyChangeCalculator;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionReconciliationDto;
import com.marand.thinkmed.medications.dto.change.StringTherapyChangeDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeType;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeReconciliationDto;
import com.marand.thinkmed.medications.dto.reconsiliation.ReconciliationRowDto;
import com.marand.thinkmed.medications.dto.reconsiliation.ReconciliationRowGroupEnum;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * @author Mitja Lapajne
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ReconciliationSummaryHandlerTest
{
  @InjectMocks
  private final ReconciliationSummaryHandler reconciliationSummaryHandler = new ReconciliationSummaryHandler();

  @Mock
  private TherapyChangeCalculator therapyChangeCalculator;

  @Before
  public void resetMocks()
  {
    Mockito.reset(therapyChangeCalculator);
  }

  @Test
  public void testFillChangeDetails1()
  {
    //unchanged from admission to discharge, without change reason

    final Locale locale = new Locale("en");

    //admission
    final ConstantSimpleTherapyDto admissionTherapy = new ConstantSimpleTherapyDto();
    final MedicationOnAdmissionReconciliationDto admission = buildAdmission(admissionTherapy, null);

    //discharge
    final ConstantSimpleTherapyDto dischargeTherapy = new ConstantSimpleTherapyDto();
    final MedicationOnDischargeReconciliationDto discharge = buildDischarge(dischargeTherapy, null);

    //mock
    Mockito
        .when(therapyChangeCalculator.calculateTherapyChanges(admissionTherapy, dischargeTherapy, false, locale))
        .thenReturn(Lists.newArrayList());

    final ReconciliationRowDto reconciliationRow = new ReconciliationRowDto();
    reconciliationSummaryHandler.fillChangeDetails(admission, discharge, null, reconciliationRow, locale);

    assertEquals(ReconciliationRowGroupEnum.NOT_CHANGED, reconciliationRow.getGroupEnum());
  }

  @Test
  public void testFillChangeDetails2()
  {
    //unchanged from admission to inpatient
    //changed from inpatient to discharge

    //admission
    final ConstantSimpleTherapyDto admissionTherapy = new ConstantSimpleTherapyDto();
    final MedicationOnAdmissionReconciliationDto admission = buildAdmission(admissionTherapy, null);

    //discharge
    final ConstantSimpleTherapyDto dischargeTherapy = new ConstantSimpleTherapyDto();
    final TherapyChangeReasonDto changeReason = buildChangeReason("reason1");
    final MedicationOnDischargeReconciliationDto discharge = buildDischarge(dischargeTherapy, changeReason);

    //mock
    final StringTherapyChangeDto doseChange = new StringTherapyChangeDto(TherapyChangeType.DOSE);
    doseChange.setOldValue("500 mg");
    doseChange.setNewValue("1000 mg");
    final List<TherapyChangeDto<?, ?>> changes = Lists.newArrayList(doseChange);

    final Locale locale = new Locale("en");
    Mockito
        .when(therapyChangeCalculator.calculateTherapyChanges(admissionTherapy, dischargeTherapy, false, locale))
        .thenReturn(changes);

    final ReconciliationRowDto reconciliationRow = new ReconciliationRowDto();
    reconciliationSummaryHandler.fillChangeDetails(admission, discharge, null, reconciliationRow, locale);

    assertEquals(ReconciliationRowGroupEnum.CHANGED, reconciliationRow.getGroupEnum());
    assertEquals(changeReason, reconciliationRow.getChangeReasonDto());
    assertEquals(changes, reconciliationRow.getChanges());
  }

  @Test
  public void testFillChangeDetails3()
  {
    //changed from admission to inpatient
    //unchanged from inpatient to discharge

    //admission
    final ConstantSimpleTherapyDto admissionTherapy = new ConstantSimpleTherapyDto();
    final TherapyChangeReasonDto changeReason = buildChangeReason("reason1");
    final MedicationOnAdmissionReconciliationDto admission = buildAdmission(admissionTherapy, changeReason);

    //discharge
    final ConstantSimpleTherapyDto dischargeTherapy = new ConstantSimpleTherapyDto();
    final MedicationOnDischargeReconciliationDto discharge = buildDischarge(dischargeTherapy, null);

    //mock
    final StringTherapyChangeDto doseChange = new StringTherapyChangeDto(TherapyChangeType.DOSE);
    doseChange.setOldValue("500 mg");
    doseChange.setNewValue("1000 mg");
    final List<TherapyChangeDto<?, ?>> changes = Lists.newArrayList(doseChange);

    final Locale locale = new Locale("en");
    Mockito
        .when(therapyChangeCalculator.calculateTherapyChanges(admissionTherapy, dischargeTherapy, false, locale))
        .thenReturn(changes);

    final ReconciliationRowDto reconciliationRow = new ReconciliationRowDto();
    reconciliationSummaryHandler.fillChangeDetails(admission, discharge, null, reconciliationRow, locale);

    assertEquals(ReconciliationRowGroupEnum.CHANGED, reconciliationRow.getGroupEnum());
    assertEquals(changeReason, reconciliationRow.getChangeReasonDto());
    assertEquals(changes, reconciliationRow.getChanges());
  }

  @Test
  public void testFillChangeDetails4()
  {
    //unchanged from admission to inpatient
    //changed during inpatient
    //unchanged from inpatient to discharge

    //admission
    final ConstantSimpleTherapyDto admissionTherapy = new ConstantSimpleTherapyDto();
    final MedicationOnAdmissionReconciliationDto admission = buildAdmission(admissionTherapy, null);

    //discharge
    final ConstantSimpleTherapyDto dischargeTherapy = new ConstantSimpleTherapyDto();
    final MedicationOnDischargeReconciliationDto discharge = buildDischarge(dischargeTherapy, null);

    //mock
    final StringTherapyChangeDto doseChange = new StringTherapyChangeDto(TherapyChangeType.DOSE);
    doseChange.setOldValue("500 mg");
    doseChange.setNewValue("1000 mg");
    final List<TherapyChangeDto<?, ?>> changes = Lists.newArrayList(doseChange);

    final Locale locale = new Locale("en");
    Mockito
        .when(therapyChangeCalculator.calculateTherapyChanges(admissionTherapy, dischargeTherapy, false, locale))
        .thenReturn(changes);

    final ReconciliationRowDto reconciliationRow = new ReconciliationRowDto();
    final TherapyChangeReasonDto changeReason = buildChangeReason("reason1");
    reconciliationSummaryHandler.fillChangeDetails(admission, discharge, changeReason, reconciliationRow, locale);

    assertEquals(ReconciliationRowGroupEnum.CHANGED, reconciliationRow.getGroupEnum());
    assertEquals(changeReason, reconciliationRow.getChangeReasonDto());
    assertEquals(changes, reconciliationRow.getChanges());
  }

  @Test
  public void testFillChangeDetails5()
  {
    //changed from admission to inpatient
    //changed during inpatient
    //changed from inpatient to discharge

    //admission
    final ConstantSimpleTherapyDto admissionTherapy = new ConstantSimpleTherapyDto();
    final TherapyChangeReasonDto changeReasonAdmission = buildChangeReason("reasonAdmission");
    final MedicationOnAdmissionReconciliationDto admission = buildAdmission(admissionTherapy, changeReasonAdmission);

    //discharge
    final ConstantSimpleTherapyDto dischargeTherapy = new ConstantSimpleTherapyDto();
    final TherapyChangeReasonDto changeReasonDischarge = buildChangeReason("reasonDischarge");
    final MedicationOnDischargeReconciliationDto discharge = buildDischarge(dischargeTherapy, changeReasonDischarge);

    //mock
    final StringTherapyChangeDto doseChange = new StringTherapyChangeDto(TherapyChangeType.DOSE);
    doseChange.setOldValue("500 mg");
    doseChange.setNewValue("1000 mg");
    final List<TherapyChangeDto<?, ?>> changes = Lists.newArrayList(doseChange);

    final Locale locale = new Locale("en");
    Mockito
        .when(therapyChangeCalculator.calculateTherapyChanges(admissionTherapy, dischargeTherapy, false, locale))
        .thenReturn(changes);

    final ReconciliationRowDto reconciliationRow = new ReconciliationRowDto();
    final TherapyChangeReasonDto changeReasonInpatient = buildChangeReason("reasonInpatient");
    reconciliationSummaryHandler.fillChangeDetails(admission, discharge, changeReasonInpatient, reconciliationRow, locale);

    assertEquals(ReconciliationRowGroupEnum.CHANGED, reconciliationRow.getGroupEnum());
    assertEquals(changeReasonDischarge, reconciliationRow.getChangeReasonDto());
    assertEquals(changes, reconciliationRow.getChanges());
  }

  @Test
  public void testFillChangeDetails6()
  {
    //changed from admission to inpatient
    //changed from inpatient to discharge back to therapy identical to admission

    final Locale locale = new Locale("en");

    //admission
    final ConstantSimpleTherapyDto admissionTherapy = new ConstantSimpleTherapyDto();
    final TherapyChangeReasonDto changeReasonAdmission = buildChangeReason("reasonAdmission");
    final MedicationOnAdmissionReconciliationDto admission = buildAdmission(admissionTherapy, changeReasonAdmission);

    //discharge
    final ConstantSimpleTherapyDto dischargeTherapy = new ConstantSimpleTherapyDto();
    final TherapyChangeReasonDto changeReasonDischarge = buildChangeReason("reasonDischarge");
    final MedicationOnDischargeReconciliationDto discharge = buildDischarge(dischargeTherapy, changeReasonDischarge);

    //mock
    Mockito
        .when(therapyChangeCalculator.calculateTherapyChanges(admissionTherapy, dischargeTherapy, false, locale))
        .thenReturn(Lists.newArrayList());

    final ReconciliationRowDto reconciliationRow = new ReconciliationRowDto();
    reconciliationSummaryHandler.fillChangeDetails(admission, discharge, null, reconciliationRow, locale);

    assertEquals(ReconciliationRowGroupEnum.NOT_CHANGED, reconciliationRow.getGroupEnum());
  }

  public MedicationOnDischargeReconciliationDto buildDischarge(
      final ConstantSimpleTherapyDto dischargeTherapy,
      final TherapyChangeReasonDto changeReason)
  {
    return new MedicationOnDischargeReconciliationDto(
        dischargeTherapy,
        "uid1",
        null,
        changeReason,
        null);
  }

  public MedicationOnAdmissionReconciliationDto buildAdmission(
      final ConstantSimpleTherapyDto admissionTherapy,
      final TherapyChangeReasonDto changeReason)
  {
    return new MedicationOnAdmissionReconciliationDto(
        admissionTherapy,
        changeReason,
        null);
  }

  public TherapyChangeReasonDto buildChangeReason(final String reason)
  {
    final TherapyChangeReasonDto changeReason = new TherapyChangeReasonDto();
    changeReason.setChangeReason(new CodedNameDto(reason, reason));
    return changeReason;
  }
}
