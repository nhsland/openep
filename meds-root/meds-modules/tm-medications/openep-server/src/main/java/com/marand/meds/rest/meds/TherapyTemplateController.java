package com.marand.meds.rest.meds;

import java.util.List;
import java.util.Locale;

import com.marand.auditing.auditing.Auditable;
import com.marand.auditing.auditing.AuditableType;
import com.marand.maf.core.JsonUtil;
import com.marand.thinkmed.medications.MedsJsonDeserializer;
import com.marand.thinkmed.medications.TherapyTemplateContextEnum;
import com.marand.thinkmed.medications.TherapyTemplateModeEnum;
import com.marand.thinkmed.medications.dto.template.TherapyTemplateDto;
import com.marand.thinkmed.medications.dto.template.TherapyTemplatesDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTemplateDto;
import com.marand.thinkmed.medications.service.MedicationsService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Boris Marn.
 */
@RestController
@RequestMapping("/medications")
public class TherapyTemplateController
{
  private final MedicationsService service;

  @Autowired
  public TherapyTemplateController(final MedicationsService service)
  {
    this.service = service;
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @GetMapping(path = "getTherapyTemplates", produces = MediaType.APPLICATION_JSON_VALUE)
  public TherapyTemplatesDto getTherapyTemplates(
      @RequestParam("patientId") final String patientId,
      @RequestParam("templateContext") final TherapyTemplateContextEnum templateContext,
      @RequestParam(value = "careProviderId", required = false) final String careProviderId,
      @RequestParam(value = "referenceWeight", required = false) final Double referenceWeight,
      @RequestParam(value = "patientHeight", required = false) final Double patientHeight,
      @RequestParam(value = "birthDate", required = false) final String birthDate,
      @RequestParam("language") final String language)
  {
    return service.getTherapyTemplates(
        patientId,
        templateContext,
        careProviderId,
        referenceWeight,
        patientHeight,
        JsonUtil.fromJson(birthDate, DateTime.class),
        new Locale(language));
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @GetMapping(path = "getAllTherapyTemplates", produces = MediaType.APPLICATION_JSON_VALUE)
  public TherapyTemplatesDto getAllTherapyTemplates(
      @RequestParam("templateMode") final TherapyTemplateModeEnum templateMode,
      @RequestParam(value = "careProviderId", required = false) final String careProviderId,
      @RequestParam("language") final String language)
  {
    return service.getAllTherapyTemplates(templateMode, careProviderId, new Locale(language));
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @GetMapping(path = "getTherapyTemplateGroups", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<String> getTherapyTemplateGroups(
      @RequestParam("templateMode") final TherapyTemplateModeEnum templateMode)
  {
    return service.getTherapyTemplateGroups(templateMode);
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(path = "saveTherapyTemplate", produces = MediaType.APPLICATION_JSON_VALUE)
  public long saveTherapyTemplate(
      @RequestParam("template") final String template,
      @RequestParam("templateMode") final TherapyTemplateModeEnum templateMode)
  {
    final TherapyTemplateDto templateDto =
        JsonUtil.fromJson(template, TherapyTemplateDto.class, MedsJsonDeserializer.INSTANCE.getTypeAdapters());
    return service.saveTherapyTemplate(templateDto, templateMode);
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(path = "deleteTherapyTemplate")
  public void deleteTherapyTemplate(
      @RequestParam("templateId") final Long templateId)
  {
    service.deleteTherapyTemplate(templateId);
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getMentalHealthTemplates", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<MentalHealthTemplateDto> getMentalHealthTemplates()
  {
    return service.getMentalHealthTemplates();
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(path = "addTherapyTemplateGroup")
  public void addTherapyTemplateGroup(
      @RequestParam("name") final String name,
      @RequestParam("templateMode") final TherapyTemplateModeEnum templateMode)
  {
    service.addTherapyTemplateGroup(name,templateMode);
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @DeleteMapping(path = "deleteTherapyTemplateGroup")
  public void deleteTherapyTemplateGroup(
      @RequestParam("id") final long id)
  {
    service.deleteTherapyTemplateGroup(id);
  }
}
