
package com.marand.thinkmed.medications.ehr.model;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvDateTime;

/**
 * @author Mitja Lapajne
 */
public class OrderDetails
{
  @EhrMapped("items[at0012]/value")
  private DvDateTime orderStartDateTime;

  @EhrMapped("items[at0013]/value")
  private DvDateTime orderStopDateTime;

  public DvDateTime getOrderStartDateTime()
  {
    return orderStartDateTime;
  }

  public void setOrderStartDateTime(final DvDateTime orderStartDateTime)
  {
    this.orderStartDateTime = orderStartDateTime;
  }

  public DvDateTime getOrderStopDateTime()
  {
    return orderStopDateTime;
  }

  public void setOrderStopDateTime(final DvDateTime orderStopDateTime)
  {
    this.orderStopDateTime = orderStopDateTime;
  }
}
