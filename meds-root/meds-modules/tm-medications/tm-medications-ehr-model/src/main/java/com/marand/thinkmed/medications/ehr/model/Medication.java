
package com.marand.thinkmed.medications.ehr.model;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */
public class Medication
{
  @EhrMapped("items[at0132]/value")
  private DvText componentName;

  @EhrMapped("items[at0071]/value")
  private DvText form;

  @EhrMapped("items[at0142]/value")
  private DvText category;

  @EhrMapped("items[openEHR-EHR-CLUSTER.medication.v0]")
  private List<Medication> constituent = new ArrayList<>();

  @EhrMapped("items[at0139]/value")
  private DvQuantity amountValue;

  @EhrMapped("items[at0008]/value")
  private DvText amountUnit;

  @EhrMapped("items[at0148]/value")
  private DvQuantity alternateAmountValue;

  @EhrMapped("items[at0007]/value")
  private DvText alternateAmountUnit;

  @EhrMapped("items[at0127]/value")
  private DvText role;

  public DvText getComponentName()
  {
    return componentName;
  }

  public void setComponentName(final DvText componentName)
  {
    this.componentName = componentName;
  }

  public DvText getForm()
  {
    return form;
  }

  public void setForm(final DvText form)
  {
    this.form = form;
  }

  public DvText getCategory()
  {
    return category;
  }

  public void setCategory(final DvText category)
  {
    this.category = category;
  }

  public List<Medication> getConstituent()
  {
    return constituent;
  }

  public void setConstituent(final List<Medication> constituent)
  {
    this.constituent = constituent;
  }

  public DvQuantity getAmountValue()
  {
    return amountValue;
  }

  public void setAmountValue(final DvQuantity amountValue)
  {
    this.amountValue = amountValue;
  }

  public DvText getAmountUnit()
  {
    return amountUnit;
  }

  public void setAmountUnit(final DvText amountUnit)
  {
    this.amountUnit = amountUnit;
  }

  public DvQuantity getAlternateAmountValue()
  {
    return alternateAmountValue;
  }

  public void setAlternateAmountValue(final DvQuantity alternateAmountValue)
  {
    this.alternateAmountValue = alternateAmountValue;
  }

  public DvText getAlternateAmountUnit()
  {
    return alternateAmountUnit;
  }

  public void setAlternateAmountUnit(final DvText alternateAmountUnit)
  {
    this.alternateAmountUnit = alternateAmountUnit;
  }

  public DvText getRole()
  {
    return role;
  }

  public void setRole(final DvText role)
  {
    this.role = role;
  }
}
