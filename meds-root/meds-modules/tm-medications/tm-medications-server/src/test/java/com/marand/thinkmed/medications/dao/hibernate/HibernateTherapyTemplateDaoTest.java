package com.marand.thinkmed.medications.dao.hibernate;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.marand.thinkmed.medications.TherapyTemplateModeEnum;
import com.marand.thinkmed.medications.TherapyTemplatePreconditionEnum;
import com.marand.thinkmed.medications.TherapyTemplateTypeEnum;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.dao.TherapyTemplateDao;
import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.template.TherapyTemplateDto;
import com.marand.thinkmed.medications.dto.template.TherapyTemplateElementDto;
import com.marand.thinkmed.medications.dto.template.TherapyTemplatePreconditionDto;
import com.marand.thinkmed.medications.dto.template.TherapyTemplateStatus;
import com.marand.thinkmed.medications.dto.template.TherapyTemplatesDto;
import com.marand.thinkmed.medications.model.impl.TherapyTemplateElementImpl;
import com.marand.thinkmed.medications.model.impl.TherapyTemplateImpl;
import com.marand.thinkmed.medications.model.impl.TherapyTemplatePreconditionImpl;
import org.hibernate.SessionFactory;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Mitja Lapajne
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/com/marand/thinkmed/medications/dao/hibernate/HibernateTherapyTemplateDaoTest-context.xml"})
@DbUnitConfiguration(databaseConnection = {"dbUnitDatabaseConnection"})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionDbUnitTestExecutionListener.class})
@Transactional
public class HibernateTherapyTemplateDaoTest
{
  @Autowired
  private TherapyTemplateDao therapyTemplateDao;

  @Autowired
  private SessionFactory sessionFactory;

  @Autowired
  private MedicationsBo medicationsBo;

  @Before
  public void setMocks()
  {
    Mockito
        .when(medicationsBo.calculateBodySurfaceArea(120.0, 40.0))
        .thenReturn(1.1547005383792515);
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetTherapyTemplates.xml")
  public void testGetTherapyTemplatesPatientUserCareProvider()
  {
    final TherapyTemplatesDto templatesDto =
        therapyTemplateDao.getTherapyTemplates(
            TherapyTemplateModeEnum.INPATIENT,
            Collections.emptyList(),
            "555",
            "2",
            "1",
            40.0,
            120.0,
            new DateTime().minusYears(2),
            new Locale("en"));
    assertEquals(2L, (long)templatesDto.getUserTemplates().size());

    final TherapyTemplateDto firstUserTemplate = templatesDto.getUserTemplates().get(0);
    assertEquals(TherapyTemplateTypeEnum.USER, firstUserTemplate.getType());
    assertEquals("U1", firstUserTemplate.getName());
    assertEquals("2", firstUserTemplate.getUserId());
    assertNull(firstUserTemplate.getCareProviderId());
    assertEquals(2L, (long)firstUserTemplate.getTemplateElements().size());
    assertSame(TherapyTemplateStatus.COMPLETE, firstUserTemplate.getTemplateElements().get(0).getStatus());
    assertFalse(firstUserTemplate.getTemplateElements().get(0).doRecordAdministration());
    assertNotSame(TherapyTemplateStatus.COMPLETE, firstUserTemplate.getTemplateElements().get(1).getStatus());
    assertFalse(firstUserTemplate.getTemplateElements().get(1).doRecordAdministration());

    final TherapyTemplateDto secondUserTemplate = templatesDto.getUserTemplates().get(1);
    assertEquals(TherapyTemplateTypeEnum.USER, secondUserTemplate.getType());
    assertEquals("U2", secondUserTemplate.getName());
    assertEquals("2", secondUserTemplate.getUserId());
    assertNull(secondUserTemplate.getCareProviderId());
    assertEquals(1L, (long)secondUserTemplate.getTemplateElements().size());
    assertSame(TherapyTemplateStatus.COMPLETE, secondUserTemplate.getTemplateElements().get(0).getStatus());
    assertTrue(secondUserTemplate.getTemplateElements().get(0).doRecordAdministration());

    assertEquals(1L, (long)templatesDto.getOrganizationTemplates().size());
    final TherapyTemplateDto organizationTemplate = templatesDto.getOrganizationTemplates().get(0);
    assertEquals(TherapyTemplateTypeEnum.ORGANIZATIONAL, organizationTemplate.getType());
    assertEquals("O1", organizationTemplate.getName());
    assertNull(organizationTemplate.getUserId());
    assertEquals("1", organizationTemplate.getCareProviderId());
    assertEquals(1L, (long)organizationTemplate.getTemplateElements().size());
    assertSame(TherapyTemplateStatus.COMPLETE, organizationTemplate.getTemplateElements().get(0).getStatus());
    assertFalse(organizationTemplate.getTemplateElements().get(0).doRecordAdministration());


    assertEquals(0L, (long)templatesDto.getPatientTemplates().size());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetTherapyTemplates.xml")
  public void testGetTherapyTemplatesUserPreconditionsSomeNotMet()
  {
    final TherapyTemplatesDto templatesDto =
        therapyTemplateDao.getTherapyTemplates(
            TherapyTemplateModeEnum.INPATIENT,
            Collections.emptyList(),
            "10",
            "2",
            null,
            60.0,
            null,
            new DateTime().minusYears(2),
            new Locale("en"));
    assertEquals(1, (long)templatesDto.getUserTemplates().size());
    assertEquals(0L, (long)templatesDto.getOrganizationTemplates().size());
    assertEquals(0L, (long)templatesDto.getPatientTemplates().size());

    final TherapyTemplateDto secondUserTemplate = templatesDto.getUserTemplates().get(0);
    assertEquals(TherapyTemplateTypeEnum.USER, secondUserTemplate.getType());
    assertEquals("U2", secondUserTemplate.getName());
    assertEquals("2", secondUserTemplate.getUserId());
    assertNull(secondUserTemplate.getCareProviderId());
    assertEquals(1L, (long)secondUserTemplate.getTemplateElements().size());
    assertSame(TherapyTemplateStatus.COMPLETE, secondUserTemplate.getTemplateElements().get(0).getStatus());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetTherapyTemplates.xml")
  public void testGetTherapyTemplatesUserPreconditionsNoneMet()
  {
    final TherapyTemplatesDto templatesDto =
        therapyTemplateDao.getTherapyTemplates(
            TherapyTemplateModeEnum.INPATIENT,
            Collections.emptyList(),
            "10",
            "2",
            null,
            40.0,
            120.0,
            new DateTime().minusDays(20),
            new Locale("en"));

    assertEquals(1, (long)templatesDto.getUserTemplates().size());
    assertEquals(0L, (long)templatesDto.getOrganizationTemplates().size());
    assertEquals(0L, (long)templatesDto.getPatientTemplates().size());

    final TherapyTemplateDto secondUserTemplate = templatesDto.getUserTemplates().get(0);
    assertEquals(TherapyTemplateTypeEnum.USER, secondUserTemplate.getType());
    assertEquals("U2", secondUserTemplate.getName());
    assertEquals("2", secondUserTemplate.getUserId());
    assertNull(secondUserTemplate.getCareProviderId());
    assertEquals(1L, (long)secondUserTemplate.getTemplateElements().size());
    assertSame(TherapyTemplateStatus.COMPLETE, secondUserTemplate.getTemplateElements().get(0).getStatus());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetTherapyTemplates.xml")
  public void testGetTherapyTemplatesPatient()
  {
    final TherapyTemplatesDto templatesDto =
        therapyTemplateDao.getTherapyTemplates(
            TherapyTemplateModeEnum.INPATIENT,
            Collections.emptyList(),
            "3",
            "1",
            null,
            null,
            null,
            null,
            new Locale("en"));
    assertEquals(0, (long)templatesDto.getUserTemplates().size());
    assertEquals(0L, (long)templatesDto.getOrganizationTemplates().size());
    assertEquals(1L, (long)templatesDto.getPatientTemplates().size());

    final TherapyTemplateDto patientTemplate = templatesDto.getPatientTemplates().get(0);
    assertEquals(TherapyTemplateTypeEnum.PATIENT, patientTemplate.getType());
    assertEquals("P1", patientTemplate.getName());
    assertNull(patientTemplate.getUserId());
    assertEquals("3", patientTemplate.getPatientId());
    assertEquals(1L, (long)patientTemplate.getTemplateElements().size());
    assertSame(TherapyTemplateStatus.COMPLETE, patientTemplate.getTemplateElements().get(0).getStatus());
  }


  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetTherapyTemplates.xml")
  public void testGetAllTherapyTemplates()
  {
    final TherapyTemplatesDto templatesDto = therapyTemplateDao.getAllTherapyTemplates(
        TherapyTemplateModeEnum.INPATIENT,
        "2",
        "1",
        new Locale("en"));

    assertEquals(2L, (long)templatesDto.getUserTemplates().size());

    final TherapyTemplateDto firstUserTemplate = templatesDto.getUserTemplates().get(0);
    assertEquals(TherapyTemplateTypeEnum.USER, firstUserTemplate.getType());
    assertEquals("U1", firstUserTemplate.getName());
    assertEquals("2", firstUserTemplate.getUserId());
    assertNull(firstUserTemplate.getCareProviderId());
    assertEquals(2L, (long)firstUserTemplate.getTemplateElements().size());
    assertSame(TherapyTemplateStatus.COMPLETE, firstUserTemplate.getTemplateElements().get(0).getStatus());
    assertNotSame(TherapyTemplateStatus.COMPLETE, firstUserTemplate.getTemplateElements().get(1).getStatus());

    final TherapyTemplateDto secondUserTemplate = templatesDto.getUserTemplates().get(1);
    assertEquals(TherapyTemplateTypeEnum.USER, secondUserTemplate.getType());
    assertEquals("U2", secondUserTemplate.getName());
    assertEquals("2", secondUserTemplate.getUserId());
    assertNull(secondUserTemplate.getCareProviderId());
    assertEquals(1L, (long)secondUserTemplate.getTemplateElements().size());
    assertSame(TherapyTemplateStatus.COMPLETE, secondUserTemplate.getTemplateElements().get(0).getStatus());

    assertEquals(1L, (long)templatesDto.getOrganizationTemplates().size());
    final TherapyTemplateDto organizationTemplate = templatesDto.getOrganizationTemplates().get(0);
    assertEquals(TherapyTemplateTypeEnum.ORGANIZATIONAL, organizationTemplate.getType());
    assertEquals("O1", organizationTemplate.getName());
    assertNull(organizationTemplate.getUserId());
    assertEquals("1", organizationTemplate.getCareProviderId());
    assertEquals(1L, (long)organizationTemplate.getTemplateElements().size());
    assertSame(TherapyTemplateStatus.COMPLETE, organizationTemplate.getTemplateElements().get(0).getStatus());

    assertEquals(0L, (long)templatesDto.getPatientTemplates().size());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetTherapyTemplates.xml")
  public void testSaveTherapyTemplate()
  {
    final TherapyTemplateDto therapyTemplateDto = new TherapyTemplateDto();
    therapyTemplateDto.setName("Painkillers");
    therapyTemplateDto.setGroup("JUNIOR DOCTOR");
    therapyTemplateDto.setType(TherapyTemplateTypeEnum.CUSTOM_GROUP);

    final TherapyTemplatePreconditionDto preconditionDto = new TherapyTemplatePreconditionDto();
    preconditionDto.setPrecondition(TherapyTemplatePreconditionEnum.WEIGHT);
    preconditionDto.setMinValue(10.0);
    preconditionDto.setMaxValue(40.0);
    therapyTemplateDto.getPreconditions().add(preconditionDto);

    final TherapyTemplateElementDto elementDto = new TherapyTemplateElementDto();
    elementDto.setStatus(TherapyTemplateStatus.COMPLETE);
    elementDto.setRecordAdministration(true);
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    final MedicationDto medication = new MedicationDto();
    medication.setName("Paracetamol 500 mg tablet");
    therapy.setMedication(medication);
    elementDto.setTherapy(therapy);
    therapyTemplateDto.getTemplateElements().add(elementDto);
    
    final long templateId = therapyTemplateDao.saveTherapyTemplate(therapyTemplateDto, TherapyTemplateModeEnum.INPATIENT);

    final TherapyTemplateImpl therapyTemplate =
        sessionFactory.getCurrentSession().load(TherapyTemplateImpl.class, templateId);

    assertEquals("Painkillers", therapyTemplate.getName());
    assertEquals("JUNIOR DOCTOR", therapyTemplate.getTemplateGroup().getName());

    final TherapyTemplatePreconditionImpl precondition = therapyTemplate.getPreconditions().iterator().next();
    assertEquals(TherapyTemplatePreconditionEnum.WEIGHT, precondition.getPrecondition());
    assertEquals(Double.valueOf(10.0), precondition.getMinValue());
    assertEquals(Double.valueOf(40.0), precondition.getMaxValue());
    assertNull(precondition.getExactValue());

    final TherapyTemplateElementImpl element = therapyTemplate.getTherapyTemplateElements().iterator().next();
    assertTrue(element.getTherapy().contains("Paracetamol 500 mg tablet"));
    assertTrue(element.getCompleted());
    assertTrue(element.getRecordAdministration());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetTherapyTemplates.xml")
  public void testGetTherapyTemplateGroups()
  {
    final List<String> therapyTemplateGroups = therapyTemplateDao.getTherapyTemplateGroups(TherapyTemplateModeEnum.INPATIENT);
    assertEquals("JUNIOR DOCTOR", therapyTemplateGroups.get(0));
  }
}
