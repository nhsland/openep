package com.marand.thinkmed.medications.ehr.model.pharmacist;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Nejc Korasa
 */

public class RequestReferral
{
  @EhrMapped("activities[at0001]/description[at0009]/items[at0121]/value")
  private DvText serviceRequested;

  @EhrMapped("activities[at0001]/description[at0009]/items[at0064]/value")
  private DvText reasonDescription;

  public DvText getServiceRequested()
  {
    return serviceRequested;
  }

  public void setServiceRequested(final DvText serviceRequested)
  {
    this.serviceRequested = serviceRequested;
  }

  public DvText getReasonDescription()
  {
    return reasonDescription;
  }

  public void setReasonDescription(final DvText reasonDescription)
  {
    this.reasonDescription = reasonDescription;
  }
}
