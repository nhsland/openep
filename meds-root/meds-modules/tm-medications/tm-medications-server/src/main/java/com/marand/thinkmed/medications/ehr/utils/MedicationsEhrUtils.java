package com.marand.thinkmed.medications.ehr.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.marand.maf.core.time.Intervals;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkehr.util.ConversionUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.MedicationDeliveryMethodEnum;
import com.marand.thinkmed.medications.api.internal.dto.CodedNameDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationOrderFormType;
import com.marand.thinkmed.medications.dto.MedicationActionEnum;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.InstructionDetails;
import com.marand.thinkmed.medications.ehr.model.Medication;
import com.marand.thinkmed.medications.ehr.model.MedicationCategory;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.model.OrderDetails;
import com.marand.thinkmed.medications.ehr.model.Participation;
import com.marand.thinkmed.medications.ehr.model.composition.EhrComposition;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.LocatableRef;
import org.openehr.jaxb.rm.ObjectVersionId;

/**
 * @author Bostjan Vester
 */
public final class MedicationsEhrUtils
{
  public static final String BOLUS = "BOLUS";

  private MedicationsEhrUtils()
  {
  }

  public static boolean isSimpleTherapy(final MedicationOrder order)
  {
    return MedicationOrderFormType.SIMPLE.matches(order.getAdditionalDetails().getPrescriptionType());
  }

  public static boolean isComplexTherapy(final MedicationOrder order)
  {
    return MedicationOrderFormType.COMPLEX.matches(order.getAdditionalDetails().getPrescriptionType());
  }

  public static boolean isAdHocMixture(final Medication preparation)
  {
    return MedicationCategory.AD_HOC_MIXTURE.matches(preparation.getCategory());
  }

  public static boolean isOxygen(final MedicationOrder order)
  {
    return MedicationOrderFormType.OXYGEN.matches(order.getAdditionalDetails().getPrescriptionType());
  }

  public static boolean isVariableTherapy(final MedicationOrder medicationOrder)
  {
    return medicationOrder.getStructuredDoseAndTimingDirections().stream()
        .mapToLong(s -> s.getDosage().size())
        .sum() > 1;
  }

  public static boolean isContinuousInfusion(final MedicationOrder medicationOrder)
  {
    return MedicationDeliveryMethodEnum.isContinuousInfusion(medicationOrder.getAdministrationMethod());
  }

  public static void addMedicationActionTo(
      final InpatientPrescription composition,
      final MedicationActionEnum actionEnum,
      final NamedExternalDto user,
      final TherapyChangeReasonDto changeReasonDto,
      final DateTime when)
  {
    final MedicationManagement action = buildMedicationAction(
        composition,
        actionEnum,
        InpatientPrescription.getMedicationOrderPath(),
        when);

    if (changeReasonDto != null)
    {
      action.getReason().addAll(getTherapyChangeReasons(changeReasonDto));
    }

    final Participation participation = new Participation();
    participation.setId(user.getId());
    participation.setName(user.getName());
    participation.setFunction("Performer");
    action.getOtherParticipations().add(participation);
    composition.getActions().add(action);
  }

  public static void addMedicationActionTo(
      final InpatientPrescription inpatientPrescription,
      final MedicationActionEnum actionEnum,
      final NamedExternalDto user,
      final DateTime when)
  {
    addMedicationActionTo(inpatientPrescription, actionEnum, user, null, when);
  }

  public static MedicationManagement buildMedicationAction(
      final EhrComposition composition,
      final MedicationActionEnum medicationActionEnum,
      final String instructionPath,
      final DateTime when)
  {
    final MedicationManagement action = new MedicationManagement();
    action.setTime(DataValueUtils.getDateTime(when));
    action.setComment(ConversionUtils.getText("/"));

    final InstructionDetails instructionDetails = new InstructionDetails();
    final LocatableRef instructionId = createMedicationOrderLocatableRef(composition.getUid(), instructionPath);
    instructionDetails.setInstructionId(instructionId);
    instructionDetails.setActivityId("activities[at0001] ");
    action.setInstructionDetails(instructionDetails);

    action.setIsmTransition(medicationActionEnum.buildIsmTransition());

    return action;
  }

  public static LocatableRef createMedicationOrderLocatableRef(final String compositionUid, final String path)
  {
    final LocatableRef locatableRef = new LocatableRef();

    locatableRef.setType("INSTRUCTION");
    locatableRef.setNamespace("local");

    final ObjectVersionId objectVersionId = new ObjectVersionId();
    objectVersionId.setValue(
        compositionUid != null ?
        TherapyIdUtils.getCompositionUidWithoutVersion(compositionUid) :
        "/");

    locatableRef.setId(objectVersionId);
    locatableRef.setPath(path);

    return locatableRef;
  }

  public static TherapyChangeReasonDto getTherapyChangeReasonDtoFromAction(final MedicationManagement action)
  {
    final DvCodedText codedReason = action.getReason().stream()
        .filter(r -> r instanceof DvCodedText)
        .map(r -> (DvCodedText)r)
        .findFirst()
        .orElse(null);

    if (codedReason != null)
    {
      final TherapyChangeReasonDto reason = new TherapyChangeReasonDto();
      reason.setChangeReason(new CodedNameDto(codedReason.getDefiningCode().getCodeString(), codedReason.getValue()));

      final DvText comment = action.getReason().stream()
          .filter(r -> !(r instanceof DvCodedText))
          .findFirst()
          .orElse(null);

      if (comment != null)
      {
        final String uncodedReason = comment.getValue();
        reason.setComment(uncodedReason);
      }

      return reason;
    }

    return null;
  }

  public static List<DvText> getTherapyChangeReasons(final TherapyChangeReasonDto changeReasonDto)
  {
    final List<DvText> reasonList = new ArrayList<>();
    if (changeReasonDto != null)
    {
      reasonList.add(
          DataValueUtils.getLocalCodedText(
              changeReasonDto.getChangeReason().getCode(),
              changeReasonDto.getChangeReason().getName()));

      if (changeReasonDto.getComment() != null)
      {
        reasonList.add(DataValueUtils.getText(changeReasonDto.getComment()));
      }
    }
    return reasonList;
  }

  public static MedicationManagement getLatestAction(final List<MedicationManagement> actionsList)
  {
    DateTime latestDate = null;
    MedicationManagement latestAction = null;

    for (final MedicationManagement action : actionsList)
    {
      final DateTime actionTime = DataValueUtils.getDateTime(action.getTime());
      if (latestDate == null || actionTime.isAfter(latestDate))
      {
        latestDate = actionTime;
        latestAction = action;
      }
    }
    return latestAction;
  }

  public static MedicationManagement getLatestModifyAction(final List<MedicationManagement> actionsList)
  {
    DateTime latestDate = null;
    MedicationManagement latestAction = null;

    for (final MedicationManagement action : actionsList)
    {
      final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
      if (actionEnum == MedicationActionEnum.MODIFY_EXISTING || actionEnum == MedicationActionEnum.COMPLETE)
      {
        final DateTime actionTime = DataValueUtils.getDateTime(action.getTime());
        if (latestDate == null || actionTime.isAfter(latestDate))
        {
          latestDate = actionTime;
          latestAction = action;
        }
      }
    }
    return latestAction;
  }

  public static MedicationManagement getLatestActionWithChangeReason(
      final List<MedicationManagement> actionsList,
      final boolean onlyForAbortedOrSuspended)
  {
    DateTime latestDate = null;
    MedicationManagement latestAction = null;

    for (final MedicationManagement action : actionsList)
    {
      final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
      final boolean isModifiedActionType = actionEnum == MedicationActionEnum.MODIFY_EXISTING || actionEnum == MedicationActionEnum.COMPLETE;
      final boolean isSuspendOrReissueActionType = actionEnum == MedicationActionEnum.SUSPEND || actionEnum == MedicationActionEnum.REISSUE;
      if (actionEnum == MedicationActionEnum.ABORT || actionEnum == MedicationActionEnum.CANCEL)
      {
        return action;
      }
      else if (isSuspendOrReissueActionType || (!onlyForAbortedOrSuspended && isModifiedActionType))
      {
        final DateTime actionTime = DataValueUtils.getDateTime(action.getTime());
        if (latestDate == null || actionTime.isAfter(latestDate))
        {
          latestDate = actionTime;
          latestAction = action;
        }
      }
    }
    return latestAction;
  }

  public static Interval getMedicationOrderInterval(final MedicationOrder medicationOrder)
  {
    final OrderDetails orderDetails = medicationOrder.getOrderDetails();
    final DateTime start = DataValueUtils.getDateTime(orderDetails.getOrderStartDateTime());
    final DateTime stop =
        orderDetails.getOrderStopDateTime() != null ?
        DataValueUtils.getDateTime(orderDetails.getOrderStopDateTime()) :
        null;

    return stop != null ? new Interval(start, stop) : Intervals.infiniteFrom(start);
  }

  public static List<Long> getMedicationIds(final Medication preparation)
  {
    final boolean adHocMixture = isAdHocMixture(preparation);
    if (adHocMixture)
    {
      return preparation.getConstituent().stream()
          .map(Medication::getComponentName)
          .filter(n -> n instanceof DvCodedText)
          .map(n -> ((DvCodedText)n).getDefiningCode().getCodeString())
          .map(Long::valueOf)
          .collect(Collectors.toList());
    }
    if (preparation.getComponentName() instanceof DvCodedText)
    {
      final String medicationId = ((DvCodedText)preparation.getComponentName()).getDefiningCode().getCodeString();
      return Collections.singletonList(Long.parseLong(medicationId));
    }

    return Collections.emptyList();
  }

  public static List<Long> getMedicationIds(final @NonNull MedicationOrder medicationOrder)
  {
    return getMedicationIds(medicationOrder.getPreparationDetails());
  }

  public static List<Long> getMedicationIds(final @NonNull MedicationManagement action)
  {
    return getMedicationIds(action.getMedicationDetails());
  }

  public static Long getMainMedicationId(final @NonNull MedicationOrder medicationOrder)
  {
    return getMedicationIds(medicationOrder).stream().findFirst().orElse(null);
  }
}
