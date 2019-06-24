package com.marand.thinkmed.medications.dto;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Klavdij Lapajne
 */
public class MedicationDocumentDto extends DataTransferObject
{
  private String documentReference;
  private String externalSystem;
  private MedicationDocumentType type;

  public String getDocumentReference()
  {
    return documentReference;
  }

  public void setDocumentReference(final String documentReference)
  {
    this.documentReference = documentReference;
  }

  public String getExternalSystem()
  {
    return externalSystem;
  }

  public void setExternalSystem(final String externalSystem)
  {
    this.externalSystem = externalSystem;
  }

  public MedicationDocumentType getType()
  {
    return type;
  }

  public void setType(final MedicationDocumentType type)
  {
    this.type = type;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("documentReference", documentReference);
    tsb.append("externalSystem", externalSystem);
    tsb.append("type", type);
  }
}