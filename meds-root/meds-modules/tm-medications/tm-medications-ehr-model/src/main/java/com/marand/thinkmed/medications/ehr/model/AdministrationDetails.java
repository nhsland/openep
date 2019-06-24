
package com.marand.thinkmed.medications.ehr.model;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */
public class AdministrationDetails
{
  @EhrMapped("items[at0147]/value")
  private DvCodedText route;

  @EhrMapped("items[at0143]/value")
  private DvText administrationMethod;

  @EhrMapped("items[openEHR-EHR-CLUSTER.device.v1]")
  private MedicalDevice administrationDevice;

  public DvCodedText getRoute()
  {
    return route;
  }

  public void setRoute(final DvCodedText route)
  {
    this.route = route;
  }

  public DvText getAdministrationMethod()
  {
    return administrationMethod;
  }

  public void setAdministrationMethod(final DvText administrationMethod)
  {
    this.administrationMethod = administrationMethod;
  }

  public MedicalDevice getAdministrationDevice()
  {
    return administrationDevice;
  }

  public void setAdministrationDevice(final MedicalDevice administrationDevice)
  {
    this.administrationDevice = administrationDevice;
  }
}
