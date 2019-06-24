package com.marand.thinkmed.medications.ehr.model.pharmacist;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import com.marand.thinkmed.medications.ehr.model.Action;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvDuration;
import org.openehr.jaxb.rm.DvEhrUri;

/**
 * @author Vid Kumse
 */
public class MedicationSupply extends Action
{
  @EhrMapped("description[at0001]/items[at0004]/value")
  private DvEhrUri linkToMedicationOrder;

  @EhrMapped("description[at0001]/items[at0008]/value")
  private DvCodedText supplyCategory;

  @EhrMapped("description[at0001]/items[at0003]/value")
  private DvDuration supplyDuration;

  public DvEhrUri getLinkToMedicationOrder()
  {
    return linkToMedicationOrder;
  }

  public void setLinkToMedicationOrder(final DvEhrUri linkToMedicationOrder)
  {
    this.linkToMedicationOrder = linkToMedicationOrder;
  }

  public DvCodedText getSupplyCategory()
  {
    return supplyCategory;
  }

  public void setSupplyCategory(final DvCodedText supplyCategory)
  {
    this.supplyCategory = supplyCategory;
  }

  public DvDuration getSupplyDuration()
  {
    return supplyDuration;
  }

  public void setSupplyDuration(final DvDuration supplyDuration)
  {
    this.supplyDuration = supplyDuration;
  }
}
