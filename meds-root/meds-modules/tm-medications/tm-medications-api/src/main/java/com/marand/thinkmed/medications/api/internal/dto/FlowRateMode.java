
package com.marand.thinkmed.medications.api.internal.dto;

/**
 * @author Mitja Lapajne
 */
public enum FlowRateMode
{
  HIGH_FLOW(MedicationAdditionalInstructionEnum.HIGH_FLOW),
  LOW_FLOW(MedicationAdditionalInstructionEnum.LOW_FLOW);

  private final MedicationAdditionalInstructionEnum additionalInstructionEnum;

  FlowRateMode(final MedicationAdditionalInstructionEnum additionalInstructionEnum)
  {
    this.additionalInstructionEnum = additionalInstructionEnum;
  }

  public MedicationAdditionalInstructionEnum getAdditionalInstructionEnum()
  {
    return additionalInstructionEnum;
  }
}