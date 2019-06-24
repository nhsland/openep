package com.marand.thinkmed.medications.dto.pharmacist.review;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.dto.change.TherapyChangeDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class PharmacistReviewTherapyDto extends DataTransferObject
{
  private TherapyDto therapy;
  private PharmacistTherapyChangeType changeType;
  private List<TherapyChangeDto<?,? >> changes = new ArrayList<>();

  public TherapyDto getTherapy()
  {
    return therapy;
  }

  public void setTherapy(final TherapyDto therapy)
  {
    this.therapy = therapy;
  }

  public PharmacistTherapyChangeType getChangeType()
  {
    return changeType;
  }

  public void setChangeType(final PharmacistTherapyChangeType changeType)
  {
    this.changeType = changeType;
  }

  public List<TherapyChangeDto<?, ?>> getChanges()
  {
    return changes;
  }

  public void setChanges(final List<TherapyChangeDto<?, ?>> changes)
  {
    this.changes = changes;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("therapy", therapy)
        .append("changeType", changeType)
        .append("changes", changes)
    ;
  }
}
