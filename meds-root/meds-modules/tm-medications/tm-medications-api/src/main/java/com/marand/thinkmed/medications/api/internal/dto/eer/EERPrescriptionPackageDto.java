package com.marand.thinkmed.medications.api.internal.dto.eer;

import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.api.internal.dto.prescription.PrescriptionPackageDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Kristijan Sečan
 */
public class EERPrescriptionPackageDto extends PrescriptionPackageDto
{
  private String oePhoneNumber;
  private SurchargeType surchargeType;
  private Payer payer;
  private TreatmentReason treatmentReason;
  private NamedExternalDto careProvider;
  private DateTime encounterDateTime;

  public String getOePhoneNumber()
  {
    return oePhoneNumber;
  }

  public void setOePhoneNumber(final String oePhoneNumber)
  {
    this.oePhoneNumber = oePhoneNumber;
  }

  public SurchargeType getSurchargeType()
  {
    return surchargeType;
  }

  public void setSurchargeType(final SurchargeType surchargeType)
  {
    this.surchargeType = surchargeType;
  }

  public Payer getPayer()
  {
    return payer;
  }

  public void setPayer(final Payer payer)
  {
    this.payer = payer;
  }

  public TreatmentReason getTreatmentReason()
  {
    return treatmentReason;
  }

  public void setTreatmentReason(final TreatmentReason treatmentReason)
  {
    this.treatmentReason = treatmentReason;
  }

  public NamedExternalDto getCareProvider()
  {
    return careProvider;
  }

  public void setCareProvider(final NamedExternalDto careProvider)
  {
    this.careProvider = careProvider;
  }

  public DateTime getEncounterDateTime()
  {
    return encounterDateTime;
  }

  public void setEncounterDateTime(final DateTime encounterDateTime)
  {
    this.encounterDateTime = encounterDateTime;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("oePhoneNumber", oePhoneNumber);
    tsb.append("surchargeType", surchargeType);
    tsb.append("payer", payer);
    tsb.append("treatmentReason", treatmentReason);
    tsb.append("careProvider", careProvider);
    tsb.append("encounterDateTime", encounterDateTime);
  }
}
