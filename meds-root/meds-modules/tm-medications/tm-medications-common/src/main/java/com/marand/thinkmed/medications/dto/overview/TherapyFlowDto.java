package com.marand.thinkmed.medications.dto.overview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class TherapyFlowDto extends DataTransferObject
{
  private List<TherapyFlowRowDto> therapyRows = new ArrayList<>();
  private Map<Integer, Double> referenceWeightsDayMap = new HashMap<>();

  public List<TherapyFlowRowDto> getTherapyRows()
  {
    return therapyRows;
  }

  public void setTherapyRows(final List<TherapyFlowRowDto> therapyRows)
  {
    this.therapyRows = therapyRows;
  }

  public Map<Integer, Double> getReferenceWeightsDayMap()
  {
    return referenceWeightsDayMap;
  }

  public void setReferenceWeightsDayMap(final Map<Integer, Double> referenceWeightsDayMap)
  {
    this.referenceWeightsDayMap = referenceWeightsDayMap;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("therapyRows", therapyRows)
        .append("referenceWeightsDayMap", referenceWeightsDayMap)
    ;
  }
}
