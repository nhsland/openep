package com.marand.thinkmed.medications.api.internal.dto.document;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public class TherapyDocumentDto extends DataTransferObject
{
  private TherapyDocumentTypeEnum documentType;
  private DateTime createTimestamp; //prescriptionDate
  private NamedExternalDto creator;
  private NamedExternalDto careProvider;
  private TherapyDocumentContent content;

  public TherapyDocumentTypeEnum getDocumentType()
  {
    return documentType;
  }

  public void setDocumentType(final TherapyDocumentTypeEnum documentType)
  {
    this.documentType = documentType;
  }

  public DateTime getCreateTimestamp()
  {
    return createTimestamp;
  }

  public void setCreateTimestamp(final DateTime createTimestamp)
  {
    this.createTimestamp = createTimestamp;
  }

  public NamedExternalDto getCreator()
  {
    return creator;
  }

  public void setCreator(final NamedExternalDto creator)
  {
    this.creator = creator;
  }

  public NamedExternalDto getCareProvider()
  {
    return careProvider;
  }

  public void setCareProvider(final NamedExternalDto careProvider)
  {
    this.careProvider = careProvider;
  }

  public TherapyDocumentContent getContent()
  {
    return content;
  }

  public void setContent(final TherapyDocumentContent content)
  {
    this.content = content;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("documentType", documentType)
        .append("createTimestamp", createTimestamp)
        .append("creator", creator)
        .append("careProvider", careProvider)
        .append("content", content)
    ;
  }
}
