package com.marand.thinkmed.medications.dto.unit;

import java.util.Arrays;

import lombok.NonNull;

/**
 * @author Nejc Korasa
 */

public enum KnownUnitType
{
  L,
  DL,
  CL,
  ML,
  MICRO_L,

  G,
  KG,
  MG,
  MICRO_G,
  NANO_G,

  D,
  H,
  MIN,
  S,

  M2,

  L_MIN;

  public static KnownUnitType getByName(final @NonNull String name)
  {
    return Arrays.stream(values()).filter(t -> t.name().equals(name)).findFirst().orElse(null);
  }
}
