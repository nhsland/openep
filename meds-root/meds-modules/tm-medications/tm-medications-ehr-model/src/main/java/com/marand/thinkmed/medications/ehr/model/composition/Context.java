
package com.marand.thinkmed.medications.ehr.model.composition;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import com.marand.thinkmed.medications.ehr.model.ContextDetail;
import com.marand.thinkmed.medications.ehr.model.Identifier;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvDateTime;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */

@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
public class Context
{
  @EhrMapped("start_time")
  private DvDateTime startTime;

  @EhrMapped("other_context[at0001]/items[openEHR-EHR-CLUSTER.composition_context_detail.v1]")
  private ContextDetail contextDetail;

  @EhrMapped("setting")
  private DvCodedText setting;

  //PRESCRIPTION COMPOSITION
  @EhrMapped("other_context[at0001]/items[at0008]/value")
  private Identifier prescriptionIdentifier;

  //REPORT COMPOSITION
  @EhrMapped("other_context[at0001]/items[at0005]/value")
  private DvText status;

  public DvDateTime getStartTime()
  {
    return startTime;
  }

  public void setStartTime(final DvDateTime startTime)
  {
    this.startTime = startTime;
  }

  public ContextDetail getContextDetail()
  {
    if (contextDetail == null)
    {
      contextDetail = new ContextDetail();
    }

    return contextDetail;
  }

  public void setContextDetail(final ContextDetail contextDetail)
  {
    this.contextDetail = contextDetail;
  }

  public DvCodedText getSetting()
  {
    return setting;
  }

  public void setSetting(final DvCodedText setting)
  {
    this.setting = setting;
  }

  public Identifier getPrescriptionIdentifier()
  {
    return prescriptionIdentifier;
  }

  public void setPrescriptionIdentifier(final Identifier prescriptionIdentifier)
  {
    this.prescriptionIdentifier = prescriptionIdentifier;
  }

  public DvText getStatus()
  {
    return status;
  }

  public void setStatus(final DvText status)
  {
    this.status = status;
  }
}
