package com.marand.thinkmed.medications.api.internal.dto.eer;

import com.marand.thinkmed.medications.api.internal.dto.PrescriptionLocalDetailsDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class EERPrescriptionLocalDetailsDto extends PrescriptionLocalDetailsDto
{
  private OutpatientPrescriptionDocumentType prescriptionDocumentType;
  private IllnessConditionType illnessConditionType;
  private String instructionsToPharmacist;
  private Integer prescriptionRepetition;
  private Integer remainingDispenses;
  private Payer payer;
  private boolean maxDoseExceeded;
  private boolean doNotSwitch;
  private boolean magistralPreparation;
  private boolean urgent;

  public OutpatientPrescriptionDocumentType getPrescriptionDocumentType()
  {
    return prescriptionDocumentType;
  }

  public void setPrescriptionDocumentType(final OutpatientPrescriptionDocumentType prescriptionDocumentType)
  {
    this.prescriptionDocumentType = prescriptionDocumentType;
  }

  public IllnessConditionType getIllnessConditionType()
  {
    return illnessConditionType;
  }

  public void setIllnessConditionType(final IllnessConditionType illnessConditionType)
  {
    this.illnessConditionType = illnessConditionType;
  }

  public String getInstructionsToPharmacist()
  {
    return instructionsToPharmacist;
  }

  public void setInstructionsToPharmacist(final String instructionsToPharmacist)
  {
    this.instructionsToPharmacist = instructionsToPharmacist;
  }

  public Integer getPrescriptionRepetition()
  {
    return prescriptionRepetition;
  }

  public void setPrescriptionRepetition(final Integer prescriptionRepetition)
  {
    this.prescriptionRepetition = prescriptionRepetition;
  }

  public Integer getRemainingDispenses()
  {
    return remainingDispenses;
  }

  public void setRemainingDispenses(final Integer remainingDispenses)
  {
    this.remainingDispenses = remainingDispenses;
  }

  public boolean isMaxDoseExceeded()
  {
    return maxDoseExceeded;
  }

  public void setMaxDoseExceeded(final boolean maxDoseExceeded)
  {
    this.maxDoseExceeded = maxDoseExceeded;
  }

  public boolean isDoNotSwitch()
  {
    return doNotSwitch;
  }

  public void setDoNotSwitch(final boolean doNotSwitch)
  {
    this.doNotSwitch = doNotSwitch;
  }

  public boolean isUrgent() { return urgent; }

  public void setUrgent(final boolean urgent) { this.urgent = urgent; }

  public boolean isMagistralPreparation()
  {
    return magistralPreparation;
  }

  public void setMagistralPreparation(final boolean magistralPreparation)
  {
    this.magistralPreparation = magistralPreparation;
  }

  public Payer getPayer()
  {
    return payer;
  }

  public void setPayer(final Payer payer)
  {
    this.payer = payer;
  }

  @Override
  public String getPrescriptionSystem()
  {
    return EERPrescriptionSystemEnum.EER.name();
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("prescriptionDocumentType", prescriptionDocumentType);
    tsb.append("illnessConditionType", illnessConditionType);
    tsb.append("instructionsToPharmacist", instructionsToPharmacist);
    tsb.append("prescriptionRepetition", prescriptionRepetition);
    tsb.append("remainingDispenses", remainingDispenses);
    tsb.append("maxDoseExceeded", maxDoseExceeded);
    tsb.append("doNotSwitch", doNotSwitch);
    tsb.append("urgent", urgent);
    tsb.append("magistralPreparation", magistralPreparation);
    tsb.append("payer", payer);
  }
}
