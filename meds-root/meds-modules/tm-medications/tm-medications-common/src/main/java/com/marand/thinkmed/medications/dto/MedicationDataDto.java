package com.marand.thinkmed.medications.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.api.internal.dto.DoseFormDto;
import com.marand.thinkmed.medications.api.internal.dto.IndicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.api.internal.dto.TitrationType;
import com.marand.thinkmed.medications.api.internal.dto.dose.PrescribingDoseDto;
import com.marand.thinkmed.medications.api.internal.dto.property.MedicationPropertyType;
import com.marand.thinkmed.medications.dto.property.MedicationPropertyDto;
import lombok.NonNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

import static com.marand.thinkmed.medications.api.internal.dto.MedicationOrderFormType.DESCRIPTIVE;

/**
 * @author Nejc Korasa
 * @author Mitja Lapajne
 */
@SuppressWarnings("unused")
public class MedicationDataDto extends DataTransferObject implements JsonSerializable
{
  private MedicationDto medication;
  private DateTime validFrom;
  private DateTime validTo;

  private boolean inpatient;
  private boolean outpatient;
  private boolean orderable;
  private boolean descriptiveDose;

  private MedicationLevelEnum medicationLevel;
  private String vtmId;
  private String vmpId;
  private String ampId;

  private DoseFormDto doseForm;
  private Double roundingFactor;
  private TitrationType titration;
  private String medicationPackaging;
  private String atcGroupCode;
  private String atcGroupName;

  private String administrationUnit;
  private Double administrationUnitFactor;
  private String supplyUnit;
  private Double supplyUnitFactor;

  private MedicationRouteDto defaultRoute;
  private List<MedicationRouteDto> routes = new ArrayList<>();

  private PrescribingDoseDto prescribingDose;
  private boolean formulary;
  private Integer sortOrder;

  private String interchangeableDrugsGroup;

  private List<MedicationIngredientDto> medicationIngredients = new ArrayList<>();
  private final Set<MedicationPropertyDto> properties = new HashSet<>();
  private List<IndicationDto> indications = new ArrayList<>();
  private List<MedicationDocumentDto> medicationDocuments = new ArrayList<>();

  //custom groups with type CARE_PROVIDER (careProviderId, customGroupName, customGroupSortOrder)
  private Map<String, Pair<String, Integer>> careProviderCustomGroups = new HashMap<>();

  public MedicationDto getMedication()
  {
    return medication;
  }

  public void setMedication(final MedicationDto medication)
  {
    this.medication = medication;
  }

  public DateTime getValidFrom()
  {
    return validFrom;
  }

  public void setValidFrom(final DateTime validFrom)
  {
    this.validFrom = validFrom;
  }

  public DateTime getValidTo()
  {
    return validTo;
  }

  public void setValidTo(final DateTime validTo)
  {
    this.validTo = validTo;
  }

  public boolean isValid(final @NonNull DateTime when)
  {
    if (validFrom != null && validFrom.isBefore(when))
    {
      return validTo == null || when.isBefore(validTo);
    }
    return false;
  }

  public boolean isInpatient()
  {
    return inpatient;
  }

  public void setInpatient(final boolean inpatient)
  {
    this.inpatient = inpatient;
  }

  public boolean isOutpatient()
  {
    return outpatient;
  }

  public void setOutpatient(final boolean outpatient)
  {
    this.outpatient = outpatient;
  }

  public boolean isOrderable()
  {
    return orderable;
  }

  public void setOrderable(final boolean orderable)
  {
    this.orderable = orderable;
  }

  public boolean isDescriptiveDose()
  {
    return descriptiveDose || Opt.resolve(() -> doseForm.getMedicationOrderFormType()).map(t -> t == DESCRIPTIVE).orElse(false);
  }

  public void setDescriptiveDose(final boolean descriptiveDose)
  {
    this.descriptiveDose = descriptiveDose;
  }

  public MedicationLevelEnum getMedicationLevel()
  {
    return medicationLevel;
  }

  public void setMedicationLevel(final MedicationLevelEnum medicationLevel)
  {
    this.medicationLevel = medicationLevel;
  }

  public String getVtmId()
  {
    return vtmId;
  }

  public void setVtmId(final String vtmId)
  {
    this.vtmId = vtmId;
  }

  public String getVmpId()
  {
    return vmpId;
  }

  public void setVmpId(final String vmpId)
  {
    this.vmpId = vmpId;
  }

  public String getAmpId()
  {
    return ampId;
  }

  public void setAmpId(final String ampId)
  {
    this.ampId = ampId;
  }

  public DoseFormDto getDoseForm()
  {
    return doseForm;
  }

  public void setDoseForm(final DoseFormDto doseForm)
  {
    this.doseForm = doseForm;
  }

  public Double getRoundingFactor()
  {
    return roundingFactor;
  }

  public void setRoundingFactor(final Double roundingFactor)
  {
    this.roundingFactor = roundingFactor;
  }

  public String getAdministrationUnit()
  {
    return administrationUnit;
  }

  public void setAdministrationUnit(final String administrationUnit)
  {
    this.administrationUnit = administrationUnit;
  }

  public Double getAdministrationUnitFactor()
  {
    return administrationUnitFactor;
  }

  public void setAdministrationUnitFactor(final Double administrationUnitFactor)
  {
    this.administrationUnitFactor = administrationUnitFactor;
  }

  public String getSupplyUnit()
  {
    return supplyUnit;
  }

  public void setSupplyUnit(final String supplyUnit)
  {
    this.supplyUnit = supplyUnit;
  }

  public Double getSupplyUnitFactor()
  {
    return supplyUnitFactor;
  }

  public void setSupplyUnitFactor(final Double supplyUnitFactor)
  {
    this.supplyUnitFactor = supplyUnitFactor;
  }

  public Set<MedicationPropertyDto> getProperties()
  {
    return properties;
  }

  public TitrationType getTitration()
  {
    return titration;
  }

  public void setTitration(final TitrationType titration)
  {
    this.titration = titration;
  }

  public PrescribingDoseDto getPrescribingDose()
  {
    return prescribingDose;
  }

  public void setPrescribingDose(final PrescribingDoseDto prescribingDose)
  {
    this.prescribingDose = prescribingDose;
  }

  public List<MedicationIngredientDto> getMedicationIngredients()
  {
    return medicationIngredients;
  }

  public void setMedicationIngredients(final List<MedicationIngredientDto> medicationIngredients)
  {
    this.medicationIngredients = medicationIngredients;
  }

  public MedicationRouteDto getDefaultRoute()
  {
    return defaultRoute;
  }

  public void setDefaultRoute(final MedicationRouteDto defaultRoute)
  {
    this.defaultRoute = defaultRoute;
  }

  public List<MedicationRouteDto> getRoutes()
  {
    return routes;
  }

  public void setRoutes(final List<MedicationRouteDto> routes)
  {
    this.routes = routes;
  }

  public List<MedicationDocumentDto> getMedicationDocuments()
  {
    return medicationDocuments;
  }

  public void setMedicationDocuments(final List<MedicationDocumentDto> medicationDocuments)
  {
    this.medicationDocuments = medicationDocuments;
  }

  public List<IndicationDto> getIndications()
  {
    return indications;
  }

  public void setIndications(final List<IndicationDto> indications)
  {
    this.indications = indications;
  }

  public String getMedicationPackaging()
  {
    return medicationPackaging;
  }

  public void setMedicationPackaging(final String medicationPackaging)
  {
    this.medicationPackaging = medicationPackaging;
  }

  public String getAtcGroupCode()
  {
    return atcGroupCode;
  }

  public void setAtcGroupCode(final String atcGroupCode)
  {
    this.atcGroupCode = atcGroupCode;
  }

  public String getAtcGroupName()
  {
    return atcGroupName;
  }

  public void setAtcGroupName(final String atcGroupName)
  {
    this.atcGroupName = atcGroupName;
  }

  public Map<String, Pair<String, Integer>> getCareProviderCustomGroups()
  {
    return careProviderCustomGroups;
  }

  public void setCareProviderCustomGroups(final Map<String, Pair<String, Integer>> careProviderCustomGroups)
  {
    this.careProviderCustomGroups = careProviderCustomGroups;
  }

  public boolean isFormulary()
  {
    return formulary;
  }

  public void setFormulary(final boolean formulary)
  {
    this.formulary = formulary;
  }

  public Integer getSortOrder()
  {
    return sortOrder;
  }

  public void setSortOrder(final Integer sortOrder)
  {
    this.sortOrder = sortOrder;
  }

  public String getInterchangeableDrugsGroup()
  {
    return interchangeableDrugsGroup;
  }

  public void setInterchangeableDrugsGroup(final String interchangeableDrugsGroup)
  {
    this.interchangeableDrugsGroup = interchangeableDrugsGroup;
  }

  public boolean isReviewReminder()
  {
    return properties.stream().anyMatch(p -> p.getType() == MedicationPropertyType.REVIEW_REMINDER);
  }

  public boolean isControlledDrug()
  {
    return properties.stream().anyMatch(p -> p.getType() == MedicationPropertyType.CONTROLLED_DRUG);
  }

  public boolean isMentalHealthDrug()
  {
    return properties.stream().anyMatch(p -> p.getType() == MedicationPropertyType.MENTAL_HEALTH_DRUG);
  }

  public boolean isBlackTriangleMedication()
  {
    return properties.stream().anyMatch(p -> p.getType() == MedicationPropertyType.BLACK_TRIANGLE_MEDICATION);
  }

  public boolean isClinicalTrialMedication()
  {
    return properties.stream().anyMatch(p -> p.getType() == MedicationPropertyType.CLINICAL_TRIAL_MEDICATION);
  }

  public boolean isHighAlertMedication()
  {
    return properties.stream().anyMatch(p -> p.getType() == MedicationPropertyType.HIGH_ALERT_MEDICATION);
  }

  public boolean isUnlicensedMedication()
  {
    return properties.stream().anyMatch(p -> p.getType() == MedicationPropertyType.UNLICENSED_MEDICATION);
  }

  public boolean isExpensiveDrug()
  {
    return properties.stream().anyMatch(p -> p.getType() == MedicationPropertyType.EXPENSIVE_DRUG);
  }

  public boolean isNotForPrn()
  {
    return properties.stream().anyMatch(p -> p.getType() == MedicationPropertyType.NOT_FOR_PRN);
  }

  public boolean isAntibiotic()
  {
    return properties.stream().anyMatch(p -> p.getType() == MedicationPropertyType.ANTIBIOTIC);
  }

  public boolean isAnticoagulant()
  {
    return properties.stream().anyMatch(p -> p.getType() == MedicationPropertyType.ANTICOAGULANT);
  }

  public boolean isInsulin()
  {
    return properties.stream().anyMatch(p -> p.getType() == MedicationPropertyType.INSULIN);
  }

  public boolean isFluid()
  {
    return properties.stream().anyMatch(p -> p.getType() == MedicationPropertyType.FLUID);
  }

  public boolean ignoreDuplicationWarnings()
  {
    return properties.stream().anyMatch(p -> p.getType() == MedicationPropertyType.IGNORE_DUPLICATION_WARNINGS);
  }

  public String getPrice()
  {
    return properties.stream()
        .filter(p -> p.getType() == MedicationPropertyType.PRICE)
        .findAny()
        .map(p -> (String)p.getValue())
        .orElse(null);
  }

  public boolean isSuggestSwitchToOral()
  {
    return properties.stream().anyMatch(p -> p.getType() == MedicationPropertyType.SUGGEST_SWITCH_TO_ORAL);
  }

  public boolean hasProperty(final MedicationPropertyType type)
  {
    return properties.stream().anyMatch(p -> p.getType() == type);
  }

  public MedicationPropertyDto getProperty(final MedicationPropertyType type)
  {
    return properties.stream().filter(p -> p.getType() == type).findFirst().orElse(null);
  }

  public void setProperty(final MedicationPropertyDto property)
  {
    properties.add(property);
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("medication", medication)
        .append("defaultRoute", defaultRoute)
        .append("doseForm", doseForm)
        .append("inpatient", inpatient)
        .append("outpatient", outpatient)
        .append("descriptiveDose", descriptiveDose)
        .append("validFrom", validFrom)
        .append("validTo", validTo)
        .append("prescribingDose", prescribingDose)
        .append("medicationIngredients", medicationIngredients)
        .append("routes", routes)
        .append("medicationDocuments", medicationDocuments)
        .append("indications", indications)
        .append("titration", titration)
        .append("roundingFactor", roundingFactor)
        .append("formulary", formulary)
        .append("sortOrder", sortOrder)
        .append("interchangeableDrugsGroup", interchangeableDrugsGroup)
        .append("medicationPackaging", medicationPackaging)
    ;
  }
}