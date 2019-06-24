package com.marand.thinkmed.medications.dto.pharmacist.review;

import java.util.Arrays;

import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */

@SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
public enum PharmacistReviewStatusEnum
{
  DRAFT("Draft"),
  FINAL("Final");

  private final String code;

  PharmacistReviewStatusEnum(final String code)
  {
    this.code = code;
  }

  public DvText getDvText()
  {
    return DataValueUtils.getText(code);
  }

  public boolean matches(final DvText dvText)
  {
    if (dvText == null)
    {
      return false;
    }

    return code.equals(dvText.getValue());
  }

  public static PharmacistReviewStatusEnum valueOf(final DvText dvText)
  {
    if (dvText == null)
    {
      return null;
    }

    return Arrays.stream(values()).filter(v -> v.matches(dvText)).findFirst().orElse(null);
  }

  public String getCode()
  {
    return code;
  }
}
