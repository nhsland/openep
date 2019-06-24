package com.marand.thinkmed.elmdoc.service;

import java.io.ByteArrayInputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.Opt;
import com.marand.maf.core.StackTraceUtils;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.elmdoc.config.ElmdocProperties;
import com.marand.thinkmed.elmdoc.data.AlertDo;
import com.marand.thinkmed.elmdoc.data.AlertsDo;
import com.marand.thinkmed.elmdoc.data.AllergenClassDo;
import com.marand.thinkmed.elmdoc.data.ContraindicationSeverityLevelEnum;
import com.marand.thinkmed.elmdoc.data.DiseaseDo;
import com.marand.thinkmed.elmdoc.data.GenderEnum;
import com.marand.thinkmed.elmdoc.data.InteractionSeverityLevelEnum;
import com.marand.thinkmed.elmdoc.data.PatientDo;
import com.marand.thinkmed.elmdoc.data.ScreenRequestDo;
import com.marand.thinkmed.elmdoc.data.ScreenableDrugDo;
import com.marand.thinkmed.elmdoc.data.ScreeningSummaryDo;
import com.marand.thinkmed.elmdoc.data.ScreeningTypesEnum;
import com.marand.thinkmed.elmdoc.data.SeverityDo;
import com.marand.thinkmed.elmdoc.data.TokenRequestDo;
import com.marand.thinkmed.elmdoc.data.TokenResponseDo;
import com.marand.thinkmed.elmdoc.rest.ElmdocRestService;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import com.marand.thinkmed.medications.service.WarningSeverity;
import com.marand.thinkmed.medications.service.WarningType;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import com.marand.thinkmed.medications.service.dto.WarningScreenMedicationDto;
import com.marand.thinkmed.medications.warnings.WarningsPlugin;
import lombok.NonNull;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;
import org.springframework.xml.transform.StringResult;

/**
 * @author Mitja Lapajne
 */
@Secured("ROLE_User")
@Component
public class ElmdocServiceImpl implements WarningsPlugin, InitializingBean
{
  private static final Logger LOG = LoggerFactory.getLogger(ElmdocServiceImpl.class);

  private ElmdocRestService restService;
  private ElmdocProperties elmdocProperties;
  private Transformer monographTransformer;

  @Autowired
  public void setElmdocProperties(final ElmdocProperties elmdocProperties)
  {
    this.elmdocProperties = elmdocProperties;
  }

  @Override
  public void afterPropertiesSet() throws Exception
  {
    final SSLContext ctx = getSslContext();

    final SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
        ctx,
        SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

    final HttpClientContext context = HttpClientContext.create();
    final ResteasyClient client = new ResteasyClientBuilder().httpEngine(
        new ApacheHttpClient4Engine(
            HttpClientBuilder.create().setSSLSocketFactory(sslConnectionSocketFactory).build(), context))
        .build();
    final ResteasyWebTarget target = client.target(elmdocProperties.getElmdocRestUri());
    restService = target.proxy(ElmdocRestService.class);

    final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    monographTransformer = transformerFactory.newTransformer(new StreamSource(getClass().getResourceAsStream("monograph.xslt")));
  }

  private SSLContext getSslContext() throws NoSuchAlgorithmException, KeyManagementException
  {
    final TrustManager tm = new X509TrustManager()
    {
      @Override
      public void checkClientTrusted(final X509Certificate[] chain, final String authType)
      {
      }

      @Override
      public X509Certificate[] getAcceptedIssuers()
      {
        return null;
      }

      @Override
      public void checkServerTrusted(final X509Certificate[] chain, final String authType)
      {
      }
    };

    final SSLContext ctx = SSLContext.getInstance("TLS");
    ctx.init(null, new TrustManager[]{tm}, null);
    return ctx;
  }

  @Override
  public void reloadCache()
  {
  }

  @Override
  public List<MedicationsWarningDto> findMedicationWarnings(
      @NonNull final DateTime dateOfBirth,
      final Double patientWeightInKg,
      final Double bsaInM2,
      @NonNull final Gender gender,
      @NonNull final List<IdNameDto> diseaseTypeValues,
      @NonNull final List<IdNameDto> allergiesExternalValues,
      @NonNull final List<WarningScreenMedicationDto> medicationSummaries,
      @NonNull final DateTime when)
  {
    final ScreenRequestDo screening = buildScreenRequestDo(
        dateOfBirth,
        patientWeightInKg,
        bsaInM2,
        gender,
        diseaseTypeValues,
        allergiesExternalValues,
        medicationSummaries);

    final String tokenResponseJson = restService.generateToken(JsonUtil.toJson(new TokenRequestDo(elmdocProperties.getUsername(), elmdocProperties.getPassword())));
    final TokenResponseDo tokenResponse = JsonUtil.fromJson(tokenResponseJson, TokenResponseDo.class);
    final String screeningSummary = restService.screening(tokenResponse.getToken(), JsonUtil.toJson(screening));

    final ScreeningSummaryDo screeningSummaryDo = JsonUtil.fromJson(screeningSummary, ScreeningSummaryDo.class);
    return mapMedicationsWarnings(screeningSummaryDo);
  }

  List<MedicationsWarningDto> mapMedicationsWarnings(final ScreeningSummaryDo screeningSummaryDo)
  {
    final List<MedicationsWarningDto> warningsList = new ArrayList<>();

    Opt.of(screeningSummaryDo.getDrugDrugInteractions())
        .ifPresent(a -> mapMedicationsWarnings(a, WarningType.INTERACTION)
            .forEach(warningsList::add));

    Opt.of(screeningSummaryDo.getDrugAlcoholInteractions())
        .ifPresent(a -> mapMedicationsWarnings(a, WarningType.INTERACTION)
            .forEach(warningsList::add));

    Opt.of(screeningSummaryDo.getDrugFoodInteractions())
        .ifPresent(a -> mapMedicationsWarnings(a, WarningType.INTERACTION)
            .forEach(warningsList::add));

    Opt.of(screeningSummaryDo.getAllergicReactions())
        .ifPresent(a -> mapMedicationsWarnings(a, WarningType.ALLERGY)
            .forEach(warningsList::add));

    Opt.of(screeningSummaryDo.getDiseaseContraindications())
        .ifPresent(a -> mapMedicationsWarnings(a, WarningType.PATIENT_CHECK)
            .forEach(warningsList::add));

    Opt.of(screeningSummaryDo.getLactationContraindications())
        .ifPresent(a -> mapMedicationsWarnings(a, WarningType.PATIENT_CHECK)
            .forEach(warningsList::add));

    Opt.of(screeningSummaryDo.getPregnancyContraindications())
        .ifPresent(a -> mapMedicationsWarnings(a, WarningType.PATIENT_CHECK)
            .forEach(warningsList::add));

    Opt.of(screeningSummaryDo.getAgeContraindications())
        .ifPresent(a -> mapMedicationsWarnings(a, WarningType.PATIENT_CHECK)
            .forEach(warningsList::add));

    Opt.of(screeningSummaryDo.getGenderContraindications())
        .ifPresent(a -> mapMedicationsWarnings(a, WarningType.PATIENT_CHECK)
            .forEach(warningsList::add));

    Opt.of(screeningSummaryDo.getDuplicateTherapies())
        .ifPresent(a -> mapMedicationsWarnings(a, WarningType.DUPLICATE)
            .forEach(warningsList::add));

    Opt.of(screeningSummaryDo.getGeneticTests())
        .ifPresent(a -> mapMedicationsWarnings(a, WarningType.INTERACTION)
            .forEach(warningsList::add));

    Opt.of(screeningSummaryDo.getDopingAlerts())
        .ifPresent(a -> mapMedicationsWarnings(a, WarningType.INTERACTION)
            .forEach(w ->
                     {
                       w.setSeverity(null);
                       warningsList.add(w);
                     }));

    return warningsList;
  }

  private Stream<MedicationsWarningDto> mapMedicationsWarnings(final AlertsDo alertsDo, final WarningType type)
  {
    return alertsDo.getItems().stream().map(a -> mapMedicationsWarning(a, type));
  }

  private MedicationsWarningDto mapMedicationsWarning(final AlertDo alertDo, final WarningType type)
  {
    final List<NamedExternalDto> mappedDrugs = alertDo.getDrugs().stream()
        .map(d -> new NamedExternalDto(d.getCustomCode(), d.getCustomName()))
        .collect(Collectors.toList());

    final MedicationsWarningDto warning = new MedicationsWarningDto();
    warning.setDescription(alertDo.getAlert());
    warning.setMonographHtml(monographToHtml(alertDo.getProfessionalMonograph()));
    warning.setType(type);
    warning.setSeverity(mapSeverity(alertDo.getSeverity(), type));
    warning.setMedications(mappedDrugs);

    return warning;
  }

  private WarningSeverity mapSeverity(final SeverityDo severityDo, final WarningType type)
  {
    if (type == WarningType.ALLERGY)
    {
      return WarningSeverity.HIGH_OVERRIDE;
    }

    if (type == WarningType.DUPLICATE)
    {
      return WarningSeverity.HIGH;
    }

    if (severityDo != null)
    {
      if (type == WarningType.PATIENT_CHECK)
      {
        return ContraindicationSeverityLevelEnum.getWarningSeverityByKey(severityDo.getLevel());
      }

      if (type == WarningType.INTERACTION)
      {
        return InteractionSeverityLevelEnum.getWarningSeverityByKey(severityDo.getLevel());
      }
    }

    if (type == WarningType.INTERACTION)
    {
      return WarningSeverity.OTHER;
    }
    throw new IllegalArgumentException("Elmdoc Severity " + type + " not supported.");
  }

  ScreenRequestDo buildScreenRequestDo(
      final DateTime dateOfBirth,
      final Double patientWeightInKg,
      final Double bsaInM2,
      final Gender gender,
      final List<IdNameDto> diseaseTypeValues,
      final List<IdNameDto> allergiesExternalValues,
      final List<WarningScreenMedicationDto> medicationSummaries)
  {
    final ScreenRequestDo screenRequest = new ScreenRequestDo();
    screenRequest.setScreeningTypes(ScreeningTypesEnum.getAllNames());

    final PatientDo patient = new PatientDo();
    patient.setBirthDate(dateOfBirth.toLocalDate());
    patient.setWeight(patientWeightInKg);
    patient.setBodySurfaceArea(bsaInM2);
    if (gender.getIsoCode() == Gender.MALE.getIsoCode())
    {
      patient.setGender(GenderEnum.Male);
    }
    else if (gender.getIsoCode() == Gender.FEMALE.getIsoCode())
    {
      patient.setGender(GenderEnum.Female);
    }
    else if ((gender.getIsoCode() == Gender.INDEFINABLE.getIsoCode()) ||
        (gender.getIsoCode() == Gender.NOT_KNOWN.getIsoCode()))
    {
      patient.setGender(GenderEnum.Unspecified);
    }
    screenRequest.setPatient(patient);

    screenRequest.setDrugs(
        medicationSummaries.stream()
            .map(m -> new ScreenableDrugDo(
                String.valueOf(m.getId()),
                m.getName(),
                m.getExternalId(),
                m.getName(),
                m.isProspective()))
            .collect(Collectors.toList()));

    screenRequest.setAllergies(
        allergiesExternalValues.stream()
            .map(a -> new AllergenClassDo(String.valueOf(a.getId()), a.getName(), false))
            .collect(Collectors.toList()));

    screenRequest.setDiseases(
        diseaseTypeValues.stream()
            .map(a -> new DiseaseDo(String.valueOf(a.getId()), a.getName(), false))
            .collect(Collectors.toList()));

    return screenRequest;
  }

  @SuppressWarnings("OverlyBroadCatchBlock")
  private String monographToHtml(final String xml)
  {
    if (Opt.of(xml).isPresent())
    {
      try
      {
        final StringResult htmlOutput = new StringResult();
        monographTransformer.setParameter("documentuniqueid", UUID.randomUUID().toString());
        monographTransformer.transform(new StreamSource(new ByteArrayInputStream(xml.getBytes())), htmlOutput);
        return htmlOutput.toString();
      }
      catch (final Exception e)
      {
        final String errorMessage = "Error parsing xml from Elmdoc: " + StackTraceUtils.getStackTraceString(e);
        LOG.error(errorMessage);
        return errorMessage;
      }
    }
    return null;
  }

  @Override
  public String getExternalSystemName()
  {
    return "ELMDOC";
  }

  @Override
  public boolean requiresDiseaseCodesTranslation()
  {
    return true;
  }
}
