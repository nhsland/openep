package com.marand.thinkmed.medications.ehr.model.consentform;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvQuantity;

/**
 * @author Vid Kumse
 */
public class MedicationConsent
{
  @EhrMapped("data[at0001]/items[at0002]/value")
  private DvCodedText consentType;

  @EhrMapped("data[at0001]/items[at0003]/value")
  private DvQuantity maximumCumulativeDose;

  public DvCodedText getConsentType()
  {
    return consentType;
  }

  public void setConsentType(final DvCodedText consentType)
  {
    this.consentType = consentType;
  }

  public DvQuantity getMaximumCumulativeDose()
  {
    return maximumCumulativeDose;
  }

  public void setMaximumCumulativeDose(final DvQuantity maximumCumulativeDose)
  {
    this.maximumCumulativeDose = maximumCumulativeDose;
  }
}
