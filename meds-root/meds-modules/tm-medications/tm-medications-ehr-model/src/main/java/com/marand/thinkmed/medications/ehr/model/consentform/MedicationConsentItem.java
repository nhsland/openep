package com.marand.thinkmed.medications.ehr.model.consentform;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Vid Kumse
 */
public class MedicationConsentItem
{
  @EhrMapped("data[at0001]/items[at0002]")
  private MedicationItem medicationItem;

  @EhrMapped("data[at0001]/items[at0008]/value")
  private DvText route;

  public MedicationItem getMedicationItem()
  {
    return medicationItem;
  }

  public void setMedicationItem(final MedicationItem medicationItem)
  {
    this.medicationItem = medicationItem;
  }

  public DvText getRoute()
  {
    return route;
  }

  public void setRoute(final DvText route)
  {
    this.route = route;
  }
}
