package com.marand.thinkmed.medications.connector.impl.provider.fhir;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import ca.uhn.fhir.model.base.composite.BaseIdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.AddressDt;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.AddressUseEnum;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.Opt;
import com.marand.maf.core.exception.SystemException;
import com.marand.maf.core.server.util.DefinedLocaleHolder;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.api.externals.data.object.ExternalIdentityDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDemographicsDto;
import com.marand.thinkmed.medications.connector.impl.config.FhirProperties;
import com.marand.thinkmed.medications.connector.impl.provider.PatientDemographicsProvider;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Mitja Lapajne
 */

@SuppressWarnings("OverlyBroadCatchBlock")
public class FhirPatientDemographicsProvider implements PatientDemographicsProvider
{
  private FhirProperties fhirProperties;
  private FhirClientFactory fhirClientFactory;
  private static final String GENDER_IDENTITY_EXTENSION_URL = "http://hl7.org/fhir/StructureDefinition/patient-genderIdentity";

  @Autowired
  public void setFhirClientFactory(final FhirClientFactory fhirClientFactory)
  {
    this.fhirClientFactory = fhirClientFactory;
  }

  @Autowired
  public void setFhirProperties(final FhirProperties fhirProperties)
  {
    this.fhirProperties = fhirProperties;
  }

  @Override
  public List<PatientDemographicsDto> getPatientsDemographics(final @NonNull Collection<String> patientsIds)
  {
    if (patientsIds.isEmpty())
    {
      return Collections.emptyList();
    }
    final List<Patient> patients = queryPatients(patientsIds);
    final List<PatientDemographicsDto> patientDtos = patients.stream()
        .map(this::getPatientDemographicsDto)
        .collect(Collectors.toList());

    final Set<String> returnedPatientIds = patientDtos.stream()
        .map(ExternalIdentityDto::getId)
        .collect(Collectors.toSet());

    final Set<String> missingPatients = new HashSet<>(patientsIds);
    missingPatients.removeAll(returnedPatientIds);
    if (!missingPatients.isEmpty())
    {
      throw new FhirValidationException("fhir.patient.not.found", String.join(", ", missingPatients));
    }

    return patientDtos;
  }

  private List<Patient> queryPatients(final Collection<String> patientsIds)
  {
    final List<BaseIdentifierDt> fhirIdentifiers = patientsIds.stream()
        .map(i -> new IdentifierDt(fhirProperties.getPatientIdSystem(), i))
        .collect(Collectors.toList());

    final Bundle results;
    try
    {
      results = fhirClientFactory.create(fhirProperties.getPatientServerUri())
          .search()
          .forResource(Patient.class)
          .where(Patient.IDENTIFIER.exactly().identifiers(fhirIdentifiers))
          .count(patientsIds.size())
          .returnBundle(Bundle.class)
          .execute();
    }
    catch (final Exception exception)
    {
      //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
      throw new SystemException(
          Dictionary.getEntry(
              "fhir.error.reading.patient.data",
              DefinedLocaleHolder.INSTANCE.getCalculatedNotNullLocale()) + ". "
              + exception.getMessage()
      );
    }

    return results.getEntry().stream()
        .map(e -> (Patient)e.getResource())
        .collect(Collectors.toList());
  }

  private PatientDemographicsDto getPatientDemographicsDto(final Patient patient)
  {
    final String patientName = FhirUtils.getNameString(patient.getName())
        .orElseThrow(() -> new FhirValidationException("fhir.no.patient.name"));

    final String identifierDt = patient.getIdentifier()
        .stream()
        .filter(i -> fhirProperties.getPatientIdSystem().equals(i.getSystem()))
        .map(IdentifierDt::getValue)
        .filter(Objects::nonNull)
        .findFirst()
        .orElseThrow(() -> new FhirValidationException("fhir.no.identifier", "Patient", fhirProperties.getPatientIdSystem()));
    return new PatientDemographicsDto(
        identifierDt,
        patientName,
        getBirthDate(patient),
        getGender(patient),
        getGenderIdentity(patient),
        getAddressDisplay(patient),
        fhirProperties.getPatientIdSystem());
  }

  private DateTime getBirthDate(final Patient patient)
  {
    return Opt.of(patient.getBirthDate())
        .map(DateTime::new)
        .orElseThrow(() -> new FhirValidationException("fhir.no.patient.birth.date"));
  }

  private Gender getGender(final Patient patient)
  {
    return Opt.of(patient.getGender())
        .map(g -> mapGender(AdministrativeGenderEnum.forCode(g)))
        .orElse(Gender.NOT_KNOWN);
  }

  private Gender getGenderIdentity(final Patient patient)
  {
    return patient.getUndeclaredExtensions().stream()
        .filter(e -> e.getUrl().equals(GENDER_IDENTITY_EXTENSION_URL))
        .filter(e -> e.getValue() instanceof CodeableConceptDt)
        .map(e -> ((CodeableConceptDt)e.getValue()).getCodingFirstRep().getCode())
        .map(g -> mapGender(AdministrativeGenderEnum.forCode(g)))
        .findFirst()
        .orElse(null);
  }

  private String getAddressDisplay(final Patient patient)
  {
    final List<AddressDt> addresses = patient.getAddress();
    if (addresses.isEmpty())
    {
      return null;
    }
    if (addresses.size() == 1)
    {
      return getAddressDisplay(addresses.get(0));
    }
    final AddressDt mainAddress = addresses.stream()
        .filter(a -> AddressUseEnum.forCode(a.getUse()) == AddressUseEnum.HOME)
        .findFirst()
        .orElse(addresses.get(0));
    return getAddressDisplay(mainAddress);
  }

  private String getAddressDisplay(final AddressDt address)
  {
    final StringJoiner stringJoiner = new StringJoiner(", ");

    address.getLine()
        .forEach(l -> stringJoiner.add(l.getValue()));

    if (address.getCity() != null)
    {
      stringJoiner.add(address.getCity());
    }

    if (address.getPostalCode() != null)
    {
      stringJoiner.add(address.getPostalCode());
    }

    return stringJoiner.toString();
  }

  private Gender mapGender(final AdministrativeGenderEnum gender)
  {
    if (gender == AdministrativeGenderEnum.MALE)
    {
      return Gender.MALE;
    }
    if (gender == AdministrativeGenderEnum.FEMALE)
    {
      return Gender.FEMALE;
    }
    if (gender == AdministrativeGenderEnum.OTHER)
    {
      return Gender.INDEFINABLE;
    }
    return Gender.NOT_KNOWN;
  }
}
