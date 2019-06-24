package com.marand.thinkmed.medications.ehr.model;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvDateTime;


/**
 * @author Nejc Korasa
 */

public class MedicationReconciliationDetails
{
  @EhrMapped("activities[at0001]/description[at0002]/items[at0004]/value")
  private DvDateTime dischargeLastUpdateTime;

  @EhrMapped("activities[at0001]/description[at0002]/items[at0005]/value")
  private DvDateTime admissionLastUpdateTime;

  public DvDateTime getDischargeLastUpdateTime()
  {
    return dischargeLastUpdateTime;
  }

  public void setDischargeLastUpdateTime(final DvDateTime dischargeLastUpdateTime)
  {
    this.dischargeLastUpdateTime = dischargeLastUpdateTime;
  }

  public DvDateTime getAdmissionLastUpdateTime()
  {
    return admissionLastUpdateTime;
  }

  public void setAdmissionLastUpdateTime(final DvDateTime admissionLastUpdateTime)
  {
    this.admissionLastUpdateTime = admissionLastUpdateTime;
  }
}
