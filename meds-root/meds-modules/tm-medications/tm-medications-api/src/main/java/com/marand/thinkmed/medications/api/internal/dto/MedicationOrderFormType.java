package com.marand.thinkmed.medications.api.internal.dto;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.DvCodedText;

/**
 * @author Mitja Lapajne
 */
public enum MedicationOrderFormType
{
  SIMPLE("Simple"),
  COMPLEX("Complex"),
  OXYGEN("Oxygen"),
  DESCRIPTIVE("Descriptive");

  public static final Set<MedicationOrderFormType> SIMPLE_ORDERS = EnumSet.of(SIMPLE, DESCRIPTIVE);

  private final String ehrValue;

  MedicationOrderFormType(final String ehrValue)
  {
    this.ehrValue = ehrValue;
  }

  public DvCodedText getDvCodedText()
  {
    return DataValueUtils.getLocalCodedText(ehrValue, ehrValue);
  }

  public String getEhrValue()
  {
    return ehrValue;
  }

  public static MedicationOrderFormType valueOf(final DvCodedText dvCodedText)
  {
    return Arrays.stream(values())
        .filter(e -> e.getEhrValue().equals(dvCodedText.getDefiningCode().getCodeString()))
        .findFirst()
        .orElse(null);
  }

  @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
  public boolean matches(final DvCodedText dvCodedText)
  {
    return dvCodedText != null && ehrValue.equals(dvCodedText.getDefiningCode().getCodeString());
  }
}
