package com.marand.thinkmed.medications.dao;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import lombok.NonNull;

import com.marand.thinkmed.medications.TherapyTemplateModeEnum;
import com.marand.thinkmed.medications.dto.template.TherapyTemplateDto;
import com.marand.thinkmed.medications.dto.template.TherapyTemplatesDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTemplateDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTemplateMemberDto;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public interface TherapyTemplateDao
{
  TherapyTemplatesDto getTherapyTemplates(
      @NonNull TherapyTemplateModeEnum templateMode,
      @NonNull Collection<String> templateGroups,
      String patientId,
      String userId,
      String careProviderId,
      Double referenceWeight,
      Double patientHeightInCm,
      DateTime birthDate,
      @NonNull Locale locale);

  TherapyTemplatesDto getAllTherapyTemplates(
      @NonNull TherapyTemplateModeEnum templateMode,
      @NonNull String userId,
      String careProviderId,
      @NonNull Locale locale);

  long saveTherapyTemplate(@NonNull TherapyTemplateDto therapyTemplate, @NonNull TherapyTemplateModeEnum templateMode);

  void deleteTherapyTemplate(long templateId);

  void deleteTherapyTemplateGroup(long templateId);

  void addTherapyTemplateGroup(String name, TherapyTemplateModeEnum tempalteMode);

  List<String> getTherapyTemplateGroups(@NonNull TherapyTemplateModeEnum templateMode);

  List<MentalHealthTemplateDto> getMentalHealthTemplates();

  List<MentalHealthTemplateMemberDto> getMentalHealthTemplateMembers(@NonNull Collection<Long> mentalHealthTemplateIds);
}
