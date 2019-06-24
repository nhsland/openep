package com.marand.thinkmed.medications.ehr.model.consentform;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Vid Kumse
 */
public class MedicationItem
{
  @EhrMapped("items[at0003]/value")
  private DvText name;

  @EhrMapped("items[at0004]/value")
  private DvCodedText type;

  public DvText getName()
  {
    return name;
  }

  public void setName(final DvText name)
  {
    this.name = name;
  }

  public DvCodedText getType()
  {
    return type;
  }

  public void setType(final DvCodedText type)
  {
    this.type = type;
  }
}
