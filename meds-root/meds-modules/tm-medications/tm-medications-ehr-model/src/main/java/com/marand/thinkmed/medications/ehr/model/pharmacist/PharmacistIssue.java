package com.marand.thinkmed.medications.ehr.model.pharmacist;

import java.util.List;

import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Nejc Korasa
 */

public interface PharmacistIssue
{
  List<DvCodedText> getCategory();

  void setCategory(final List<DvCodedText> category);

  DvCodedText getOutcome();

  void setOutcome(final DvCodedText outcome);

  DvCodedText getImpact();

  void setImpact(final DvCodedText impact);

  DvText getRecommendation();

  void setRecommendation(final DvText recommendation);
}
