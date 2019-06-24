package com.marand.thinkmed.medications.connector.impl.rest;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import com.marand.maf.core.JsonUtil;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.connector.MedicationsConnector;
import com.marand.thinkmed.medications.connector.data.object.IntervalDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.medications.connector.data.object.QuantityWithTimeDto;
import com.marand.thinkmed.medications.connector.impl.provider.ehr.EhrLabResultProvider;
import com.marand.thinkmed.request.auth.OAuth2RestTemplateFactory;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Nejc Korasa
 *
 * This class should not exist long. TC team should migrate to fhir integration. If you have to change this class, maybe it's
 * better to pressure TC team a bit into doing their part. ¯\_(ツ)_/¯
 */

public class TcRestMedicationsConnector implements MedicationsConnector
{
  private TcProperties tcProperties;
  private GsonHttpMessageConverter gsonHttpMessageConverter;
  private OAuth2RestTemplateFactory oAuth2RestTemplateFactory;
  private EhrLabResultProvider ehrLabResultProvider;

  private RestTemplate restTemplate;

  @Autowired
  public void setTcProperties(final TcProperties tcProperties)
  {
    this.tcProperties = tcProperties;
  }

  @Autowired
  public void setGsonHttpMessageConverter(final GsonHttpMessageConverter gsonHttpMessageConverter)
  {
    this.gsonHttpMessageConverter = gsonHttpMessageConverter;
  }

  @Autowired
  public void setEhrLabResultProvider(final EhrLabResultProvider ehrLabResultProvider)
  {
    this.ehrLabResultProvider = ehrLabResultProvider;
  }

  @Autowired
  public void setOAuth2RestTemplateFactory(final OAuth2RestTemplateFactory oAuth2RestTemplateFactory)
  {
    this.oAuth2RestTemplateFactory = oAuth2RestTemplateFactory;
  }

  @PostConstruct
  public void init()
  {
    restTemplate = oAuth2RestTemplateFactory.createTokenRelayTemplate(gsonHttpMessageConverter);
  }

  @Override
  public PatientDataForMedicationsDto getPatientData(
      final @NonNull String patientId,
      final String centralCaseId,
      final @NonNull DateTime when)
  {
    final UriComponentsBuilder builder = UriComponentsBuilder
        .fromHttpUrl(tcProperties.getUrl())
        .path("/medications/patients/" + patientId + "/data")
        .queryParam("time", when);

    final URI uri = builder.build().encode().toUri();

    return restTemplate.getForObject(uri, PatientDataForMedicationsDto.class);
  }

  @Override
  public PatientDataForTherapyReportDto getPatientDataForTherapyReport(
      final String patientId,
      final boolean mainDiseaseTypeOnly,
      final DateTime when,
      final Locale locale)
  {
    final URI uri = UriComponentsBuilder.fromHttpUrl(tcProperties.getUrl())
        .path("/medications/patients/" + patientId + "/data/report")
        .queryParam("mainDiseaseTypeOnly", mainDiseaseTypeOnly)
        .queryParam("time", when)
        .queryParam("language", locale.getLanguage())
        .build().encode().toUri();

    return restTemplate.getForObject(uri, PatientDataForTherapyReportDto.class);
  }

  @Override
  public Interval getLastDischargedCentralCaseEffectiveInterval(final String patientId)
  {
    final URI uri = UriComponentsBuilder.fromHttpUrl(tcProperties.getUrl())
        .path("/medications/patients/" + patientId + "/last-discharged-central-case-interval")
        .build().encode().toUri();

    final IntervalDto intervalDto = restTemplate.getForObject(uri, IntervalDto.class);
    return intervalDto != null ? intervalDto.toInterval() : null;
  }

  @Override
  public List<NamedExternalDto> getCurrentUserCareProviders()
  {
    final URI uri = UriComponentsBuilder.fromHttpUrl(tcProperties.getUrl())
        .path("/medications/current-user-care-providers")
        .build().encode().toUri();

    final NamedExternalDto[] careProviders = restTemplate.getForObject(uri, NamedExternalDto[].class);
    return careProviders != null ? Arrays.asList(careProviders) : Collections.emptyList();
  }

  @Override
  public List<PatientDisplayWithLocationDto> getPatientDisplaysWithLocation(
      final Collection<String> careProviderIds,
      final Collection<String> patientIds)
  {
    final UriComponentsBuilder ucb = UriComponentsBuilder
        .fromHttpUrl(tcProperties.getUrl())
        .path("/medications/patient-displays-with-location");

    if (careProviderIds != null)
    {
      ucb.queryParam("careProviderIds", JsonUtil.toJson(careProviderIds));
    }
    if (patientIds != null)
    {
      ucb.queryParam("patientIds", JsonUtil.toJson(patientIds));
    }

    final URI uri = ucb.build().encode().toUri();

    final PatientDisplayWithLocationDto[] locations = restTemplate.getForObject(uri, PatientDisplayWithLocationDto[].class);
    return locations != null ? Arrays.asList(locations) : Collections.emptyList();
  }

  @Override
  public List<QuantityWithTimeDto> getBloodSugarObservations(
      final @NonNull String patientId,
      final @NonNull Interval interval)
  {
    final URI uri = UriComponentsBuilder.fromHttpUrl(tcProperties.getUrl())
        .path("/medications/patients/" + patientId + "/observations/blood-sugar")
        .queryParam("fromTime", interval.getStart())
        .queryParam("toTime", interval.getEnd())
        .build().encode().toUri();

    final QuantityWithTimeDto[] observations = restTemplate.getForObject(uri, QuantityWithTimeDto[].class);
    return observations != null ? Arrays.asList(observations) : Collections.emptyList();
  }

  @Override
  public List<QuantityWithTimeDto> getLabResults(
      final @NonNull String patientId,
      final @NonNull String resultCode,
      final @NonNull Interval interval)
  {
    return ehrLabResultProvider.getLabResults(patientId, resultCode, interval)
        .stream()
        .map(m -> new QuantityWithTimeDto(m.getTimestamp(), m.getValue(), m.getComment()))
        .collect(Collectors.toList());
  }

  @Override
  public List<QuantityWithTimeDto> findMeanArterialPressureMeasurements(
      final @NonNull String patientId,
      final @NonNull Interval interval)
  {
    final URI uri = UriComponentsBuilder.fromHttpUrl(tcProperties.getUrl())
        .path("/medications/patients/" + patientId + "/observations/means-arterial-pressure")
        .queryParam("fromTime", interval.getStart())
        .queryParam("toTime", interval.getEnd())
        .build().encode().toUri();

    final QuantityWithTimeDto[] observations = restTemplate.getForObject(uri, QuantityWithTimeDto[] .class);
    return observations != null ? Arrays.asList(observations) : Collections.emptyList();
  }
}
