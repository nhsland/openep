package com.marand.thinkmed.medications.api.internal.dto;

import java.util.Arrays;

import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */

public enum MedicalDeviceEnum
{
  CPAP_MASK("CPAP (mask)"),
  CPAP_NASAL("CPAP (nasal)"),
  FULL_FACE_MASK("Full face mask"),
  NASAL_NIV_MASK("Nasal NIV mask"),
  OXYGEN_MASK("Oxygen mask"),
  NASAL_CATHETER("Nasal catheter"),
  HIGH_FLOW_NASAL_CATHETER("High-flow nasal catheter"),
  VENTURI_MASK("Venturi mask"),
  OHIO_MASK("Ohio mask"),
  INCUBATOR("Incubator"),
  TENT("Tent"),
  T_TUBE("T-Tube"),
  TRACHEAL_TUBE("Tracheal Tube"),
  TRACHEAL_CANNULA("Tracheal Cannula"),
  HIGH_FLOW_TRACHEAL_CANNULA("High-flow tracheal cannula");

  private final String ehrValue;

  MedicalDeviceEnum(final String ehrValue)
  {
    this.ehrValue = ehrValue;
  }

  public String getDictionaryKey()
  {
    return getClass().getSimpleName() + "." + name();
  }

  public DvCodedText getDvCodedText()
  {
    return DataValueUtils.getLocalCodedText(ehrValue, ehrValue);
  }

  public String getEhrValue()
  {
    return ehrValue;
  }

  public static MedicalDeviceEnum valueOf(final DvText dvText)
  {
    if (dvText instanceof DvCodedText)
    {
      return Arrays.stream(values())
          .filter(e -> e.getEhrValue().equals(((DvCodedText)dvText).getDefiningCode().getCodeString()))
          .findFirst()
          .orElse(null);
    }
    return null;
  }
}
