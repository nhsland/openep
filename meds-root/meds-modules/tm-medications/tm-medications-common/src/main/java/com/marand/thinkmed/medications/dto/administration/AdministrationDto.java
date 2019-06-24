package com.marand.thinkmed.medications.dto.administration;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.api.internal.dto.CodedNameDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.api.internal.dto.SelfAdministeringActionEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public abstract class AdministrationDto extends DataTransferObject implements JsonSerializable
{
  private final AdministrationTypeEnum administrationType;
  private AdministrationStatusEnum administrationStatus;
  private AdministrationResultEnum administrationResult;
  private CodedNameDto notAdministeredReason; //if administrationResult is GIVEN/DEFER
  private SelfAdministeringActionEnum selfAdministrationType; //if administrationResult is SELF_ADMINISTERED
  private DateTime administrationTime;
  private DateTime plannedTime;
  private boolean additionalAdministration;
  private String administrationId; //composition uid
  private String taskId; //activity task id
  private String groupUUId;
  private String therapyId;
  private String composerName;
  private NamedExternalDto witness;
  private String comment;
  private String doctorsComment;
  private Boolean doctorConfirmation;
  private MedicationRouteDto route;

  protected AdministrationDto(final AdministrationTypeEnum administrationType)
  {
    this.administrationType = administrationType;
  }

  public Boolean getDoctorConfirmation()
  {
    return doctorConfirmation;
  }

  public void setDoctorConfirmation(final Boolean doctorConfirmation)
  {
    this.doctorConfirmation = doctorConfirmation;
  }

  public AdministrationTypeEnum getAdministrationType()
  {
    return administrationType;
  }

  public AdministrationStatusEnum getAdministrationStatus()
  {
    return administrationStatus;
  }

  public void setAdministrationStatus(final AdministrationStatusEnum administrationStatus)
  {
    this.administrationStatus = administrationStatus;
  }

  public AdministrationResultEnum getAdministrationResult()
  {
    return administrationResult;
  }

  public void setAdministrationResult(final AdministrationResultEnum administrationResult)
  {
    this.administrationResult = administrationResult;
  }

  public CodedNameDto getNotAdministeredReason()
  {
    return notAdministeredReason;
  }

  public void setNotAdministeredReason(final CodedNameDto notAdministeredReason)
  {
    this.notAdministeredReason = notAdministeredReason;
  }

  public SelfAdministeringActionEnum getSelfAdministrationType()
  {
    return selfAdministrationType;
  }

  public void setSelfAdministrationType(final SelfAdministeringActionEnum selfAdministrationType)
  {
    this.selfAdministrationType = selfAdministrationType;
  }

  public DateTime getAdministrationTime()
  {
    return administrationTime;
  }

  public void setAdministrationTime(final DateTime administrationTime)
  {
    this.administrationTime = administrationTime;
  }

  public DateTime getPlannedTime()
  {
    return plannedTime;
  }

  public void setPlannedTime(final DateTime plannedTime)
  {
    this.plannedTime = plannedTime;
  }

  public boolean isAdditionalAdministration()
  {
    return additionalAdministration;
  }

  public void setAdditionalAdministration(final boolean additionalAdministration)
  {
    this.additionalAdministration = additionalAdministration;
  }

  public String getAdministrationId()
  {
    return administrationId;
  }

  public void setAdministrationId(final String administrationId)
  {
    this.administrationId = administrationId;
  }

  public String getTaskId()
  {
    return taskId;
  }

  public void setTaskId(final String taskId)
  {
    this.taskId = taskId;
  }

  public String getGroupUUId()
  {
    return groupUUId;
  }

  public void setGroupUUId(final String groupUUId)
  {
    this.groupUUId = groupUUId;
  }

  public String getTherapyId()
  {
    return therapyId;
  }

  public void setTherapyId(final String therapyId)
  {
    this.therapyId = therapyId;
  }

  public String getComposerName()
  {
    return composerName;
  }

  public void setComposerName(final String composerName)
  {
    this.composerName = composerName;
  }

  public NamedExternalDto getWitness()
  {
    return witness;
  }

  public void setWitness(final NamedExternalDto witness)
  {
    this.witness = witness;
  }

  public String getComment()
  {
    return comment;
  }

  public void setComment(final String comment)
  {
    this.comment = comment;
  }

  public String getDoctorsComment()
  {
    return doctorsComment;
  }

  public void setDoctorsComment(final String doctorsComment)
  {
    this.doctorsComment = doctorsComment;
  }

  public MedicationRouteDto getRoute()
  {
    return route;
  }

  public void setRoute(final MedicationRouteDto route)
  {
    this.route = route;
  }

  public boolean isAdministeredAtDifferentTime()
  {
    return plannedTime != null && administrationTime != null && !plannedTime.equals(administrationTime);
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("administrationType", administrationType)
        .append("administrationStatus", administrationStatus)
        .append("administrationResult", administrationResult)
        .append("notAdministeredReason", notAdministeredReason)
        .append("selfAdministrationType", selfAdministrationType)
        .append("administrationTime", administrationTime)
        .append("plannedTime", plannedTime)
        .append("additionalAdministration", additionalAdministration)
        .append("administrationId", administrationId)
        .append("taskId", taskId)
        .append("groupUUId", groupUUId)
        .append("therapyId", therapyId)
        .append("composerName", composerName)
        .append("witness", witness)
        .append("comment", comment)
        .append("doctorsComment", doctorsComment)
        .append("route", route)
    ;
  }
}
