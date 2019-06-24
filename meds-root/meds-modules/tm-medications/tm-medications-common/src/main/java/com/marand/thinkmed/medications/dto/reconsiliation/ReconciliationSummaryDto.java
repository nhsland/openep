package com.marand.thinkmed.medications.dto.reconsiliation;

import java.util.Collections;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */

public class ReconciliationSummaryDto extends DataTransferObject implements JsonSerializable
{
  private final List<ReconciliationRowDto> rows;
  private final DateTime reconciliationStarted;
  private final boolean admissionReviewed;
  private final boolean dischargeReviewed;
  private final DateTime admissionLastUpdateTime;
  private final DateTime dischargeLastUpdateTime;

  public ReconciliationSummaryDto(
      final List<ReconciliationRowDto> rows,
      final DateTime reconciliationStarted,
      final boolean admissionReviewed,
      final boolean dischargeReviewed,
      final DateTime admissionLastUpdateTime,
      final DateTime dischargeLastUpdateTime)
  {
    //noinspection AssignmentOrReturnOfFieldWithMutableType
    this.rows = rows;
    this.reconciliationStarted = reconciliationStarted;
    this.admissionReviewed = admissionReviewed;
    this.dischargeReviewed = dischargeReviewed;
    this.admissionLastUpdateTime = admissionLastUpdateTime;
    this.dischargeLastUpdateTime = dischargeLastUpdateTime;
  }

  public List<ReconciliationRowDto> getRows()
  {
    return Collections.unmodifiableList(rows);
  }

  public DateTime getReconciliationStarted()
  {
    return reconciliationStarted;
  }

  public boolean isAdmissionReviewed()
  {
    return admissionReviewed;
  }

  public boolean isDischargeReviewed()
  {
    return dischargeReviewed;
  }

  public DateTime getAdmissionLastUpdateTime()
  {
    return admissionLastUpdateTime;
  }

  public DateTime getDischargeLastUpdateTime()
  {
    return dischargeLastUpdateTime;
  }

  public static ReconciliationSummaryDto empty()
  {
    //noinspection unchecked
    return new ReconciliationSummaryDto(Collections.emptyList(), null, false, false, null, null);
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("rows", rows)
        .append("reconciliationStarted", reconciliationStarted)
        .append("admissionReviewed", admissionReviewed)
        .append("dischargeReviewed", dischargeReviewed)
        .append("admissionLastUpdateTime", admissionLastUpdateTime)
        .append("dischargeLastUpdateTime", dischargeLastUpdateTime)
    ;
  }
}
