package com.marand.thinkmed.medications.api.internal.dto.eer;

/**
 * @author Miha Anzicek
 */
public enum EPrescriptionStatus
{
  PRESCRIBED("Predpisan"),
  PARTIALLY_USED("DelnoPorabljen"),
  PARTIALLY_USED_CANCELLED("DelnoPorabljenRazveljavljen"),
  CANCELLED("Razveljavljen"),
  USED("Porabljen"),
  IN_PREPARATION("VPripravi"),
  IN_DISPENSE("VIzdajanju"),
  REJECTED("Zavrnjen"),
  PARTIALLY_USED_REJECTED("DelnoPorabljenZavrnjen"),
  WITHDRAWN("Umaknjen")
  ;

  private final String code;

  EPrescriptionStatus(final String code)
  {
    this.code = code;
  }

  public String getCode()
  {
    return code;
  }

  public static EPrescriptionStatus getInstance(final String code)
  {
    for (final EPrescriptionStatus statusEnum : values())
    {
      if (statusEnum.getCode().equals(code))
      {
        return statusEnum;
      }
    }
    throw new IllegalArgumentException("Unknown digital prescription status code: " + code);
  }
}
