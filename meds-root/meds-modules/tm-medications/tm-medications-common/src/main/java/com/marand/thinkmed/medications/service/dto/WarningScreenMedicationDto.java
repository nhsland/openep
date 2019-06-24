package com.marand.thinkmed.medications.service.dto;

import com.marand.maf.core.data.object.NamedIdDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 */

public class WarningScreenMedicationDto extends NamedIdDto
{
  private String externalId;
  private boolean prospective;
  private Long routeId;
  private String routeExternalId;
  private boolean product;

  public WarningScreenMedicationDto() {}

  public WarningScreenMedicationDto(
      final long id,
      final String name,
      final String externalId,
      final boolean prospective,
      final Long routeId,
      final String routeExternalId)
  {
    super(id, name);
    this.externalId = externalId;
    this.prospective = prospective;
    this.routeId = routeId;
    this.routeExternalId = routeExternalId;
  }

  public String getExternalId()
  {
    return externalId;
  }

  public void setExternalId(final String externalId)
  {
    this.externalId = externalId;
  }

  public boolean isProspective()
  {
    return prospective;
  }

  public void setProspective(final boolean prospective)
  {
    this.prospective = prospective;
  }

  public Long getRouteId()
  {
    return routeId;
  }

  public void setRouteId(final Long routeId)
  {
    this.routeId = routeId;
  }

  public String getRouteExternalId()
  {
    return routeExternalId;
  }

  public void setRouteExternalId(final String routeExternalId)
  {
    this.routeExternalId = routeExternalId;
  }

  public boolean isProduct()
  {
    return product;
  }

  public void setProduct(final boolean product)
  {
    this.product = product;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("externalId", externalId)
        .append("prospective", prospective)
        .append("product", product)
        .append("routeId", routeId)
        .append("routeExternalId", routeExternalId)
    ;
  }
}
