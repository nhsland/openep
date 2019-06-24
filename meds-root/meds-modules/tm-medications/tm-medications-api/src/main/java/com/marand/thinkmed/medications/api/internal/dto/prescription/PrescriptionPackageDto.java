package com.marand.thinkmed.medications.api.internal.dto.prescription;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.api.internal.dto.document.TherapyDocumentContent;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public class PrescriptionPackageDto extends DataTransferObject implements JsonSerializable, TherapyDocumentContent
{
  private String prescriptionPackageId;
  private String compositionUid;
  private List<PrescriptionTherapyDto> prescriptionTherapies = new ArrayList<>();
  private NamedExternalDto composer;
  private DateTime lastUpdateTimestamp;

  @Override
  public String getContentId()
  {
    return compositionUid;
  }

  public String getPrescriptionPackageId()
  {
    return prescriptionPackageId;
  }

  public void setPrescriptionPackageId(final String prescriptionPackageId)
  {
    this.prescriptionPackageId = prescriptionPackageId;
  }

  public String getCompositionUid()
  {
    return compositionUid;
  }

  public void setCompositionUid(final String compositionUid)
  {
    this.compositionUid = compositionUid;
  }

  public List<com.marand.thinkmed.medications.api.internal.dto.prescription.PrescriptionTherapyDto> getPrescriptionTherapies()
  {
    return prescriptionTherapies;
  }

  public void setPrescriptionTherapies(final List<com.marand.thinkmed.medications.api.internal.dto.prescription.PrescriptionTherapyDto> prescriptionTherapies)
  {
    this.prescriptionTherapies = prescriptionTherapies;
  }

  public NamedExternalDto getComposer()
  {
    return composer;
  }

  public void setComposer(final NamedExternalDto composer)
  {
    this.composer = composer;
  }

  public DateTime getLastUpdateTimestamp()
  {
    return lastUpdateTimestamp;
  }

  public void setLastUpdateTimestamp(final DateTime lastUpdateTimestamp)
  {
    this.lastUpdateTimestamp = lastUpdateTimestamp;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("prescriptionPackageId", prescriptionPackageId)
        .append("compositionUid", compositionUid)
        .append("prescriptionTherapies", prescriptionTherapies)
        .append("composer", composer)
        .append("lastUpdateTimestamp", lastUpdateTimestamp)
    ;
  }
}
