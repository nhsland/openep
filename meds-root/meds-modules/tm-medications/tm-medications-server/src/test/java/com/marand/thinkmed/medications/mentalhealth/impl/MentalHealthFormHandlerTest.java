package com.marand.thinkmed.medications.mentalhealth.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.marand.maf.core.Opt;
import com.marand.maf.core.security.remoting.GlobalAuditContext;
import com.marand.maf.core.service.ConstantUserMetadataProvider;
import com.marand.maf.core.service.RequestContextHolder;
import com.marand.maf.core.service.RequestContextImpl;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentType;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthMedicationDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTemplateDto;
import com.marand.thinkmed.medications.task.MedicationsTasksHandler;
import com.marand.thinkmed.request.user.RequestUser;
import com.marand.thinkmed.request.user.UserDto;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Nejc Korasa
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class MentalHealthFormHandlerTest
{
  @InjectMocks
  private ConsentFormToEhrSaver consentFormToEhrSaver;

  @Mock
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Mock
  private MedicationsTasksHandler medicationsTasksHandler;

  @BeforeClass
  public static void initRequest()
  {
    RequestUser.init(auth -> new UserDto("Test", null, "Test", Collections.emptyList()));
  }

  @Before
  public void setUpMocks()
  {
    RequestContextHolder.clearContext();
    RequestContextHolder.setContext(
        new RequestContextImpl(
            1L,
            GlobalAuditContext.current(),
            new DateTime(),
            Opt.of(ConstantUserMetadataProvider.createMetadata("User"))));
  }

  @Test
  public void testSaveNewMentalHealthReport()
  {
    final List<MentalHealthMedicationDto> mentalHealthMedicationDtos = new ArrayList<>();
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(1L, 1));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(1L, 2));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(2L, 3));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(2L, 4));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(2L, 5));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(2L, 6));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(2L, 7));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(2L, 8));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(2L, 9));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(3L, 10));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(3L, 11));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(3L, 12));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(3L, 13));
    mentalHealthMedicationDtos.add(createMentalHealthDrugDto(3L, 14));

    final List<MentalHealthTemplateDto> mentalHealthTemplateDtos = new ArrayList<>();
    mentalHealthTemplateDtos.add(createMentalHealthTemplateDto("template1", 1L, "oral", 1L));
    mentalHealthTemplateDtos.add(createMentalHealthTemplateDto("template2", 2L, "oral", 1L));
    mentalHealthTemplateDtos.add(createMentalHealthTemplateDto("template3", 3L, "oral", 1L));

    final DateTime when = DateTime.now();

    final MentalHealthDocumentDto mentalHealthDocumentDto = new MentalHealthDocumentDto(
        null,
        null,
        null,
        "2",
        null,
        MentalHealthDocumentType.T2,
        null,
        mentalHealthMedicationDtos,
        mentalHealthTemplateDtos);

    consentFormToEhrSaver.saveNewMentalHealthForm(mentalHealthDocumentDto, null, when);
  }

  private MentalHealthMedicationDto createMentalHealthDrugDto(final long routeId, final long medicationId)
  {
    final MedicationRouteDto route = new MedicationRouteDto();
    route.setId(routeId);
    route.setName(String.valueOf(routeId));

    return new MentalHealthMedicationDto(medicationId, "medication name", "generic name", route);
  }

  private MentalHealthTemplateDto createMentalHealthTemplateDto(
      final String templateName,
      final Long templateId,
      final String routeName,
      final Long routeId)
  {
    final MedicationRouteDto route = new MedicationRouteDto();
    route.setId(routeId);
    route.setName(routeName);

    return  new MentalHealthTemplateDto(templateId, templateName, route);
  }
}