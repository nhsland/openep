package com.marand.thinkmed.medications.dto.report;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import lombok.NonNull;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class TherapyReportPdfDto extends DataTransferObject
{
  private final byte[] data;
  private final String filename;

  public TherapyReportPdfDto(final @NonNull byte[] data, final @NonNull String filename)
  {
    this.data = data;
    this.filename = filename;
  }

  public byte[] getData()
  {
    return data;
  }

  public String getFilename()
  {
    return filename;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("data.size", data.length).append("filename", filename);
  }
}
