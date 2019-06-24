package com.marand.thinkmed.medications;

import java.util.EnumSet;
import java.util.Set;

import com.marand.maf.core.Opt;

/**
 * @author Mitja Lapajne
 */
public enum MedicationLevelEnum
{
  /**
   * Sort order is important!
   */

  AMP("ampId"),
  VMP("vmpId"),
  VTM("vtmId");

  MedicationLevelEnum(final String identifier)
  {
    this.identifier = identifier;
  }

  private final String identifier;

  public MedicationLevelEnum getParent()
  {
    final int parentIndex = ordinal() + 1;
    return parentIndex >= values().length ? null : values()[parentIndex];
  }

  public MedicationLevelEnum getChild()
  {
    final int childIndex = ordinal() - 1;
    return childIndex < 0 ? null : values()[childIndex];
  }

  public String getIdentifier()
  {
    return identifier;
  }

  public String getParentIdentifier()
  {
    return Opt.of(getParent()).map(MedicationLevelEnum::getIdentifier).orElse(null);
  }

  public String getChildIdentifier()
  {
    return Opt.of(getChild()).map(MedicationLevelEnum::getIdentifier).orElse(null);
  }

  public static final Set<MedicationLevelEnum> PRODUCT_LEVELS = EnumSet.of(VMP, AMP);
}
