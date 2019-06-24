package com.marand.thinkmed.medications.dao.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.data.IdentityDto;
import com.marand.maf.core.data.object.NamedIdentityDto;
import com.marand.maf.core.hibernate.query.Alias;
import com.marand.maf.core.hibernate.query.Criterion;
import com.marand.maf.core.hibernate.query.Hql;
import com.marand.maf.core.resultrow.ProcessingException;
import com.marand.maf.core.resultrow.TupleProcessor;
import com.marand.maf.core.resultrow.TwoLevelJoinProcessor;
import com.marand.thinkmed.medications.MedsJsonDeserializer;
import com.marand.thinkmed.medications.TherapyTemplateModeEnum;
import com.marand.thinkmed.medications.TherapyTemplatePreconditionEnum;
import com.marand.thinkmed.medications.TherapyTemplateTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.dao.TherapyTemplateDao;
import com.marand.thinkmed.medications.dto.ValidationIssueEnum;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTemplateDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTemplateMemberDto;
import com.marand.thinkmed.medications.dto.template.CustomTemplatesGroupDto;
import com.marand.thinkmed.medications.dto.template.TherapyTemplateDto;
import com.marand.thinkmed.medications.dto.template.TherapyTemplateElementDto;
import com.marand.thinkmed.medications.dto.template.TherapyTemplatePreconditionDto;
import com.marand.thinkmed.medications.dto.template.TherapyTemplateStatus;
import com.marand.thinkmed.medications.dto.template.TherapyTemplatesDto;
import com.marand.thinkmed.medications.model.impl.MedicationImpl;
import com.marand.thinkmed.medications.model.impl.MedicationRouteImpl;
import com.marand.thinkmed.medications.model.impl.MentalHealthTemplateImpl;
import com.marand.thinkmed.medications.model.impl.MentalHealthTemplateMemberImpl;
import com.marand.thinkmed.medications.model.impl.TherapyTemplateElementImpl;
import com.marand.thinkmed.medications.model.impl.TherapyTemplateGroupImpl;
import com.marand.thinkmed.medications.model.impl.TherapyTemplateImpl;
import com.marand.thinkmed.medications.model.impl.TherapyTemplatePreconditionImpl;
import com.marand.thinkmed.medications.template.TemplateValidationManager;
import com.marand.thinkmed.request.time.RequestDateTimeHolder;
import lombok.NonNull;
import org.hibernate.SessionFactory;
import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.Years;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.marand.maf.core.hibernate.query.Alias.permanentEntities;
import static com.marand.maf.core.hibernate.query.Criterion.and;
import static com.marand.maf.core.hibernate.query.Criterion.or;

/**
 * @author Mitja Lapajne
 */

@Component
public class HibernateTherapyTemplateDao implements TherapyTemplateDao
{
  private static final Alias.Permanent<MedicationImpl> medication = Alias.forPermanentEntity(MedicationImpl.class);
  private static final Alias.Permanent<MedicationRouteImpl> medicationRoute = Alias.forPermanentEntity(MedicationRouteImpl.class);
  private static final Alias.Permanent<MentalHealthTemplateImpl> mentalHealthTemplate = Alias.forPermanentEntity(
      MentalHealthTemplateImpl.class);
  private static final Alias.Permanent<TherapyTemplateImpl> therapyTemplate = Alias.forPermanentEntity(TherapyTemplateImpl.class);
  private static final Alias.Permanent<MentalHealthTemplateMemberImpl> mentalHealthTemplateMember = Alias.forPermanentEntity(
      MentalHealthTemplateMemberImpl.class);
  private static final Alias.Permanent<TherapyTemplateElementImpl> therapyTemplateElement = Alias.forPermanentEntity(
      TherapyTemplateElementImpl.class);
  private static final Alias.Permanent<TherapyTemplatePreconditionImpl> therapyTemplatePrecondition = Alias.forPermanentEntity(
      TherapyTemplatePreconditionImpl.class);
  private static final Alias.Permanent<TherapyTemplateGroupImpl> therapyTemplateGroup = Alias.forPermanentEntity(
      TherapyTemplateGroupImpl.class);

  private MedicationsBo medicationsBo;
  private TherapyDisplayProvider therapyDisplayProvider;
  private SessionFactory sessionFactory;
  private RequestDateTimeHolder requestDateTimeHolder;
  private TemplateValidationManager templateValidationManager;

  @Autowired
  public void setSessionFactory(final SessionFactory sessionFactory)
  {
    this.sessionFactory = sessionFactory;
  }

  @Autowired
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Autowired
  public void setTherapyDisplayProvider(final TherapyDisplayProvider therapyDisplayProvider)
  {
    this.therapyDisplayProvider = therapyDisplayProvider;
  }

  @Autowired
  public void setRequestDateTimeHolder(final RequestDateTimeHolder requestDateTimeHolder)
  {
    this.requestDateTimeHolder = requestDateTimeHolder;
  }

  @Autowired
  public void setTemplateValidationManager(final TemplateValidationManager templateValidationManager)
  {
    this.templateValidationManager = templateValidationManager;
  }

  @Override
  public TherapyTemplatesDto getTherapyTemplates(
      final @NonNull TherapyTemplateModeEnum templateMode,
      final @NonNull Collection<String> templateGroups,
      final String patientId,
      final String userId,
      final String careProviderId,
      final Double referenceWeight,
      final Double patientHeightInCm,
      final DateTime birthDate,
      final @NonNull Locale locale)
  {
    final List<TherapyTemplateDto> filteredTemplates = getTherapyTemplatesList(
        templateMode,
        patientId,
        userId,
        templateGroups,
        careProviderId,
        referenceWeight,
        patientHeightInCm,
        birthDate,
        locale);

    return buildTherapyTemplatesDto(filteredTemplates);
  }

  private TherapyTemplatesDto buildTherapyTemplatesDto(final List<TherapyTemplateDto> filteredTemplates)
  {
    final TherapyTemplatesDto allTemplatesDto = new TherapyTemplatesDto();
    final Map<String, List<TherapyTemplateDto>> groupedTemplatesMap = new HashMap<>();

    for (final TherapyTemplateDto template : filteredTemplates)
    {
      if (template.getType() == TherapyTemplateTypeEnum.ORGANIZATIONAL)
      {
        allTemplatesDto.getOrganizationTemplates().add(template);
      }
      else if (template.getType() == TherapyTemplateTypeEnum.USER)
      {
        allTemplatesDto.getUserTemplates().add(template);
      }
      else if (template.getType() == TherapyTemplateTypeEnum.PATIENT)
      {
        allTemplatesDto.getPatientTemplates().add(template);
      }
      else if (template.getType() == TherapyTemplateTypeEnum.CUSTOM_GROUP)
      {
        groupedTemplatesMap.computeIfAbsent(template.getGroup(), t -> new ArrayList<>()).add(template);
      }
      else
      {
        throw new UnsupportedOperationException("Template type " + template.getType() + " not supported");
      }
    }

    groupedTemplatesMap
        .forEach((key, value) -> allTemplatesDto.getCustomTemplateGroups().add(new CustomTemplatesGroupDto(key, value)));
    return allTemplatesDto;
  }

  @Override
  public TherapyTemplatesDto  getAllTherapyTemplates(
      final @NonNull TherapyTemplateModeEnum templateMode,
      final @NonNull String userId,
      final String careProviderId,
      final @NonNull Locale locale)
  {
    final Criterion additionalCriterion = or(
        therapyTemplate.get("type").eq(TherapyTemplateTypeEnum.CUSTOM_GROUP),
        and(
            therapyTemplate.get("type").eq(TherapyTemplateTypeEnum.USER),
            therapyTemplate.get("userId").eq(userId)),
        and(
            careProviderId != null,
            therapyTemplate.get("type").eq(TherapyTemplateTypeEnum.ORGANIZATIONAL),
            therapyTemplate.get("careProviderId").eq(careProviderId))
    );

    final List<TherapyTemplateDto> templates = queryTemplates(templateMode, additionalCriterion);

    final Set<Long> templateIds = templates.stream()
        .map(IdentityDto::getId)
        .collect(Collectors.toSet());

    final Map<Long, List<TherapyTemplateElementDto>> elementsMap = queryTemplateElements(
        templateIds,
        null,
        null,
        locale);

    templates
        .forEach(t -> t.setTemplateElements(elementsMap.get(t.getId())));

    return buildTherapyTemplatesDto(templates);
  }

  private List<TherapyTemplateDto> getTherapyTemplatesList(
      final TherapyTemplateModeEnum templateMode,
      final String patientId,
      final String userId,
      final Collection<String> templateGroups,
      final String careProviderId,
      final Double referenceWeight,
      final Double patientHeightInCm,
      final DateTime birthDate,
      final Locale locale)
  {
    final Criterion additionalCriterion = or(
        and(patientId != null,
            therapyTemplate.get("type").eq(TherapyTemplateTypeEnum.PATIENT),
            therapyTemplate.get("patientId").eq(patientId)),
        and(
            userId != null,
            therapyTemplate.get("type").eq(TherapyTemplateTypeEnum.USER),
            therapyTemplate.get("userId").eq(userId)),
        and(
            careProviderId != null,
            therapyTemplate.get("type").eq(TherapyTemplateTypeEnum.ORGANIZATIONAL),
            therapyTemplate.get("careProviderId").eq(careProviderId)),
        and(
            therapyTemplate.get("type").eq(TherapyTemplateTypeEnum.CUSTOM_GROUP),
            therapyTemplateGroup.get("name").in(templateGroups))
    );

    final List<TherapyTemplateDto> templates = queryTemplates(templateMode, additionalCriterion);

    final List<TherapyTemplateDto> filteredTemplates = templates.stream()
        .filter(tt -> areTemplatePreconditionsMet(
            tt.getPreconditions(),
            referenceWeight,
            patientHeightInCm,
            birthDate,
            requestDateTimeHolder.getRequestTimestamp()))
        .collect(Collectors.toList());

    final Set<Long> templateIds = filteredTemplates.stream()
        .map(IdentityDto::getId)
        .collect(Collectors.toSet());

    final Map<Long, List<TherapyTemplateElementDto>> elementsMap = queryTemplateElements(
        templateIds,
        referenceWeight,
        patientHeightInCm,
        locale);

    filteredTemplates
        .forEach(t -> t.setTemplateElements(elementsMap.get(t.getId())));
    return filteredTemplates;
  }

  private boolean areTemplatePreconditionsMet(
      final Collection<TherapyTemplatePreconditionDto> preconditions,
      final Double referenceWeight,
      final Double patientHeightInCm,
      final DateTime birthDate,
      final DateTime when)
  {
    for (final TherapyTemplatePreconditionDto precondition : preconditions)
    {
      if (precondition.getPrecondition() == TherapyTemplatePreconditionEnum.AGE_IN_MONTHS)
      {
        if (birthDate == null ||
            !precondition.isRangePreconditionMet((double)Months.monthsBetween(birthDate, when).getMonths()))
        {
          return false;
        }
      }
      else if (precondition.getPrecondition() == TherapyTemplatePreconditionEnum.AGE_IN_YEARS)
      {
        if (birthDate == null ||
            !precondition.isRangePreconditionMet((double)Years.yearsBetween(birthDate, when).getYears()))
        {
          return false;
        }
      }
      else if (precondition.getPrecondition() == TherapyTemplatePreconditionEnum.WEIGHT)
      {
        if (referenceWeight == null || !precondition.isRangePreconditionMet(referenceWeight))
        {
          return false;
        }
      }
      else if (precondition.getPrecondition() == TherapyTemplatePreconditionEnum.BODY_SURFACE)
      {
        if (referenceWeight == null ||
            patientHeightInCm == null ||
            !precondition.isRangePreconditionMet(medicationsBo.calculateBodySurfaceArea(patientHeightInCm, referenceWeight)))
        {
          return false;
        }
      }
      else
      {
        throw new UnsupportedOperationException("Precondition " + precondition.getPrecondition() + " not supported");
      }
    }

    return true;
  }

  private List<TherapyTemplateDto> queryTemplates(
      final TherapyTemplateModeEnum templateMode,
      final Criterion additionalCriterion)
  {
    final List<TherapyTemplateDto> templates = new ArrayList<>();
    new Hql()
        .select(
            therapyTemplate.id(),
            therapyTemplate.get("version"),
            therapyTemplate.get("name"),
            therapyTemplate.get("type"),
            therapyTemplate.get("userId"),
            therapyTemplate.get("careProviderId"),
            therapyTemplate.get("patientId"),
            therapyTemplateGroup.get("name"),
            therapyTemplatePrecondition.id(),
            therapyTemplatePrecondition.get("precondition"),
            therapyTemplatePrecondition.get("minValue"),
            therapyTemplatePrecondition.get("maxValue"),
            therapyTemplatePrecondition.get("exactValue")
        )
        .from(
            therapyTemplate.leftOuterJoin("preconditions").as(therapyTemplatePrecondition)
                .with(therapyTemplatePrecondition.notDeleted()),
            therapyTemplate.leftOuterJoin("templateGroup").as(therapyTemplateGroup)
        )
        .where(
            therapyTemplate.get("templateMode").eq(templateMode),
            additionalCriterion,
            permanentEntities(therapyTemplate).notDeleted()
        )
        .orderBy(therapyTemplate.get("name"), therapyTemplate.id(), therapyTemplatePrecondition.id())
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            new TwoLevelJoinProcessor.ToList<TherapyTemplateDto, TherapyTemplatePreconditionDto>()
            {
              @Override
              protected TherapyTemplateDto mapParent()
              {
                final TherapyTemplateDto templateDto = new TherapyTemplateDto();
                templateDto.setId(nextLong());
                templateDto.setVersion(next(Integer.class));
                templateDto.setName(nextString());
                templateDto.setType(next(TherapyTemplateTypeEnum.class));
                templateDto.setUserId(nextString());
                templateDto.setCareProviderId(nextString());
                templateDto.setPatientId(nextString());
                templateDto.setGroup(nextString());

                templates.add(templateDto);
                return templateDto;
              }

              @Override
              protected TherapyTemplatePreconditionDto mapChild()
              {
                nextLong();
                final TherapyTemplatePreconditionDto preconditionDto = new TherapyTemplatePreconditionDto();
                preconditionDto.setPrecondition(next(TherapyTemplatePreconditionEnum.class));
                preconditionDto.setMinValue(nextDouble());
                preconditionDto.setMaxValue(nextDouble());
                preconditionDto.setExactValue(nextString());

                return preconditionDto;
              }

              @Override
              protected void associate(final TherapyTemplateDto parentDto, final TherapyTemplatePreconditionDto childDto)
              {
                parentDto.getPreconditions().add(childDto);
              }
            });

    return templates;
  }

  private Map<Long, List<TherapyTemplateElementDto>> queryTemplateElements(
      final Set<Long> templateIds,
      final Double referenceWeight,
      final Double patientHeight,
      final Locale locale)
  {

    final Map<Long, List<TherapyTemplateElementDto>> templateElementsMap = new HashMap<>();
    new Hql()
        .select(
            therapyTemplateElement.id(),
            therapyTemplateElement.get("therapyTemplate").id(),
            therapyTemplateElement.get("therapy"),
            therapyTemplateElement.get("completed"),
            therapyTemplateElement.get("recordAdministration")
        )
        .from(
            therapyTemplateElement
        )
        .where(
            therapyTemplateElement.get("therapyTemplate").id().in(templateIds),
            therapyTemplateElement.notDeleted())
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            new TupleProcessor<Void>()
            {
              @Override
              protected Void process(final boolean hasNextTuple) throws ProcessingException
              {
                final TherapyTemplateElementDto templateElementDto = new TherapyTemplateElementDto();

                final Long therapyTemplateElementId = next();
                final Long therapyTemplateId = next();
                final String therapyJson = next();
                final Boolean completed = next(Boolean.class);
                final Boolean recordAdministration = next(Boolean.class);

                final TherapyDto therapyDto =
                    JsonUtil.fromJson(therapyJson, TherapyDto.class, MedsJsonDeserializer.INSTANCE.getTypeAdapters());

                templateElementDto.setId(therapyTemplateElementId);

                if (therapyDto instanceof ComplexTherapyDto && referenceWeight != null)
                {
                  if (((ComplexTherapyDto)therapyDto).isContinuousInfusion())
                  {
                    medicationsBo.fillInfusionRateFromFormula((ComplexTherapyDto)therapyDto, referenceWeight, patientHeight);
                  }
                  else
                  {
                    medicationsBo.fillInfusionFormulaFromRate((ComplexTherapyDto)therapyDto, referenceWeight, patientHeight);
                  }
                }

                //old templates were saved without routeId
                if (therapyDto.getRoutes() != null)
                {
                  therapyDto.getRoutes()
                      .stream()
                      .filter(medicationRouteDto -> medicationRouteDto.getId() == 0)
                      .forEach(medicationRouteDto -> medicationRouteDto.setId(Long.parseLong(medicationRouteDto.getCode())));
                }

                //in old templates solutions had only volume without quantity
                if (therapyDto instanceof ComplexTherapyDto)
                {
                  for (final InfusionIngredientDto ingredient : ((ComplexTherapyDto)therapyDto).getIngredientsList())
                  {
                    if (ingredient.getQuantity() == null && ingredient.getQuantityDenominator() != null)
                    {
                      ingredient.setQuantity(ingredient.getQuantityDenominator());
                      ingredient.setQuantityUnit(ingredient.getQuantityDenominatorUnit());
                      ingredient.setQuantityDenominator(null);
                    }
                  }
                }

                therapyDisplayProvider.fillDisplayValues(therapyDto, true, false, true, locale, false);
                templateElementDto.setTherapy(therapyDto);

                if (completed)
                {
                  templateElementDto.setStatus(TherapyTemplateStatus.COMPLETE);
                }
                else
                {
                  templateElementDto.getValidationIssues().add(ValidationIssueEnum.INCOMPLETE);
                  templateElementDto.setStatus(TherapyTemplateStatus.INCOMPLETE);
                }
                templateElementDto.setRecordAdministration(recordAdministration != null && recordAdministration);

                // clear units if not valid
                templateValidationManager.clearUnitsIfNotValid(templateElementDto);

                templateElementsMap.computeIfAbsent(therapyTemplateId, t -> new ArrayList<>()).add(templateElementDto);

                return null;
              }
            });

    templateElementsMap.values()
        .forEach(tt -> tt.sort(Comparator.comparing(tte -> tte.getTherapy().getTherapyDescription())));

    return templateElementsMap;
  }

  @Override
  public long saveTherapyTemplate(
      final @NonNull TherapyTemplateDto templateDto,
      final @NonNull TherapyTemplateModeEnum templateMode)
  {
    if (templateDto.getId() > 0L)
    {
      deleteTherapyTemplate(templateDto.getId(), templateDto.getVersion());
    }

    final TherapyTemplateImpl template = new TherapyTemplateImpl();
    template.setName(templateDto.getName());
    template.setType(templateDto.getType());
    template.setTemplateMode(templateMode);

    if (templateDto.getType() == TherapyTemplateTypeEnum.USER)
    {
      Preconditions.checkNotNull(templateDto.getUserId(), "templateDto.userId is required for USER template type");
      template.setUserId(templateDto.getUserId());
    }
    else if (templateDto.getType() == TherapyTemplateTypeEnum.ORGANIZATIONAL)
    {
      Preconditions.checkNotNull(
          templateDto.getCareProviderId(),
          "templateDto.careProviderId is required for ORGANIZATIONAL template type");

      template.setCareProviderId(templateDto.getCareProviderId());
    }
    else if (templateDto.getType() == TherapyTemplateTypeEnum.PATIENT)
    {
      Preconditions.checkNotNull(templateDto.getPatientId(), "templateDto.patientId is required for PATIENT template type");

      template.setPatientId(templateDto.getPatientId());
    }
    else if (templateDto.getType() == TherapyTemplateTypeEnum.CUSTOM_GROUP)
    {
      Preconditions.checkNotNull(templateDto.getGroup(), "templateDto.group is required for CUSTOM_GROUP template type");

      final TherapyTemplateGroupImpl group = getTherapyTemplateGroup(templateDto);
      Preconditions.checkNotNull(group, "group with name " + templateDto.getGroup() + " does not exist");

      template.setTemplateGroup(group);
    }

    sessionFactory.getCurrentSession().save(template);

    for (final TherapyTemplateElementDto templateElementDto : templateDto.getTemplateElements())
    {
      final TherapyTemplateElementImpl element = new TherapyTemplateElementImpl();
      final TherapyDto therapyDto = templateElementDto.getTherapy();
      element.setTherapy(JsonUtil.toJson(therapyDto));
      element.setCompleted(templateElementDto.getStatus() == TherapyTemplateStatus.COMPLETE);
      element.setRecordAdministration(templateElementDto.doRecordAdministration());
      element.setTherapyTemplate(template);
      sessionFactory.getCurrentSession().save(element);
      template.getTherapyTemplateElements().add(element);
    }

    for (final TherapyTemplatePreconditionDto preconditionDto : templateDto.getPreconditions())
    {
      final TherapyTemplatePreconditionImpl precondition = new TherapyTemplatePreconditionImpl();
      precondition.setTherapyTemplate(template);
      precondition.setPrecondition(preconditionDto.getPrecondition());
      precondition.setMinValue(preconditionDto.getMinValue());
      precondition.setMaxValue(preconditionDto.getMaxValue());
      precondition.setExactValue(preconditionDto.getExactValue());
      sessionFactory.getCurrentSession().save(precondition);
      template.getPreconditions().add(precondition);
    }

    return template.getId();
  }

  private TherapyTemplateGroupImpl getTherapyTemplateGroup(final TherapyTemplateDto templateDto)
  {
    return new Hql()
        .select(
            therapyTemplateGroup
        )
        .from(
            therapyTemplateGroup
        )
        .where(
            therapyTemplateGroup.get("name").eq(templateDto.getGroup()),
            therapyTemplateGroup.notDeleted()
        )
        .buildQuery(sessionFactory.getCurrentSession(), TherapyTemplateGroupImpl.class)
        .getSingleRowOrNull();
  }

  @Override
  public void deleteTherapyTemplate(final long templateId)
  {
    deleteTherapyTemplate(templateId, null);
  }

  @Override
  public void deleteTherapyTemplateGroup(final long templateId)
  {
    final TherapyTemplateGroupImpl templateGroup = sessionFactory.getCurrentSession().load(TherapyTemplateGroupImpl.class,
                                                                                           templateId);
    templateGroup.setDeleted(true);
  }

  @Override
  public void addTherapyTemplateGroup(final @NonNull String name, final @NonNull TherapyTemplateModeEnum templateMode)
  {
    final TherapyTemplateGroupImpl therapyTemplatGroup = new TherapyTemplateGroupImpl();

    therapyTemplatGroup.setName(name);
    therapyTemplatGroup.setTemplateMode(templateMode);

    sessionFactory.getCurrentSession().save(therapyTemplatGroup);
  }

  private void deleteTherapyTemplate(final long templateId, final Integer templateVersion)
  {
    final TherapyTemplateImpl template = sessionFactory.getCurrentSession().load(TherapyTemplateImpl.class, templateId);
    if (templateVersion != null)
    {
      template.setVersion(templateVersion);
    }
    template.setDeleted(true);

    template.getTherapyTemplateElements()
        .forEach(e -> e.setDeleted(true));

    template.getPreconditions()
        .forEach(p -> p.setDeleted(true));
  }

  @Override
  public List<String> getTherapyTemplateGroups(final @NonNull TherapyTemplateModeEnum templateMode)
  {
    return new Hql()
        .select(
            therapyTemplateGroup.get("name")
        )
        .from(
            therapyTemplateGroup
        )
        .where(
            therapyTemplateGroup.get("templateMode").eq(templateMode),
            therapyTemplateGroup.notDeleted()
        )
        .orderBy(therapyTemplateGroup.get("code"))
        .buildQuery(sessionFactory.getCurrentSession(), String.class)
        .list();
  }

  @Override
  public List<MentalHealthTemplateDto> getMentalHealthTemplates()
  {
    return new Hql()
        .select(
            mentalHealthTemplate.id(),
            mentalHealthTemplate.get("name"),
            medicationRoute.id(),
            medicationRoute.get("code"),
            medicationRoute.get("shortName"),
            medicationRoute.get("type")
        )
        .from(
            mentalHealthTemplate,
            mentalHealthTemplate.leftOuterJoin("medicationRoute").as(medicationRoute)
        )
        .where(mentalHealthTemplate.notDeleted())
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            new TupleProcessor<MentalHealthTemplateDto>()
            {
              @Override
              protected MentalHealthTemplateDto process(final boolean hasNextTuple) throws ProcessingException
              {
                final Long id = nextLong();
                final String name = nextString();
                final Long routeId = nextLong();
                final String routeCode = nextString();
                final String routeName = nextString();
                final MedicationRouteTypeEnum routeTypeEnum = next(MedicationRouteTypeEnum.class);

                MedicationRouteDto route = null;
                if (routeId != null)
                {
                  route = new MedicationRouteDto();
                  route.setId(routeId);
                  route.setCode(routeCode);
                  route.setName(routeName);
                  route.setType(routeTypeEnum);
                }

                return new MentalHealthTemplateDto(id, name, route);
              }
            });
  }

  @Override
  public List<MentalHealthTemplateMemberDto> getMentalHealthTemplateMembers(final @NonNull Collection<Long> mentalHealthTemplateIds)
  {
    return new Hql()
        .select(
            mentalHealthTemplateMember.id(),
            mentalHealthTemplate.id(),
            mentalHealthTemplate.get("name"),
            medication.id(),
            medicationRoute.id(),
            medicationRoute.get("code"),
            medicationRoute.get("shortName"),
            medicationRoute.get("type")
        )
        .from(
            mentalHealthTemplate.innerJoin("mentalHealthTemplateMemberList").as(mentalHealthTemplateMember),
            mentalHealthTemplate.leftOuterJoin("medicationRoute").as(medicationRoute),
            mentalHealthTemplateMember.innerJoin("medication").as(medication)
        )
        .where(
            mentalHealthTemplateMember.notDeleted(),
            mentalHealthTemplate.notDeleted(),
            mentalHealthTemplate.id().in(mentalHealthTemplateIds)
        )

        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            new TupleProcessor<MentalHealthTemplateMemberDto>()
            {
              @Override
              protected MentalHealthTemplateMemberDto process(final boolean hasNextTuple) throws ProcessingException
              {
                final Long id = nextLong();
                final Long templateId = nextLong();
                final String templateName = nextString();
                final Long medicationId = nextLong();
                final Long routeId = nextLong();
                final String routeCode = nextString();
                final String routeName = nextString();
                final MedicationRouteTypeEnum routeTypeEnum = next(MedicationRouteTypeEnum.class);

                final NamedIdentityDto template = new NamedIdentityDto();
                template.setName(templateName);
                template.setId(templateId);

                if (routeId != null)
                {
                  final MedicationRouteDto medicationRouteDto = new MedicationRouteDto();
                  medicationRouteDto.setId(routeId);
                  medicationRouteDto.setCode(routeCode);
                  medicationRouteDto.setName(routeName);
                  medicationRouteDto.setType(routeTypeEnum);

                  return new MentalHealthTemplateMemberDto(id, medicationId, medicationRouteDto, template);
                }
                return new MentalHealthTemplateMemberDto(id, medicationId, null, template);
              }
            });
  }
}
