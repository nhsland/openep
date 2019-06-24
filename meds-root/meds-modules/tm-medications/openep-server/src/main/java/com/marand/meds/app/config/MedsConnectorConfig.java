package com.marand.meds.app.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.ServerValidationModeEnum;
import com.marand.meds.events.EhrEventsController;
import com.marand.thinkmed.medications.connector.impl.config.FhirProperties;
import com.marand.thinkmed.medications.connector.impl.demo.DemoMedicationsConnector;
import com.marand.thinkmed.medications.connector.impl.provider.ProviderBasedMedicationsConnector;
import com.marand.thinkmed.medications.connector.impl.provider.fhir.FhirClientFactory;
import com.marand.thinkmed.medications.connector.impl.provider.fhir.FhirEncounterProvider;
import com.marand.thinkmed.medications.connector.impl.provider.fhir.FhirPatientDemographicsProvider;
import com.marand.thinkmed.medications.connector.impl.rest.TcRestMedicationsConnector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Nejc Korasa
 */

@Configuration
public class MedsConnectorConfig
{
  @Configuration
  @Profile("demo-int")
  @Import({DemoMedicationsConnector.class})
  public static class DemoInt { }

  @Configuration
  @Profile("provider-int")
  @Import({
      // EHR events
      EhrEventsController.class,

      // fhir classes
      FhirEncounterProvider.class,
      FhirPatientDemographicsProvider.class,
      FhirClientFactory.class,

      ProviderBasedMedicationsConnector.class})
  public static class ProviderInt
  {
    public ProviderInt(final FhirProperties fhirProperties)
    {
      checkNotNull(
          fhirProperties.getPatientServerUri(),
          "Fhir patient server uri must be set when provider-int profile is active!");
    }

    @Bean
    public FhirContext fhirContext()
    {
      final FhirContext fhirContext = FhirContext.forDstu2();
      fhirContext.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
      return fhirContext;
    }
  }

  @Configuration
  @Profile("tc-int")
  @Import({TcRestMedicationsConnector.class})
  public static class TCInt { }
}
