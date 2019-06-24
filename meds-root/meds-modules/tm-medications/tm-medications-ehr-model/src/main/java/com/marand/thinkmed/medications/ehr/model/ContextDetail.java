package com.marand.thinkmed.medications.ehr.model;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Nejc Korasa
 */

@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
public class ContextDetail
{
  @EhrMapped("items[at0001]/value")
  private DvText periodOfCareIdentifier;

  @EhrMapped("items[at0002]/value")
  private DvText departmentalPeriodOfCareIdentifier;

  @EhrMapped("items[at0003]/value")
  private DvText portletId;

  @EhrMapped("items[at0004]/value")
  private DvCodedText medicationOrderType;

  @EhrMapped("items[at0008]/value")
  private List<DvText> tags = new ArrayList<>();

  @EhrMapped("items[at0010]/value")
  private DvCodedText documentStatus;

  public DvText getPeriodOfCareIdentifier()
  {
    return periodOfCareIdentifier;
  }

  public void setPeriodOfCareIdentifier(final DvText periodOfCareIdentifier)
  {
    this.periodOfCareIdentifier = periodOfCareIdentifier;
  }

  public DvText getDepartmentalPeriodOfCareIdentifier()
  {
    return departmentalPeriodOfCareIdentifier;
  }

  public void setDepartmentalPeriodOfCareIdentifier(final DvText departmentalPeriodOfCareIdentifier)
  {
    this.departmentalPeriodOfCareIdentifier = departmentalPeriodOfCareIdentifier;
  }

  public DvText getPortletId()
  {
    return portletId;
  }

  public void setPortletId(final DvText portletId)
  {
    this.portletId = portletId;
  }

  public DvCodedText getMedicationOrderType()
  {
    return medicationOrderType;
  }

  public void setMedicationOrderType(final DvCodedText medicationOrderType)
  {
    this.medicationOrderType = medicationOrderType;
  }

  public List<DvText> getTags()
  {
    return tags;
  }

  public void setTags(final List<DvText> tags)
  {
    this.tags = tags;
  }

  public DvCodedText getDocumentStatus()
  {
    return documentStatus;
  }

  public void setDocumentStatus(final DvCodedText documentStatus)
  {
    this.documentStatus = documentStatus;
  }
}
