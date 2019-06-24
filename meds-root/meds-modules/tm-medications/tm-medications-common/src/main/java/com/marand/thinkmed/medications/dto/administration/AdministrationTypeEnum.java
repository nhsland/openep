package com.marand.thinkmed.medications.dto.administration;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */

public enum AdministrationTypeEnum
{
  START, STOP, ADJUST_INFUSION, INFUSION_SET_CHANGE, BOLUS;

  public static final Set<AdministrationTypeEnum> MEDICATION_ADMINISTRATION = EnumSet.of(START, STOP, ADJUST_INFUSION, BOLUS);
  public static final Set<AdministrationTypeEnum> DOSE_ADMINISTRATION = EnumSet.of(START, ADJUST_INFUSION, BOLUS);
  public static final Set<AdministrationTypeEnum> START_OR_ADJUST = EnumSet.of(START, ADJUST_INFUSION);
  public static final Set<AdministrationTypeEnum> NOT_STOP = EnumSet.of(START, ADJUST_INFUSION, INFUSION_SET_CHANGE, BOLUS);

  public DvText getDvText()
  {
    return DataValueUtils.getText(name());
  }

  public static AdministrationTypeEnum valueOf(final DvText dvText)
  {
    return Arrays.stream(values())
        .filter(e -> e.name().equals(dvText.getValue()))
        .findFirst()
        .orElse(null);
  }
}
