
package com.marand.thinkmed.medications.ehr.model;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvBoolean;

/**
 * @author Mitja Lapajne
 */
public class MedicationSafety
{
  @EhrMapped("items[at0064]/value")
  private DvBoolean exceptionalSafetyOverride;

  @EhrMapped("items[at0178]")
  private List<SafetyOverride> safetyOverrides = new ArrayList<>();

  @EhrMapped("items[at0051]")
  private MaximumDose maximumDose;

  public DvBoolean getExceptionalSafetyOverride()
  {
    return exceptionalSafetyOverride;
  }

  public void setExceptionalSafetyOverride(final DvBoolean exceptionalSafetyOverride)
  {
    this.exceptionalSafetyOverride = exceptionalSafetyOverride;
  }

  public List<SafetyOverride> getSafetyOverrides()
  {
    return safetyOverrides;
  }

  public void setSafetyOverrides(final List<SafetyOverride> safetyOverrides)
  {
    this.safetyOverrides = safetyOverrides;
  }

  public MaximumDose getMaximumDose()
  {
    return maximumDose;
  }

  public void setMaximumDose(final MaximumDose maximumDose)
  {
    this.maximumDose = maximumDose;
  }
}
