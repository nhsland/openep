package com.marand.thinkmed.medications.ehr.model;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvBoolean;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Nejc Korasa
 */

public class PlannedAdministration
{
  @EhrMapped("items[openEHR-EHR-CLUSTER.dosage.v1]")
  private Dosage plannedDosage;

  @EhrMapped("items[at0017]/value")
  private DvBoolean differentDoseAdministered;

  @EhrMapped("items[at0018]/value")
  private DvText taskId;

  @EhrMapped("items[openEHR-EHR-CLUSTER.device.v1]")
  private MedicalDevice plannedStartingDevice;

  @EhrMapped("items[at0023]/value")
  private DvText doctorsComment;

  @EhrMapped("items[at0027]/value")
  private DvBoolean doctorsConfirmation;

  public Dosage getPlannedDosage()
  {
    return plannedDosage;
  }

  public void setPlannedDosage(final Dosage plannedDosage)
  {
    this.plannedDosage = plannedDosage;
  }

  public DvBoolean getDifferentDoseAdministered()
  {
    return differentDoseAdministered;
  }

  public void setDifferentDoseAdministered(final DvBoolean differentDoseAdministered)
  {
    this.differentDoseAdministered = differentDoseAdministered;
  }

  public DvText getTaskId()
  {
    return taskId;
  }

  public void setTaskId(final DvText taskId)
  {
    this.taskId = taskId;
  }

  public MedicalDevice getPlannedStartingDevice()
  {
    return plannedStartingDevice;
  }

  public void setPlannedStartingDevice(final MedicalDevice plannedStartingDevice)
  {
    this.plannedStartingDevice = plannedStartingDevice;
  }

  public DvText getDoctorsComment()
  {
    return doctorsComment;
  }

  public void setDoctorsComment(final DvText doctorsComment)
  {
    this.doctorsComment = doctorsComment;
  }

  public DvBoolean getDoctorsConfirmation()
  {
    return doctorsConfirmation;
  }

  public void setDoctorsConfirmation(final DvBoolean doctorsConfirmation)
  {
    this.doctorsConfirmation = doctorsConfirmation;
  }
}
