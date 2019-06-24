package com.marand.thinkmed.medications.connector.impl.provider.fhir;

import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import ca.uhn.fhir.model.primitive.StringDt;
import com.google.common.collect.Lists;
import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.connector.impl.config.FhirProperties;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Mitja Lapajne
 */

public class FhirUtilsTest
{

  public static FhirProperties buildFhirProperties()
  {
    final FhirProperties fhirProperties = new FhirProperties();
    fhirProperties.setEncounterServerUri("uri1");
    fhirProperties.setPatientServerUri("uri2");
    fhirProperties.setPatientTicketHeaderName("header");
    fhirProperties.setPatientIdSystem("patientIdSystem1");
    fhirProperties.setEncounterIdSystem("encounterIdSystem1");
    fhirProperties.setWardIdSystem("wardIdSystem1");
    fhirProperties.setWardLocationPhysicalType("ward");
    fhirProperties.setLocationLocationPhysicalType("location");
    fhirProperties.setDoctorParticipantType("primaryDoctor");
    return fhirProperties;
  }

  @Test
  public void testGetNameStringValid()
  {
    final HumanNameDt nameDt = getHumanNameDt("Jane", "Smith");
    final Opt<String> nameString = FhirUtils.getNameString(nameDt);
    assertEquals("Jane Smith", nameString.get());
  }

  @Test
  public void testGetNameStringValidList()
  {
    final HumanNameDt nameDt = getHumanNameDt("Jane", "Smith");
    final Opt<String> nameString = FhirUtils.getNameString(Lists.newArrayList(nameDt));
    assertEquals("Jane Smith", nameString.get());
  }

  @Test
  public void testGetNameStringNoName()
  {
    final Opt<String> nameString = FhirUtils.getNameString(Lists.newArrayList());
    assertEquals(Opt.none(), nameString);
  }

  @Test
  public void testGetNameStringGivenOnly()
  {
    final HumanNameDt nameDt = getHumanNameDt("Jane", null);
    final Opt<String> nameString = FhirUtils.getNameString(Lists.newArrayList(nameDt));
    assertEquals("Jane", nameString.get());
  }

  @Test
  public void testGetNameStringFamilyOnly()
  {
    final HumanNameDt nameDt = getHumanNameDt(null, "Smith");
    final Opt<String> nameString = FhirUtils.getNameString(Lists.newArrayList(nameDt));
    assertEquals("Smith", nameString.get());
  }

  @Test
  public void testGetNameStringNoGivenNoFamily()
  {
    final HumanNameDt nameDt = getHumanNameDt(null, null);
    final Opt<String> nameString = FhirUtils.getNameString(Lists.newArrayList(nameDt));
    assertEquals(Opt.none(), nameString);
  }

  private HumanNameDt getHumanNameDt(final String given, final String family)
  {
    final HumanNameDt nameDt = new HumanNameDt();
    if (given != null)
    {
      nameDt.getGiven().add(new StringDt(given));
    }
    if (family != null)
    {
      nameDt.getFamily().add(new StringDt(family));
    }
    return nameDt;
  }
}
