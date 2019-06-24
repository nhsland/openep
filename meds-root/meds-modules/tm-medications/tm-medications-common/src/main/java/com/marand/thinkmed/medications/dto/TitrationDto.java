package com.marand.thinkmed.medications.dto;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.api.internal.dto.TitrationType;
import com.marand.thinkmed.medications.connector.data.object.QuantityWithTimeDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class TitrationDto extends DataTransferObject implements JsonSerializable
{
  private TitrationType titrationType;
  private String name;
  private String unit;
  private Double normalRangeMin;
  private Double normalRangeMax;
  private MedicationDataDto medicationData;
  private List<QuantityWithTimeDto> results = new ArrayList<>();
  private List<TherapyForTitrationDto> therapies = new ArrayList<>();

  public TitrationType getTitrationType()
  {
    return titrationType;
  }

  public void setTitrationType(final TitrationType titrationType)
  {
    this.titrationType = titrationType;
  }

  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }

  public String getUnit()
  {
    return unit;
  }

  public void setUnit(final String unit)
  {
    this.unit = unit;
  }

  public Double getNormalRangeMin()
  {
    return normalRangeMin;
  }

  public void setNormalRangeMin(final Double normalRangeMin)
  {
    this.normalRangeMin = normalRangeMin;
  }

  public Double getNormalRangeMax()
  {
    return normalRangeMax;
  }

  public void setNormalRangeMax(final Double normalRangeMax)
  {
    this.normalRangeMax = normalRangeMax;
  }

  public MedicationDataDto getMedicationData()
  {
    return medicationData;
  }

  public void setMedicationData(final MedicationDataDto medicationData)
  {
    this.medicationData = medicationData;
  }

  public List<QuantityWithTimeDto> getResults()
  {
    return results;
  }

  public void setResults(final List<QuantityWithTimeDto> results)
  {
    this.results = results;
  }

  public List<TherapyForTitrationDto> getTherapies()
  {
    return therapies;
  }

  public void setTherapies(final List<TherapyForTitrationDto> therapies)
  {
    this.therapies = therapies;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("titrationType", titrationType)
        .append("name", name)
        .append("unit", unit)
        .append("normalRangeMin", normalRangeMin)
        .append("normalRangeMax", normalRangeMax)
        .append("medicationData", medicationData)
        .append("results", results)
        .append("therapies", therapies);
  }
}
