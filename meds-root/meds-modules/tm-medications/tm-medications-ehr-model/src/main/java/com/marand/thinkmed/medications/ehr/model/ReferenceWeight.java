
package com.marand.thinkmed.medications.ehr.model;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import com.marand.thinkmed.medications.ehr.model.composition.EhrComposition;

/**
 * @author Mitja Lapajne
 */
public class ReferenceWeight extends EhrComposition
{
  @EhrMapped("/content[openEHR-EHR-OBSERVATION.body_weight.v1,'Medication reference body weight']")
  private BodyWeight referenceBodyWeight;

  @Override
  public String getTemplateId()
  {
    return "OPENeP - Reference Weight";
  }

  public BodyWeight getReferenceBodyWeight()
  {
    return referenceBodyWeight;
  }

  public void setReferenceBodyWeight(final BodyWeight referenceBodyWeight)
  {
    this.referenceBodyWeight = referenceBodyWeight;
  }
}
