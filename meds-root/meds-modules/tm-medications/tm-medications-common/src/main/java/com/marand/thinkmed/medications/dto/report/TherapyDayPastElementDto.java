package com.marand.thinkmed.medications.dto.report;

import java.util.List;

import com.marand.thinkmed.medications.TherapyReportStatusEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.TherapyPharmacistReviewStatusEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Vid Kumse
 */

@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
public class TherapyDayPastElementDto extends TherapyDayCombinedDisplayDto
{
  private List<TherapyDayReportAdministrationDateGroupDto> givenAdministrations;
  private List<TherapyDayReportAdministrationDateGroupDto> notGivenAdministrations;
  private List<TherapyDayReportAdministrationDateGroupDto> deferredAdministrations;

  public TherapyDayPastElementDto(
      final List<TherapyDayReportAdministrationDateGroupDto> givenAdministrations,
      final List<TherapyDayReportAdministrationDateGroupDto> notGivenAdministrations,
      final List<TherapyDayReportAdministrationDateGroupDto> deferredAdministrations)
  {
    this.givenAdministrations = givenAdministrations;
    this.notGivenAdministrations = notGivenAdministrations;
    this.deferredAdministrations = deferredAdministrations;
  }

  public TherapyDayPastElementDto(
      final TherapyDto order,
      final String therapyStart,
      final String therapyEnd,
      final String currentRate,
      final TherapyPharmacistReviewStatusEnum pharmacistsReviewState,
      final TherapyReportStatusEnum therapyReportStatusEnum,
      final String therapyConsecutiveDay,
      final boolean showTherapyConsecutiveDay,
      final List<TherapyDayReportAdministrationDateGroupDto> givenAdministrations,
      final List<TherapyDayReportAdministrationDateGroupDto> notGivenAdministrations,
      final List<TherapyDayReportAdministrationDateGroupDto> deferredAdministrations,
      final String consecutiveDayLabel,
      final String currentOxygenDevice)
  {
    super(
        order,
        therapyStart,
        therapyEnd,
        currentRate,
        pharmacistsReviewState,
        therapyReportStatusEnum,
        therapyConsecutiveDay,
        showTherapyConsecutiveDay,
        consecutiveDayLabel,
        currentOxygenDevice);

    this.givenAdministrations = givenAdministrations;
    this.notGivenAdministrations = notGivenAdministrations;
    this.deferredAdministrations = deferredAdministrations;
  }

  public List<TherapyDayReportAdministrationDateGroupDto> getGivenAdministrations()
  {
    return givenAdministrations;
  }

  public void setGivenAdministrations(final List<TherapyDayReportAdministrationDateGroupDto> givenAdministrations)
  {
    this.givenAdministrations = givenAdministrations;
  }

  public List<TherapyDayReportAdministrationDateGroupDto> getNotGivenAdministrations()
  {
    return notGivenAdministrations;
  }

  public void setNotGivenAdministrations(final List<TherapyDayReportAdministrationDateGroupDto> notGivenAdministrations)
  {
    this.notGivenAdministrations = notGivenAdministrations;
  }

  public List<TherapyDayReportAdministrationDateGroupDto> getDeferredAdministrations()
  {
    return deferredAdministrations;
  }

  public void setDeferredAdministrations(final List<TherapyDayReportAdministrationDateGroupDto> deferredAdministrations)
  {
    this.deferredAdministrations = deferredAdministrations;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("givenAdministrations", givenAdministrations)
        .append("notGivenAdministrations", notGivenAdministrations)
        .append("deferredAdministrations", deferredAdministrations)
    ;
  }
}
