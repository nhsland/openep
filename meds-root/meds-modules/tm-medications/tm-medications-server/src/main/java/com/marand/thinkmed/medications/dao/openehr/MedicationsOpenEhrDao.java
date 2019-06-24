package com.marand.thinkmed.medications.dao.openehr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.maf.core.openehr.dao.OpenEhrDaoSupport;
import com.marand.maf.core.resultrow.ResultRowProcessor;
import com.marand.maf.core.server.util.DefinedLocaleHolder;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.rm.RmObject;
import com.marand.openehr.util.DataValueUtils;
import com.marand.openehr.util.OpenEhrRefUtils;
import com.marand.thinkehr.builder.BuilderContext;
import com.marand.thinkehr.mapping.EhrMapper;
import com.marand.thinkehr.query.builder.EhrResult;
import com.marand.thinkehr.query.builder.EhrResultRow;
import com.marand.thinkehr.query.builder.QueryBuilder;
import com.marand.thinkehr.query.service.QueryService;
import com.marand.thinkehr.session.EhrSessionManager;
import com.marand.thinkehr.session.EhrSessioned;
import com.marand.thinkehr.web.WebTemplate;
import com.marand.thinkehr.web.build.input.CodedValueWithDescription;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.api.internal.dto.SelfAdministeringActionEnum;
import com.marand.thinkmed.medications.charting.NormalInfusionAutomaticChartingDto;
import com.marand.thinkmed.medications.charting.SelfAdminAutomaticChartingDto;
import com.marand.thinkmed.medications.charting.TherapyAutomaticChartingDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.MedicationAdministration;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.model.OutpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.SafetyOverride;
import com.marand.thinkmed.medications.ehr.model.composition.EhrComposition;
import com.marand.thinkmed.medications.ehr.model.consentform.MedicationConsentForm;
import com.marand.thinkmed.medications.ehr.model.pharmacist.PharmacyReviewReport;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.openehr.jaxb.rm.Composition;
import org.openehr.jaxb.rm.DvDateTime;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.LocatableRef;
import org.openehr.jaxb.rm.ObjectVersionId;
import org.springframework.stereotype.Component;

/**
 * @author Bostjan Vester
 */

@SuppressWarnings({"rawtypes", "unchecked"})
@Component
public class MedicationsOpenEhrDao extends OpenEhrDaoSupport<String>
{
  private final EhrMappersHolder ehrMappersHolder;
  private final EhrSessionManager ehrSessionManager;
  private final QueryService ehrQueryService;

  public MedicationsOpenEhrDao(
      final EhrMappersHolder ehrMappersHolder,
      final EhrSessionManager ehrSessionManager,
      final QueryService ehrQueryService)
  {
    this.ehrMappersHolder = ehrMappersHolder;
    this.ehrSessionManager = ehrSessionManager;
    this.ehrQueryService = ehrQueryService;
  }

  private EhrMapper getEhrMapper(final Class<?> objectClass)
  {
    return ehrMappersHolder.getEhrMapper(objectClass);
  }

  public List<InpatientPrescription> findInpatientPrescriptions(
      final @NonNull String patientId,
      final @NonNull Interval searchInterval)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      sb
          .append("SELECT c FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.prescription.v0]")
          .append(" CONTAINS Instruction i[openEHR-EHR-INSTRUCTION.medication_order.v2]")
          .append(" WHERE c/name/value = 'Inpatient Prescription'")
          .append(buildTherapyIntervalCriterion(searchInterval));

      return queryEhrContent(
          sb.toString(),
          (resultRow, hasNext) -> (InpatientPrescription)getEhrMapper(InpatientPrescription.class).fromRM((RmObject)resultRow[0])
      );
    }
    return Lists.newArrayList();
  }

  private String buildTherapyIntervalCriterion(final Interval searchInterval)
  {
    //(medication_start <= start && (medication_end > start || medication_end == null)) || (medication_start >= start && medication_start < end)
    final StringBuilder sb = new StringBuilder();
    sb
        .append(" AND (")
        .append(" i/activities[at0001]/description[at0002]/items[at0113]/items[at0012]/value <= ")
        .append(getAqlDateTimeQuoted(searchInterval.getStart()))
        .append(" AND (i/activities[at0001]/description[at0002]/items[at0113]/items[at0013]/value > ")
        .append(getAqlDateTimeQuoted(searchInterval.getStart()))
        .append(" OR NOT EXISTS i/activities[at0001]/description[at0002]/items[at0113]/items[at0013]/value)")
        .append(" OR (i/activities[at0001]/description[at0002]/items[at0113]/items[at0012]/value >= ")
        .append(getAqlDateTimeQuoted(searchInterval.getStart()))
        .append(" AND i/activities[at0001]/description[at0002]/items[at0113]/items[at0012]/value < ")
        .append(getAqlDateTimeQuoted(searchInterval.getEnd()))
        .append(')')
        .append(')');

    return sb.toString();
  }

  public InpatientPrescription saveNewInpatientPrescription(
      final @NonNull String patientId,
      final @NonNull InpatientPrescription prescription)
  {
    final WebTemplate webTemplate = getWebTemplate(prescription.getTemplateId());
    final EhrMapper ehrMapper = getEhrMapper(InpatientPrescription.class);

    final Composition composition = ehrMapper.toRM(
        prescription,
        webTemplate,
        createBuilderContext());

    final String ehrId = getOrCreatePatientEhrId(patientId);
    currentSession().useEhr(ehrId);
    final String uidTemp = currentSession().createComposition(composition);

    linkActionsToInstruction(prescription, uidTemp);

    final Composition compositionWithUpdatedActions = ehrMapper.toRM(
        prescription,
        webTemplate,
        createBuilderContext());

    final String uidFinal = currentSession().modifyComposition(uidTemp, compositionWithUpdatedActions);
    prescription.setUid(uidFinal);

    return prescription;
  }

  private void linkActionsToInstruction(final InpatientPrescription inpatientPrescription, final String compositionUid)
  {
    for (final MedicationManagement action : inpatientPrescription.getActions())
    {
      final LocatableRef actionInstructionId = action.getInstructionDetails().getInstructionId();
      if (actionInstructionId.getId() == null || "/".equals(actionInstructionId.getId().getValue()))
      {
        final ObjectVersionId objectVersionId = new ObjectVersionId();
        objectVersionId.setValue(compositionUid);
        actionInstructionId.setId(objectVersionId);
      }
    }
  }

  public String modifyComposition(
      final @NonNull String patientId,
      final @NonNull EhrComposition composition)
  {
    return saveComposition(patientId, composition, composition.getUid());
  }

  public String saveComposition(
      final @NonNull String patientId,
      final @NonNull EhrComposition composition,
      final String uid)
  {
    final WebTemplate webTemplate = getWebTemplate(composition.getTemplateId());
    final Composition rmComposition = getEhrMapper(composition.getClass()).toRM(
        composition,
        webTemplate,
        createBuilderContext());

    if (uid != null)
    {
      return updateSubjectRMComposition(patientId, rmComposition, uid);
    }
    return saveSubjectRMComposition(patientId, rmComposition, null);
  }

  public String saveMedicationAdministration(
      final @NonNull String patientId,
      final @NonNull EhrComposition composition,
      final String uid)
  {
    return saveComposition(
        patientId,
        composition,
        uid != null ? getLatestCompositionUid(patientId, uid) : null);
  }

  private BuilderContext createBuilderContext()
  {
    final Locale locale = DefinedLocaleHolder.INSTANCE.getCalculatedNotNullLocale();
    final BuilderContext context = new BuilderContext();
    context.setLanguage(locale.getLanguage());
    context.setTerritory(locale.getCountry());
    context.setIdScheme("scheme");
    context.setIdNamespace("ns");
    context.setIdentifierIssuer("issuer");
    context.setIdentifierAssigner("assigner");
    context.setIdentifierType("id");
    return context;
  }

  public void deleteComposition(final String patientId, final String compositionUid)
  {
    deleteSubjectComposition(patientId, compositionUid);
  }

  public void deleteTherapyAdministration(
      final String patientId,
      final String administrationCompositionUid,
      final String comment)
  {
    deleteSubjectComposition(patientId, administrationCompositionUid, comment);
  }

  public void appendWarningsToTherapy(
      final @NonNull String patientId,
      final @NonNull String therapyId,
      final @NonNull Collection<String> warnings)
  {
    final String compositionUid = TherapyIdUtils.extractCompositionUid(therapyId);
    final InpatientPrescription prescription = loadInpatientPrescription(patientId, compositionUid);
    final MedicationOrder order = prescription.getMedicationOrder();

    warnings.forEach(warning -> {
      final SafetyOverride so = new SafetyOverride();
      so.setOverridenSafetyAdvice(DataValueUtils.getText(warning));
      order.getMedicationSafety().getSafetyOverrides().add(so);
    });

    saveComposition(patientId, prescription, prescription.getUid());
  }

  public List<MedicationAdministration> getMedicationAdministrations(
      final @NonNull String patientId,
      final @NonNull Collection<String> therapyCompositionUids,
      final Interval searchInterval,
      final boolean clinicalIntervention)
  {
    if (!therapyCompositionUids.isEmpty())
    {
      final String ehrId = currentSession().findEhr(patientId);
      if (!StringUtils.isEmpty(ehrId))
      {
        currentSession().useEhr(ehrId);

        final List<MedicationAdministration> administrationsFromMapper = new ArrayList<>();

        administrationsFromMapper.addAll(findAdministrations(
            ehrId,
            therapyCompositionUids,
            searchInterval,
            false));

        if (clinicalIntervention)
        {
          administrationsFromMapper.addAll(findAdministrations(ehrId, therapyCompositionUids, searchInterval, true));
        }

        return administrationsFromMapper.stream()
            .sorted(Comparator.comparing(MedicationAdministration::getUid))
            .collect(Collectors.toList());
      }
    }
    return Collections.emptyList();
  }

  private List<MedicationAdministration> findAdministrations(
      final @NonNull String ehrId,
      final @NonNull Collection<String> therapyCompositionUids,
      final Interval searchInterval,
      final boolean clinicalInterventions)
  {
    final StringBuilder sb = new StringBuilder();
    sb
        .append("SELECT c")
        .append(" FROM EHR[ehr_id/value='").append(ehrId).append("']")
        .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]");
    if (clinicalInterventions)
    {
      sb.append(" CONTAINS Action a[openEHR-EHR-ACTION.procedure.v1]");
    }
    else
    {
      sb.append(" CONTAINS Action a[openEHR-EHR-ACTION.medication.v1]");
    }
    sb.append(" WHERE c/name/value = 'Medication Administration'")
        .append(" AND a/instruction_details/instruction_id/id/value matches {" + getAqlQuoted(therapyCompositionUids) + '}');
    if (searchInterval != null)
    {
      sb.append(" AND a/time >= ").append(getAqlDateTimeQuoted(searchInterval.getStart()))
          .append(" AND a/time <= ").append(getAqlDateTimeQuoted(searchInterval.getEnd()));
    }
    sb.append(" FETCH 100000");

    final EhrMapper<MedicationAdministration> ehrMapper = getEhrMapper(MedicationAdministration.class);
    return queryEhrContent(sb.toString(), (resultRow, hasNext) -> ehrMapper.fromRM((RmObject)resultRow[0]));
  }

  public Opt<DateTime> getAdministrationTime(final @NonNull String patientId, final @NonNull String compositionUid)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final String aql = "SELECT " +
          "a/time \n" + // action
          "FROM EHR[ehr_id/value='" + ehrId + "'] \n" +
          "CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]\n" +
          "CONTAINS Action a[openEHR-EHR-ACTION.medication.v1]\n" +
          "WHERE c/name/value = 'Medication Administration'\n" +
          "AND c/uid/value = '" + compositionUid + "'";

      return Opt.of(
          queryEhrContent(aql, (resultRow, hasNext) -> (DvDateTime)resultRow[0])
              .stream()
              .filter(Objects::nonNull)
              .findAny()
              .map(DataValueUtils::getDateTime)
              .orElse(null));
    }
    return Opt.none();
  }

  @EhrSessioned
  public InpatientPrescription loadInpatientPrescription(
      final @NonNull String patientId,
      final @NonNull String compositionUid)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final Composition composition = currentSession().getComposition(compositionUid);
      if (composition == null)
      {
        throw new CompositionNotFoundException(compositionUid);
      }
      return (InpatientPrescription)getEhrMapper(InpatientPrescription.class).fromRM(composition);
    }
    throw new CompositionNotFoundException(compositionUid);
  }

  public List<Interval> getPatientBaselineInfusionIntervals(final String patientId, final Interval searchInterval)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      sb
          .append(
              "SELECT i/activities[at0001]/description[at0002]/items[at0113]/items[at0012]/value, " +
                  "i/activities[at0001]/description[at0002]/items[at0113]/items[at0013]/value"
          )
          .append(" FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.prescription.v0]")
          .append(" CONTAINS Instruction i[openEHR-EHR-INSTRUCTION.medication_order.v2]")
          .append(" CONTAINS Cluster ad[openEHR-EHR-CLUSTER.Medication_additional_details.v0]")
          .append(" WHERE c/name/value = 'Inpatient Prescription'")
          .append(" AND ad/items[at0007]/value = true") //baseline infusion
          .append(buildTherapyIntervalCriterion(searchInterval));

      return queryEhrContent(
          sb.toString(),
          (resultRow, hasNext) -> {
            final DvDateTime dvTherapyStart = (DvDateTime)resultRow[0];
            final DvDateTime dvTherapyEnd = (DvDateTime)resultRow[1];
            if (dvTherapyEnd != null)
            {
              return new Interval(DataValueUtils.getDateTime(dvTherapyStart), DataValueUtils.getDateTime(dvTherapyEnd));
            }
            return Intervals.infiniteFrom(DataValueUtils.getDateTime(dvTherapyStart));
          }
      );
    }
    return new ArrayList<>();
  }

  public DateTime getInpatientPrescriptionStart(final @NonNull String patientId, final @NonNull String compositionUid)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      sb
          .append(" SELECT i/activities[at0001]/description[at0002]/items[at0113]/items[at0012]/value")
          .append(" FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.prescription.v0]")
          .append(" CONTAINS Instruction i[openEHR-EHR-INSTRUCTION.medication_order.v2]")
          .append(" WHERE c/name/value = 'Inpatient Prescription'")
          .append(" AND c/uid/value like '").append(compositionUid).append("*'");

      return queryEhrContent(sb.toString(), (resultRow, hasNext) -> DataValueUtils.getDateTime((DvDateTime)resultRow[0]))
          .stream()
          .filter(Objects::nonNull)
          .findFirst()
          .orElseThrow(() -> new CompositionNotFoundException(compositionUid));
    }
    throw new CompositionNotFoundException(compositionUid);
  }

  /**
   * Can be used for scenarios where administration was planned in the future and administered in the past. If an action
   * (like reissue or modify) happened in between it can cause issues like duplicated tasks
   *
   * @param patientId patient identifier
   * @param compositionUid composition uid
   *
   * @return Planned time of the confirmed administration, that was planned in the future but was confirmed in the past
   */
  public DateTime getFutureAdministrationPlannedTime(
      final @NonNull String patientId,
      final @NonNull String compositionUid,
      final @NonNull DateTime when)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      final String compositionUidWithoutVersion = TherapyIdUtils.getCompositionUidWithoutVersion(compositionUid);
      sb
          .append(" SELECT a/description[at0017]/items[at0043]/value")
          .append(" FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]")
          .append(" CONTAINS Action a[openEHR-EHR-ACTION.medication.v1]")
          .append(" WHERE c/name/value = 'Medication Administration'")
          .append(" AND a/instruction_details/instruction_id/id/value = '" + compositionUidWithoutVersion + "'")
          .append(" AND a/description[at0017]/items[at0043]/value > " + getAqlDateTimeQuoted(when))
          .append(" ORDER BY a/description[at0017]/items[at0043]/value desc");

      return queryEhrContent(sb.toString(), (resultRow, hasNext) -> DataValueUtils.getDateTime((DvDateTime)resultRow[0]))
          .stream()
          .filter(Objects::nonNull)
          .findFirst()
          .orElse(null);
    }
    throw new CompositionNotFoundException(compositionUid);
  }

  public Pair<Double, DateTime> getPatientLastReferenceWeightAndDate(final String patientId, final Interval searchInterval)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      sb
          .append("SELECT o/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value,")
          .append(" o/data[at0002]/events[at0003]/time")
          .append(" FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Observation o[openEHR-EHR-OBSERVATION.body_weight.v1]")
          .append(" WHERE o/name/value = 'Medication reference body weight'")
          .append(" AND o/data[at0002]/events[at0003]/time >= ")
          .append(getAqlDateTimeQuoted(searchInterval.getStart()))
          .append(" AND o/data[at0002]/events[at0003]/time <= ")
          .append(getAqlDateTimeQuoted(searchInterval.getEnd()))
          .append(" ORDER BY o/data[at0002]/events[at0003]/time DESC")
          .append(" FETCH 1");

      final List<Pair<Double, DateTime>> values = query(
          sb.toString(), (resultRow, hasNext) -> {
            final DvQuantity weight = (DvQuantity)resultRow[0];
            final DvDateTime referenceWeightDate = (DvDateTime)resultRow[1];
            return Pair.of(weight.getMagnitude(), DataValueUtils.getDateTime(referenceWeightDate));
          }
      );
      return values.isEmpty() ? null : values.get(0);
    }
    return null;
  }

  public Double getPatientLastReferenceWeight(final String patientId, final Interval searchInterval)
  {
    final Pair<Double, DateTime> referenceWeightAndDate = getPatientLastReferenceWeightAndDate(patientId, searchInterval);
    if (referenceWeightAndDate != null)
    {
      return referenceWeightAndDate.getFirst();
    }
    return null;
  }

  private String getLatestCompositionUid(final String patientId, final String compositionUid)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final String uidWithoutVersion = TherapyIdUtils.getCompositionUidWithoutVersion(compositionUid);

      final StringBuilder sb = new StringBuilder();
      sb
          .append("SELECT v/uid/value")
          .append(" FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS VERSIONED_OBJECT vo")
          .append(" CONTAINS VERSION v[all_versions]")
          .append(" WHERE vo/uid/value = '" + uidWithoutVersion + "'");

      final List<Long> versions = query(
          sb.toString(), (resultRow, hasNext) -> TherapyIdUtils.getCompositionVersion((String)resultRow[0])
      );
      if (!versions.isEmpty())
      {
        Collections.sort(versions);
        final Long latestVersion = versions.get(versions.size() - 1);
        return TherapyIdUtils.buildCompositionUid(compositionUid, latestVersion);
      }
    }
    return null;
  }

  @EhrSessioned
  public List<InpatientPrescription> getLinkedPrescriptions(
      final @NonNull String patientId,
      final @NonNull String compositionUid,
      final @NonNull EhrLinkType linkType)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      final String compositionUidWithoutVersion = TherapyIdUtils.getCompositionUidWithoutVersion(compositionUid);

      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      sb
          .append("SELECT c FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.prescription.v0]")
          .append(" WHERE c/name/value = 'Inpatient Prescription'")
          .append(" AND c/links/target/value like '*" + compositionUidWithoutVersion + "*'")
          .append(" AND c/links/type/value = '" + linkType.getName() + "'");

      return queryEhrContent(
          sb.toString(),
          (resultRow, hasNext) -> (InpatientPrescription)getEhrMapper(InpatientPrescription.class).fromRM((RmObject)resultRow[0])
      );
    }

    return null;
  }

  public Map<String, List<NamedExternalDto>> getTemplateTerms(
      final String templateId, final Set<String> pathIds, final Locale locale)
  {
    final Map<String, List<NamedExternalDto>> localisationMap = new HashMap<>();

    final WebTemplate webTemplate = getWebTemplate(templateId);

    for (final String id : pathIds)
    {
      final List<CodedValueWithDescription> codesWithDescriptions =
          webTemplate.getCodesWithDescriptions(id, locale.getLanguage());
      final List<NamedExternalDto> namedIdentities = new ArrayList<>();
      for (final CodedValueWithDescription codeWithDescription : codesWithDescriptions)
      {
        namedIdentities.add(new NamedExternalDto(codeWithDescription.getValue(), codeWithDescription.getDescription()));
      }

      localisationMap.put(id, namedIdentities);
    }

    return localisationMap;
  }

  public List<PharmacyReviewReport> findPharmacistsReviewReports(final String patientId, final DateTime from)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      sb
          .append("SELECT c FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.report.v1]")
          .append(" WHERE c/name/value = 'Pharmacy Review Report'")
          .append(" AND c/context/start_time > " + getAqlDateTimeQuoted(from))
          .append(" ORDER BY c/context/start_time DESC");

      return queryEhrContent(
          sb.toString(),
          (resultRow, hasNext) -> (PharmacyReviewReport)getEhrMapper(PharmacyReviewReport.class).fromRM((RmObject)resultRow[0]));
    }
    return Lists.newArrayList();
  }

  public PharmacyReviewReport loadPharmacistsReviewReport(final String patientId, final String compositionUid)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final Composition composition = currentSession().getComposition(compositionUid);
      return (PharmacyReviewReport)getEhrMapper(PharmacyReviewReport.class).fromRM(composition);
    }
    return null;
  }

  public MedicationAdministration loadMedicationAdministration(final String patientId, final String compositionUid)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final Composition composition = currentSession().getComposition(compositionUid);
      return (MedicationAdministration)getEhrMapper(MedicationAdministration.class).fromRM(composition);
    }
    return null;
  }

  public Opt<MedicationConsentForm> findLatestConsentForm(final @NonNull String patientId)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);

      final String consentFormQuery = getConsentFormQuery(ehrId, null, 1);

      final List<MedicationConsentForm> result = query(
          consentFormQuery,
          (resultRow, hasNext) ->
              (MedicationConsentForm)getEhrMapper(MedicationConsentForm.class).fromRM((RmObject)resultRow[0])
      );

      return result.isEmpty() ? Opt.none() : Opt.of(result.get(0));
    }

    return Opt.none();
  }

  public Collection<MedicationConsentForm> findMedicationConsentFormCompositions(
      final @NonNull String patientId,
      final Interval interval,
      final Integer fetchCount)
  {


    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      return query(
          getConsentFormQuery(ehrId, interval, fetchCount),
          (resultRow, hasNext) ->
              (MedicationConsentForm)getEhrMapper(MedicationConsentForm.class).fromRM((RmObject)resultRow[0])
      );
    }

    return Collections.emptyList();
  }

  private String getConsentFormQuery(final String ehrId, final Interval interval, final Integer fetchCount)
  {
    final StringBuilder query = new StringBuilder()
        .append("SELECT c FROM EHR[ehr_id/value='").append(ehrId).append("']")
        .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]")
        .append(" WHERE c/name/value = 'Consent Form'");

    if (interval != null)
    {
      query
          .append(" AND c/context/start_time > " + getAqlDateTimeQuoted(interval.getStart()))
          .append(" AND c/context/start_time < " + getAqlDateTimeQuoted(interval.getEnd()));
    }

    query.append(" ORDER BY c/context/start_time DESC");

    if (fetchCount != null)
    {
      query.append(" FETCH " + fetchCount);
    }

    return query.toString();
  }

  public MedicationConsentForm loadConsentFormComposition(
      final @NonNull String patientId,
      final @NonNull String compositionUid)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final Composition composition = currentSession().getComposition(compositionUid);
      if (composition == null)
      {
        throw new CompositionNotFoundException(compositionUid);
      }
      return (MedicationConsentForm)getEhrMapper(MedicationConsentForm.class).fromRM(composition);
    }
    throw new CompositionNotFoundException(compositionUid);
  }

  public OutpatientPrescription loadOutpatientPrescription(
      final String patientId,
      final String compositionUid)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final Composition composition = currentSession().getComposition(compositionUid);
      return (OutpatientPrescription)getEhrMapper(OutpatientPrescription.class).fromRM(composition);
    }
    return null;
  }

  public List<InpatientPrescription> loadInpatientPrescriptions(final Set<String> compositionUids)
  {
    if (!compositionUids.isEmpty())
    {
      final StringBuilder aql = new StringBuilder();
      aql
          .append("SELECT c FROM EHR e")
          .append(" CONTAINS VERSIONED_OBJECT vo")
          .append(" CONTAINS VERSION v")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.prescription.v0]")
          .append(" WHERE c/name/value = 'Inpatient Prescription'")
          .append(" AND vo/uid/value matches {'")
          .append(StringUtils.join(compositionUids, "', '"))
          .append("'}");

      return query(
          aql.toString(),
          (resultRow, hasNext) -> (InpatientPrescription)getEhrMapper(InpatientPrescription.class).fromRM((RmObject)resultRow[0])
      );
    }
    return Collections.emptyList();
  }

  public List<InpatientPrescription> getAllInpatientPrescriptionVersions(final @NonNull String compositionUid)
  {
    final List<Composition> compositions =
        currentSession().getAllCompositionVersions(TherapyIdUtils.getCompositionUidWithoutVersion(compositionUid));
    Collections.reverse(compositions);
    return compositions.stream()
        .map(c -> (InpatientPrescription)getEhrMapper(InpatientPrescription.class).fromRM(c))
        .collect(Collectors.toList());
  }

  public Map<String, InpatientPrescription> getLatestCompositionsForOriginalCompositionUids(
      final Set<String> originalCompositionUids,
      final Set<String> patientIds,
      final int searchIntervalInWeeks,
      final DateTime when)
  {
    final Map<String, InpatientPrescription> originalUidWithLatestCompositionMap = new HashMap<>();

    if (originalCompositionUids.isEmpty() || patientIds.isEmpty())
    {
      return originalUidWithLatestCompositionMap;
    }

    final Map<String, String> ehrIdsMap = getEhrIds(patientIds);
    final Set<String> ehrIds = new HashSet<>(ehrIdsMap.values());

    if (ehrIds.isEmpty())
    {
      return originalUidWithLatestCompositionMap;
    }

    final StringBuilder sb = new StringBuilder();
    sb
        .append("SELECT c/uid/value, c/links/target/value, c/links/type/value, c FROM EHR e")
        .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.prescription.v0]")
        .append(" CONTAINS Instruction i[openEHR-EHR-INSTRUCTION.medication_order.v2]")
        .append(" WHERE c/name/value = 'Inpatient Prescription'")
        .append(buildTherapyIntervalCriterion(Intervals.infiniteFrom(when.minusWeeks(searchIntervalInWeeks).withTimeAtStartOfDay())))
        .append(" AND e/ehr_id/value matches {" + getAqlQuoted(ehrIds) + '}')
        .append(" ORDER BY c/context/start_time DESC");

    final EhrMapper<InpatientPrescription> ehrMapper = getEhrMapper(InpatientPrescription.class);

    final Function<String, Boolean> shouldAddCompositionToResult = uid ->
        originalCompositionUids.contains(uid) && !originalUidWithLatestCompositionMap.containsKey(uid);

    query(
        sb.toString(),
        (ResultRowProcessor<Object[], Void>)(resultRow, hasNext) -> {
          final String compositionUid = (String)resultRow[0];
          final String linkTarget = (String)resultRow[1];
          final String linkType = (String)resultRow[2];
          final RmObject prescriptionRmObject = (RmObject)resultRow[3];

          final String compositionUidWithoutVersion = TherapyIdUtils.getCompositionUidWithoutVersion(compositionUid);
          if (shouldAddCompositionToResult.apply(compositionUidWithoutVersion))
          {
            originalUidWithLatestCompositionMap.put(compositionUidWithoutVersion, ehrMapper.fromRM(prescriptionRmObject));
          }

          else if (linkTarget != null && linkType.equals(EhrLinkType.ORIGIN.getName()))
          {
            final OpenEhrRefUtils.EhrUriComponents ehrUri = OpenEhrRefUtils.parseEhrUri(linkTarget);
            final String linkedCompositionUidWithoutVersion = TherapyIdUtils.getCompositionUidWithoutVersion(ehrUri.getCompositionId());

            if (shouldAddCompositionToResult.apply(linkedCompositionUidWithoutVersion))
            {
              originalUidWithLatestCompositionMap.put(linkedCompositionUidWithoutVersion, ehrMapper.fromRM(prescriptionRmObject));
            }
          }
          return null;
        }
    );

    return originalUidWithLatestCompositionMap;
  }

  @EhrSessioned
  public Collection<TherapyAutomaticChartingDto> getAutoChartingTherapyDtos(final DateTime when)
  {
    final StringBuilder sb = new StringBuilder();
    sb
        .append("SELECT c/uid/value, " +
                    "i/name/value, " +
                    "e/ehr_status/subject/external_ref/id/value, " +
                    "ad/items[at0019]/value") //self administration start

        .append(" FROM EHR e")
        .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.prescription.v0]")
        .append(" CONTAINS Instruction i[openEHR-EHR-INSTRUCTION.medication_order.v2]")
        .append(" CONTAINS Cluster ad[openEHR-EHR-CLUSTER.Medication_additional_details.v0]")
        .append(" WHERE c/name/value = 'Inpatient Prescription'")
        .append(" AND ad/items[at0005]/value/value = '") //self administration type
        .append(SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED.getEhrValue() + "'")
        .append(buildTherapyIntervalCriterion(Intervals.infiniteFrom(when.minusDays(1).withTimeAtStartOfDay())));

    final List<TherapyAutomaticChartingDto> autoChartingTherapies = new ArrayList<>();
    try (final EhrResult ehrResult = new QueryBuilder(ehrQueryService, ehrSessionManager.getSessionId())
        .poll(100L, 1000L * 30L)
        .withAql(sb.toString())
        .execute())
    {
      while (ehrResult.isActive())
      {
        if (ehrResult.hasNext(100L))
        {
          final EhrResultRow resultRow = ehrResult.next();

          final String compositionUid = (String)resultRow.get(0);
          final String instructionName = (String)resultRow.get(1);
          final String patientId = (String)resultRow.get(2);
          final DvDateTime selfAdministrationStart = (DvDateTime)resultRow.get(3);

          final SelfAdminAutomaticChartingDto dto = new SelfAdminAutomaticChartingDto(
              compositionUid,
              instructionName,
              patientId,
              DataValueUtils.getDateTime(selfAdministrationStart),
              SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED);

          autoChartingTherapies.add(dto);
        }
      }
    }

    final List<TherapyAutomaticChartingDto> autoChartingNormalInfusions = getAutoChartingTherapiesWithRate(when);
    return joinAutoChartingDtos(autoChartingTherapies, autoChartingNormalInfusions);
  }

  private Collection<TherapyAutomaticChartingDto> joinAutoChartingDtos(
      final List<TherapyAutomaticChartingDto> collection1,
      final List<TherapyAutomaticChartingDto> collection2)
  {
    final Set<String> collection1Ids = collection1
        .stream()
        .map(TherapyAutomaticChartingDto::getCompositionUid)
        .collect(Collectors.toSet());

    final List<TherapyAutomaticChartingDto> result = new ArrayList<>(collection1);
    result.addAll(collection2.stream()
                      .filter(a -> !collection1Ids.contains(a.getCompositionUid()))
                      .collect(Collectors.toList()));
    return result;
  }

  private List<TherapyAutomaticChartingDto> getAutoChartingTherapiesWithRate(final DateTime when)
  {
    final StringBuilder sb = new StringBuilder();
    sb
        .append("SELECT " +
                    "c/uid/value, " +
                    "i/name/value, " +
                    "e/ehr_status/subject/external_ref/id/value")

        .append(" FROM EHR e")
        .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.prescription.v0]")
        .append(" CONTAINS Instruction i[openEHR-EHR-INSTRUCTION.medication_order.v2]")
        .append(" CONTAINS Cluster t[openEHR-EHR-CLUSTER.therapeutic_direction.v1]")
        .append(" CONTAINS Cluster d[openEHR-EHR-CLUSTER.dosage.v1]")
        .append(" WHERE c/name/value = 'Inpatient Prescription'")
        .append(" AND EXISTS d/items[at0134]/value") // has rate
        .append(" AND NOT EXISTS i/activities[at0001]/description[at0002]/items[at0094]/value") // delivery method is empty - not continuous infusion or bolus
        .append(buildTherapyIntervalCriterion(Intervals.infiniteFrom(when.minusDays(1).withTimeAtStartOfDay())));

    final List<TherapyAutomaticChartingDto> autoChartingNormalInfusions = new ArrayList<>();
    try (final EhrResult ehrResult = new QueryBuilder(ehrQueryService, ehrSessionManager.getSessionId())
        .poll(100L, 1000L * 30L)
        .withAql(sb.toString())
        .execute())
    {
      while (ehrResult.isActive())
      {
        if (ehrResult.hasNext(100L))
        {
          final EhrResultRow resultRow = ehrResult.next();

          final NormalInfusionAutomaticChartingDto dto = new NormalInfusionAutomaticChartingDto(
              (String)resultRow.get(0),
              (String)resultRow.get(1),
              (String)resultRow.get(2));

          autoChartingNormalInfusions.add(dto);
        }
      }
    }

    return autoChartingNormalInfusions;
  }

  @EhrSessioned
  public Map<InpatientPrescription, String> getActiveInpatientPrescriptionsWithPatientIds(final @NonNull DateTime when)
  {
    final String selectQuery = buildActiveInpatientPrescriptionsWithPatientIdQuery(
        "SELECT c,  e/ehr_status/subject/external_ref/id/value FROM EHR e ",
        when);

    final Map<InpatientPrescription, String> medicationOrderWithPatientId = new HashMap<>();
    try (EhrResult ehrResult = new QueryBuilder(ehrQueryService, ehrSessionManager.getSessionId())
        .poll(100L, 1000L * 30L)
        .withAql(selectQuery)
        .execute())
    {
      while (ehrResult.isActive())
      {
        if (ehrResult.hasNext(100L))
        {
          final EhrResultRow resultRow = ehrResult.next();

          final InpatientPrescription prescription = (InpatientPrescription)getEhrMapper(InpatientPrescription.class)
              .fromRM((RmObject)resultRow.get(0));
          final String patientId = (String)resultRow.get(1);

          medicationOrderWithPatientId.put(prescription, patientId);
        }
      }
    }

    return medicationOrderWithPatientId;
  }

  @EhrSessioned
  public int countActiveMedicationOrdersWithPatientIds(final DateTime when)
  {
    return query(
        buildActiveInpatientPrescriptionsWithPatientIdQuery("SELECT DISTINCT COUNT(i) from EHR ", when),
        (resultRow, hasNext) -> (int)resultRow[0]).get(0);
  }

  private String buildActiveInpatientPrescriptionsWithPatientIdQuery(final String selectAql, final DateTime time)
  {
    final StringBuilder baseQuery = new StringBuilder();
    baseQuery
        .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.prescription.v0]")
        .append(" CONTAINS Instruction i[openEHR-EHR-INSTRUCTION.medication_order.v2]")
        .append(" WHERE c/name/value = 'Inpatient Prescription'")
        .append(" AND c/context/start_time > ").append(getAqlDateTimeQuoted(time.minusMonths(6)))
        .append(buildTherapyIntervalCriterion(Intervals.infiniteFrom(time)));

    return selectAql + baseQuery;
  }

  public List<InpatientPrescription> getPrescriptionsWithMaxDosePercentage(final DateTime when, final String patientId)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      sb
          .append("SELECT c")
          .append(" FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.prescription.v0]")
          .append(" CONTAINS Instruction i[openEHR-EHR-INSTRUCTION.medication_order.v2]")
          .append(" CONTAINS Cluster ad[openEHR-EHR-CLUSTER.Medication_additional_details.v0]")
          .append(" WHERE c/name/value = 'Inpatient Prescription'")
          .append(" AND exists ad/items[at0012]/value/numerator") // max dose percentage (DvProportion)
          .append(buildTherapyIntervalCriterion(Intervals.infiniteFrom(when)));

      return query(
          sb.toString(),
          (resultRow, hasNext) -> (InpatientPrescription)getEhrMapper(InpatientPrescription.class).fromRM((RmObject)resultRow[0]));
    }

    return Collections.emptyList();
  }

  public List<OutpatientPrescription> findOutpatientPrescriptions(final String patientId, final Integer numberOfResults)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (StringUtils.isEmpty(ehrId))
    {
      return Collections.emptyList();
    }
    currentSession().useEhr(ehrId);
    final StringBuilder sb = new StringBuilder();
    sb
        .append("SELECT c FROM EHR[ehr_id/value='").append(ehrId).append("']")
        .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.prescription.v0]")
        .append(" WHERE c/name/value = 'Outpatient Prescription'")
        .append(" ORDER BY c/context/start_time desc")
        .append(" FETCH " + numberOfResults);

    return queryEhrContent(
        sb.toString(),
        (resultRow, hasNext) -> (OutpatientPrescription)getEhrMapper(OutpatientPrescription.class).fromRM((RmObject)resultRow[0])
    );
  }
}
