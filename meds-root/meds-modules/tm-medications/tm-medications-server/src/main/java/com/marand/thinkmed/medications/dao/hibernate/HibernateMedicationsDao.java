package com.marand.thinkmed.medications.dao.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.base.Functions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.maf.core.hibernate.query.Alias;
import com.marand.maf.core.hibernate.query.Hql;
import com.marand.maf.core.resultrow.ProcessingException;
import com.marand.maf.core.resultrow.TupleProcessor;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.ActionReasonType;
import com.marand.thinkmed.medications.MedicationExternalSystemType;
import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.MedicationsExternalValueType;
import com.marand.thinkmed.medications.api.internal.dto.CodedNameDto;
import com.marand.thinkmed.medications.api.internal.dto.DoseFormDto;
import com.marand.thinkmed.medications.api.internal.dto.IndicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MaxDoseDto;
import com.marand.thinkmed.medications.api.internal.dto.MaxDosePeriod;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationOrderFormType;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.TitrationType;
import com.marand.thinkmed.medications.api.internal.dto.property.MedicationPropertyType;
import com.marand.thinkmed.medications.config.MedsProperties;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dose.DoseUtils;
import com.marand.thinkmed.medications.dto.DispenseSourceDto;
import com.marand.thinkmed.medications.dto.InformationSourceDto;
import com.marand.thinkmed.medications.dto.InformationSourceGroupEnum;
import com.marand.thinkmed.medications.dto.InformationSourceTypeEnum;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.MedicationDocumentDto;
import com.marand.thinkmed.medications.dto.MedicationDocumentType;
import com.marand.thinkmed.medications.dto.MedicationIngredientDto;
import com.marand.thinkmed.medications.dto.customGroup.MedicationCustomGroupType;
import com.marand.thinkmed.medications.dto.property.MedicationPropertyDto;
import com.marand.thinkmed.medications.dto.unit.MedicationUnitDto;
import com.marand.thinkmed.medications.mapper.DoseFormDtoMapper;
import com.marand.thinkmed.medications.model.impl.ActionReasonImpl;
import com.marand.thinkmed.medications.model.impl.AtcClassificationImpl;
import com.marand.thinkmed.medications.model.impl.DispenseSourceImpl;
import com.marand.thinkmed.medications.model.impl.InformationSourceImpl;
import com.marand.thinkmed.medications.model.impl.MedicationBaseImpl;
import com.marand.thinkmed.medications.model.impl.MedicationBaseVersionImpl;
import com.marand.thinkmed.medications.model.impl.MedicationCustomGroupImpl;
import com.marand.thinkmed.medications.model.impl.MedicationCustomGroupMemberImpl;
import com.marand.thinkmed.medications.model.impl.MedicationDoseFormImpl;
import com.marand.thinkmed.medications.model.impl.MedicationExternalImpl;
import com.marand.thinkmed.medications.model.impl.MedicationFormularyOrganization;
import com.marand.thinkmed.medications.model.impl.MedicationGenericImpl;
import com.marand.thinkmed.medications.model.impl.MedicationImpl;
import com.marand.thinkmed.medications.model.impl.MedicationIndicationImpl;
import com.marand.thinkmed.medications.model.impl.MedicationIndicationLinkImpl;
import com.marand.thinkmed.medications.model.impl.MedicationIngredientImpl;
import com.marand.thinkmed.medications.model.impl.MedicationIngredientLinkImpl;
import com.marand.thinkmed.medications.model.impl.MedicationPropertyImpl;
import com.marand.thinkmed.medications.model.impl.MedicationPropertyLinkImpl;
import com.marand.thinkmed.medications.model.impl.MedicationRouteImpl;
import com.marand.thinkmed.medications.model.impl.MedicationRouteLinkImpl;
import com.marand.thinkmed.medications.model.impl.MedicationRouteRelationImpl;
import com.marand.thinkmed.medications.model.impl.MedicationTypeImpl;
import com.marand.thinkmed.medications.model.impl.MedicationVersionImpl;
import com.marand.thinkmed.medications.model.impl.MedicationWarningImpl;
import com.marand.thinkmed.medications.model.impl.MedicationsExternalCrossTabImpl;
import com.marand.thinkmed.medications.model.impl.PatientTherapyLastLinkNameImpl;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import com.marand.thinkmed.medications.service.WarningSeverity;
import com.marand.thinkmed.medications.service.WarningType;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import com.marand.thinkmed.medications.units.dao.UnitsDao;
import lombok.NonNull;
import org.hibernate.SessionFactory;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.marand.maf.core.hibernate.query.Alias.effectiveEntities;
import static com.marand.maf.core.hibernate.query.Alias.permanentEntities;
import static com.marand.maf.core.hibernate.query.Criterion.or;

/**
 * @author Mitja Lapajne
 */

@Component
public class HibernateMedicationsDao implements MedicationsDao
{
  private static final Logger LOG = LoggerFactory.getLogger(HibernateMedicationsDao.class);

  private static final Alias.Permanent<MedicationImpl> medication = Alias.forPermanentEntity(MedicationImpl.class);
  private static final Alias.Effective<MedicationVersionImpl> medicationVersion = Alias.forEffectiveEntity(
      MedicationVersionImpl.class);
  private static final Alias.Effective<MedicationIngredientLinkImpl> medicationIngredientLink = Alias.forEffectiveEntity(
      MedicationIngredientLinkImpl.class);
  private static final Alias.Permanent<MedicationIndicationImpl> medicationIndication = Alias.forPermanentEntity(
      MedicationIndicationImpl.class);
  private static final Alias.Effective<MedicationIndicationLinkImpl> medicationIndicationLink = Alias.forEffectiveEntity(
      MedicationIndicationLinkImpl.class);
  private static final Alias.Permanent<MedicationIngredientImpl> medicationIngredient = Alias.forPermanentEntity(
      MedicationIngredientImpl.class);
  private static final Alias.Effective<MedicationRouteLinkImpl> medicationRouteLink = Alias.forEffectiveEntity(
      MedicationRouteLinkImpl.class);
  private static final Alias.Permanent<MedicationRouteImpl> medicationRoute = Alias.forPermanentEntity(MedicationRouteImpl.class);
  private static final Alias.Permanent<MedicationRouteRelationImpl> medicationRouteRelation = Alias.forPermanentEntity(
      MedicationRouteRelationImpl.class);
  private static final Alias.Permanent<MedicationDoseFormImpl> doseForm = Alias.forPermanentEntity(MedicationDoseFormImpl.class);
  private static final Alias.Permanent<MedicationExternalImpl> medicationExternal = Alias.forPermanentEntity(
      MedicationExternalImpl.class);
  private static final Alias.Permanent<AtcClassificationImpl> atcClassification = Alias.forPermanentEntity(
      AtcClassificationImpl.class);
  private static final Alias.Permanent<MedicationsExternalCrossTabImpl> medicationExternalTranslator = Alias.forPermanentEntity(
      MedicationsExternalCrossTabImpl.class);
  private static final Alias.Permanent<MedicationFormularyOrganization> formularyOrganization = Alias.forPermanentEntity(
      MedicationFormularyOrganization.class);
  private static final Alias.Permanent<MedicationTypeImpl> medicationType = Alias.forPermanentEntity(MedicationTypeImpl.class);
  private static final Alias.Permanent<MedicationGenericImpl> medicationGeneric = Alias.forPermanentEntity(
      MedicationGenericImpl.class);
  private static final Alias.Permanent<MedicationCustomGroupImpl> medicationCustomGroup = Alias.forPermanentEntity(
      MedicationCustomGroupImpl.class);
  private static final Alias.Permanent<MedicationCustomGroupMemberImpl> medicationCustomGroupMember = Alias.forPermanentEntity(
      MedicationCustomGroupMemberImpl.class);
  private static final Alias.Permanent<PatientTherapyLastLinkNameImpl> patientTherapyLastLinkName = Alias.forPermanentEntity(
      PatientTherapyLastLinkNameImpl.class);
  private static final Alias.Effective<ActionReasonImpl> actionReason = Alias.forEffectiveEntity(ActionReasonImpl.class);
  private static final Alias.Effective<MedicationWarningImpl> medicationWarning = Alias.forEffectiveEntity(
      MedicationWarningImpl.class);

  private static final Alias.Permanent<MedicationBaseImpl> medicationBase = Alias.forPermanentEntity(MedicationBaseImpl.class);
  private static final Alias.Effective<MedicationBaseVersionImpl> medicationBaseVersion = Alias.forEffectiveEntity(
      MedicationBaseVersionImpl.class);
  private static final Alias.Permanent<MedicationPropertyImpl> medicationProperty = Alias.forPermanentEntity(
      MedicationPropertyImpl.class);
  private static final Alias.Effective<MedicationPropertyLinkImpl> medicationPropertyLink = Alias.forEffectiveEntity(
      MedicationPropertyLinkImpl.class);
  private static final Alias.Permanent<InformationSourceImpl> informationSource = Alias.forPermanentEntity(
      InformationSourceImpl.class);
  private static final Alias.Permanent<DispenseSourceImpl> dispenseSource = Alias.forPermanentEntity(
      DispenseSourceImpl.class);

  private UnitsDao unitsDao;
  private DoseFormDtoMapper doseFormDtoMapper;
  private DoseUtils doseUtils;
  private MedsProperties medsProperties;
  private SessionFactory sessionFactory;

  @Autowired
  public void setSessionFactory(final SessionFactory sessionFactory)
  {
    this.sessionFactory = sessionFactory;
  }

  @Autowired
  public void setUnitsDao(final UnitsDao unitsDao)
  {
    this.unitsDao = unitsDao;
  }

  @Autowired
  public void setDoseFormDtoMapper(final DoseFormDtoMapper doseFormDtoMapper)
  {
    this.doseFormDtoMapper = doseFormDtoMapper;
  }

  @Autowired
  public void setDoseUtils(final DoseUtils doseUtils)
  {
    this.doseUtils = doseUtils;
  }

  @Autowired
  public void setMedsProperties(final MedsProperties medsProperties)
  {
    this.medsProperties = medsProperties;
  }

  @Override
  public Map<Long, MedicationDataDto> loadMedicationsMap(final @NonNull DateTime when)
  {
    final Stopwatch stopwatch = Stopwatch.createStarted();

    // load properties
    final Multimap<Long, MedicationPropertyDto> medicationPropertiesMap = loadMedicationProperties(when);
    final Multimap<Long, MedicationPropertyDto> basePropertiesMap = loadBaseProperties(when);

    // load VTM medications
    final VtmMedicationsResult vtmMedicationsResult = loadVtmMedications(when, medicationPropertiesMap);

    // load units
    final Map<Long, MedicationUnitDto> unitsMap = unitsDao.loadUnits();

    // load PRODUCT medications
    final ProductMedicationsResult productMedicationsResult = loadProductMedications(
        when,
        medicationPropertiesMap,
        basePropertiesMap,
        vtmMedicationsResult,
        unitsMap);

    // map relations to medications
    // formulary mapping must run first! - Ingredient mapping relies on formulary data
    mapFormularyDataToMedications(productMedicationsResult, vtmMedicationsResult);
    mapRoutesToMedications(productMedicationsResult, vtmMedicationsResult, unitsMap, when);
    mapIngredientsToMedications(productMedicationsResult, vtmMedicationsResult, unitsMap, when);
    mapIndicationsToMedications(productMedicationsResult, vtmMedicationsResult, when);

    final Map<Long, MedicationDataDto> result = new HashMap<>();
    result.putAll(vtmMedicationsResult.getMedicationDataMap());
    result.putAll(productMedicationsResult.getMedicationDataMap());

    mapDocumentsToMedications(result);

    // set prescribing dose
    final List<Boolean> prescribingDoseErrors = result.values()
        .stream()
        .map(this::setPrescribingDose)
        .filter(a -> !a)
        .collect(Collectors.toList());

    if (!prescribingDoseErrors.isEmpty())
    {
      LOG.error("There are " + prescribingDoseErrors.size() + " errors in prescribing dose for medications");
    }

    LOG.info("Medications data map loaded - size: " + result.size() + " - took: " + stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) + " ms");
    return result;
  }

  @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
  private boolean setPrescribingDose(final MedicationDataDto medication)
  {
    try
    {
      medication.setPrescribingDose(doseUtils.buildPrescribingDose(medication));
      return true;
    }
    catch (final RuntimeException e)
    {
      LOG.debug(
          new StringBuilder()
              .append("Could not set prescribing dose to medication ")
              .append(medication.getMedication().getId())
              .append(" - ")
              .append(e.getMessage())
              .toString());
      return false;
    }
  }

  private ProductMedicationsResult loadProductMedications(
      final DateTime when,
      final Multimap<Long, MedicationPropertyDto> medicationPropertiesMap,
      final Multimap<Long, MedicationPropertyDto> basePropertiesMap,
      final VtmMedicationsResult vtmMedicationsResult,
      final Map<Long, MedicationUnitDto> unitsMap)
  {
    final Map<String, Long> vmpIdMedicationIdMap = new HashMap<>();
    final Map<Long, MedicationDataDto> productMedicationDataMap = new HashMap<>();
    final Alias.Permanent<AtcClassificationImpl> topAtcClassification = Alias.forPermanentEntity(AtcClassificationImpl.class);

    final Stopwatch stopwatch = Stopwatch.createStarted();

    new Hql()
        .select(
            medication.id(),
            medicationBase.id(),
            medication.get("validFrom"),
            medication.get("validTo"),
            medication.get("inpatient"),
            medication.get("outpatient"),

            medicationVersion.get("name"),

            medicationType.get("type"),

            medication.get("medicationLevel"),
            medication.get("vtmId"),
            medication.get("vmpId"),
            medication.get("ampId"),
            medication.get("orderable"),

            medicationCustomGroup.get("name"),
            medicationCustomGroup.get("careProviderId"),
            medicationCustomGroup.get("sortOrder"),
            medicationCustomGroup.get("customGroupType"),

            topAtcClassification.get("code"),
            topAtcClassification.get("name"),

            doseForm,

            medicationBaseVersion.get("administrationUnit").id(),
            medicationBaseVersion.get("administrationUnitFactor"),
            medicationBaseVersion.get("supplyUnit").id(),
            medicationBaseVersion.get("supplyUnitFactor"),

            medicationBaseVersion.get("titration"),
            medicationBaseVersion.get("roundingFactor"),

            medicationVersion.get("medicationPackaging"),
            medicationGeneric.get("name"),
            medicationBaseVersion.get("descriptiveDose")
        )
        .from(
            medication.innerJoin("versions").as(medicationVersion),
            medication.innerJoin("medicationBase").as(medicationBase),

            medicationBase.leftOuterJoin("types").as(medicationType).with(medicationType.notDeleted()),

            medication.leftOuterJoin("customGroupMembers")
                .as(medicationCustomGroupMember)
                .with(medicationCustomGroupMember.notDeleted()),
            medicationCustomGroupMember.leftOuterJoin("medicationCustomGroup")
                .as(medicationCustomGroup)
                .with(medicationCustomGroup.notDeleted()),

            medicationBase.innerJoin("versions").as(medicationBaseVersion),

            medicationBaseVersion.leftOuterJoin("doseForm").as(doseForm),
            medicationBaseVersion.leftOuterJoin("medicationGeneric").as(medicationGeneric),

            medicationBaseVersion.leftOuterJoin("atcClassification").as(atcClassification),
            atcClassification.leftOuterJoin("topParent").as(topAtcClassification)
        )
        .where(
            medication.notDeleted(),
            medication.get("medicationLevel").notIn(MedicationLevelEnum.VTM),
            effectiveEntities(
                medicationVersion,
                medicationBaseVersion).notDeletedAndEffectiveAt(when)
        )

        // All VMP's should be loaded before AMP's
        .orderBy(medication.get("medicationLevel").desc())
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            (resultRow, hasNext) ->
            {
              final Long medicationId = (Long)resultRow[0];
              final Long baseId = (Long)resultRow[1];
              final DateTime validFrom = (DateTime)resultRow[2];
              final DateTime validTo = (DateTime)resultRow[3];
              final boolean inpatient = (Boolean)resultRow[4];
              final boolean outpatient = (Boolean)resultRow[5];
              final String name = (String)resultRow[6];
              final MedicationTypeEnum medicationTypeEnum = (MedicationTypeEnum)resultRow[7];

              final MedicationLevelEnum medicationLevelEnum = (MedicationLevelEnum)resultRow[8];
              final String vtmId = (String)resultRow[9];
              final String vmpId = (String)resultRow[10];
              final String ampId = (String)resultRow[11];
              final Boolean orderable = (Boolean)resultRow[12];

              final String customGroupName = (String)resultRow[13];
              final String customGroupCareProviderId = (String)resultRow[14];
              final Integer customGroupSort = (Integer)resultRow[15];
              final MedicationCustomGroupType customGroupType = (MedicationCustomGroupType)resultRow[16];

              final String atcCode = (String)resultRow[17];
              final String atcName = (String)resultRow[18];

              final MedicationDoseFormImpl aDoseForm = (MedicationDoseFormImpl)resultRow[19];

              final Long administrationUnitId = (Long)resultRow[20];
              final Double administrationUnitFactor = (Double)resultRow[21];
              final Long supplyUnitId = (Long)resultRow[22];
              final Double supplyUnitFactor = (Double)resultRow[23];

              final TitrationType titrationType = (TitrationType)resultRow[24];
              final Double roundingFactor = (Double)resultRow[25];

              final String packaging = (String)resultRow[26];
              final String genericName = (String)resultRow[27];
              final Boolean descriptiveDose = (Boolean)resultRow[28];

              final MedicationDataDto dto;
              if (productMedicationDataMap.containsKey(medicationId))
              {
                dto = productMedicationDataMap.get(medicationId);
              }
              else
              {
                dto = new MedicationDataDto();
                final MedicationDto medicationDto = new MedicationDto();
                medicationDto.setId(medicationId);
                dto.setMedication(medicationDto);

                dto.setValidFrom(validFrom);
                dto.setValidTo(validTo);

                dto.setInpatient(inpatient);
                dto.setOutpatient(outpatient);

                medicationDto.setName(name);
                medicationDto.setMedicationType(medicationTypeEnum);

                dto.setMedicationLevel(medicationLevelEnum);
                dto.setVtmId(vtmId);
                dto.setVmpId(vmpId);
                dto.setAmpId(ampId);
                dto.setOrderable(orderable);

                medicationPropertiesMap.get(medicationId).forEach(dto::setProperty);
                // set all properties from it's VTM
                basePropertiesMap.get(baseId).forEach(dto::setProperty);

                dto.setMedicationPackaging(packaging);
                dto.setTitration(titrationType);
                dto.setRoundingFactor(roundingFactor);
                dto.setDescriptiveDose(descriptiveDose == null ? false : descriptiveDose);

                Opt.of(administrationUnitId).ifPresent(u -> dto.setAdministrationUnit(unitsMap.get(u).getName()));
                dto.setAdministrationUnitFactor(administrationUnitFactor);
                Opt.of(supplyUnitId).ifPresent(u -> dto.setSupplyUnit(unitsMap.get(u).getName()));
                dto.setSupplyUnitFactor(supplyUnitFactor);
              }

              if (aDoseForm != null)
              {
                if (aDoseForm.getMedicationOrderFormType() == MedicationOrderFormType.DESCRIPTIVE)
                {
                  dto.setDescriptiveDose(true);
                }
                dto.setDoseForm(doseFormDtoMapper.map(aDoseForm, DoseFormDto.class));
              }

              dto.setAtcGroupCode(atcCode);
              dto.setAtcGroupName(atcName);
              dto.getMedication().setGenericName(genericName);

              if (customGroupType == MedicationCustomGroupType.CARE_PROVIDER && customGroupName != null && customGroupCareProviderId != null)
              {
                dto.getCareProviderCustomGroups().put(customGroupCareProviderId, Pair.of(customGroupName, customGroupSort));
              }
              if (customGroupType == MedicationCustomGroupType.INTERCHANGEABLE_DRUGS)
              {
                dto.setInterchangeableDrugsGroup(customGroupName);
              }

              if (medicationLevelEnum == MedicationLevelEnum.VMP)
              {
                vmpIdMedicationIdMap.put(vmpId, medicationId);
              }

              if (medicationLevelEnum == MedicationLevelEnum.AMP && vmpId != null && vmpIdMedicationIdMap.containsKey(vmpId))
              {
                // set AMP properties to it's VMP
                medicationPropertiesMap
                    .get(medicationId)
                    .stream()
                    .filter(p -> p.getType() != null && p.getType().isAmpToVmp())
                    .forEach(productMedicationDataMap.get(vmpIdMedicationIdMap.get(vmpId))::setProperty);
              }

              final MedicationDataDto vtmMedicationDataDto = vtmMedicationsResult.getByVtmId(vtmId);
              if (vtmMedicationDataDto != null)
              {
                if (medicationLevelEnum == MedicationLevelEnum.VMP)
                {
                  if (titrationType != null)
                  {
                    vtmMedicationDataDto.setTitration(titrationType);
                  }

                  if (dto.getDoseForm() != null)
                  {
                    final MedicationOrderFormType currentFormType = dto.getDoseForm().getMedicationOrderFormType();
                    if (vtmMedicationDataDto.getDoseForm() == null)
                    {
                      final DoseFormDto vtmDoseForm = new DoseFormDto();
                      vtmDoseForm.setMedicationOrderFormType(currentFormType);
                      vtmMedicationDataDto.setDoseForm(vtmDoseForm);
                    }
                    else if (vtmMedicationDataDto.getDoseForm()
                        .getMedicationOrderFormType() != MedicationOrderFormType.SIMPLE)
                    {
                      vtmMedicationDataDto.getDoseForm().setMedicationOrderFormType(currentFormType);
                    }
                  }

                  vtmMedicationDataDto.getMedication().setGenericName(genericName);

                  // set VMP properties to it's VTM
                  basePropertiesMap
                      .get(baseId)
                      .stream()
                      .filter(p -> p.getType() != null && p.getType().isVmpToVtm())
                      .forEach(vtmMedicationDataDto::setProperty);

                  // set medication type to VTM ( default to MEDICATION type if different types are present on it's VMP's )
                  final MedicationTypeEnum vtmMedicationType = vtmMedicationDataDto.getMedication().getMedicationType();
                  if (vtmMedicationType != null && vtmMedicationType != medicationTypeEnum)
                  {
                    vtmMedicationDataDto.getMedication().setMedicationType(MedicationTypeEnum.MEDICATION);
                  }
                  else
                  {
                    vtmMedicationDataDto.getMedication().setMedicationType(medicationTypeEnum);
                  }
                }
                else if (medicationLevelEnum == MedicationLevelEnum.AMP)
                {
                  // set AMP properties to it's VTM
                  medicationPropertiesMap
                      .get(medicationId)
                      .stream()
                      .filter(p -> p.getType() != null && p.getType().isAmpToVtm())
                      .forEach(vtmMedicationDataDto::setProperty);
                }
              }

              productMedicationDataMap.put(medicationId, dto);
              return null;
            });

    LOG.info(String.format("Medication products loaded - took: %d ms", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS)));
    return new ProductMedicationsResult(productMedicationDataMap);
  }

  private VtmMedicationsResult loadVtmMedications(
      final DateTime when,
      final Multimap<Long, MedicationPropertyDto> medicationPropertiesMap)
  {
    final Stopwatch stopwatch = Stopwatch.createStarted();

    final Map<Long, MedicationDataDto> vtmMedicationDataMap = new HashMap<>();
    final Map<String, Long> vtmIdMedicationIdMap = new HashMap<>();

    new Hql()
        .select(
            medication.id(),
            medication.get("validFrom"),
            medication.get("validTo"),

            medication.get("inpatient"),
            medication.get("outpatient"),

            medicationVersion.get("name"),

            medication.get("medicationLevel"),
            medication.get("vtmId"),
            medication.get("vmpId"),
            medication.get("ampId"),
            medication.get("orderable"),

            medicationCustomGroup.get("name"),
            medicationCustomGroup.get("careProviderId"),
            medicationCustomGroup.get("sortOrder"),
            medicationCustomGroup.get("customGroupType"),

            medicationVersion.get("medicationPackaging")
        )
        .from(
            medication.innerJoin("versions").as(medicationVersion),

            medication.leftOuterJoin("customGroupMembers")
                .as(medicationCustomGroupMember).with(medicationCustomGroupMember.notDeleted()),
            medicationCustomGroupMember.leftOuterJoin("medicationCustomGroup")
                .as(medicationCustomGroup).with(medicationCustomGroup.notDeleted())
        )
        .where(
            medication.notDeleted(), medication.get("medicationLevel").eq(MedicationLevelEnum.VTM),
            effectiveEntities(medicationVersion).notDeletedAndEffectiveAt(when)
        )
        .orderBy(medication.id())
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            (resultRow, hasNext) ->
            {
              final Long medicationId = (Long)resultRow[0];
              final DateTime validFrom = (DateTime)resultRow[1];
              final DateTime validTo = (DateTime)resultRow[2];

              final boolean inpatient = (Boolean)resultRow[3];
              final boolean outpatient = (Boolean)resultRow[4];

              final String name = (String)resultRow[5];

              final MedicationLevelEnum medicationLevelEnum = (MedicationLevelEnum)resultRow[6];
              final String vtmId = (String)resultRow[7];
              final String vmpId = (String)resultRow[8];
              final String ampId = (String)resultRow[9];
              final Boolean orderable = (Boolean)resultRow[10];

              final String customGroupName = (String)resultRow[11];
              final String customGroupCareProviderId = (String)resultRow[12];
              final Integer customGroupSort = (Integer)resultRow[13];
              final MedicationCustomGroupType customGroupType = (MedicationCustomGroupType)resultRow[14];

              final String packaging = (String)resultRow[15];

              final MedicationDataDto dto;
              if (vtmMedicationDataMap.containsKey(medicationId))
              {
                dto = vtmMedicationDataMap.get(medicationId);
              }
              else
              {
                dto = new MedicationDataDto();
                final MedicationDto medicationDto = new MedicationDto();
                medicationDto.setId(medicationId);
                dto.setMedication(medicationDto);

                dto.setValidFrom(validFrom);
                dto.setValidTo(validTo);

                dto.setInpatient(inpatient);
                dto.setOutpatient(outpatient);

                medicationDto.setName(name);

                vtmIdMedicationIdMap.put(vtmId, medicationId);

                dto.setMedicationLevel(medicationLevelEnum);
                dto.setVtmId(vtmId);
                dto.setVmpId(vmpId);
                dto.setAmpId(ampId);
                dto.setOrderable(orderable);

                dto.setMedicationPackaging(packaging);
                medicationPropertiesMap.get(medicationId).forEach(dto::setProperty);
              }

              if (customGroupType == MedicationCustomGroupType.CARE_PROVIDER && customGroupName != null && customGroupCareProviderId != null)
              {
                dto.getCareProviderCustomGroups().put(customGroupCareProviderId, Pair.of(customGroupName, customGroupSort));
              }
              if (customGroupType == MedicationCustomGroupType.INTERCHANGEABLE_DRUGS)
              {
                dto.setInterchangeableDrugsGroup(customGroupName);
              }
              vtmMedicationDataMap.put(medicationId, dto);
              return null;
            });

    LOG.info(String.format("Medication VTMs loaded - took: %d ms", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS)));
    return new VtmMedicationsResult(vtmIdMedicationIdMap, vtmMedicationDataMap);
  }

  void mapDocumentsToMedications(final Map<Long, MedicationDataDto> medicationsMap)
  {

    new Hql()
        .select(
            medication.id(),
            medicationExternal.get("externalId"),
            medicationExternal.get("externalSystem"),
            medicationExternal.get("externalSystemType")
        )
        .from(
            medicationExternal.innerJoin("medication").as(medication)
        )
        .where(
            or(
                medicationExternal.get("externalSystemType").eq(MedicationExternalSystemType.DOCUMENTS_PROVIDER),
                medicationExternal.get("externalSystemType").eq(MedicationExternalSystemType.URL)
            ),
            medicationExternal.notDeleted()
        )
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            (resultRow, hasNext) ->
            {
              final Long medicationId = (Long)resultRow[0];
              final String documentReference = (String)resultRow[1];
              final String externalSystem = (String)resultRow[2];
              final MedicationExternalSystemType externalSystemType = (MedicationExternalSystemType)resultRow[3];

              final MedicationDocumentDto documentDto = new MedicationDocumentDto();
              documentDto.setDocumentReference(documentReference);
              documentDto.setExternalSystem(externalSystem);
              documentDto.setType(MedicationDocumentType.fromMedicationExternalSystemType(externalSystemType));

              if (medicationsMap.containsKey(medicationId))
              {
                medicationsMap.get(medicationId).getMedicationDocuments().add(documentDto);
              }

              return null;
            });
  }

  private void mapIndicationsToMedications(
      final ProductMedicationsResult productMedicationsResult,
      final VtmMedicationsResult vtmMedicationsResult,
      final DateTime when)
  {

    new Hql()
        .select(
            medication.id(),
            medication.get("medicationLevel"),
            medication.get("vtmId"),

            medicationIndication.get("code"),
            medicationIndication.get("name")
        )
        .from(
            medication.innerJoin("medicationBase").as(medicationBase),
            medicationBase.innerJoin("indications").as(medicationIndicationLink),
            medicationIndicationLink.innerJoin("indication").as(medicationIndication)
        )
        .where(
            permanentEntities(medication, medicationIndication).notDeleted(),
            effectiveEntities(medicationIndicationLink).notDeletedAndEffectiveAt(when)
        )
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            (resultRow, hasNext) ->
            {
              final Long medicationId = (Long)resultRow[0];
              final MedicationLevelEnum medicationLevelEnum = (MedicationLevelEnum)resultRow[1];
              final String vtmId = (String)resultRow[2];

              final String indicationCode = (String)resultRow[3];
              final String indicationName = (String)resultRow[4];

              final MedicationDataDto medicationDataDto = productMedicationsResult.getById(medicationId);
              medicationDataDto.getIndications().add(new IndicationDto(indicationCode, indicationName));

              if (MedicationLevelEnum.PRODUCT_LEVELS.contains(medicationLevelEnum))
              {
                final MedicationDataDto vtmMedicationDataDto = vtmMedicationsResult.getByVtmId(vtmId);
                if (vtmMedicationDataDto != null && vtmMedicationDataDto.getIndications()
                    .stream()
                    .noneMatch(i -> i.getId().equals(indicationCode)))
                {
                  vtmMedicationDataDto.getIndications().add(new IndicationDto(indicationCode, indicationName));
                }
              }
              return null;
            });
  }

  private void mapFormularyDataToMedications(
      final ProductMedicationsResult productMedicationsResult,
      final VtmMedicationsResult vtmMedicationsResult)
  {
    new Hql()
        .select(
            formularyOrganization.get("medication").id(),
            formularyOrganization.get("sortOrder")
        )
        .from(formularyOrganization)
        .where(
            permanentEntities(formularyOrganization).notDeleted(),
            formularyOrganization.get("organizationCode").eq(medsProperties.getOrganizationCode()))
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            (resultRow, hasNext) ->
            {
              final Long medicationId = (Long)resultRow[0];
              final Integer sortOrderOrg = (Integer)resultRow[1];

              Opt.of(vtmMedicationsResult.getMedicationDataMap().get(medicationId))
                  .forEach(m -> {
                    m.setFormulary(true);
                    m.setSortOrder(sortOrderOrg);
                  });

              Opt.of(productMedicationsResult.getMedicationDataMap().get(medicationId))
                  .forEach(m -> {
                    m.setFormulary(true);
                    m.setSortOrder(sortOrderOrg);
                  });

              return null;
            }
        );
  }

  private void mapIngredientsToMedications(
      final ProductMedicationsResult productMedicationsResult,
      final VtmMedicationsResult vtmMedicationsResult,
      final Map<Long, MedicationUnitDto> unitsMap,
      final DateTime when)
  {
    // used to update VTM units to most frequent among VMPs
    final Multimap<Long, String> vtmFormularyChildNumeratorUnits = ArrayListMultimap.create();
    final Multimap<Long, String> vtmNonFormularyChildNumeratorUnits = ArrayListMultimap.create();

    new Hql()
        .select(
            medication.id(),
            medication.get("medicationLevel"),
            medication.get("vtmId"),

            medicationIngredient.get("name"),
            medicationIngredient.id(),
            medicationIngredient.get("medicationRule"),
            medicationIngredientLink.id(),
            medicationIngredientLink.get("strengthNumerator"),
            medicationIngredientLink.get("strengthNumeratorUnit").id(),
            medicationIngredientLink.get("strengthDenominator"),
            medicationIngredientLink.get("strengthDenominatorUnit").id(),
            medicationIngredientLink.get("main")
        )
        .from(
            medication.innerJoin("medicationBase").as(medicationBase),
            medicationBase.innerJoin("ingredients").as(medicationIngredientLink),
            medicationIngredientLink.innerJoin("ingredient").as(medicationIngredient)
        )
        .where(
            permanentEntities(medication, medicationIngredient).notDeleted(),
            effectiveEntities(medicationIngredientLink).notDeletedAndEffectiveAt(when)
        )
        .orderBy(medicationIngredient.get("name"))
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            (resultRow, hasNext) ->
            {
              final Long medicationId = (Long)resultRow[0];
              final MedicationLevelEnum medicationLevelEnum = (MedicationLevelEnum)resultRow[1];
              final String vtmId = (String)resultRow[2];

              final String ingredientName = (String)resultRow[3];
              final Long ingredientId = (Long)resultRow[4];
              final MedicationRuleEnum ingredientRuleEnum = (MedicationRuleEnum)resultRow[5];
              final Long medicationIngredientLinkId = (Long)resultRow[6];
              final Double strengthNumerator = (Double)resultRow[7];
              final Long strengthNumeratorUnitId = (Long)resultRow[8];
              final Double strengthDenominator = (Double)resultRow[9];
              final Long strengthDenominatorUnitId = (Long)resultRow[10];
              final Boolean mainIngredient = (Boolean)resultRow[11];

              final MedicationIngredientDto medicationIngredientDto = new MedicationIngredientDto();
              medicationIngredientDto.setIngredientName(ingredientName);
              medicationIngredientDto.setIngredientRule(ingredientRuleEnum);
              medicationIngredientDto.setIngredientId(ingredientId);
              medicationIngredientDto.setId(medicationIngredientLinkId);
              medicationIngredientDto.setStrengthNumerator(strengthNumerator);

              Opt.of(strengthNumeratorUnitId)
                  .ifPresent(u -> medicationIngredientDto.setStrengthNumeratorUnit(unitsMap.get(u).getName()));
              medicationIngredientDto.setStrengthDenominator(strengthDenominator);

              Opt.of(strengthDenominatorUnitId)
                  .ifPresent(u -> medicationIngredientDto.setStrengthDenominatorUnit(unitsMap.get(u).getName()));
              medicationIngredientDto.setMain(mainIngredient != null ? mainIngredient : false);

              final MedicationDataDto medicationDataDto = productMedicationsResult.getById(medicationId);
              if (medicationDataDto != null && !medicationDataDto.getMedicationIngredients()
                  .contains(medicationIngredientDto))
              {
                medicationDataDto.getMedicationIngredients().add(medicationIngredientDto);
              }

              if (MedicationLevelEnum.PRODUCT_LEVELS.contains(medicationLevelEnum))
              {
                final MedicationDataDto vtmMedicationDataDto = vtmMedicationsResult.getByVtmId(vtmId);

                final boolean withoutIngredient = vtmMedicationDataDto != null
                    && vtmMedicationDataDto.getMedicationIngredients()
                    .stream()
                    .noneMatch(i -> i.getIngredientId() == ingredientId);

                final boolean vmpWithVtm = vtmMedicationDataDto != null && medicationLevelEnum == MedicationLevelEnum.VMP;
                final boolean formularyVmp = medicationDataDto != null && medicationDataDto.isFormulary();

                if (vmpWithVtm && strengthNumeratorUnitId != null)
                {
                  final Long vtmMedicationId = vtmMedicationDataDto.getMedication().getId();
                  final String strengthNumeratorUnitName = unitsMap.get(strengthNumeratorUnitId).getName();

                  if (formularyVmp)
                  {
                    // keep all numerator units for VTMs formulary VMPs
                    vtmFormularyChildNumeratorUnits.put(vtmMedicationId, strengthNumeratorUnitName);
                  }
                  else
                  {
                    // keep all numerator units for VTMs non-formulary VMPs
                    vtmNonFormularyChildNumeratorUnits.put(vtmMedicationId, strengthNumeratorUnitName);
                  }
                }

                if (withoutIngredient)
                {
                  final MedicationIngredientDto vtmIngredient = new MedicationIngredientDto();
                  vtmIngredient.setIngredientName(ingredientName);
                  vtmIngredient.setStrengthNumerator(1.0);
                  if (strengthNumeratorUnitId != null)
                  {
                    vtmIngredient.setStrengthNumeratorUnit(unitsMap.get(strengthNumeratorUnitId).getName());
                  }
                  vtmIngredient.setIngredientRule(ingredientRuleEnum);
                  vtmIngredient.setIngredientId(ingredientId);
                  vtmIngredient.setId(medicationIngredientLinkId);
                  vtmMedicationDataDto.getMedicationIngredients().add(vtmIngredient);
                }
              }

              return null;
            });

    // update VTMs with one ingredient to most frequent unit from it's VMPs,
    // use most frequent unit of formulary VMPs, if non exists, use most frequent unit of non-formulary VMPs
    vtmMedicationsResult.getMedicationDataMap().values().forEach(vtm -> {

      final Long vtmId = vtm.getMedication().getId();

      final Collection<String> vmpNumeratorUnits =
          vtmFormularyChildNumeratorUnits.containsKey(vtmId)
          ? vtmFormularyChildNumeratorUnits.get(vtmId)
          : vtmNonFormularyChildNumeratorUnits.get(vtmId);

      final String mostFrequentNumeratorUnit = findMostFrequentItem(vmpNumeratorUnits);
      if (mostFrequentNumeratorUnit != null)
      {
        vtm.getMedicationIngredients().forEach(i -> i.setStrengthNumeratorUnit(mostFrequentNumeratorUnit));
      }
    });
  }

  private <V> V findMostFrequentItem(final Collection<V> items)
  {
    return items.stream()
        .filter(Objects::nonNull)
        .collect(Collectors.groupingBy(Functions.identity(), Collectors.counting())).entrySet().stream()
        .max(Comparator.comparing(Entry::getValue))
        .map(Entry::getKey)
        .orElse(null);
  }

  private void mapRoutesToMedications(
      final ProductMedicationsResult productMedicationsResult,
      final VtmMedicationsResult vtmMedicationsResult,
      final Map<Long, MedicationUnitDto> unitsMap,
      final DateTime when)
  {
    final Alias.Permanent<MedicationRouteImpl> childRoute = Alias.forPermanentEntity(MedicationRouteImpl.class);
    final Alias.Permanent<MedicationRouteRelationImpl> medicationRouteChildRelation = Alias.forPermanentEntity(MedicationRouteRelationImpl.class);

    final Multimap<String, MedicationRouteDto> formularyVtmRoutes = HashMultimap.create();
    final Multimap<String, MedicationRouteDto> allVtmRoutes = HashMultimap.create();

    new Hql()
        .select(
            medication.id(),
            medication.get("medicationLevel"),
            medication.get("vtmId"),

            medicationRoute.id(),
            medicationRoute.get("code"),
            medicationRoute.get("shortName"),
            medicationRoute.get("type"),
            medicationRouteLink.get("unlicensed"),
            medicationRouteLink.get("discretionary"),
            medicationRouteLink.get("maxDose"),
            medicationRouteLink.get("maxDoseUnit").id(),
            medicationRouteLink.get("maxDosePeriod"),
            medicationRouteLink.get("defaultRoute"),

            childRoute.id(),
            childRoute.get("code"),
            childRoute.get("shortName"),
            childRoute.get("type")
        )
        .from(
            medication.innerJoin("medicationBase").as(medicationBase),
            medicationBase.innerJoin("routes").as(medicationRouteLink),
            medicationRouteLink.innerJoin("route").as(medicationRoute),
            medicationRoute.leftOuterJoin("childRelations")
                .as(medicationRouteChildRelation)
                .with(medicationRouteChildRelation.notDeleted()),
            medicationRouteChildRelation.leftOuterJoin("childRoute").as(childRoute).with(childRoute.notDeleted())
        )
        .where(
            effectiveEntities(medicationRouteLink).notDeletedAndEffectiveAt(when)
        )
        .orderBy(medication.id())
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            (resultRow, hasNext) ->
            {
              final Long medicationId = (Long)resultRow[0];
              final MedicationLevelEnum medicationLevelEnum = (MedicationLevelEnum)resultRow[1];
              final String vtmId = (String)resultRow[2];

              final Long routeId = (Long)resultRow[3];
              final String routeCode = (String)resultRow[4];
              final String routeShortName = (String)resultRow[5];
              final MedicationRouteTypeEnum routeType = (MedicationRouteTypeEnum)resultRow[6];

              final Boolean ur = (Boolean)resultRow[7];
              //noinspection TooBroadScope
              final boolean unlicensedRoute = ur != null && ur;

              final Boolean dr = (Boolean)resultRow[8];
              //noinspection TooBroadScope
              final boolean discretionaryRoute = dr != null && dr;

              final Integer maxDose = (Integer)resultRow[9];
              final Long maxDoseUnitId = (Long)resultRow[10];
              final MaxDosePeriod maxDosePeriod = (MaxDosePeriod)resultRow[11];

              final Boolean defaultRoute = (Boolean)resultRow[12];

              final Long childRouteId = (Long)resultRow[13];
              final String childRouteCode = (String)resultRow[14];
              final String childRouteShortName = (String)resultRow[15];
              final MedicationRouteTypeEnum childRouteType = (MedicationRouteTypeEnum)resultRow[16];

              final MedicationDataDto medicationDataDto = productMedicationsResult.getById(medicationId);
              if (medicationDataDto != null)
              {
                final boolean hasChildRoute = childRouteId != null;
                final boolean setAsDefault = hasChildRoute ? false : defaultRoute;

                final MedicationRouteDto routeDto = new MedicationRouteDto();

                if (hasChildRoute)
                {
                  routeDto.setId(childRouteId);
                  routeDto.setCode(childRouteCode);
                  routeDto.setName(childRouteShortName);
                  routeDto.setType(childRouteType);
                }
                else
                {
                  routeDto.setId(routeId);
                  routeDto.setCode(routeCode);
                  routeDto.setName(routeShortName);
                  routeDto.setType(routeType);
                }

                routeDto.setUnlicensedRoute(unlicensedRoute);
                routeDto.setDiscretionary(discretionaryRoute);

                if (maxDose != null)
                {
                  final MaxDoseDto maxDoseDto = new MaxDoseDto();
                  maxDoseDto.setDose(maxDose);
                  Opt.of(maxDoseUnitId).ifPresent(u -> maxDoseDto.setUnit(unitsMap.get(u).getName()));
                  maxDoseDto.setPeriod(maxDosePeriod);
                  routeDto.setMaxDose(maxDoseDto);
                }

                if (!medicationDataDto.getRoutes().contains(routeDto))
                {
                  medicationDataDto.getRoutes().add(routeDto);
                }

                if (setAsDefault)
                {
                  medicationDataDto.setDefaultRoute(routeDto);
                }

                // collect all VTM routes from it's products
                if (MedicationLevelEnum.PRODUCT_LEVELS.contains(medicationLevelEnum))
                {
                  if (vtmMedicationsResult.getByVtmId(vtmId) != null)
                  {
                    addOrUpdateRoute(allVtmRoutes, vtmId, routeDto);
                    if (medicationDataDto.isFormulary())
                    {
                      addOrUpdateRoute(formularyVtmRoutes, vtmId, routeDto);
                    }
                  }
                }
              }
              return null;
            });

    setRoutesToVTMs(vtmMedicationsResult, formularyVtmRoutes, allVtmRoutes);
  }

  private void addOrUpdateRoute(
      final Multimap<String, MedicationRouteDto> routes,
      final String vtmId,
      final MedicationRouteDto route)
  {
    if (!routes.get(vtmId).contains(route)) // add if not yet contained
    {
      routes.put(vtmId, route);
    }
    else if (route.isDiscretionary() || route.isUnlicensedRoute()) // update discretionary and unlicensedRoute
    {
      routes.get(vtmId)
          .stream()
          .filter(r -> r.equals(route)) // match by ID
          .findFirst()
          .ifPresent(r -> {

            if (route.isDiscretionary())
            {
              r.setDiscretionary(true);
            }
            if (route.isUnlicensedRoute())
            {
              r.setUnlicensedRoute(true);
            }
          });
    }
  }

  private void setRoutesToVTMs(
      final VtmMedicationsResult vtmMedicationsResult,
      final Multimap<String, MedicationRouteDto> formularyVtmRoutes,
      final Multimap<String, MedicationRouteDto> allVtmRoutes)
  {
    allVtmRoutes.asMap().keySet().forEach(vtmId -> {

      final Collection<MedicationRouteDto> formularyRoutes = formularyVtmRoutes.get(vtmId);
      final Collection<MedicationRouteDto> allRoutes = allVtmRoutes.get(vtmId);

      final MedicationDataDto vtm = vtmMedicationsResult.getByVtmId(vtmId);

      if (formularyRoutes.isEmpty() || !vtm.isFormulary())
      {
        vtm.setRoutes(new ArrayList<>(allRoutes));
      }
      else
      {
        vtm.setRoutes(new ArrayList<>(formularyRoutes));
      }

    });
  }

  @SuppressWarnings("Duplicates")
  private Multimap<Long, MedicationPropertyDto> loadMedicationProperties(final DateTime when)
  {
    final Multimap<Long, MedicationPropertyDto> propertiesMap = ArrayListMultimap.create();

    new Hql()
        .select(
            medication.id(),
            medicationProperty.id(),
            medicationProperty.get("name"),
            medicationProperty.get("propertyType"),
            medicationPropertyLink.get("value")
        )
        .from(
            medicationPropertyLink.innerJoin("property").as(medicationProperty),
            medicationPropertyLink.innerJoin("medication").as(medication)
        )
        .where(effectiveEntities(medicationPropertyLink).notDeletedAndEffectiveAt(when))
        .orderBy(medicationProperty.get("propertyType"))
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            new TupleProcessor<Void>()
            {
              @Override
              protected Void process(final boolean hasNextTuple) throws ProcessingException
              {
                final Long medicationId = nextLong();
                final Long propertyId = nextLong();
                final String name = nextString();
                final MedicationPropertyType type = MedicationPropertyType.valueOfOrNull(next(String.class));

                propertiesMap.put(medicationId, new MedicationPropertyDto(propertyId, type, name, nextString()));
                return null;
              }
            });

    return propertiesMap;
  }

  @SuppressWarnings("Duplicates")
  private Multimap<Long, MedicationPropertyDto> loadBaseProperties(final DateTime when)
  {
    final Multimap<Long, MedicationPropertyDto> propertiesMap = ArrayListMultimap.create();

    new Hql()
        .select(
            medicationBase.id(),
            medicationProperty.id(), medicationProperty.get("name"),
            medicationProperty.get("propertyType"),
            medicationPropertyLink.get("value")
        )
        .from(
            medicationPropertyLink.innerJoin("property").as(medicationProperty),
            medicationPropertyLink.innerJoin("medicationBase").as(medicationBase)
        )
        .where(effectiveEntities(medicationPropertyLink).notDeletedAndEffectiveAt(when))
        .orderBy(medicationProperty.get("propertyType"))
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            new TupleProcessor<Void>()
            {
              @Override
              protected Void process(final boolean hasNextTuple) throws ProcessingException
              {
                final Long baseId = nextLong();
                final Long propertyId = nextLong();
                final String name = nextString();
                final MedicationPropertyType type = MedicationPropertyType.valueOfOrNull(next(String.class));

                propertiesMap.put(baseId, new MedicationPropertyDto(propertyId, type, name, nextString()));
                return null;
              }
            });

    return propertiesMap;
  }

  @Override
  public Map<Long, String> getMedicationsExternalIds(
      final @NonNull String externalSystem,
      final @NonNull Collection<Long> medicationIds)
  {
    if (medicationIds.isEmpty())
    {
      return Collections.emptyMap();
    }
    final Map<Long, String> medicationsExternalIds = new HashMap<>();
    new Hql()
        .select(
            medicationExternal.get("medication").id(),
            medicationExternal.get("externalId")
        )
        .from(
            medicationExternal
        )
        .where(
            permanentEntities(medicationExternal).notDeleted(),
            medicationExternal.get("externalSystem").eq(externalSystem),
            medicationExternal.get("medication").id().in(medicationIds)
        )
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(new TupleProcessor<Void>()
        {
          @Override
          protected Void process(final boolean hasNextTuple) throws ProcessingException
          {
            medicationsExternalIds.put(nextLong(), nextString());
            return null;
          }
        });
    return medicationsExternalIds;
  }

  @Override
  public DoseFormDto getDoseFormByCode(final String doseFormCode)
  {
    final MedicationDoseFormImpl aDoseForm =
        new Hql().select(doseForm)
            .from(doseForm)
            .where(
                doseForm.get("code").eq(doseFormCode),
                doseForm.notDeleted())
            .buildQuery(sessionFactory.getCurrentSession(), MedicationDoseFormImpl.class).uniqueResult();
    return doseFormDtoMapper.map(aDoseForm);
  }

  @Override
  public Map<String, String> getMedicationExternalValues(
      final String externalSystem, final MedicationsExternalValueType valueType, final Set<String> valuesSet)
  {
    final Map<String, String> valuesMap = new HashMap<>();
    if (valuesSet.isEmpty())
    {
      return valuesMap;
    }

    new Hql()
        .select(
            medicationExternalTranslator.get("value"),
            medicationExternalTranslator.get("externalValue")
        )
        .from(
            medicationExternalTranslator
        )
        .where(
            medicationExternalTranslator.get("externalSystem").eq(externalSystem),
            medicationExternalTranslator.get("valueType").eq(valueType),
            medicationExternalTranslator.get("value").in(valuesSet),
            medicationExternalTranslator.notDeleted()
        )
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            new TupleProcessor<Void>()
            {
              @Override
              protected Void process(final boolean hasNextTuple) throws ProcessingException
              {
                final String value = next();
                final String externalValue = next();
                valuesMap.put(value, externalValue);
                return null;
              }
            });

    return valuesMap;
  }

  @Override
  public List<MedicationRouteDto> getRoutes()
  {
    return new Hql()
        .select(
            medicationRoute.id(),
            medicationRoute.get("code"),
            medicationRoute.get("shortName"),
            medicationRoute.get("type")
        )
        .from(
            medicationRoute.leftOuterJoin("childRelations").as(medicationRouteRelation)
                .with(medicationRouteRelation.notDeleted())
        )
        .where(
            medicationRoute.notDeleted(),
            medicationRouteRelation.id().isNull(),
            medicationRoute.get("shortName").isNotNull()
        )
        .orderBy(medicationRoute.get("sortOrder"))
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            new TupleProcessor<MedicationRouteDto>()
            {
              @Override
              protected MedicationRouteDto process(final boolean hasNextTuple) throws ProcessingException
              {
                final MedicationRouteDto routeDto = new MedicationRouteDto();
                routeDto.setId(nextLong());
                routeDto.setCode(nextString());
                routeDto.setName(nextString());
                routeDto.setType(next(MedicationRouteTypeEnum.class));
                return routeDto;
              }
            });
  }

  @Override
  public List<DoseFormDto> getDoseForms()
  {
    return new Hql()
        .select(
            doseForm)

        .from(
            doseForm)

        .where(
            doseForm.notDeleted()
        )
        .buildQuery(sessionFactory.getCurrentSession(), MedicationDoseFormImpl.class)
        .list((resultRow, hasNext) -> doseFormDtoMapper.map(resultRow));
  }

  @Override
  public List<InformationSourceDto> getInformationSources()
  {
    return new Hql()
        .select(informationSource.get("id"),
                informationSource.get("name"),
                informationSource.get("informationSourceType"),
                informationSource.get("informationSourceGroup"))
        .from(informationSource)
        .where(informationSource.notDeleted())
        .orderBy(informationSource.get("code"))
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            new TupleProcessor<InformationSourceDto>()
            {
              @Override
              protected InformationSourceDto process(final boolean hasNextTuple) throws ProcessingException
              {
                final InformationSourceDto sourceOfInformation = new InformationSourceDto();
                sourceOfInformation.setId(nextLong());
                sourceOfInformation.setName(nextString());
                sourceOfInformation.setInformationSourceType(next(InformationSourceTypeEnum.class));
                sourceOfInformation.setInformationSourceGroup(next(InformationSourceGroupEnum.class));
                return sourceOfInformation;
              }
            });
  }

  @Override
  public Map<Long, Pair<String, Integer>> getCustomGroupNameSortOrderMap(
      final String careProviderId,
      final Collection<Long> medicationIds)
  {
    if (medicationIds.isEmpty())
    {
      return new HashMap<>();
    }
    final Map<Long, Pair<String, Integer>> customGroupNameSortOrderMap = new HashMap<>();
    new Hql()
        .select(
            medicationCustomGroup.get("name"),
            medicationCustomGroup.get("sortOrder"),
            medication.id()
        )
        .from(
            medication.innerJoin("customGroupMembers").as(medicationCustomGroupMember),
            medicationCustomGroupMember.innerJoin("medicationCustomGroup").as(medicationCustomGroup)
        )
        .where(
            medication.id().in(medicationIds),
            medicationCustomGroup.get("careProviderId").eq(careProviderId)
        )
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            new TupleProcessor<Void>()
            {
              @Override
              protected Void process(final boolean hasNextTuple) throws ProcessingException
              {
                final Pair<String, Integer> nameSortOrderPair = Pair.of(nextString(), next(Integer.class));
                customGroupNameSortOrderMap.put(nextLong(), nameSortOrderPair);
                return null;
              }
            }
        );
    return customGroupNameSortOrderMap;
  }

  @Override
  public List<String> getCustomGroupNames(final @NonNull String careProviderId)
  {
    return new Hql()
        .select(
            medicationCustomGroup.get("name"),
            medicationCustomGroup.get("sortOrder")
        )
        .distinct()
        .from(
            medicationCustomGroupMember.innerJoin("medicationCustomGroup").as(medicationCustomGroup)
        )
        .where(
            medicationCustomGroup.get("careProviderId").eq(careProviderId)
        )
        .orderBy(medicationCustomGroup.get("sortOrder"))
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            new TupleProcessor<String>()
            {
              @Override
              protected String process(final boolean hasNextTuple) throws ProcessingException
              {
                return next();
              }
            }
        );
  }

  @Override
  public String getPatientLastLinkName(final long patientId)
  {
    return new Hql()
        .select(
            patientTherapyLastLinkName.get("lastLinkName")
        )
        .from(
            patientTherapyLastLinkName
        )
        .where(
            patientTherapyLastLinkName.get("patientId").eq(patientId),
            patientTherapyLastLinkName.notDeleted()
        )
        .buildQuery(sessionFactory.getCurrentSession(), String.class)
        .getSingleRowOrNull();
  }

  @Override
  public void savePatientLastLinkName(final long patientId, final String lastLinkName)
  {
    PatientTherapyLastLinkNameImpl lastLink =
        new Hql()
            .select(
                patientTherapyLastLinkName
            )
            .from(
                patientTherapyLastLinkName
            )
            .where(
                patientTherapyLastLinkName.get("patientId").eq(patientId),
                patientTherapyLastLinkName.notDeleted()
            )
            .buildQuery(sessionFactory.getCurrentSession(), PatientTherapyLastLinkNameImpl.class)
            .getSingleRowOrNull();

    if (lastLink == null)
    {
      lastLink = new PatientTherapyLastLinkNameImpl();
      lastLink.setPatientId(patientId);
    }
    lastLink.setLastLinkName(lastLinkName);
    sessionFactory.getCurrentSession().saveOrUpdate(lastLink);
  }

  @Override
  public Map<ActionReasonType, List<CodedNameDto>> getActionReasons(
      final @NonNull DateTime when,
      final ActionReasonType type)
  {
    //noinspection MapReplaceableByEnumMap
    final Map<ActionReasonType, List<CodedNameDto>> actionReasonMap = new HashMap<>();
    //noinspection ConstantConditions
    new Hql()
        .select(
            actionReason.id(),
            actionReason.get("name"),
            actionReason.get("reasonType")
        )
        .from(
            actionReason
        )
        .where(
            effectiveEntities(actionReason).notDeletedAndEffectiveAt(when),
            or(type == null, actionReason.get("reasonType").eq(type))
        )
        .orderBy(actionReason.get("code"))
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            new TupleProcessor<Void>()
            {
              @Override
              protected Void process(final boolean hasNextTuple) throws ProcessingException
              {
                final Long id = nextLong();
                final String name = nextString();

                final CodedNameDto dto = new CodedNameDto(String.valueOf(id), name);
                final ActionReasonType reasonType = next();

                if (actionReasonMap.containsKey(reasonType))
                {
                  actionReasonMap.get(reasonType).add(dto);
                }
                else
                {
                  final List<CodedNameDto> dtoList = new ArrayList<>();
                  dtoList.add(dto);
                  actionReasonMap.put(reasonType, dtoList);
                }
                return null;
              }
            });

    return actionReasonMap;
  }

  @Override
  public Map<Long, MedicationRouteDto> loadRoutesMap()
  {
    final Map<Long, MedicationRouteDto> routesMap = new HashMap<>();
    new Hql()
        .select(
            medicationRoute.id(),
            medicationRoute.get("code"),
            medicationRoute.get("shortName"),
            medicationRoute.get("type")
        )
        .from(
            medicationRoute)

        .where(
            medicationRoute.notDeleted()
        )
        .orderBy(medicationRoute.get("sortOrder"))
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            new TupleProcessor<MedicationRouteDto>()
            {
              @Override
              protected MedicationRouteDto process(final boolean hasNextTuple) throws ProcessingException
              {
                final MedicationRouteDto routeDto = new MedicationRouteDto();
                final Long routeId = next();
                routeDto.setId(routeId);
                routeDto.setCode(nextString());
                routeDto.setName(nextString());
                routeDto.setType(next());
                routesMap.put(routeId, routeDto);
                return routeDto;
              }
            })
    ;
    return routesMap;
  }

  @Override
  public List<MedicationsWarningDto> getCustomWarningsForMedication(
      final @NonNull Set<Long> medicationIds,
      final @NonNull DateTime when)
  {
    if (medicationIds.isEmpty())
    {
      return Collections.emptyList();
    }

    final List<MedicationsWarningDto> warnings = new ArrayList<>();
    warnings.addAll(loadBaseWarnings(medicationIds, when));
    warnings.addAll(loadMedicationWarnings(medicationIds, when));
    return warnings;
  }

  private List<MedicationsWarningDto> loadBaseWarnings(final Set<Long> medicationIds, final DateTime when)
  {
    return new Hql()
        .select(
            medicationWarning.get("description"),
            medicationWarning.get("severity"),
            medication.id(),
            medicationVersion.get("name")
        )
        .from(
            medication.innerJoin("versions").as(medicationVersion),
            medication.innerJoin("medicationBase").as(medicationBase),
            medicationBase.innerJoin("warnings").as(medicationWarning)
        )
        .where(
            medication.id().in(medicationIds),
            effectiveEntities(medicationVersion, medicationWarning).notDeletedAndEffectiveAt(when)
        )
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            new TupleProcessor<MedicationsWarningDto>()
            {
              @SuppressWarnings("Duplicates")
              @Override
              protected MedicationsWarningDto process(final boolean hasNextTuple) throws ProcessingException
              {
                final String description = nextString();
                final WarningSeverity severity = next(WarningSeverity.class);
                final Long medicationId = nextLong();
                final String medicationName = nextString();

                return new MedicationsWarningDto(
                    description,
                    severity,
                    WarningType.CUSTOM,
                    Collections.singletonList(new NamedExternalDto(String.valueOf(medicationId), medicationName)));
              }
            });
  }

  private List<MedicationsWarningDto> loadMedicationWarnings(final Set<Long> medicationIds, final DateTime when)
  {
    return new Hql()
        .select(
            medicationWarning.get("description"),
            medicationWarning.get("severity"),
            medication.id(),
            medicationVersion.get("name")
        )
        .from(
            medication.innerJoin("versions").as(medicationVersion),
            medication.innerJoin("warnings").as(medicationWarning)
        )
        .where(
            medication.id().in(medicationIds),
            effectiveEntities(medicationVersion, medicationWarning).notDeletedAndEffectiveAt(when)
        )
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            new TupleProcessor<MedicationsWarningDto>()
            {
              @SuppressWarnings("Duplicates")
              @Override
              protected MedicationsWarningDto process(final boolean hasNextTuple) throws ProcessingException
              {
                final String description = nextString();
                final WarningSeverity severity = next(WarningSeverity.class);
                final Long medicationId = nextLong();
                final String medicationName = nextString();

                return new MedicationsWarningDto(
                    description,
                    severity,
                    WarningType.CUSTOM,
                    Collections.singletonList(new NamedExternalDto(String.valueOf(medicationId), medicationName)));
              }
            });
  }

  @Override
  public Long getMedicationIdForBarcode(final @NonNull String barcode)
  {
    return new Hql()
        .select(medication.id())
        .from(medication)
        .where(
            medication.get("barcode").eq(barcode),
            medication.notDeleted()
        )
        .buildQuery(sessionFactory.getCurrentSession(), Long.class)
        .getSingleRowOrNull();
  }

  @Override
  public List<DispenseSourceDto> getDispenseSources()
  {
    return new Hql()
        .select(
            dispenseSource.id(),
            dispenseSource.get("name"),
            dispenseSource.get("defaultSource")
        )
        .from(dispenseSource)
        .where(
            dispenseSource.notDeleted()
        )
        .orderBy(dispenseSource.get("code"))
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            new TupleProcessor<DispenseSourceDto>()
            {
              @Override
              protected DispenseSourceDto process(final boolean hasNextTuple) throws ProcessingException
              {
                return new DispenseSourceDto(nextLong(), nextString(), Opt.of(nextBoolean()).orElse(false));
              }
            });
  }

  private static class VtmMedicationsResult
  {
    private final Map<String, Long> vtmIdMedicationIdMap;
    private final Map<Long, MedicationDataDto> medicationDataMap;

    private VtmMedicationsResult(
        final Map<String, Long> vtmIdMedicationIdMap,
        final Map<Long, MedicationDataDto> medicationDataMap)
    {
      this.vtmIdMedicationIdMap = vtmIdMedicationIdMap;
      this.medicationDataMap = medicationDataMap;
    }

    public Map<Long, MedicationDataDto> getMedicationDataMap()
    {
      //noinspection AssignmentOrReturnOfFieldWithMutableType
      return medicationDataMap;
    }

    public MedicationDataDto getByVtmId(final String vtmId)
    {
      return medicationDataMap.get(vtmIdMedicationIdMap.get(vtmId));
    }
  }

  private static class ProductMedicationsResult
  {
    private final Map<Long, MedicationDataDto> medicationDataMap;

    private ProductMedicationsResult(final Map<Long, MedicationDataDto> medicationDataMap)
    {
      this.medicationDataMap = medicationDataMap;
    }

    public Map<Long, MedicationDataDto> getMedicationDataMap()
    {
      //noinspection AssignmentOrReturnOfFieldWithMutableType
      return medicationDataMap;
    }

    public MedicationDataDto getById(final long id)
    {
      return medicationDataMap.get(id);
    }
  }
}
