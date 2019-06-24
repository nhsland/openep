package com.marand.thinkmed.medications.ehr.model.pharmacist;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Vid Kumse
 */

@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
public class DrugRelatedProblem implements PharmacistIssue
{
  @EhrMapped("items[at0008]/value")
  private List<DvCodedText> category = new ArrayList<>();

  @EhrMapped("items[at0009]/value")
  private DvCodedText outcome;

  @EhrMapped("items[at0011]/value")
  private DvCodedText impact;

  @EhrMapped("items[at0010]/value")
  private DvText recommendation;

  @Override
  public List<DvCodedText> getCategory()
  {
    return category;
  }

  @Override
  public void setCategory(final List<DvCodedText> category)
  {
    this.category = category;
  }

  @Override
  public DvCodedText getOutcome()
  {
    return outcome;
  }

  @Override
  public void setOutcome(final DvCodedText outcome)
  {
    this.outcome = outcome;
  }

  @Override
  public DvCodedText getImpact()
  {
    return impact;
  }

  @Override
  public void setImpact(final DvCodedText impact)
  {
    this.impact = impact;
  }

  @Override
  public DvText getRecommendation()
  {
    return recommendation;
  }

  @Override
  public void setRecommendation(final DvText recommendation)
  {
    this.recommendation = recommendation;
  }
}
