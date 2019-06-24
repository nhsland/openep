package com.marand.thinkmed.medications.ehr.model;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Nejc Korasa
 */

public class Procedure extends Action
{
  @EhrMapped("description[at0001]/items[at0002]/value")
  private DvText procedureName;

  @EhrMapped("description[at0001]/items[openEHR-EHR-CLUSTER.device.v1]")
  private MedicalDevice medicalDevice;

  @EhrMapped("description[at0001]/items[openEHR-EHR-CLUSTER.medication_supply_amount.v0]")
  private MedicationSupplyAmount supplyAmount;

  @EhrMapped("description[at0001]/items[at0005]/value")
  private DvText comment;

  public DvText getProcedureName()
  {
    return procedureName;
  }

  public void setProcedureName(final DvText procedureName)
  {
    this.procedureName = procedureName;
  }

  public MedicalDevice getMedicalDevice()
  {
    return medicalDevice;
  }

  public void setMedicalDevice(final MedicalDevice medicalDevice)
  {
    this.medicalDevice = medicalDevice;
  }

  public MedicationSupplyAmount getSupplyAmount()
  {
    return supplyAmount;
  }

  public void setSupplyAmount(final MedicationSupplyAmount supplyAmount)
  {
    this.supplyAmount = supplyAmount;
  }

  public DvText getComment()
  {
    return comment;
  }

  public void setComment(final DvText comment)
  {
    this.comment = comment;
  }
}
