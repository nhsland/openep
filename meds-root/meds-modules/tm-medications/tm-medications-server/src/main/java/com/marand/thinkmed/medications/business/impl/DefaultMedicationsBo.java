package com.marand.thinkmed.medications.business.impl;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.maf.core.time.DateTimeFormatters;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.administration.AdministrationProvider;
import com.marand.thinkmed.medications.api.internal.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.DocumentationTherapiesDto;
import com.marand.thinkmed.medications.api.internal.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.ComplexDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.PrescribingDoseDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.data.TherapyDocumentationData;
import com.marand.thinkmed.medications.business.data.TherapyLinkType;
import com.marand.thinkmed.medications.business.data.TherapySimilarityType;
import com.marand.thinkmed.medications.business.mapper.MedicationDataDtoMapper;
import com.marand.thinkmed.medications.config.MedsProperties;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dao.openehr.ReconciliationOpenEhrDao;
import com.marand.thinkmed.medications.dto.InfusionRateCalculationDto;
import com.marand.thinkmed.medications.dto.MedicationActionEnum;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.MedicationDataForTherapyDto;
import com.marand.thinkmed.medications.dto.RateFormulaUnitsDo;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationResultEnum;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthMedicationDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTherapyDto;
import com.marand.thinkmed.medications.dto.unit.KnownUnitType;
import com.marand.thinkmed.medications.dto.utils.TherapyTimingUtils;
import com.marand.thinkmed.medications.ehr.model.BodyWeight;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import com.marand.thinkmed.medications.ehr.model.MedicationOnDischarge;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.model.ReferenceWeight;
import com.marand.thinkmed.medications.ehr.model.composition.EhrComposition;
import com.marand.thinkmed.medications.ehr.utils.EhrContextVisitor;
import com.marand.thinkmed.medications.ehr.utils.LinksEhrUtils;
import com.marand.thinkmed.medications.ehr.utils.MedicationsEhrUtils;
import com.marand.thinkmed.medications.ehr.utils.PrescriptionsEhrUtils;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.therapy.converter.TherapyConverter;
import com.marand.thinkmed.medications.therapy.ehr.TherapyEhrHandler;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import com.marand.thinkmed.medications.units.converter.UnitsConverter;
import com.marand.thinkmed.medications.units.provider.UnitsProvider;
import com.marand.thinkmed.medications.valueholder.MedicationRoutesValueHolder;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolder;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider;
import com.marand.thinkmed.request.user.RequestUser;
import lombok.NonNull;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.Link;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.marand.thinkmed.medications.dto.unit.KnownUnitType.H;
import static com.marand.thinkmed.medications.dto.unit.KnownUnitType.KG;
import static com.marand.thinkmed.medications.dto.unit.KnownUnitType.M2;
import static com.marand.thinkmed.medications.dto.unit.KnownUnitType.ML;

/**
 * @author Mitja Lapajne
 */

@Component
public class DefaultMedicationsBo implements MedicationsBo
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private ReconciliationOpenEhrDao reconciliationOpenEhrDao;

  private TherapyDisplayProvider therapyDisplayProvider;
  private MedicationsValueHolder medicationsValueHolder;
  private MedicationRoutesValueHolder medicationRoutesValueHolder;
  private MedicationDataDtoMapper medicationDataDtoMapper;
  private MedicationsTasksProvider medicationsTasksProvider;
  private AdministrationProvider administrationProvider;
  private MedicationsValueHolderProvider medicationsValueHolderProvider;
  private TherapyConverter therapyConverter;
  private UnitsConverter unitsConverter;
  private UnitsProvider unitsProvider;
  private TherapyEhrHandler therapyEhrHandler;
  private MedsProperties medsProperties;

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Autowired
  public void setReconciliationOpenEhrDao(final ReconciliationOpenEhrDao reconciliationOpenEhrDao)
  {
    this.reconciliationOpenEhrDao = reconciliationOpenEhrDao;
  }

  @Autowired
  public void setTherapyDisplayProvider(final TherapyDisplayProvider therapyDisplayProvider)
  {
    this.therapyDisplayProvider = therapyDisplayProvider;
  }

  @Autowired
  public void setMedicationsValueHolder(final MedicationsValueHolder medicationsValueHolder)
  {
    this.medicationsValueHolder = medicationsValueHolder;
  }

  @Autowired
  public void setMedicationRoutesValueHolder(final MedicationRoutesValueHolder medicationRoutesValueHolder)
  {
    this.medicationRoutesValueHolder = medicationRoutesValueHolder;
  }

  @Autowired
  public void setMedicationDataDtoMapper(final MedicationDataDtoMapper medicationDataDtoMapper)
  {
    this.medicationDataDtoMapper = medicationDataDtoMapper;
  }

  @Autowired
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Autowired
  public void setAdministrationProvider(final AdministrationProvider administrationProvider)
  {
    this.administrationProvider = administrationProvider;
  }

  @Autowired
  public void setMedicationsValueHolderProvider(final MedicationsValueHolderProvider medicationsValueHolderProvider)
  {
    this.medicationsValueHolderProvider = medicationsValueHolderProvider;
  }

  @Autowired
  public void setTherapyConverter(final TherapyConverter therapyConverter)
  {
    this.therapyConverter = therapyConverter;
  }

  @Autowired
  public void setUnitsConverter(final UnitsConverter unitsConverter)
  {
    this.unitsConverter = unitsConverter;
  }

  @Autowired
  public void setUnitsProvider(final UnitsProvider unitsProvider)
  {
    this.unitsProvider = unitsProvider;
  }

  @Autowired
  public void setTherapyEhrHandler(final TherapyEhrHandler therapyEhrHandler)
  {
    this.therapyEhrHandler = therapyEhrHandler;
  }

  @Autowired
  public void setMedsProperties(final MedsProperties medsProperties)
  {
    this.medsProperties = medsProperties;
  }

  @Override
  public Map<Long, MedicationDataForTherapyDto> getMedicationDataForInpatientPrescriptions(
      final @NonNull List<InpatientPrescription> inpatientPrescriptions,
      final String careProviderId)
  {
    final Set<Long> medicationIds = new HashSet<>();

    for (final InpatientPrescription inpatientPrescription : inpatientPrescriptions)
    {
      medicationIds.addAll(MedicationsEhrUtils.getMedicationIds(inpatientPrescription.getMedicationOrder()));
    }
    final Map<Long, MedicationDataForTherapyDto> medicationsMap = new HashMap<>();
    for (final Long medicationId : medicationIds)
    {
      final MedicationDataDto dataDto = medicationsValueHolder.getMedications().get(medicationId);
      medicationsMap.put(medicationId, medicationDataDtoMapper.mapToMedicationDataForTherapyDto(dataDto, careProviderId));
    }
    return medicationsMap;
  }

  @Override
  public int compareTherapiesForSort(final TherapyDto firstTherapy, final TherapyDto secondTherapy, final Collator collator)
  {
    final boolean firstTherapyIsBaselineInfusion =
        firstTherapy instanceof ComplexTherapyDto && ((ComplexTherapyDto)firstTherapy).isBaselineInfusion();
    final boolean secondTherapyIsBaselineInfusion =
        secondTherapy instanceof ComplexTherapyDto && ((ComplexTherapyDto)secondTherapy).isBaselineInfusion();

    if (firstTherapyIsBaselineInfusion && !secondTherapyIsBaselineInfusion)
    {
      return -1;
    }
    if (!firstTherapyIsBaselineInfusion && secondTherapyIsBaselineInfusion)
    {
      return 1;
    }
    return collator.compare(firstTherapy.getTherapyDescription(), secondTherapy.getTherapyDescription());
  }

  @Override
  public boolean areInpatientPrescriptionsLinkedByUpdate(
      final InpatientPrescription inpatientPrescription,
      final InpatientPrescription compareInpatientPrescription)
  {
    if (doesLinkExist(
        inpatientPrescription,
        compareInpatientPrescription,
        EhrLinkType.UPDATE))
    {
      return true;
    }
    return doesLinkExist(
        compareInpatientPrescription,
        inpatientPrescription,
        EhrLinkType.UPDATE);
  }

  private boolean doesLinkExist(
      final InpatientPrescription inpatientPrescription,
      final InpatientPrescription compareInpatientPrescription,
      final EhrLinkType linkType)
  {
    return inpatientPrescription.getLinks()
        .stream()
        .anyMatch(l -> LinksEhrUtils.isLinkToComposition(compareInpatientPrescription.getUid(), l, linkType));
  }

  boolean areTherapiesSimilar(
      final InpatientPrescription inpatientPrescription,
      final InpatientPrescription compareInpatientPrescription,
      final Map<Long, MedicationDataForTherapyDto> medicationsMap,
      final boolean canOverlap)
  {
    final MedicationOrder medicationOrder = inpatientPrescription.getMedicationOrder();
    final MedicationOrder compareMedicationOrder = compareInpatientPrescription.getMedicationOrder();

    final boolean therapyIsAdHocMixture = MedicationsEhrUtils.isAdHocMixture(medicationOrder.getPreparationDetails());
    final Interval therapyInterval = MedicationsEhrUtils.getMedicationOrderInterval(medicationOrder);
    final Interval therapyOrderInterval = MedicationsEhrUtils.getMedicationOrderInterval(compareMedicationOrder);
    if (canOverlap || !therapyInterval.overlaps(therapyOrderInterval))
    {
      final boolean compareTherapyIsAdHocMixture = MedicationsEhrUtils.isAdHocMixture(compareMedicationOrder.getPreparationDetails());
      if (!therapyIsAdHocMixture && !compareTherapyIsAdHocMixture)
      {
        return isSimilarSimpleTherapy(medicationOrder, compareMedicationOrder, medicationsMap);
      }
    }
    return false;
  }

  private boolean isSimilarSimpleTherapy(
      final MedicationOrder medicationOrder,
      final MedicationOrder compareMedicationOrder,
      final Map<Long, MedicationDataForTherapyDto> medicationsMap)
  {
    //therapy
    final List<DvCodedText> routes = medicationOrder.getRoute();
    final Long medicationId =
        medicationOrder.getMedicationItem() instanceof DvCodedText
        ? Long.valueOf(((DvCodedText)medicationOrder.getMedicationItem()).getDefiningCode().getCodeString())
        : null;
    final String medicationName =
        medicationOrder.getMedicationItem() != null ?
        medicationOrder.getMedicationItem().getValue() :
        null;

    //compare therapy
    final List<DvCodedText> compareRoutes = compareMedicationOrder.getRoute();
    final Long compareMedicationId =
        compareMedicationOrder.getMedicationItem() instanceof DvCodedText
        ? Long.valueOf(((DvCodedText)compareMedicationOrder.getMedicationItem()).getDefiningCode().getCodeString())
        : null;
    final String compareMedicationName =
        compareMedicationOrder.getMedicationItem() != null ?
        compareMedicationOrder.getMedicationItem().getValue() :
        null;

    final boolean similarMedication =
        isSimilarMedication(medicationId, medicationName, compareMedicationId, compareMedicationName, medicationsMap);
    final boolean sameRoute = CollectionUtils.containsAny(routes, compareRoutes);

    return similarMedication && sameRoute;
  }

  private boolean isSimilarMedication(
      final Long medicationId,
      final String medicationName,
      final Long compareMedicationId,
      final String compareMedicationName,
      final Map<Long, MedicationDataForTherapyDto> medicationsMap)
  {
    final MedicationDataForTherapyDto medicationData = medicationId != null ? medicationsMap.get(medicationId) : null;
    final MedicationDataForTherapyDto compareMedicationData =
        compareMedicationId != null ? medicationsMap.get(compareMedicationId) : null;

    final boolean sameGeneric = isSameGeneric(medicationData, compareMedicationData);
    final boolean sameName = medicationName != null && medicationName.equals(compareMedicationName);

    final boolean sameAtc = isSameAtc(medicationData, compareMedicationData);
    final boolean sameCustomGroup = isSameOrNoCustomGroup(medicationData, compareMedicationData);

    return (sameGeneric || sameName) && sameAtc && sameCustomGroup;
  }

  private boolean isSameGeneric(
      final MedicationDataForTherapyDto medication, final MedicationDataForTherapyDto compareMedication)
  {
    return medication != null && compareMedication != null &&
        medication.getGenericName() != null && compareMedication.getGenericName() != null &&
        medication.getGenericName().equals(compareMedication.getGenericName());
  }

  private boolean isSameAtc(
      final MedicationDataForTherapyDto medication, final MedicationDataForTherapyDto compareMedication)
  {
    return medication != null && compareMedication != null &&
        ((medication.getAtcGroupCode() == null && compareMedication.getAtcGroupCode() == null) ||
            medication.getAtcGroupCode() != null && compareMedication.getAtcGroupCode() != null &&
                medication.getAtcGroupCode().equals(compareMedication.getAtcGroupCode()));
  }

  private boolean isSameOrNoCustomGroup(
      final MedicationDataForTherapyDto medication, final MedicationDataForTherapyDto compareMedication)
  {
    return medication != null && compareMedication != null &&
        ((medication.getCustomGroupName() == null && compareMedication.getCustomGroupName() == null) ||
            (medication.getCustomGroupName() != null && compareMedication.getCustomGroupName() != null &&
                medication.getCustomGroupName().equals(compareMedication.getCustomGroupName()))
        );
  }

  private boolean isOnlyOnceThenEx(final MedicationOrder medicationOrder)
  {
    final Opt<Long> maxAdministrations = Opt.resolve(
        () -> medicationOrder.getStructuredDoseAndTimingDirections()
            .get(0)
            .getMaximumNumberOfAdministration()
            .getMagnitude());
    return maxAdministrations.isPresent() && maxAdministrations.get() == 1;
  }

  @Override
  public boolean isMentalHealthMedication(final long medicationId)
  {
    final Map<Long, MedicationDataDto> value = medicationsValueHolder.getMedications();
    final MedicationDataDto dataDto = value.get(medicationId);

    return dataDto != null && dataDto.isMentalHealthDrug();
  }

  @Override
  public boolean isTherapyActive(
      final List<String> daysOfWeek,
      final Integer dosingDaysFrequency,
      final Interval therapyInterval,
      final DateTime when)
  {
    if (therapyInterval.overlap(Intervals.wholeDay(when)) == null)
    {
      return false;
    }
    if (daysOfWeek != null && !daysOfWeek.isEmpty())
    {
      boolean activeDay = false;
      final String searchDay = TherapyTimingUtils.dayOfWeekToEhrEnum(when).name();
      for (final String day : daysOfWeek)
      {
        if (day.equals(searchDay))
        {
          activeDay = true;
        }
      }
      if (!activeDay)
      {
        return false;
      }
    }
    if (dosingDaysFrequency != null)
    {
      final int daysFromStart =
          Days.daysBetween(therapyInterval.getStart().withTimeAtStartOfDay(), when.withTimeAtStartOfDay()).getDays();
      if (daysFromStart % dosingDaysFrequency != 0)
      {
        return false;
      }
    }
    return true;
  }

  @Override
  public String getTherapyFormattedDisplay(
      final String patientId, final String therapyId, final Locale locale)
  {
    final TherapyDto therapy = getTherapy(patientId, therapyId, locale);
    return therapy.getFormattedTherapyDisplay();
  }

  @Override
  public TherapyDto getTherapy(
      final String patientId,
      final String compositionId,
      final String ehrOrderName,
      final Locale locale)
  {
    final InpatientPrescription inpatientPrescription = medicationsOpenEhrDao.loadInpatientPrescription(
        patientId,
        compositionId);
    return convertMedicationOrderToTherapyDto(
        inpatientPrescription,
        inpatientPrescription.getMedicationOrder(),
        null,
        null,
        true,
        locale);
  }

  private TherapyDto getTherapy(
      final String patientId, final String therapyId, final Locale locale)
  {
    final Pair<String, String> compositionIdAndInstructionName = TherapyIdUtils.parseTherapyId(therapyId);
    final InpatientPrescription inpatientPrescription =
        medicationsOpenEhrDao.loadInpatientPrescription(patientId, compositionIdAndInstructionName.getFirst());
    return convertMedicationOrderToTherapyDto(
        inpatientPrescription,
        inpatientPrescription.getMedicationOrder(),
        null,
        null,
        true,
        locale);
  }

  @Override
  public TherapyDto convertMedicationOrderToTherapyDto(
      final @NonNull EhrComposition ehrComposition,
      final @NonNull MedicationOrder medicationOrder)
  {
    final TherapyDto therapyDto = therapyConverter.convertToTherapyDto(
        medicationOrder,
        ehrComposition.getUid(),
        DataValueUtils.getDateTime(ehrComposition.getContext().getStartTime()));

    therapyDto.setComposerName(ehrComposition.getComposer().getName());
    therapyDto.setPrescriberName(ehrComposition.getComposer().getName());

    therapyDto.setAdmissionId(LinksEhrUtils.getLinkedCompositionUid(
        ehrComposition.getLinks(),
        EhrLinkType.MEDICATION_ON_ADMISSION));

    return therapyDto;
  }

  @Override
  public TherapyDto convertMedicationOrderToTherapyDto(
      final @NonNull EhrComposition ehrComposition,
      final @NonNull MedicationOrder medicationOrder,
      final Double referenceWeight,
      final Double patientHeight,
      final boolean isToday,
      final Locale locale)
  {
    final TherapyDto therapyDto = convertMedicationOrderToTherapyDto(ehrComposition, medicationOrder);

    if (therapyDto instanceof ComplexTherapyDto && referenceWeight != null)
    {
      fillInfusionFormulaFromRate((ComplexTherapyDto)therapyDto, referenceWeight, patientHeight);
    }

    fillDisplayValues(therapyDto, referenceWeight, patientHeight, isToday, locale);
    return therapyDto;
  }

  @Override
  public void fillDisplayValues(
      final @NonNull TherapyDto therapy,
      final Double referenceWeight,
      final Double patientHeight,
      final boolean isToday,
      final Locale locale)
  {
    if (locale != null)
    {
      therapyDisplayProvider.fillDisplayValues(therapy, isToday, locale);
    }
  }

  @Override
  public void fillInfusionFormulaFromRate(
      final ComplexTherapyDto therapy,
      final Double referenceWeight,
      final Double patientHeight)
  {
    final InfusionRateCalculationDto calculationData = getInfusionRateCalculationData(therapy);
    if (calculationData != null && calculationData.getQuantity() != null && calculationData.getQuantityDenominator() != null)
    {
      if (therapy instanceof ConstantComplexTherapyDto)
      {
        final ConstantComplexTherapyDto constantTherapy = (ConstantComplexTherapyDto)therapy;
        final ComplexDoseElementDto doseElement = constantTherapy.getDoseElement();
        if (doseElement != null && doseElement.getRate() != null && doseElement.getRateFormulaUnit() != null)
        {
          final Double formula = calculateInfusionFormulaFromRate(
              doseElement.getRate(),
              calculationData,
              patientHeight,
              doseElement.getRateFormulaUnit(),
              referenceWeight);
          doseElement.setRateFormula(formula);
        }
      }
      else
      {
        final VariableComplexTherapyDto variableTherapy = (VariableComplexTherapyDto)therapy;
        for (final TimedComplexDoseElementDto timedDoseElement : variableTherapy.getTimedDoseElements())
        {
          if (timedDoseElement.getDoseElement() != null &&
              timedDoseElement.getDoseElement().getRate() != null &&
              timedDoseElement.getDoseElement().getRateFormulaUnit() != null)
          {
            final Double formula = calculateInfusionFormulaFromRate(
                timedDoseElement.getDoseElement().getRate(),
                calculationData,
                patientHeight,
                timedDoseElement.getDoseElement().getRateFormulaUnit(),
                referenceWeight);
            timedDoseElement.getDoseElement().setRateFormula(formula);
          }
        }
      }
    }
  }

  Double calculateInfusionFormulaFromRate(
      final Double rate,
      final InfusionRateCalculationDto calculationDto,
      final Double patientHeight,
      final String formulaUnit,
      final Double referenceWeight)
  {
    final RateFormulaUnitsDo rateFormulaUnitsDo = buildRateFormulaUnitDo(formulaUnit);

    final Double rateWithPatientUnit = getRateWithPatientUnits(
        rate,
        patientHeight,
        referenceWeight,
        rateFormulaUnitsDo.getPatientUnitKnownType());

    final Double rateInMassUnit = rateWithPatientUnit * calculationDto.getQuantity() / calculationDto.getQuantityDenominator();

    if (!unitsConverter.isConvertible(calculationDto.getQuantityUnit(), rateFormulaUnitsDo.getMassUnitName()))
    {
      return null;
    }

    final double rateInFormulaMassUnit = unitsConverter.convert(
        rateInMassUnit,
        calculationDto.getQuantityUnit(),
        rateFormulaUnitsDo.getMassUnitName());

    final double timeRatio = unitsConverter.convert(1.0, H, rateFormulaUnitsDo.getTimeUnitKnownType());
    return rateInFormulaMassUnit / timeRatio;
  }

  private RateFormulaUnitsDo buildRateFormulaUnitDo(final String rateFormulaUnit)
  {
    final RateFormulaUnitsDo rateFormulaUnitsDo = new RateFormulaUnitsDo();
    if (rateFormulaUnit == null)
    {
      return rateFormulaUnitsDo;
    }

    final String[] formulaUnitParts = Pattern.compile("/").split(rateFormulaUnit);

    rateFormulaUnitsDo.setMassUnitName(formulaUnitParts[0]);

    if (formulaUnitParts.length == 2)
    {
      rateFormulaUnitsDo.setTimeUnitKnownType(
          unitsProvider
              .findKnownUnitByDisplayName(formulaUnitParts[1])
              .orElseThrow(() -> new IllegalArgumentException("Formula time unit must be set as known unit type!")));
    }
    else
    {
      rateFormulaUnitsDo.setPatientUnitKnownType(
          unitsProvider
              .findKnownUnitByDisplayName(formulaUnitParts[1])
              .orElseThrow(() -> new IllegalArgumentException("Formula time unit must be set as known unit type!")));

      rateFormulaUnitsDo.setTimeUnitKnownType(
          unitsProvider
              .findKnownUnitByDisplayName(formulaUnitParts[2])
              .orElseThrow(() -> new IllegalArgumentException("Formula time unit must be set as known unit type!")));
    }

    return rateFormulaUnitsDo;
  }

  private Double getRateWithPatientUnits(
      final Double rate,
      final Double patientHeight,
      final Double referenceWeight,
      final KnownUnitType formulaPatientKnownUnit)
  {
    final Double rateWithPatientUnit;
    final Boolean isKGKnownUnit = Opt.of(formulaPatientKnownUnit).map(k -> k == KG).orElse(false);
    final Boolean isM2KnownUnit = Opt.of(formulaPatientKnownUnit).map(k -> k == M2).orElse(false);

    if (isKGKnownUnit)
    {
      rateWithPatientUnit = rate / referenceWeight;  // ml/kg/h
    }
    else if (isM2KnownUnit && patientHeight != null)
    {
      final Double bodySurface = calculateBodySurfaceArea(patientHeight, referenceWeight);
      rateWithPatientUnit = rate / bodySurface;
    }
    else
    {
      rateWithPatientUnit = rate;
    }
    return rateWithPatientUnit;
  }

  @Override
  public void fillInfusionRateFromFormula(
      final ComplexTherapyDto therapy, final Double referenceWeight, final Double patientHeight)
  {
    final InfusionRateCalculationDto calculationData = getInfusionRateCalculationData(therapy);
    if (calculationData != null && calculationData.getQuantity() != null && calculationData.getQuantityDenominator() != null)
    {
      if (therapy instanceof ConstantComplexTherapyDto)
      {
        final ConstantComplexTherapyDto constantTherapy = (ConstantComplexTherapyDto)therapy;
        final ComplexDoseElementDto doseElement = constantTherapy.getDoseElement();
        if (doseElement != null && doseElement.getRateFormula() != null && doseElement.getRateFormulaUnit() != null)
        {
          final Double rate = calculateInfusionRateFromFormula(
              doseElement.getRateFormula(),
              doseElement.getRateFormulaUnit(),
              calculationData,
              referenceWeight,
              patientHeight);

          if (rate != null)
          {
            doseElement.setRate(rate);
          }
        }
      }
      else
      {
        final VariableComplexTherapyDto variableTherapy = (VariableComplexTherapyDto)therapy;
        for (final TimedComplexDoseElementDto timedDoseElement : variableTherapy.getTimedDoseElements())
        {
          if (timedDoseElement.getDoseElement() != null &&
              timedDoseElement.getDoseElement().getRateFormula() != null &&
              timedDoseElement.getDoseElement().getRateFormulaUnit() != null)
          {
            final Double rate = calculateInfusionRateFromFormula(
                timedDoseElement.getDoseElement().getRateFormula(),
                timedDoseElement.getDoseElement().getRateFormulaUnit(),
                calculationData,
                referenceWeight,
                patientHeight);

            if (rate != null)
            {
              timedDoseElement.getDoseElement().setRate(rate);
            }
          }
        }
      }
    }
  }

  Double calculateInfusionRateFromFormula(
      final Double formula,
      final String formulaUnit,
      final InfusionRateCalculationDto calculationDto,
      final Double referenceWeight,
      final Double patientHeight)
  {
    final RateFormulaUnitsDo rateFormulaUnitsDo = buildRateFormulaUnitDo(formulaUnit);

    final Boolean patientUnitKG = Opt.of(rateFormulaUnitsDo.getPatientUnitKnownType()).map(k -> k == KG).orElse(false);
    final Boolean patientUnitM2 = Opt.of(rateFormulaUnitsDo.getPatientUnitKnownType()).map(k -> k == M2).orElse(false);

    final Double formulaWithoutPatientUnit;
    if (patientUnitKG)
    {
      formulaWithoutPatientUnit = formula * referenceWeight;  // ug/min
    }
    else if (patientUnitM2 && patientHeight != null)
    {
      final Double bodySurfaceArea = calculateBodySurfaceArea(patientHeight, referenceWeight);
      formulaWithoutPatientUnit = formula * bodySurfaceArea;
    }
    else
    {
      formulaWithoutPatientUnit = formula;
    }

    if (!unitsConverter.isConvertible(calculationDto.getQuantityUnit(), rateFormulaUnitsDo.getMassUnitName()))
    {
      return null;
    }

    final double formulaInRateMassUnit = unitsConverter.convert(
        formulaWithoutPatientUnit,
        rateFormulaUnitsDo.getMassUnitName(),
        calculationDto.getQuantityUnit());

    final double timeRatio = unitsConverter.convert(1.0, rateFormulaUnitsDo.getTimeUnitKnownType(), H);
    final double formulaInHours = formulaInRateMassUnit / timeRatio;  // mg/min
    return formulaInHours * calculationDto.getQuantityDenominator() / calculationDto.getQuantity(); // ml/h
  }

  @Override
  public double calculateBodySurfaceArea(final double heightInCm, final double weightInKg)
  {
    return Math.sqrt((heightInCm * weightInKg) / 3600.0);
  }

  InfusionRateCalculationDto getInfusionRateCalculationData(final ComplexTherapyDto therapy)
  {
    if (therapy.getIngredientsList().size() == 1)
    {
      final InfusionIngredientDto onlyInfusionIngredient = therapy.getIngredientsList().get(0);
      if (MedicationTypeEnum.MEDICATION == onlyInfusionIngredient.getMedication().getMedicationType())
      {
        if (therapy.isContinuousInfusion() && onlyInfusionIngredient.getMedication().getId() != null)
        {
          final PrescribingDoseDto prescribingDose = medicationsValueHolderProvider
              .getMedicationData(onlyInfusionIngredient.getMedication().getId())
              .getPrescribingDose();

          if (prescribingDose != null
              && prescribingDose.getDenominatorUnit() != null
              && unitsConverter.isKnownUnit(prescribingDose.getDenominatorUnit(), ML))
          {
            final InfusionRateCalculationDto calculationDto = new InfusionRateCalculationDto();
            calculationDto.setQuantity(prescribingDose.getNumerator());
            calculationDto.setQuantityUnit(prescribingDose.getNumeratorUnit());
            calculationDto.setQuantityDenominator(prescribingDose.getDenominator());
            return calculationDto;
          }
        }
        else
        {
          final InfusionRateCalculationDto calculationDto = new InfusionRateCalculationDto();
          calculationDto.setQuantity(onlyInfusionIngredient.getQuantity());
          calculationDto.setQuantityUnit(onlyInfusionIngredient.getQuantityUnit());
          calculationDto.setQuantityDenominator(onlyInfusionIngredient.getQuantityDenominator());
          return calculationDto;
        }
      }
    }
    else
    {
      InfusionRateCalculationDto calculationDto = null;
      for (final InfusionIngredientDto ingredient : therapy.getIngredientsList())
      {
        final MedicationTypeEnum medicationType = ingredient.getMedication().getMedicationType();
        if (MedicationTypeEnum.MEDICATION == medicationType)
        {
          if (calculationDto != null)    //more than one medication on supplement
          {
            return null;
          }
          calculationDto = new InfusionRateCalculationDto();
          calculationDto.setQuantity(ingredient.getQuantity());
          calculationDto.setQuantityUnit(ingredient.getQuantityUnit());
          calculationDto.setQuantityDenominator(therapy.getVolumeSum());
        }
      }
      return calculationDto;
    }
    return null;
  }

  @Override
  public List<TherapyDto> getTherapies(
      final String patientId,
      final Interval searchInterval,
      final Double referenceWeight,
      final Double patientHeight,
      @Nullable final Locale locale)
  {
    final List<InpatientPrescription> inpatientPrescriptions = medicationsOpenEhrDao.findInpatientPrescriptions(
        patientId,
        searchInterval);
    return convertInpatientPrescriptionsToTherapies(inpatientPrescriptions, referenceWeight, patientHeight, locale);
  }

  @Override
  public List<MentalHealthTherapyDto> getMentalHealthTherapies(
      final String patientId,
      final Interval searchInterval,
      final DateTime when,
      final Locale locale)
  {
    final List<InpatientPrescription> inpatientPrescriptions = medicationsOpenEhrDao.findInpatientPrescriptions(
        patientId,
        searchInterval);

    final List<MentalHealthTherapyDto> mentalHealthTherapyDtos = extractMentalHealthTherapiesList(inpatientPrescriptions);
    final Set<MentalHealthTherapyDto> filteredMentalHealthTherapyList = filterMentalHealthTherapyList(mentalHealthTherapyDtos);

    return new ArrayList(filteredMentalHealthTherapyList);
  }

  @Override
  public List<TherapyDto> getLinkTherapyCandidates(
      final @NonNull String patientId,
      final Double referenceWeight,
      final Double patientHeight,
      final @NonNull DateTime when,
      final @NonNull Locale locale)
  {
    final List<InpatientPrescription> continuousInfusions =
        medicationsOpenEhrDao.findInpatientPrescriptions(patientId, Intervals.infiniteFrom(when))
            .stream()
            .filter(i -> MedicationsEhrUtils.isContinuousInfusion(i.getMedicationOrder()))
            .collect(Collectors.toList());

    final Set<String> followedCompositionIds = continuousInfusions
        .stream()
        .filter(i -> !PrescriptionsEhrUtils.isInpatientPrescriptionCancelledOrAborted(i))
        .filter(this::hasFollowLink)
        .map(this::getFollowLinkCompositionUid)
        .collect(Collectors.toSet());

    final List<InpatientPrescription> followCandidates =
        continuousInfusions
            .stream()
            .filter(i -> !PrescriptionsEhrUtils.isInpatientPrescriptionCancelledOrAborted(i))
            .filter(this::hasTherapyEnd)
            .filter(i -> !followedCompositionIds.contains(
                TherapyIdUtils.getCompositionUidWithoutVersion(i.getUid())))
            .collect(Collectors.toList());

    return convertInpatientPrescriptionsToTherapies(followCandidates, referenceWeight, patientHeight, locale);
  }

  private boolean hasFollowLink(final InpatientPrescription inpatientPrescription)
  {
    return !LinksEhrUtils.getLinksOfType(inpatientPrescription.getLinks(), EhrLinkType.FOLLOW).isEmpty();
  }

  private String getFollowLinkCompositionUid(final InpatientPrescription inpatientPrescription)
  {
    final List<Link> followLinks = LinksEhrUtils.getLinksOfType(inpatientPrescription.getLinks(), EhrLinkType.FOLLOW);

    if (followLinks.isEmpty())
    {
      return null;
    }
    else
    {
      final String targetCompositionId = LinksEhrUtils.getTargetCompositionIdFromLink(followLinks.get(0));
      return TherapyIdUtils.getCompositionUidWithoutVersion(targetCompositionId);
    }
  }

  private boolean hasTherapyEnd(final InpatientPrescription inpatientPrescription)
  {
    return inpatientPrescription.getMedicationOrder().getOrderDetails().getOrderStopDateTime() != null;
  }

  Set<MentalHealthTherapyDto> filterMentalHealthTherapyList(final List<MentalHealthTherapyDto> mentalHealthTherapyDtos)
  {
    final Set<MentalHealthTherapyDto> removedDuplicates = new TreeSet<>((o1, o2) -> {
      final int statusCompare =  o1.getTherapyStatusEnum().compareTo(o2.getTherapyStatusEnum());
      if (statusCompare == 0)
      {
        final Long o1Id = o1.getMentalHealthMedicationDto().getId();
        final Long o2Id = o2.getMentalHealthMedicationDto().getId();

        final int idCompare = o1Id.compareTo(o2Id);

        final Long o1RouteId = o1.getMentalHealthMedicationDto().getRoute().getId();
        final Long o2RouteId = o2.getMentalHealthMedicationDto().getRoute().getId();

        return idCompare == 0 ? o1RouteId.compareTo(o2RouteId) : idCompare;
      }
      else
      {
        return statusCompare;
      }
    });

    removedDuplicates.addAll(mentalHealthTherapyDtos);
    return removedDuplicates;
  }

  @Override
  public List<TherapyDto> convertInpatientPrescriptionsToTherapies(
      final @NonNull List<InpatientPrescription> inpatientPrescriptions,
      final Double referenceWeight,
      final Double patientHeight,
      final Locale locale)
  {
    final List<TherapyDto> therapiesList = new ArrayList<>();
    for (final InpatientPrescription inpatientPrescription : inpatientPrescriptions)
    {
      final List<MedicationManagement> actions = inpatientPrescription.getActions();
      final boolean therapyEndedWithModify = isTherapyCompletedWithModify(actions);
      if (!therapyEndedWithModify)
      {
        final TherapyDto therapyDto = convertMedicationOrderToTherapyDto(
            inpatientPrescription,
            inpatientPrescription.getMedicationOrder(),
            referenceWeight,
            patientHeight,
            true,
            locale);
        therapiesList.add(therapyDto);
      }
    }

    return therapiesList;
  }

  List<MentalHealthTherapyDto> extractMentalHealthTherapiesList(
      final List<InpatientPrescription> inpatientPrescriptions)
  {
    final List<MentalHealthTherapyDto> mentalHealthTherapyDtos = new ArrayList<>();
    for (final InpatientPrescription inpatientPrescription : inpatientPrescriptions)
    {
      final List<Long> medicationIds = MedicationsEhrUtils.getMedicationIds(inpatientPrescription.getMedicationOrder());

      mentalHealthTherapyDtos.addAll(
          medicationIds.stream()
              .filter(this::isMentalHealthMedication)
              .map(medicationId -> buildMentalHealthTherapyDto(medicationId, inpatientPrescription))
              .collect(Collectors.toList()));
    }
    return mentalHealthTherapyDtos;
  }

  private MentalHealthTherapyDto buildMentalHealthTherapyDto(
      final Long medicationId,
      final InpatientPrescription inpatientPrescription)
  {
    final MedicationOrder medicationOrder = inpatientPrescription.getMedicationOrder();

    final MentalHealthTherapyDto mentalHealthTherapyDto = new MentalHealthTherapyDto();
    final MedicationDto medicationDto = getMedication(medicationId);

    TherapyStatusEnum therapyStatusEnum = TherapyStatusEnum.NORMAL;
    if (PrescriptionsEhrUtils.isTherapySuspended(inpatientPrescription.getActions()))
    {
      therapyStatusEnum = TherapyStatusEnum.SUSPENDED;
    }
    else if (PrescriptionsEhrUtils.isInpatientPrescriptionCancelledOrAborted(inpatientPrescription))
    {
      therapyStatusEnum = TherapyStatusEnum.ABORTED;
    }

    final String routeId = medicationOrder.getRoute().get(0).getDefiningCode().getCodeString();

    final MentalHealthMedicationDto mentalHealthMedicationDto = new MentalHealthMedicationDto(
        medicationId,
        medicationDto.getName(),
        medicationDto.getGenericName(),
        getMedicationRoute(Long.valueOf(routeId)));

    mentalHealthTherapyDto.setMentalHealthMedicationDto(mentalHealthMedicationDto);
    mentalHealthTherapyDto.setGenericName(medicationDto.getGenericName());
    mentalHealthTherapyDto.setTherapyStatusEnum(therapyStatusEnum);

    return mentalHealthTherapyDto;
  }

  private boolean isTherapyCanceledAbortedOrSuspended(final List<MedicationManagement> actions)
  {
    if (PrescriptionsEhrUtils.isTherapySuspended(actions))
    {
      return true;
    }
    return isTherapyCanceledOrAborted(actions);
  }

  private boolean isTherapyCanceledOrAborted(final List<MedicationManagement> actions)
  {
    for (final MedicationManagement action : actions)
    {
      final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
      if (actionEnum == MedicationActionEnum.CANCEL || actionEnum == MedicationActionEnum.ABORT)
      {
        return true;
      }
    }
    return false;
  }

  private boolean isTherapyCompletedWithModify(final List<MedicationManagement> actions)
  {
    for (final MedicationManagement action : actions)
    {
      final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
      if (actionEnum == MedicationActionEnum.COMPLETE)
      {
        return true;
      }
    }
    return false;
  }

  public MedicationDto getMedication(final Long medicationId)
  {
    return medicationsValueHolderProvider.getMedication(medicationId);
  }

  public MedicationRouteDto getMedicationRoute(final long routeId)
  {
    return medicationRoutesValueHolder.getValue().get(routeId);
  }

  @Override
  public ReferenceWeight buildReferenceWeightComposition(
      final double weight,
      final @NonNull DateTime when)
  {
    final ReferenceWeight referenceWeight = new ReferenceWeight();
    final BodyWeight bodyWeight = new BodyWeight();
    bodyWeight.setWeight(DataValueUtils.getQuantity(weight, unitsProvider.getDisplayName(KG)));
    bodyWeight.setTime(DataValueUtils.getDateTime(when));
    referenceWeight.setReferenceBodyWeight(bodyWeight);

    new EhrContextVisitor(referenceWeight)
        .withComposer(RequestUser.getId(), RequestUser.getFullName())
        .withStartTime(when)
        .visit();
    return referenceWeight;
  }

  @Override
  public void sortTherapiesByMedicationTimingStart(
      final List<InpatientPrescription> prescriptions, final boolean descending)
  {
    Collections.sort(
        prescriptions, (therapy1, therapy2) ->
        {
          final DateTime firstCompositionStart =
              DataValueUtils.getDateTime(therapy1.getMedicationOrder().getOrderDetails().getOrderStartDateTime());
          final DateTime secondCompositionStart =
              DataValueUtils.getDateTime(therapy2.getMedicationOrder().getOrderDetails().getOrderStartDateTime());

          final DateTime firstContextStart =
              DataValueUtils.getDateTime(therapy1.getContext().getStartTime());
          final DateTime secondContextStart =
              DataValueUtils.getDateTime(therapy2.getContext().getStartTime());

          if (descending)
          {
            if (secondCompositionStart.equals(firstCompositionStart))
            {
              return secondContextStart.compareTo(firstContextStart);
            }
            return secondCompositionStart.compareTo(firstCompositionStart);
          }
          if (firstCompositionStart.equals(secondCompositionStart))
          {
            return firstContextStart.compareTo(secondContextStart);
          }
          return firstCompositionStart.compareTo(secondCompositionStart);
        }
    );
  }

  @Override
  public DocumentationTherapiesDto findTherapyGroupsForDocumentation(
      final String patientId,
      final Interval centralCaseEffective,
      final List<InpatientPrescription> inpatientPrescriptions,
      final boolean isOutpatient,
      final DateTime when,
      final Locale locale)
  {
    sortTherapiesByMedicationTimingStart(inpatientPrescriptions, false);

    final Map<Long, MedicationDataForTherapyDto> medicationsDataMap = getMedicationDataForInpatientPrescriptions(
        inpatientPrescriptions, null);

    return getTherapiesForDocumentation(
        patientId,
        centralCaseEffective,
        inpatientPrescriptions,
        medicationsDataMap,
        isOutpatient,
        when,
        locale
    );
  }

  DocumentationTherapiesDto getTherapiesForDocumentation(
      final String patientId,
      final Interval centralCaseEffective,
      final List<InpatientPrescription> inpatientPrescriptions,
      final Map<Long, MedicationDataForTherapyDto> medicationsDataMap,
      final boolean isOutpatient,
      final DateTime when,
      final Locale locale)
  {
    final List<TherapyDocumentationData> therapies = new ArrayList<>();
    final List<TherapyDocumentationData> dischargeTherapies = new ArrayList<>();
    final List<TherapyDocumentationData> admissionTherapies = new ArrayList<>();
    final List<TherapyDto> taggedTherapiesForPrescription = new ArrayList<>();

    if (!isOutpatient)
    {
      final String reconciliationUid = reconciliationOpenEhrDao.findLatestMedicationReconciliationUid(patientId).orElse(null);

      //use taggedTherapiesForPrescription to display medication on discharge list for EMRAM
      if (reconciliationUid != null)
      {
        final List<MedicationOnDischarge> dischargeCompositions = reconciliationOpenEhrDao.findMedicationsOnDischarge(patientId, reconciliationUid);

        for (final MedicationOnDischarge dischargeComposition : dischargeCompositions)
        {
          final TherapyDto convertedTherapy = convertMedicationOrderToTherapyDto(
              dischargeComposition,
              dischargeComposition.getMedicationOrder(),
              null,
              null,
              true,
              locale);

          taggedTherapiesForPrescription.add(convertedTherapy);
        }
      }
    }

    final Set<TherapyDocumentationData> alreadyHandled = new HashSet<>();

    final boolean isOutpatientOrLastsOneDay = isOutpatient || Intervals.durationInDays(centralCaseEffective) <= 1;

    for (final InpatientPrescription inpatientPrescription : inpatientPrescriptions)
    {
      final DateTime therapyStart =
          DataValueUtils.getDateTime(inpatientPrescription.getMedicationOrder().getOrderDetails().getOrderStartDateTime());
      final DateTime therapyEnd =
          DataValueUtils.getDateTime(inpatientPrescription.getMedicationOrder().getOrderDetails().getOrderStopDateTime());

      final TherapyDto convertedTherapy = convertMedicationOrderToTherapyDto(
          inpatientPrescription,
          inpatientPrescription.getMedicationOrder(),
          null,
          null,
          true,
          locale);

      if (!areAllIngredientsSolutions(convertedTherapy))
      {
        final Interval therapyInterval =
            new Interval(therapyStart, therapyEnd != null ? therapyEnd : Intervals.INFINITE.getEnd());

        final boolean handled =
            handleSimilarAndLinkedTherapies(
                admissionTherapies,
                dischargeTherapies,
                therapies,
                alreadyHandled,
                inpatientPrescription,
                convertedTherapy,
                medicationsDataMap,
                therapyInterval,
                centralCaseEffective.getEnd(),
                when);

        final TherapyDocumentationData therapy = createTherapyData(inpatientPrescription, convertedTherapy, therapyInterval);
        alreadyHandled.add(therapy);

        if (!handled)
        {
          if (isOutpatientOrLastsOneDay)
          {
            if (therapyInterval.overlaps(Intervals.wholeDay(centralCaseEffective.getStart())))
            {
              therapies.add(therapy);
            }
          }
          else
          {
            boolean isAdmission = false;
            if (therapyInterval.overlaps(Intervals.wholeDay(centralCaseEffective.getStart())))
            {
              admissionTherapies.add(therapy);
              isAdmission = true;
            }

            boolean isDischarge = false;
            if (isDischargeTherapy(therapyInterval, centralCaseEffective.getEnd(), when))
            {
              dischargeTherapies.add(therapy);
              isDischarge = true;
            }

            if (!isAdmission && !isDischarge)
            {
              if (therapyInterval.overlaps(centralCaseEffective))
              {
                therapies.add(therapy);
              }
            }
          }
        }
      }
    }

    return new DocumentationTherapiesDto(
        getTherapyDisplayValuesForDocumentation(therapies, locale),
        getTherapyDisplayValuesForDocumentation(dischargeTherapies, locale),
        getTherapyDisplayValuesForDocumentation(admissionTherapies, locale),
        getTherapyDisplayValues(taggedTherapiesForPrescription, locale));
  }

  private TherapyDocumentationData createTherapyData(
      final InpatientPrescription inpatientPrescription,
      final TherapyDto therapy,
      @Nullable final Interval therapyInterval)
  {
    final TherapyDocumentationData therapyData = new TherapyDocumentationData();
    therapyData.setInpatientPrescription(inpatientPrescription);
    therapyData.setTherapy(therapy);

    if (therapyInterval != null)
    {
      therapyData.addInterval(
          TherapyIdUtils.createTherapyId(therapy.getCompositionUid(), therapy.getEhrOrderName()), therapyInterval);
    }
    return therapyData;
  }

  private boolean isDischargeTherapy(final Interval therapyInterval, final DateTime centralCaseEnd, final DateTime when)
  {
    return Intervals.isEndInfinity(therapyInterval.getEnd())
        && therapyInterval.overlaps(Intervals.wholeDay(when))
        || !Intervals.isEndInfinity(therapyInterval.getEnd())
        && therapyInterval.overlaps(Intervals.wholeDay(centralCaseEnd));
  }

  private boolean handleSimilarAndLinkedTherapies(
      final List<TherapyDocumentationData> admissionTherapies,
      final List<TherapyDocumentationData> dischargeTherapies,
      final List<TherapyDocumentationData> therapies,
      final Set<TherapyDocumentationData> alreadyHandled,
      final InpatientPrescription therapyToCompare,
      final TherapyDto convertedTherapy,
      final Map<Long, MedicationDataForTherapyDto> medicationsDataMap,
      final Interval therapyInterval,
      final DateTime centralCaseEnd,
      final DateTime when)
  {
    final Pair<TherapyLinkType, TherapyDocumentationData> pair = getLinkedToTherapyPair(
        admissionTherapies,
        therapies,
        therapyToCompare);

    if (pair.getFirst() == TherapyLinkType.REGULAR_LINK)
    {
      final TherapyDocumentationData linkedToTherapy = pair.getSecond();
      final Interval interval =
          linkedToTherapy.findIntervalForId(
              TherapyIdUtils.createTherapyId(
                  linkedToTherapy.getTherapy().getCompositionUid(), linkedToTherapy.getTherapy().getEhrOrderName()));

      if (isDischargeTherapy(therapyInterval, centralCaseEnd, when))
      {
        if (therapies.contains(linkedToTherapy))
        {
          therapies.remove(linkedToTherapy);
        }
        if (admissionTherapies.contains(linkedToTherapy))
        {
          admissionTherapies.remove(linkedToTherapy);
        }
        final TherapyDocumentationData dischargeTherapy =
            createTherapyData(
                therapyToCompare, convertedTherapy, new Interval(interval.getStart(), therapyInterval.getEnd()));
        dischargeTherapies.add(dischargeTherapy);
      }
      else
      {
        final Interval newInterval = new Interval(interval.getStart(), therapyInterval.getEnd());
        linkedToTherapy.addInterval(
            TherapyIdUtils.createTherapyId(
                convertedTherapy.getCompositionUid(),
                convertedTherapy.getEhrOrderName()), newInterval);
        linkedToTherapy.removeInterval(
            TherapyIdUtils.createTherapyId(
                linkedToTherapy.getTherapy().getCompositionUid(),
                linkedToTherapy.getTherapy().getEhrOrderName()), interval);
        linkedToTherapy.setTherapy(convertedTherapy);
      }
      return true;
    }
    if (pair.getFirst() == TherapyLinkType.LINKED_TO_ADMISSION_THERAPY && alreadyHandled.contains(pair.getSecond()))
    {
      if (isDischargeTherapy(therapyInterval, centralCaseEnd, when))
      {
        final TherapyDocumentationData newDischargeTherapy =
            createTherapyData(therapyToCompare, convertedTherapy, therapyInterval);
        dischargeTherapies.add(newDischargeTherapy);
      }
      else
      {
        final TherapyDocumentationData linkedToTherapy = pair.getSecond();
        final Interval interval =
            linkedToTherapy.findIntervalForId(
                TherapyIdUtils.createTherapyId(
                    linkedToTherapy.getTherapy().getCompositionUid(),
                    linkedToTherapy.getTherapy().getEhrOrderName()));
        final Interval newInterval = new Interval(interval.getStart(), therapyInterval.getEnd());
        linkedToTherapy.addInterval(
            TherapyIdUtils.createTherapyId(
                convertedTherapy.getCompositionUid(),
                convertedTherapy.getEhrOrderName()), newInterval);
        linkedToTherapy.removeInterval(
            TherapyIdUtils.createTherapyId(
                linkedToTherapy.getTherapy().getCompositionUid(),
                linkedToTherapy.getTherapy().getEhrOrderName()), interval);
      }
      return true;
    }

    final Pair<TherapySimilarityType, TherapyDocumentationData> similarityTypePair =
        getSimilarTherapyPair(admissionTherapies, therapies, therapyToCompare, medicationsDataMap);

    if (similarityTypePair.getFirst() == TherapySimilarityType.SIMILAR_TO_ADMISSION_THERAPY)
    {
      final TherapyDocumentationData similarTherapy = similarityTypePair.getSecond();

      if (isDischargeTherapy(therapyInterval, centralCaseEnd, when))
      {
        final TherapyDocumentationData newTherapy = createTherapyData(therapyToCompare, convertedTherapy, therapyInterval);
        dischargeTherapies.add(newTherapy);
      }
      else
      {
        similarTherapy.addInterval(
            TherapyIdUtils.createTherapyId(convertedTherapy.getCompositionUid(), convertedTherapy.getEhrOrderName()),
            therapyInterval);
      }
      return true;
    }
    if (similarityTypePair.getFirst() == TherapySimilarityType.SIMILAR)
    {
      final TherapyDocumentationData similarTherapy = similarityTypePair.getSecond();

      if (isDischargeTherapy(therapyInterval, centralCaseEnd, when))
      {
        if (therapies.contains(similarTherapy))
        {
          therapies.remove(similarTherapy);
        }
        if (admissionTherapies.contains(similarTherapy))
        {
          admissionTherapies.remove(similarTherapy);
        }
        final TherapyDocumentationData dischargeTherapy =
            createTherapyData(therapyToCompare, convertedTherapy, therapyInterval);

        for (final Pair<String, Interval> pair1 : similarTherapy.getIntervals())
        {
          dischargeTherapy.addInterval(pair1.getFirst(), pair1.getSecond());
        }
        dischargeTherapies.add(dischargeTherapy);
      }
      else
      {
        similarTherapy.addInterval(
            TherapyIdUtils.createTherapyId(
                convertedTherapy.getCompositionUid(),
                convertedTherapy.getEhrOrderName()), therapyInterval);
        similarTherapy.setTherapy(convertedTherapy);
      }
      return true;
    }

    return false;
  }

  private Pair<TherapyLinkType, TherapyDocumentationData> getLinkedToTherapyPair(
      final List<TherapyDocumentationData> admissionTherapies,
      final List<TherapyDocumentationData> therapies,
      final InpatientPrescription therapyToCompare)
  {
    for (final TherapyDocumentationData data : admissionTherapies)
    {
      if (areInpatientPrescriptionsLinkedByUpdate(therapyToCompare, data.getInpatientPrescription()))
      {
        return Pair.of(TherapyLinkType.LINKED_TO_ADMISSION_THERAPY, data);
      }
    }

    for (final TherapyDocumentationData data : therapies)
    {
      if (areInpatientPrescriptionsLinkedByUpdate(therapyToCompare, data.getInpatientPrescription()))
      {
        return Pair.of(TherapyLinkType.REGULAR_LINK, data);
      }
    }

    return Pair.of(TherapyLinkType.NONE, null);
  }

  private Pair<TherapySimilarityType, TherapyDocumentationData> getSimilarTherapyPair(
      final List<TherapyDocumentationData> admissionTherapies,
      final List<TherapyDocumentationData> therapies,
      final InpatientPrescription inpatientPrescription,
      final Map<Long, MedicationDataForTherapyDto> medicationsDataMap)
  {
    for (final TherapyDocumentationData data : admissionTherapies)
    {
      final boolean similar = areTherapiesSimilar(inpatientPrescription, data.getInpatientPrescription(), medicationsDataMap, true);
      if (similar)
      {
        return Pair.of(TherapySimilarityType.SIMILAR_TO_ADMISSION_THERAPY, data);
      }
    }
    for (final TherapyDocumentationData data : therapies)
    {
      final boolean similar = areTherapiesSimilar(inpatientPrescription, data.getInpatientPrescription(), medicationsDataMap, true);
      if (similar)
      {
        return Pair.of(TherapySimilarityType.SIMILAR, data);
      }
    }
    return Pair.of(TherapySimilarityType.NONE, null);
  }

  private boolean areAllIngredientsSolutions(final TherapyDto therapy)
  {
    if (therapy instanceof ComplexTherapyDto)
    {
      final ComplexTherapyDto complexTherapy = (ComplexTherapyDto)therapy;
      for (final InfusionIngredientDto ingredient : complexTherapy.getIngredientsList())
      {
        if (ingredient.getMedication().getMedicationType() != MedicationTypeEnum.DILUENT)
        {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private List<String> getTherapyDisplayValuesForDocumentation(
      final List<TherapyDocumentationData> therapyList,
      final Locale locale)
  {
    final List<String> strings = new ArrayList<>();

    for (final TherapyDocumentationData therapy : therapyList)
    {
      therapyDisplayProvider.fillDisplayValues(therapy.getTherapy(), true, locale);
      String formatted = therapy.getTherapy().getFormattedTherapyDisplay();
      for (final Pair<String, Interval> pair : therapy.getIntervals())
      {
        final Interval interval = pair.getSecond();
        final String endDate =
            Intervals.isEndInfinity(interval.getEnd()) ?
            "..." :
            DateTimeFormatters.shortDateTime(locale).print(interval.getEnd());
        formatted =
            formatted + " "
                + DateTimeFormatters.shortDateTime(locale).print(interval.getStart()) + " &ndash; "
                + endDate + "<br>";
      }
      strings.add(formatted);
    }
    return strings;
  }

  private List<String> getTherapyDisplayValues(final List<TherapyDto> therapyList, final Locale locale)
  {
    final List<String> therapyDisplayValues = new ArrayList<>();
    for (final TherapyDto therapy : therapyList)
    {
      therapyDisplayProvider.fillDisplayValues(therapy, true, locale);
      therapyDisplayValues.add(therapy.getFormattedTherapyDisplay());
    }
    return therapyDisplayValues;
  }

  @Override
  public DateTime findPreviousTaskForTherapy(
      final String patientId,
      final String compositionUid,
      final String ehrOrderName,
      final DateTime when)
  {
    InpatientPrescription prescription = medicationsOpenEhrDao.loadInpatientPrescription(patientId, compositionUid);

    while (prescription != null)
    {
      final List<AdministrationDto> administrations = administrationProvider.getPrescriptionsAdministrations(
          patientId,
          Collections.singletonList(prescription),
          null,
          true);

      final String therapyId = TherapyIdUtils.createTherapyId(prescription);

      final DateTime lastTask = medicationsTasksProvider.findLastAdministrationTaskTimeForTherapy(
          patientId,
          therapyId,
          new Interval(when.minusDays(10), when),
          false)
          .orElse(null);

      final DateTime lastAdministration = administrations
          .stream()
          .filter(a -> a.getAdministrationResult() != AdministrationResultEnum.NOT_GIVEN)
          .map(AdministrationDto::getAdministrationTime)
          .max(Comparator.naturalOrder())
          .orElse(null);

      final DateTime lastTime = getMostRecent(lastTask, lastAdministration);
      if (lastTime != null)
      {
        return lastTime;
      }

      prescription = therapyEhrHandler.getPrescriptionFromLink(patientId, prescription, EhrLinkType.UPDATE, true);
    }

    return null;
  }

  private DateTime getMostRecent(final DateTime t1, final DateTime t2)
  {
    if (t1 != null && t2 != null && t2.isAfter(t1))
    {
      return t2;
    }
    return t1 != null ? t1 : t2;
  }
}
