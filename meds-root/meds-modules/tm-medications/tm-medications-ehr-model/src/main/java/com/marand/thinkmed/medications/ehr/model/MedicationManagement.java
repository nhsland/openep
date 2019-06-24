
package com.marand.thinkmed.medications.ehr.model;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvDateTime;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */

@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
public class MedicationManagement extends Action
{
  @EhrMapped("description[at0017]/items[at0020]/value")
  private DvText medicationItem;

  @EhrMapped("description[at0017]/items[openEHR-EHR-CLUSTER.medication.v0]")
  private Medication medicationDetails;

  @EhrMapped("description[at0017]/items[openEHR-EHR-CLUSTER.dosage.v1]")
  private Dosage amount;

  @EhrMapped("description[at0017]/items[at0132]/value")
  private DvCodedText substitution;

  @EhrMapped("description[at0017]/items[at0133]/value")
  private DvText substitutionReason;

  @EhrMapped("description[at0017]/items[at0043]/value")
  private DvDateTime scheduledDateTime;

  @EhrMapped("description[at0017]/items[at0154]/value")
  private DvDateTime restartDateTime;

  @EhrMapped("description[at0017]/items[at0155]/value")
  private DvText restartCriterion;

  @EhrMapped("description[at0017]/items[at0021]/value")
  private List<DvText> reason = new ArrayList<>();

  @EhrMapped("description[at0017]/items[at0140]")
  private AdministrationDetails administrationDetails;

  @EhrMapped("description[at0017]/items[openEHR-EHR-CLUSTER.Medication_additional_details.v0]")
  private AdditionalDetails additionalDetails;

  @EhrMapped("description[at0017]/items[at0024]/value")
  private DvText comment;

  public DvText getMedicationItem()
  {
    return medicationItem;
  }

  public void setMedicationItem(final DvText medicationItem)
  {
    this.medicationItem = medicationItem;
  }

  public Medication getMedicationDetails()
  {
    return medicationDetails;
  }

  public void setMedicationDetails(final Medication medicationDetails)
  {
    this.medicationDetails = medicationDetails;
  }

  public Dosage getAmount()
  {
    return amount;
  }

  public void setAmount(final Dosage amount)
  {
    this.amount = amount;
  }

  public List<DvText> getReason()
  {
    return reason;
  }

  public void setReason(final List<DvText> reason)
  {
    this.reason = reason;
  }

  public AdministrationDetails getAdministrationDetails()
  {
    if (administrationDetails == null)
    {
      administrationDetails = new AdministrationDetails();
    }

    return administrationDetails;
  }

  public void setAdministrationDetails(final AdministrationDetails administrationDetails)
  {
    this.administrationDetails = administrationDetails;
  }

  public DvText getComment()
  {
    return comment;
  }

  public void setComment(final DvText comment)
  {
    this.comment = comment;
  }

  public DvCodedText getSubstitution()
  {
    return substitution;
  }

  public void setSubstitution(final DvCodedText substitution)
  {
    this.substitution = substitution;
  }

  public DvText getSubstitutionReason()
  {
    return substitutionReason;
  }

  public void setSubstitutionReason(final DvText substitutionReason)
  {
    this.substitutionReason = substitutionReason;
  }

  public DvDateTime getScheduledDateTime()
  {
    return scheduledDateTime;
  }

  public void setScheduledDateTime(final DvDateTime scheduledDateTime)
  {
    this.scheduledDateTime = scheduledDateTime;
  }

  public DvDateTime getRestartDateTime()
  {
    return restartDateTime;
  }

  public void setRestartDateTime(final DvDateTime restartDateTime)
  {
    this.restartDateTime = restartDateTime;
  }

  public DvText getRestartCriterion()
  {
    return restartCriterion;
  }

  public void setRestartCriterion(final DvText restartCriterion)
  {
    this.restartCriterion = restartCriterion;
  }

  public AdditionalDetails getAdditionalDetails()
  {
    if (additionalDetails == null)
    {
      additionalDetails = new AdditionalDetails();
    }

    return additionalDetails;
  }

  public void setAdditionalDetails(final AdditionalDetails additionalDetails)
  {
    this.additionalDetails = additionalDetails;
  }
}
