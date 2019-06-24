package com.marand.thinkmed.medications.dto.mentalHealth;

import java.util.Collection;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.api.internal.dto.document.TherapyDocumentContent;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public class MentalHealthDocumentDto extends DataTransferObject implements JsonSerializable, TherapyDocumentContent
{
  private final String compositionUId;
  private final DateTime createdTime;
  private final NamedExternalDto creator;
  private final NamedExternalDto careProvider;

  private final String patientId;
  private final MentalHealthDocumentType mentalHealthDocumentType;
  private final Integer maxDosePercentage;
  private final Collection<MentalHealthMedicationDto> mentalHealthMedicationDtoList;
  private final Collection<MentalHealthTemplateDto> mentalHealthTemplateDtoList;

  public MentalHealthDocumentDto(
      final String compositionUId,
      final DateTime createdTime,
      final NamedExternalDto creator,
      final String patientId,
      final NamedExternalDto careProvider,
      final MentalHealthDocumentType mentalHealthDocumentType,
      final Integer maxDosePercentage,
      final Collection<MentalHealthMedicationDto> mentalHealthMedicationDtoList,
      final Collection<MentalHealthTemplateDto> mentalHealthTemplateDtoList)
  {
    this.compositionUId = compositionUId;
    this.createdTime = createdTime;
    this.creator = creator;
    this.patientId = patientId;
    this.careProvider = careProvider;
    this.mentalHealthDocumentType = mentalHealthDocumentType;
    this.maxDosePercentage = maxDosePercentage;
    this.mentalHealthMedicationDtoList = mentalHealthMedicationDtoList;
    this.mentalHealthTemplateDtoList = mentalHealthTemplateDtoList;
  }

  public String getCompositionUId()
  {
    return compositionUId;
  }

  public DateTime getCreatedTime()
  {
    return createdTime;
  }

  public NamedExternalDto getCreator()
  {
    return creator;
  }

  public MentalHealthDocumentType getMentalHealthDocumentType()
  {
    return mentalHealthDocumentType;
  }

  public String getPatientId()
  {
    return patientId;
  }

  public NamedExternalDto getCareProvider()
  {
    return careProvider;
  }

  public Integer getMaxDosePercentage()
  {
    return maxDosePercentage;
  }

  public Collection<MentalHealthMedicationDto> getMentalHealthMedicationDtoList()
  {
    return mentalHealthMedicationDtoList;
  }

  public Collection<MentalHealthTemplateDto> getMentalHealthTemplateDtoList()
  {
    return mentalHealthTemplateDtoList;
  }

  @Override
  public String getContentId()
  {
    return compositionUId;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("compositionUId", compositionUId)
        .append("patientId", patientId)
        .append("creator", creator)
        .append("createdTime", createdTime)
        .append("careProvider", careProvider)
        .append("mentalHealthDocumentType", mentalHealthDocumentType)
        .append("maxDosePercentage", maxDosePercentage)
        .append("mentalHealthMedicationDtoList", mentalHealthMedicationDtoList)
        .append("mentalHealthTemplateDtoList", mentalHealthTemplateDtoList)
    ;
  }
}
