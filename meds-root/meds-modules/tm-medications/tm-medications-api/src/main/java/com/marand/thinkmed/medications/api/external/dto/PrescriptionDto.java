package com.marand.thinkmed.medications.api.external.dto;

/**
 * @author Mitja Lapajne
 */
public class PrescriptionDto
{
  private String display;
  private MedicationDto medication;
  private RoutesDto routes;
  private DoseDto dose;
  private DoseFormDto doseForm;
  private TimingDirectionsDto timingDirections;
  private ReleaseCharacteristicsDto releaseCharacteristics;
  private CommentDto comment;
  private IndicationDto indication;
  private AdditionalInstructionsDto additionalInstructions;

  public String getDisplay()
  {
    return display;
  }

  public void setDisplay(final String display)
  {
    this.display = display;
  }

  public MedicationDto getMedication()
  {
    return medication;
  }

  public void setMedication(final MedicationDto medication)
  {
    this.medication = medication;
  }

  public RoutesDto getRoutes()
  {
    return routes;
  }

  public void setRoutes(final RoutesDto routes)
  {
    this.routes = routes;
  }

  public DoseDto getDose()
  {
    return dose;
  }

  public void setDose(final DoseDto dose)
  {
    this.dose = dose;
  }

  public DoseFormDto getDoseForm()
  {
    return doseForm;
  }

  public void setDoseForm(final DoseFormDto doseForm)
  {
    this.doseForm = doseForm;
  }

  public TimingDirectionsDto getTimingDirections()
  {
    return timingDirections;
  }

  public void setTimingDirections(final TimingDirectionsDto timingDirections)
  {
    this.timingDirections = timingDirections;
  }

  public ReleaseCharacteristicsDto getReleaseCharacteristics()
  {
    return releaseCharacteristics;
  }

  public void setReleaseCharacteristics(final ReleaseCharacteristicsDto releaseCharacteristics)
  {
    this.releaseCharacteristics = releaseCharacteristics;
  }

  public CommentDto getComment()
  {
    return comment;
  }

  public void setComment(final CommentDto comment)
  {
    this.comment = comment;
  }

  public IndicationDto getIndication()
  {
    return indication;
  }

  public void setIndication(final IndicationDto indication)
  {
    this.indication = indication;
  }

  public AdditionalInstructionsDto getAdditionalInstructions()
  {
    return additionalInstructions;
  }

  public void setAdditionalInstructions(final AdditionalInstructionsDto additionalInstructions)
  {
    this.additionalInstructions = additionalInstructions;
  }
}
