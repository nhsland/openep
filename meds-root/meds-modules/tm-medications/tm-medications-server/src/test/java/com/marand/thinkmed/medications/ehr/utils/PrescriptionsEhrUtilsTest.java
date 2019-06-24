package com.marand.thinkmed.medications.ehr.utils;

import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.dto.MedicationActionEnum;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonEnum;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import com.marand.thinkmed.medications.ehr.model.composition.Context;
import com.marand.thinkmed.medications.test.MedicationsTestUtils;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Nejc Korasa
 */

public class PrescriptionsEhrUtilsTest
{
  @Test
  public void testGetTherapySuspendReasonForReissuedTherapy()
  {
    final InpatientPrescription inpatientPrescription = new InpatientPrescription();
    inpatientPrescription.setUid("uid1:1");

    inpatientPrescription.getActions().add(
        MedicationsEhrUtils.buildMedicationAction(
            inpatientPrescription,
            MedicationActionEnum.SUSPEND,
            InpatientPrescription.getMedicationOrderPath(),
            new DateTime(2016, 6, 10, 12, 0)));

    MedicationsEhrUtils.buildMedicationAction(
        inpatientPrescription,
        MedicationActionEnum.REISSUE,
        InpatientPrescription.getMedicationOrderPath(),
        new DateTime(2016, 6, 10, 13, 0));

    final TherapyChangeReasonDto therapySuspendReason = PrescriptionsEhrUtils.getSuspendReason(
        inpatientPrescription);
    assertNull(therapySuspendReason);
  }

  @Test
  public void testGetTherapySuspendReasonNoAction()
  {
    final InpatientPrescription inpatientPrescription = new InpatientPrescription();
    inpatientPrescription.setUid("uid1:1");

    final TherapyChangeReasonDto therapySuspendReason = PrescriptionsEhrUtils.getSuspendReason(
        inpatientPrescription);
    assertNull(therapySuspendReason);
  }

  @Test
  public void testIsInpatientPrescriptionCancelledOrAbortedWhenAborted()
  {
    final InpatientPrescription inpatientPrescription = new InpatientPrescription();
    inpatientPrescription.getActions().add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.START, new DateTime(2018, 6, 18, 10, 0)));
    inpatientPrescription.getActions().add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.SUSPEND, new DateTime(2018, 6, 18, 11, 0)));
    inpatientPrescription.getActions().add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.REISSUE, new DateTime(2018, 6, 18, 12, 0)));
    inpatientPrescription.getActions().add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.ABORT, new DateTime(2018, 6, 18, 13, 0)));

    assertTrue(PrescriptionsEhrUtils.isInpatientPrescriptionCancelledOrAborted(inpatientPrescription));
  }

  @Test
  public void testIsInpatientPrescriptionCancelledOrAbortedWhenCancelled()
  {
    final InpatientPrescription inpatientPrescription = new InpatientPrescription();
    inpatientPrescription.getActions().add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.START, new DateTime(2018, 6, 18, 10, 0)));
    inpatientPrescription.getActions().add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.CANCEL, new DateTime(2018, 6, 18, 11, 0)));

    assertTrue(PrescriptionsEhrUtils.isInpatientPrescriptionCancelledOrAborted(inpatientPrescription));
  }

  @Test
  public void testIsInpatientPrescriptionCancelledOrAbortedWhenActive()
  {
    final InpatientPrescription inpatientPrescription = new InpatientPrescription();
    inpatientPrescription.getActions().add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.START, new DateTime(2018, 6, 18, 10, 0)));

    assertFalse(PrescriptionsEhrUtils.isInpatientPrescriptionCancelledOrAborted(inpatientPrescription));
  }

  @Test
  public void testIsInpatientPrescriptionCompletedWhenAborted()
  {
    final InpatientPrescription inpatientPrescription = new InpatientPrescription();
    inpatientPrescription.getActions().add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.START, new DateTime(2018, 6, 18, 10, 0)));
    inpatientPrescription.getActions().add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.ABORT, new DateTime(2018, 6, 18, 13, 0)));

    assertTrue(PrescriptionsEhrUtils.isInpatientPrescriptionCompleted(inpatientPrescription));
  }

  @Test
  public void testIsInpatientPrescriptionCompletedWhenCompleted()
  {
    final InpatientPrescription inpatientPrescription = new InpatientPrescription();
    inpatientPrescription.getActions().add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.START, new DateTime(2018, 6, 18, 10, 0)));
    inpatientPrescription.getActions().add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.COMPLETE, new DateTime(2018, 6, 18, 13, 0)));

    assertTrue(PrescriptionsEhrUtils.isInpatientPrescriptionCompleted(inpatientPrescription));
  }

  @Test
  public void testIsInpatientPrescriptionCompletedWhenActive()
  {
    final InpatientPrescription inpatientPrescription = new InpatientPrescription();
    inpatientPrescription.getActions().add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.START, new DateTime(2018, 6, 18, 10, 0)));

    assertFalse(PrescriptionsEhrUtils.isInpatientPrescriptionCompleted(inpatientPrescription));
  }

  @Test
  public void testGetTherapySuspendReasonForSuspendedTherapy()
  {
    final InpatientPrescription inpatientPrescription = new InpatientPrescription();
    inpatientPrescription.setUid("uid1:1");

    final MedicationManagement suspendAction =
        MedicationsEhrUtils.buildMedicationAction(
            inpatientPrescription,
            MedicationActionEnum.SUSPEND,
            InpatientPrescription.getMedicationOrderPath(),
            new DateTime(2016, 6, 10, 12, 0));
    suspendAction.getReason().add(DataValueUtils.getLocalCodedText(
        TherapyChangeReasonEnum.TEMPORARY_LEAVE.toFullString(),
        TherapyChangeReasonEnum.TEMPORARY_LEAVE.toFullString()));
    inpatientPrescription.getActions().add(suspendAction);

    final TherapyChangeReasonDto therapySuspendReason = PrescriptionsEhrUtils.getSuspendReason(
        inpatientPrescription);
    assertNotNull(therapySuspendReason);
    assertNotNull(therapySuspendReason.getChangeReason());
    assertEquals(
        TherapyChangeReasonEnum.TEMPORARY_LEAVE.toFullString(),
        therapySuspendReason.getChangeReason().getCode());
    assertEquals(
        TherapyChangeReasonEnum.TEMPORARY_LEAVE.toFullString(),
        therapySuspendReason.getChangeReason().getName());
  }

  @Test
  public void testInpatientPrescriptionModifiedFromLastReviewWithUpdateLink()
  {
    final InpatientPrescription composition = new InpatientPrescription();
    composition.setUid("uid2::1");
    final Context context = new Context();
    context.setStartTime(DataValueUtils.getDateTime(new DateTime(2013, 5, 13, 5, 0)));
    composition.setContext(context);
    composition.getLinks().add(LinksEhrUtils.createLink("uid1::1", "update", EhrLinkType.UPDATE));
    composition.getActions().add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.SCHEDULE, new DateTime(2013, 5, 10, 12, 0)));
    composition.getActions().add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.START, new DateTime(2013, 5, 10, 12, 0)));
    composition.getActions().add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2013, 5, 10, 12, 0)));
    composition.getActions().add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2013, 5, 11, 12, 0)));
    composition.getActions().add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2013, 5, 12, 12, 0)));

    final boolean modified1 = PrescriptionsEhrUtils.isInpatientPrescriptionModifiedFromLastReview(composition);
    assertTrue(modified1);

    context.setStartTime(DataValueUtils.getDateTime(new DateTime(2013, 5, 12, 5, 0)));

    final boolean modified2 = PrescriptionsEhrUtils.isInpatientPrescriptionModifiedFromLastReview(composition);
    assertFalse(modified2);
  }

  @Test
  public void testInpatientPrescriptionModifiedFromLastReviewWithModifyAction()
  {
    final InpatientPrescription composition = new InpatientPrescription();
    composition.setUid("uid2::1");
    final Context context = new Context();
    context.setStartTime(DataValueUtils.getDateTime(new DateTime(2013, 5, 12, 5, 0)));
    composition.setContext(context);

    composition.getActions().add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.SCHEDULE, new DateTime(2013, 5, 10, 12, 0)));
    composition.getActions().add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.START, new DateTime(2013, 5, 10, 12, 0)));
    composition.getActions().add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2013, 5, 10, 12, 0)));
    composition.getActions().add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2013, 5, 11, 12, 0)));
    composition.getActions().add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2013, 5, 12, 12, 0)));
    composition.getActions().add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.MODIFY_EXISTING, new DateTime(2013, 5, 12, 13, 0)));

    final boolean modified = PrescriptionsEhrUtils.isInpatientPrescriptionModifiedFromLastReview(composition);
    assertTrue(modified);
  }
}