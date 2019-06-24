package com.marand.thinkmed.medications.dto.document;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.api.internal.dto.document.TherapyDocumentDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class TherapyDocumentsDto extends DataTransferObject
{
  private List<TherapyDocumentDto> documents = new ArrayList<>();
  private boolean moreRecordsExist;

  public List<TherapyDocumentDto> getDocuments()
  {
    return documents;
  }

  public void setDocuments(final List<TherapyDocumentDto> documents)
  {
    this.documents = documents;
  }

  public boolean isMoreRecordsExist()
  {
    return moreRecordsExist;
  }

  public void setMoreRecordsExist(final boolean moreRecordsExist)
  {
    this.moreRecordsExist = moreRecordsExist;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("documents", documents)
        .append("moreRecordsExist", moreRecordsExist)
    ;
  }
}
