package com.marand.thinkmed.medications.dto.administration;

import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class StartAdministrationDto extends AdministrationDto
    implements DoseAdministration, PlannedDoseAdministration, InfusionBagAdministration
{
  private StartAdministrationSubtype startAdministrationSubtype;
  private MedicationDto substituteMedication;
  private TherapyDoseDto administeredDose;
  private TherapyDoseDto plannedDose;
  private boolean differentFromOrder;
  private Double duration; // in minutes
  private InfusionBagDto infusionBag;

  public StartAdministrationDto()
  {
    super(AdministrationTypeEnum.START);
  }

  protected StartAdministrationDto(final StartAdministrationSubtype startAdministrationSubtype)
  {
    super(AdministrationTypeEnum.START);
    this.startAdministrationSubtype = startAdministrationSubtype;
  }

  public StartAdministrationSubtype getStartAdministrationSubtype()
  {
    return startAdministrationSubtype;
  }

  public MedicationDto getSubstituteMedication()
  {
    return substituteMedication;
  }

  public void setSubstituteMedication(final MedicationDto substituteMedication)
  {
    this.substituteMedication = substituteMedication;
  }

  @Override
  public TherapyDoseDto getAdministeredDose()
  {
    return administeredDose;
  }

  @Override
  public void setAdministeredDose(final TherapyDoseDto administeredDose)
  {
    this.administeredDose = administeredDose;
  }

  @Override
  public TherapyDoseDto getPlannedDose()
  {
    return plannedDose;
  }

  @Override
  public void setPlannedDose(final TherapyDoseDto plannedDose)
  {
    this.plannedDose = plannedDose;
  }

  @Override
  public boolean isDifferentFromOrder()
  {
    return differentFromOrder;
  }

  @Override
  public void setDifferentFromOrder(final boolean differentFromOrder)
  {
    this.differentFromOrder = differentFromOrder;
  }

  @Override
  public InfusionBagDto getInfusionBag()
  {
    return infusionBag;
  }

  @Override
  public void setInfusionBag(final InfusionBagDto infusionBag)
  {
    this.infusionBag = infusionBag;
  }

  public Double getDuration()
  {
    return duration;
  }

  public void setDuration(final Double duration)
  {
    this.duration = duration;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("startAdministrationSubtype", startAdministrationSubtype)
        .append("substituteMedication", substituteMedication)
        .append("administeredDose", administeredDose)
        .append("plannedDose", plannedDose)
        .append("differentFromOrder", differentFromOrder)
        .append("duration", duration)
        .append("infusionBag", infusionBag)
    ;
  }
}