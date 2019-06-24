package com.marand.thinkmed.medications.ehr.model;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Vid Kumse
 */
public class DispenseDirections
{
  @EhrMapped("items[at0106]/value")
  private DvText dispenseInstructions;

  @EhrMapped("items[openEHR-EHR-CLUSTER.medication_supply_amount.v0]")
  private MedicationSupplyAmount dispenseAmount;

  @EhrMapped("items[openEHR-EHR-CLUSTER.medication.v0]")
  private List<Medication> dispenseDetails = new ArrayList<>();

  public DvText getDispenseInstructions()
  {
    return dispenseInstructions;
  }

  public void setDispenseInstructions(final DvText dispenseInstructions)
  {
    this.dispenseInstructions = dispenseInstructions;
  }

  public MedicationSupplyAmount getDispenseAmount()
  {
    return dispenseAmount;
  }

  public void setDispenseAmount(final MedicationSupplyAmount dispenseAmount)
  {
    this.dispenseAmount = dispenseAmount;
  }

  public List<Medication> getDispenseDetails()
  {
    return dispenseDetails;
  }

  public void setDispenseDetails(final List<Medication> dispenseDetails)
  {
    this.dispenseDetails = dispenseDetails;
  }
}
