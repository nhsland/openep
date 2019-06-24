package com.marand.thinkmed.medications.admission;

import java.util.Locale;

import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.TherapySourceGroupEnum;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.MedicationActionEnum;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionStatus;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import com.marand.thinkmed.medications.ehr.model.MedicationOnAdmission;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.model.composition.Context;
import com.marand.thinkmed.medications.test.MedicationsTestUtils;
import com.marand.thinkmed.medications.therapy.converter.TherapyConverter;
import org.joda.time.DateTime;
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
public class MedicationOnAdmissionHandlerTest
{
  @InjectMocks
  private final MedicationOnAdmissionHandler medicationOnAdmissionHandler = new MedicationOnAdmissionHandler();

  @Mock
  private TherapyConverter therapyConverter;

  @Mock
  private TherapyDisplayProvider therapyDisplayProvider;

  @Test
  public void testMapToAdmissionDto()
  {
    final MedicationOnAdmission composition = new MedicationOnAdmission();
    final Context context = new Context();
    context.setStartTime(DataValueUtils.getDateTime(new DateTime(2018, 6, 19, 10, 0, 0)));
    composition.setMedicationOrder(new MedicationOrder());
    composition.setContext(context);

    final MedicationManagement action = MedicationsTestUtils.buildMedicationAction(
        MedicationActionEnum.MODIFY_EXISTING,
        new DateTime(2018, 6, 18, 10, 0));
    action.getReason().add(DataValueUtils.getLocalCodedText("10", "Coded reason"));
    action.getReason().add(DataValueUtils.getText("Reason comment"));
    composition.getActions().add(action);

    composition.getMedicationOrder().getAdditionalDetails().setSourcePrescriptionIdentifier(
        DataValueUtils.getText(TherapySourceGroupEnum.createEhrString(
            TherapySourceGroupEnum.LAST_DISCHARGE_MEDICATIONS,
            "uid2")));

    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setCompositionUid("uid1");

    Mockito
        .when(therapyConverter.convertToTherapyDto(
            composition.getMedicationOrder(),
            composition.getUid(),
            DataValueUtils.getDateTime(composition.getContext().getStartTime())))
        .thenReturn(therapy);

    final MedicationOnAdmissionDto admissionDto = medicationOnAdmissionHandler.mapToAdmissionDto(
        composition, false,
        new Locale("en"));
    assertEquals("uid1", admissionDto.getTherapy().getCompositionUid());
    assertEquals("uid2", admissionDto.getSourceId());
    assertEquals(TherapySourceGroupEnum.LAST_DISCHARGE_MEDICATIONS, admissionDto.getSourceGroupEnum());
    assertEquals(MedicationOnAdmissionStatus.EDITED_AND_PRESCRIBED, admissionDto.getStatus());
    assertEquals("10", admissionDto.getChangeReasonDto().getChangeReason().getCode());
    assertEquals("Coded reason", admissionDto.getChangeReasonDto().getChangeReason().getName());
    assertEquals("Reason comment", admissionDto.getChangeReasonDto().getComment());
  }
}