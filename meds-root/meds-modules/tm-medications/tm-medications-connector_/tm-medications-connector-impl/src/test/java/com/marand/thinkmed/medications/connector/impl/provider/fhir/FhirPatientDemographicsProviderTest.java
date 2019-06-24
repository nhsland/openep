package com.marand.thinkmed.medications.connector.impl.provider.fhir;

import java.util.List;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.gclient.IClientExecutable;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.IUntypedQuery;
import com.google.common.collect.Lists;
import com.marand.maf.core.exception.SystemException;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.connector.data.object.PatientDemographicsDto;
import com.marand.thinkmed.medications.connector.impl.config.FhirProperties;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;

/**
 * @author Mitja Lapajne
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class FhirPatientDemographicsProviderTest
{
  @Configuration
  public static class FhirPatientDemographicsProviderContextTest
  {
    @Bean
    public FhirPatientDemographicsProvider fhirPatientDemographicsProvider()
    {
      final FhirPatientDemographicsProvider fhirPatientDemographicsProvider = new FhirPatientDemographicsProvider();
      fhirPatientDemographicsProvider.setFhirClientFactory(fhirClientFactory());
      fhirPatientDemographicsProvider.setFhirProperties(fhirProperties());
      return fhirPatientDemographicsProvider;
    }

    @Bean
    public FhirClientFactory fhirClientFactory()
    {
      return Mockito.mock(FhirClientFactory.class);
    }

    @Bean
    public FhirContext fhirContext()
    {
      return Mockito.mock(FhirContext.class);
    }

    @Bean
    public FhirProperties fhirProperties()
    {
      return FhirUtilsTest.buildFhirProperties();
    }
  }

  @SuppressWarnings("PublicField")
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Autowired
  private FhirPatientDemographicsProvider patientDemographicsProvider;

  @Autowired
  private FhirClientFactory fhirClientFactory;

  @Before
  public void setMockFhirClient()
  {
    patientDemographicsProvider.setFhirClientFactory(fhirClientFactory);
  }

  @Test
  public void testGetPatientsDemographicsAllValues()
  {
    final Bundle result = new Bundle();
    final Bundle.Entry entry1 = getPatientResultBundleEntry(
        "patient1",
        "Patient Name1",
        new DateTime(2016, 3, 3, 0, 0),
        AdministrativeGenderEnum.FEMALE,
        null);
    result.addEntry(entry1);

    final Bundle.Entry entry2 = getPatientResultBundleEntry(
        "patient2",
        "Patient Name2",
        new DateTime(2010, 10, 10, 0, 0),
        AdministrativeGenderEnum.MALE,
        null);
    result.addEntry(entry2);

    mockFhirClientResult(result);

    final List<PatientDemographicsDto> patientsDemographics =
        patientDemographicsProvider.getPatientsDemographics(Lists.newArrayList("patient1", "patient2"));

    assertEquals(2, patientsDemographics.size());

    final PatientDemographicsDto patient1 = patientsDemographics.get(0);
    assertEquals("patient1", patient1.getId());
    assertEquals("Patient Name1", patient1.getName());
    assertEquals(new DateTime(2016, 3, 3, 0, 0), patient1.getBirthDate());
    assertEquals(Gender.FEMALE, patient1.getGender());

    final PatientDemographicsDto patient2 = patientsDemographics.get(1);
    assertEquals("patient2", patient2.getId());
    assertEquals("Patient Name2", patient2.getName());
    assertEquals(new DateTime(2010, 10, 10, 0, 0), patient2.getBirthDate());
    assertEquals(Gender.MALE, patient2.getGender());
  }

  @Test
  public void testGetPatientsDemographicsNoName()
  {
    expectedException.expect(SystemException.class);
    expectedException.expectMessage("FHIR validation failed - No Patient.name.text defined");

    final Bundle result = new Bundle();
    final Bundle.Entry entry1 = getPatientResultBundleEntry(
        "patient1",
        null,
        new DateTime(2016, 3, 3, 0, 0),
        AdministrativeGenderEnum.FEMALE,
        null);
    result.addEntry(entry1);

    mockFhirClientResult(result);

    patientDemographicsProvider.getPatientsDemographics(Lists.newArrayList("patient1"));
  }

  @Test
  public void testGetPatientsDemographicsNoIdentifier()
  {
    expectedException.expect(SystemException.class);
    expectedException.expectMessage(
        "FHIR validation failed - No identifier for Patient with system \"patientIdSystem1\" defined");

    final Bundle result = new Bundle();
    final Bundle.Entry entry1 = getPatientResultBundleEntry(
        null,
        "Patient Name1",
        new DateTime(2016, 3, 3, 0, 0),
        AdministrativeGenderEnum.FEMALE,
        null);
    result.addEntry(entry1);

    mockFhirClientResult(result);

    patientDemographicsProvider.getPatientsDemographics(Lists.newArrayList("patient1"));
  }

  @Test
  public void testGetPatientsDemographicsNoBirthDate()
  {
    expectedException.expect(SystemException.class);
    expectedException.expectMessage("No Patient.birthDate defined");

    final Bundle result = new Bundle();
    final Bundle.Entry entry1 = getPatientResultBundleEntry(
        "patient1",
        "Patient Name1",
        null,
        AdministrativeGenderEnum.FEMALE,
        null);
    result.addEntry(entry1);

    mockFhirClientResult(result);

    patientDemographicsProvider.getPatientsDemographics(Lists.newArrayList("patient1"));
  }

  @Test
  public void testGetPatientsDemographicsNoGender()
  {
    final Bundle result = new Bundle();
    final Bundle.Entry entry1 = getPatientResultBundleEntry(
        "patientId",
        "Patient Name1",
        new DateTime(2016, 3, 3, 0, 0),
        null,
        null);
    result.addEntry(entry1);

    mockFhirClientResult(result);

    final List<PatientDemographicsDto> patient = patientDemographicsProvider.getPatientsDemographics(Lists.newArrayList(
        "patientId"));
    assertEquals(Gender.NOT_KNOWN, patient.get(0).getGender());
  }

  @Test
  public void testGetPatientsDemographicsWithGenderIdentity()
  {
    final Bundle result = new Bundle();
    final Bundle.Entry entry1 = getPatientResultBundleEntry(
        "patientId",
        "Patient Name1",
        new DateTime(2016, 3, 3, 0, 0),
        AdministrativeGenderEnum.MALE,
        AdministrativeGenderEnum.FEMALE);
    result.addEntry(entry1);

    mockFhirClientResult(result);

    final List<PatientDemographicsDto> patient = patientDemographicsProvider.getPatientsDemographics(Lists.newArrayList(
        "patientId"));
    assertEquals(Gender.MALE, patient.get(0).getGender());
    assertEquals(Gender.FEMALE, patient.get(0).getGenderIdentity());
  }

  private Bundle.Entry getPatientResultBundleEntry(
      final String patientId,
      final String patientName,
      final DateTime birthDate,
      final AdministrativeGenderEnum gender,
      final AdministrativeGenderEnum genderIdentity)
  {
    final Bundle.Entry entry = new Bundle.Entry();

    final Patient patient = new Patient();
    entry.setResource(patient);

    if (patientId != null)
    {
      patient.getIdentifier().add(new IdentifierDt("patientIdSystem1", patientId));
    }

    if (patientName != null)
    {
      final HumanNameDt humanName = new HumanNameDt();
      humanName.setText(patientName);
      patient.getName().add(humanName);
    }

    if (birthDate != null)
    {
      patient.setBirthDateWithDayPrecision(birthDate.toDate());
    }

    if (gender != null)
    {
      patient.setGender(gender);
    }

    if (genderIdentity != null)
    {
      final ExtensionDt extension = new ExtensionDt();
      extension.setUrl("http://hl7.org/fhir/StructureDefinition/patient-genderIdentity");
      extension.setValue(new CodeableConceptDt("Gender", genderIdentity.getCode()));
      patient.getUndeclaredExtensions().add(extension);
    }

    return entry;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void mockFhirClientResult(final Bundle result)
  {
    final IGenericClient client = Mockito.mock(IGenericClient.class);
    final IUntypedQuery search = Mockito.mock(IUntypedQuery.class);
    final IQuery forResource = Mockito.mock(IQuery.class);
    final IQuery where = Mockito.mock(IQuery.class);
    final IQuery count = Mockito.mock(IQuery.class);
    final IClientExecutable bundle = Mockito.mock(IClientExecutable.class);

    Mockito.when(fhirClientFactory.create(any())).thenReturn(client);
    Mockito.when(client.search()).thenReturn(search);
    Mockito.when(search.forResource(Patient.class)).thenReturn(forResource);
    Mockito.when(forResource.where(any())).thenReturn(where);
    Mockito.when(where.count(anyInt())).thenReturn(count);
    Mockito.when(count.returnBundle(any())).thenReturn(bundle);
    Mockito.when(bundle.execute()).thenReturn(result);
  }
}
