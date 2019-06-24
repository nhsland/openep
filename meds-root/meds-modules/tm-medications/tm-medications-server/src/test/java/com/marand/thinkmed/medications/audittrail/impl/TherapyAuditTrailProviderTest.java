package com.marand.thinkmed.medications.audittrail.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.audittrail.TherapyAuditTrailProvider;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.change.TherapyChangeCalculator;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.MedicationActionEnum;
import com.marand.thinkmed.medications.dto.TherapyActionHistoryDto;
import com.marand.thinkmed.medications.dto.TherapyActionHistoryType;
import com.marand.thinkmed.medications.dto.audittrail.TherapyAuditTrailDto;
import com.marand.thinkmed.medications.dto.change.StringTherapyChangeDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeType;
import com.marand.thinkmed.medications.ehr.model.Composer;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.model.composition.Context;
import com.marand.thinkmed.medications.ehr.model.pharmacist.PharmacyReviewReport;
import com.marand.thinkmed.medications.ehr.utils.LinksEhrUtils;
import com.marand.thinkmed.medications.ehr.utils.MedicationsEhrUtils;
import com.marand.thinkmed.medications.overview.OverviewContentProvider;
import com.marand.thinkmed.medications.therapy.ehr.TherapyEhrHandler;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openehr.jaxb.rm.Link;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Mitja Lapajne
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class TherapyAuditTrailProviderTest
{
  private static final Locale LOCALE = new Locale("en_GB");

  @InjectMocks
  private final TherapyAuditTrailProvider therapyAuditTrailProvider = new TherapyAuditTrailProviderImpl();

  @Mock
  private TherapyChangeCalculator therapyChangeCalculator;

  @Mock
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Mock
  private MedicationsBo medicationsBo;

  @Mock
  private TherapyEhrHandler therapyEhrHandler;

  @Mock
  private OverviewContentProvider overviewContentProvider;

  @Before
  public void setMocks()
  {
    final DateTime when = new DateTime(2016, 8, 12, 10, 0);

    Mockito
        .when(medicationsOpenEhrDao.getPatientLastReferenceWeight("p1", Intervals.infiniteTo(when)))
        .thenReturn(20.0);

    //NEW THERAPY
    final InpatientPrescription newInpatientPrescription = buildInpatientPrescription(
        "uid2::bla::2",
        "Instruction",
        "PerformerModify",
        new DateTime(2016, 8, 11, 8, 0));

    //add actions
    MedicationsEhrUtils.addMedicationActionTo(
        newInpatientPrescription,
        MedicationActionEnum.ABORT,
        new NamedExternalDto("PerformerAbort", "PerformerAbort"),
        new DateTime(2016, 8, 11, 23, 0));

    Mockito
        .when(medicationsOpenEhrDao.loadInpatientPrescription("p1", "uid2::bla::2"))
        .thenReturn(newInpatientPrescription);

    final ConstantSimpleTherapyDto newTherapy = new ConstantSimpleTherapyDto();
    newTherapy.setCompositionUid("uid2::bla::2");
    newTherapy.setCreatedTimestamp(new DateTime(2016, 8, 11, 18, 0));
    newTherapy.setStart(new DateTime(2016, 8, 11, 20, 0));
    newTherapy.setPrescriberName("PerformerModify");

    Mockito
        .when(medicationsBo.convertMedicationOrderToTherapyDto(
            newInpatientPrescription,
            newInpatientPrescription.getMedicationOrder(),
            20.0,
            null,
            true,
            LOCALE))
        .thenReturn(newTherapy);

    //OLD THERAPY
    final InpatientPrescription oldInpatientPrescription = buildInpatientPrescription(
        "uid1::bla::3",
        "Instruction",
        "PerformerPrescribe",
        new DateTime(2016, 8, 11, 8, 0));

    //add link
    final Link updateLink = LinksEhrUtils.createLink(
        oldInpatientPrescription.getUid(),
        "update",
        EhrLinkType.UPDATE);

    MedicationsEhrUtils.addMedicationActionTo(
        oldInpatientPrescription,
        MedicationActionEnum.COMPLETE,
        new NamedExternalDto("PerformerComplete", "PerformerComplete"),
        new DateTime(2016, 8, 11, 18, 0));

    newInpatientPrescription.getLinks().add(updateLink);
    newInpatientPrescription.getLinks().add(updateLink);

    final ConstantSimpleTherapyDto oldTherapy = new ConstantSimpleTherapyDto();
    oldTherapy.setCompositionUid("uid1::bla::3");
    oldTherapy.setCreatedTimestamp(new DateTime(2016, 8, 11, 8, 0));
    oldTherapy.setStart(new DateTime(2016, 8, 11, 15, 0));

    Mockito
        .when(therapyEhrHandler.getPrescriptionFromLink("p1", newInpatientPrescription, EhrLinkType.UPDATE, true))
        .thenReturn(oldInpatientPrescription);

    //mock modify existing
    final InpatientPrescription compositionVersion1 = buildInpatientPrescription(
        "uid1::bla::1",
        "Instruction",
        "PerformerPrescribe",
        new DateTime(2016, 8, 11, 8, 0));
    final InpatientPrescription compositionVersion2 = buildInpatientPrescription(
        "uid1::bla::2",
        "Instruction",
        "PerformerPrescribe",
        new DateTime(2016, 8, 11, 8, 0));

    //add actions
    MedicationsEhrUtils.addMedicationActionTo(
        compositionVersion2,
        MedicationActionEnum.MODIFY_EXISTING,
        new NamedExternalDto("PerformerModifyExisting", "PerformerModifyExisting"),
        new DateTime(2016, 8, 11, 9, 0));

    //therapy review on modify
    MedicationsEhrUtils.addMedicationActionTo(
        compositionVersion2,
        MedicationActionEnum.REVIEW,
        new NamedExternalDto("PerformerModifyExisting", "PerformerModifyExisting"),
        new DateTime(2016, 8, 11, 9, 0));

    MedicationsEhrUtils.addMedicationActionTo(
        compositionVersion2,
        MedicationActionEnum.SUSPEND,
        new NamedExternalDto("PerformerSuspend", "PerformerSuspend"),
        new DateTime(2016, 8, 11, 15, 30));

    MedicationsEhrUtils.addMedicationActionTo(
        compositionVersion2,
        MedicationActionEnum.REISSUE,
        new NamedExternalDto("PerformerReissue", "PerformerReissue"),
        new DateTime(2016, 8, 11, 16, 0));

    MedicationsEhrUtils.addMedicationActionTo(
        compositionVersion2,
        MedicationActionEnum.REVIEW,
        new NamedExternalDto("PerformerReview", "PerformerReview"),
        new DateTime(2016, 8, 11, 17, 0));

    Mockito
        .when(medicationsOpenEhrDao.loadInpatientPrescription("p1", "uid1::bla::1"))
        .thenReturn(compositionVersion1);

    Mockito
        .when(medicationsOpenEhrDao.loadInpatientPrescription("p1", "uid1::bla::2"))
        .thenReturn(compositionVersion2);

    Mockito
        .when(medicationsOpenEhrDao.loadInpatientPrescription("p1", "uid2::bla::1"))
        .thenReturn(newInpatientPrescription);

    final List<InpatientPrescription> oldCompositions = new ArrayList<>();
    oldCompositions.add(oldInpatientPrescription);
    oldCompositions.add(compositionVersion1);
    oldCompositions.add(compositionVersion2);

    Mockito
        .when(medicationsOpenEhrDao.getAllInpatientPrescriptionVersions("uid1::bla::2"))
        .thenReturn(oldCompositions);

    MedicationsEhrUtils.addMedicationActionTo(
        compositionVersion2,
        MedicationActionEnum.MODIFY_EXISTING,
        new NamedExternalDto("PerformerModifyExisting", "PerformerModifyExisting"),
        new DateTime(2016, 8, 11, 9, 0));

    final ConstantSimpleTherapyDto oldTherapyVersion1 = new ConstantSimpleTherapyDto();
    oldTherapyVersion1.setCreatedTimestamp(new DateTime(2016, 8, 11, 8, 0));

    oldTherapyVersion1.setCompositionUid("uid1::bla::1");
    final ConstantSimpleTherapyDto oldTherapyVersion2 = new ConstantSimpleTherapyDto();

    oldTherapyVersion2.setPrescriberName("PerformerModifyExisting");
    oldTherapyVersion2.setStart(new DateTime(2016, 8, 11, 15, 0));

    Mockito
        .when(medicationsBo.convertMedicationOrderToTherapyDto(
            compositionVersion1,
            compositionVersion1.getMedicationOrder(),
            null,
            null,
            true,
            LOCALE))
        .thenReturn(oldTherapyVersion1);
    Mockito
        .when(medicationsBo.convertMedicationOrderToTherapyDto(
            compositionVersion2,
            compositionVersion2.getMedicationOrder(),
            null,
            null,
            true,
            LOCALE))
        .thenReturn(oldTherapyVersion2);

    //mock modify
    final StringTherapyChangeDto comment = new StringTherapyChangeDto(TherapyChangeType.COMMENT);
    comment.setOldValue(null);
    comment.setNewValue("comment");

    //mock pharmacy review
    final PharmacyReviewReport pharmacyReview = new PharmacyReviewReport();
    final Context context = new Context();
    context.setStartTime(DataValueUtils.getDateTime(new DateTime(2016, 8, 11, 9, 30)));
    pharmacyReview.setContext(context);
    final Composer composer = new Composer();
    composer.setName("PerformerPharmacistReview");
    pharmacyReview.setComposer(composer);

    final List<PharmacyReviewReport> pharmacyReviews = new ArrayList<>();
    pharmacyReviews.add(pharmacyReview);
    Mockito
        .when(medicationsOpenEhrDao.findPharmacistsReviewReports("p1", new DateTime(2016, 8, 11, 8, 0)))
        .thenReturn(pharmacyReviews);
  }

  @Test
  public void testGetTherapyAuditTrail()
  {
    final TherapyAuditTrailDto therapyAuditTrail = therapyAuditTrailProvider.getTherapyAuditTrail(
        "p1",
        "uid2::bla::2",
        "Instruction",
        null,
        LOCALE,
        new DateTime(2016, 8, 12, 10, 0));

    assertEquals("uid2::bla::2", therapyAuditTrail.getCurrentTherapy().getCompositionUid());
    assertEquals("uid1::bla::1", therapyAuditTrail.getOriginalTherapy().getCompositionUid());
    assertEquals(8, therapyAuditTrail.getActionHistoryList().size());

    final TherapyActionHistoryDto prescribeAction = therapyAuditTrail.getActionHistoryList().get(0);
    assertEquals(TherapyActionHistoryType.PRESCRIBE, prescribeAction.getTherapyActionHistoryType());
    assertEquals("PerformerPrescribe", prescribeAction.getPerformer());
    assertEquals(new DateTime(2016, 8, 11, 8, 0), prescribeAction.getActionPerformedTime());

    final TherapyActionHistoryDto modifyExistingAction = therapyAuditTrail.getActionHistoryList().get(1);
    assertEquals(TherapyActionHistoryType.MODIFY_EXISTING, modifyExistingAction.getTherapyActionHistoryType());
    assertEquals("PerformerModifyExisting", modifyExistingAction.getPerformer());
    assertEquals(new DateTime(2016, 8, 11, 9, 0), modifyExistingAction.getActionPerformedTime());

    final TherapyActionHistoryDto pharmacyReviewAction = therapyAuditTrail.getActionHistoryList().get(2);
    assertEquals(TherapyActionHistoryType.PHARMACIST_REVIEW, pharmacyReviewAction.getTherapyActionHistoryType());
    assertEquals("PerformerPharmacistReview", pharmacyReviewAction.getPerformer());
    assertEquals(new DateTime(2016, 8, 11, 9, 30), pharmacyReviewAction.getActionPerformedTime());
    assertNull(pharmacyReviewAction.getActionTakesEffectTime());

    final TherapyActionHistoryDto suspendAction = therapyAuditTrail.getActionHistoryList().get(3);
    assertEquals(TherapyActionHistoryType.SUSPEND, suspendAction.getTherapyActionHistoryType());
    assertEquals("PerformerSuspend", suspendAction.getPerformer());
    assertEquals(new DateTime(2016, 8, 11, 15, 30), suspendAction.getActionPerformedTime());
    assertNull(suspendAction.getActionTakesEffectTime());

    final TherapyActionHistoryDto reissueAction = therapyAuditTrail.getActionHistoryList().get(4);
    assertEquals(TherapyActionHistoryType.REISSUE, reissueAction.getTherapyActionHistoryType());
    assertEquals("PerformerReissue", reissueAction.getPerformer());
    assertEquals(new DateTime(2016, 8, 11, 16, 0), reissueAction.getActionPerformedTime());
    assertNull(reissueAction.getActionTakesEffectTime());

    final TherapyActionHistoryDto reviewAction = therapyAuditTrail.getActionHistoryList().get(5);
    assertEquals(TherapyActionHistoryType.DOCTOR_REVIEW, reviewAction.getTherapyActionHistoryType());
    assertEquals("PerformerReview", reviewAction.getPerformer());
    assertEquals(new DateTime(2016, 8, 11, 17, 0), reviewAction.getActionPerformedTime());
    assertNull(reviewAction.getActionTakesEffectTime());

    final TherapyActionHistoryDto modifyAction = therapyAuditTrail.getActionHistoryList().get(6);
    assertEquals(TherapyActionHistoryType.MODIFY, modifyAction.getTherapyActionHistoryType());
    assertEquals("PerformerComplete", modifyAction.getPerformer());
    assertEquals(new DateTime(2016, 8, 11, 18, 0), modifyAction.getActionPerformedTime());
    assertEquals(new DateTime(2016, 8, 11, 20, 0), modifyAction.getActionTakesEffectTime());

    final TherapyActionHistoryDto stopAction = therapyAuditTrail.getActionHistoryList().get(7);
    assertEquals(TherapyActionHistoryType.STOP, stopAction.getTherapyActionHistoryType());
    assertEquals("PerformerAbort", stopAction.getPerformer());
    assertEquals(new DateTime(2016, 8, 11, 23, 0), stopAction.getActionPerformedTime());
    assertNull(reviewAction.getActionTakesEffectTime());
  }

  private InpatientPrescription buildInpatientPrescription(
      final String uid,
      final String ehrOrderName,
      final String composerName,
      final DateTime compositionCreateTime)
  {
    final InpatientPrescription inpatientPrescription = new InpatientPrescription();
    inpatientPrescription.setUid(uid);
    final Composer composer = new Composer();
    composer.setName(composerName);
    inpatientPrescription.setComposer(composer);
    final Context context = new Context();
    context.setStartTime(DataValueUtils.getDateTime(compositionCreateTime));
    inpatientPrescription.setContext(context);
    final MedicationOrder medicationOrder = new MedicationOrder();
    medicationOrder.setName(DataValueUtils.getText(ehrOrderName));
    inpatientPrescription.setMedicationOrder(medicationOrder);
    return inpatientPrescription;
  }
}
