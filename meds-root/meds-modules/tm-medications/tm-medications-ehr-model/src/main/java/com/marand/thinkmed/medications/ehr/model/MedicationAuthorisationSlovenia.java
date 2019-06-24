package com.marand.thinkmed.medications.ehr.model;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvBoolean;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvCount;
import org.openehr.jaxb.rm.DvDateTime;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Vid Kumse
 */
public class MedicationAuthorisationSlovenia
{

  @EhrMapped("items[at0110]/value")
  private DvText packageEPrescriptionUniqueIdentifier;

  @EhrMapped("items[at0139]/value")
  private DvDateTime updateTimestamp;

  @EhrMapped("items[at0087]/value")
  private DvText ePrescriptionUniqueIdentifier;

  @EhrMapped("items[at0118]/value")
  private DvBoolean renewable;

  @EhrMapped("items[at0025]/value")
  private DvCount maximumNumberOfDispenses;

  @EhrMapped("items[at0086]/value")
  private DvCount numberOfRemainingDispenses;

  @EhrMapped("items[at0091]/value")
  private DvCodedText typeOfPrescription;

  @EhrMapped("items[at0077]/value")
  private DvCodedText prescriptionDocumentType;

  @EhrMapped("items[at0085]/value")
  private DvCodedText prescriptionStatus;

  @EhrMapped("items[at0095]/value")
  private DvCodedText surchargeType;

  @EhrMapped("items[at0100]/value")
  private DvCodedText payer;

  @EhrMapped("items[at0106]/value")
  private DvText additionalInstructionsForPharmacist;

  @EhrMapped("items[at0111]/value")
  private DvBoolean doNotSwitch;

  @EhrMapped("items[at0140]/value")
  private DvBoolean urgent;

  @EhrMapped("items[at0112]/value")
  private DvBoolean interactions;

  @EhrMapped("items[at0129]/value")
  private DvBoolean maximumDoseExceeded;

  @EhrMapped("items[at0082]/value")
  private DvCodedText illnessConditionType;

  @EhrMapped("items[at0123]/value")
  private DvCodedText treatmentReason;

  public DvText getPackageEPrescriptionUniqueIdentifier()
  {
    return packageEPrescriptionUniqueIdentifier;
  }

  public void setPackageEPrescriptionUniqueIdentifier(final DvText packageEPrescriptionUniqueIdentifier)
  {
    this.packageEPrescriptionUniqueIdentifier = packageEPrescriptionUniqueIdentifier;
  }

  public DvDateTime getUpdateTimestamp()
  {
    return updateTimestamp;
  }

  public void setUpdateTimestamp(final DvDateTime updateTimestamp)
  {
    this.updateTimestamp = updateTimestamp;
  }

  public DvText getePrescriptionUniqueIdentifier()
  {
    return ePrescriptionUniqueIdentifier;
  }

  public void setePrescriptionUniqueIdentifier(final DvText ePrescriptionUniqueIdentifier)
  {
    this.ePrescriptionUniqueIdentifier = ePrescriptionUniqueIdentifier;
  }

  public DvBoolean getRenewable()
  {
    return renewable;
  }

  public void setRenewable(final DvBoolean renewable)
  {
    this.renewable = renewable;
  }

  public DvCount getMaximumNumberOfDispenses()
  {
    return maximumNumberOfDispenses;
  }

  public void setMaximumNumberOfDispenses(final DvCount maximumNumberOfDispenses)
  {
    this.maximumNumberOfDispenses = maximumNumberOfDispenses;
  }

  public DvCount getNumberOfRemainingDispenses()
  {
    return numberOfRemainingDispenses;
  }

  public void setNumberOfRemainingDispenses(final DvCount numberOfRemainingDispenses)
  {
    this.numberOfRemainingDispenses = numberOfRemainingDispenses;
  }

  public DvCodedText getTypeOfPrescription()
  {
    return typeOfPrescription;
  }

  public void setTypeOfPrescription(final DvCodedText typeOfPrescription)
  {
    this.typeOfPrescription = typeOfPrescription;
  }

  public DvCodedText getPrescriptionDocumentType()
  {
    return prescriptionDocumentType;
  }

  public void setPrescriptionDocumentType(final DvCodedText prescriptionDocumentType)
  {
    this.prescriptionDocumentType = prescriptionDocumentType;
  }

  public DvCodedText getPrescriptionStatus()
  {
    return prescriptionStatus;
  }

  public void setPrescriptionStatus(final DvCodedText prescriptionStatus)
  {
    this.prescriptionStatus = prescriptionStatus;
  }

  public DvCodedText getSurchargeType()
  {
    return surchargeType;
  }

  public void setSurchargeType(final DvCodedText surchargeType)
  {
    this.surchargeType = surchargeType;
  }

  public DvCodedText getPayer()
  {
    return payer;
  }

  public void setPayer(final DvCodedText payer)
  {
    this.payer = payer;
  }

  public DvText getAdditionalInstructionsForPharmacist()
  {
    return additionalInstructionsForPharmacist;
  }

  public void setAdditionalInstructionsForPharmacist(final DvText additionalInstructionsForPharmacist)
  {
    this.additionalInstructionsForPharmacist = additionalInstructionsForPharmacist;
  }

  public DvBoolean getDoNotSwitch()
  {
    return doNotSwitch;
  }

  public void setDoNotSwitch(final DvBoolean doNotSwitch)
  {
    this.doNotSwitch = doNotSwitch;
  }

  public DvBoolean getUrgent()
  {
    return urgent;
  }

  public void setUrgent(final DvBoolean urgent)
  {
    this.urgent = urgent;
  }

  public DvBoolean getInteractions()
  {
    return interactions;
  }

  public void setInteractions(final DvBoolean interactions)
  {
    this.interactions = interactions;
  }

  public DvBoolean getMaximumDoseExceeded()
  {
    return maximumDoseExceeded;
  }

  public void setMaximumDoseExceeded(final DvBoolean maximumDoseExceeded)
  {
    this.maximumDoseExceeded = maximumDoseExceeded;
  }

  public DvCodedText getIllnessConditionType()
  {
    return illnessConditionType;
  }

  public void setIllnessConditionType(final DvCodedText illnessConditionType)
  {
    this.illnessConditionType = illnessConditionType;
  }

  public DvCodedText getTreatmentReason()
  {
    return treatmentReason;
  }

  public void setTreatmentReason(final DvCodedText treatmentReason)
  {
    this.treatmentReason = treatmentReason;
  }
}
