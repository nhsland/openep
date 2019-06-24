
package com.marand.thinkmed.medications.ehr.model;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */

public class MedicalDevice
{
  @EhrMapped("items[at0001]/value")
  private DvText deviceName;

  @EhrMapped("items[at0003]/value")
  private DvText type;

  public DvText getName()
  {
    return deviceName;
  }

  public void setName(final DvText deviceName)
  {
    this.deviceName = deviceName;
  }

  public DvText getType()
  {
    return type;
  }

  public void setType(final DvText type)
  {
    this.type = type;
  }
}
