package com.marand.thinkmed.medications.api.internal.dto;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */
public enum MedicationAdditionalInstructionEnum
{
  BEFORE_MEAL("Before meal"),
  AFTER_MEAL("After meal"),
  WITH_FOOD("With food"),
  EMPTY_STOMACH("On an empty stomach"),
  AT_BEDTIME("At bedtime"),
  REGARDLESS_OF_MEAL("Regardless of meal"),

  HIGH_FLOW("High flow"),
  LOW_FLOW("Low flow"),
  HUMIDIFICATION("Humidification");

  private final String ehrValue;

  MedicationAdditionalInstructionEnum(final String ehrValue)
  {
    this.ehrValue = ehrValue;
  }

  public String getEhrValue()
  {
    return ehrValue;
  }

  public DvCodedText getDvCodedText()
  {
    return DataValueUtils.getLocalCodedText(ehrValue, ehrValue);
  }

  public static final Set<MedicationAdditionalInstructionEnum> APPLICATION_PRECONDITION =
      EnumSet.of(BEFORE_MEAL, AFTER_MEAL, WITH_FOOD, EMPTY_STOMACH, AT_BEDTIME, REGARDLESS_OF_MEAL);

  public static MedicationAdditionalInstructionEnum valueOf(final DvCodedText dvCodedText)
  {
    return Arrays.stream(values())
        .filter(e -> e.getEhrValue().equals(dvCodedText.getDefiningCode().getCodeString()))
        .findFirst()
        .orElse(null);
  }

  public boolean matches(final DvText dvText)
  {
    return dvText instanceof DvCodedText && ehrValue.equals(((DvCodedText)dvText).getDefiningCode().getCodeString());
  }
}