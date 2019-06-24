package com.marand.thinkmed.medications.ehr.model.pharmacist;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import com.marand.thinkmed.medications.ehr.model.Observation;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.DvUri;

public class RecommendationResponse extends Observation
{
  @EhrMapped("data[at0001]/items[at0002]/value")
  private DvCodedText response;

  @EhrMapped("data[at0001/items[at0005]/value")
  private DvText comment;

  @EhrMapped("protocol[at0003]/items[at0004]/value")
  private DvUri originalRecommendationEntry;

  public DvCodedText getResponse()
  {
    return response;
  }

  public void setResponse(final DvCodedText response)
  {
    this.response = response;
  }

  public DvText getComment()
  {
    return comment;
  }

  public void setComment(final DvText comment)
  {
    this.comment = comment;
  }

  public DvUri getOriginalRecommendationEntry()
  {
    return originalRecommendationEntry;
  }

  public void setOriginalRecommendationEntry(final DvUri originalRecommendationEntry)
  {
    this.originalRecommendationEntry = originalRecommendationEntry;
  }
}
