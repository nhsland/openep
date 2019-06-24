package com.marand.thinkmed.medications.business;

import java.text.Collator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;

import com.marand.thinkmed.medications.api.internal.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.DocumentationTherapiesDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.MedicationDataForTherapyDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTherapyDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.model.ReferenceWeight;
import com.marand.thinkmed.medications.ehr.model.composition.EhrComposition;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
public interface MedicationsBo
{
  Map<Long, MedicationDataForTherapyDto> getMedicationDataForInpatientPrescriptions(
      @NonNull List<InpatientPrescription> medicationOrders,
      String careProviderId);

  String getTherapyFormattedDisplay(String patientId, String therapyId, Locale locale);

  TherapyDto getTherapy(String patientId, String compositionId, String ehrOrderName, Locale locale);

  TherapyDto convertMedicationOrderToTherapyDto(@NonNull EhrComposition ehrComposition, @NonNull MedicationOrder medicationOrder);

  TherapyDto convertMedicationOrderToTherapyDto(
      @NonNull EhrComposition ehrComposition,
      @NonNull MedicationOrder medicationOrder,
      Double referenceWeight,
      Double patientHeight,
      boolean isToday,
      Locale locale);

  void fillDisplayValues(@NonNull TherapyDto therapy, Double referenceWeight, Double patientHeight, boolean isToday, Locale locale);

  double calculateBodySurfaceArea(final double heightInCm, final double weightInKg);

  List<TherapyDto> getTherapies(
      String patientId,
      @Nullable Interval searchInterval,
      Double referenceWeight,
      Double patientHeight,
      @Nullable Locale locale);

  List<MentalHealthTherapyDto> getMentalHealthTherapies(
      String patientId,
      Interval searchInterval,
      DateTime when,
      Locale locale);

  List<TherapyDto> getLinkTherapyCandidates(
      @NonNull String patientId,
      Double referenceWeight,
      Double patientHeight,
      @NonNull DateTime when,
      @NonNull Locale locale);

  List<TherapyDto> convertInpatientPrescriptionsToTherapies(
      @NonNull List<InpatientPrescription> inpatientPrescriptions,
      Double referenceWeight,
      Double patientHeight,
      Locale locale);

  boolean isMentalHealthMedication(long medicationId);

  boolean isTherapyActive(List<String> daysOfWeek, Integer dosingDaysFrequency, Interval therapyInterval, DateTime when);

  ReferenceWeight buildReferenceWeightComposition(double weight, DateTime when);

  void fillInfusionFormulaFromRate(ComplexTherapyDto therapy, Double referenceWeight, Double patientHeight);

  void fillInfusionRateFromFormula(ComplexTherapyDto therapy, Double referenceWeight, Double patientHeight);

  void sortTherapiesByMedicationTimingStart(
      final List<InpatientPrescription> therapies,
      final boolean descending);

  int compareTherapiesForSort(final TherapyDto firstTherapy, final TherapyDto secondTherapy, final Collator collator);

  boolean areInpatientPrescriptionsLinkedByUpdate(
      InpatientPrescription inpatientPrescription,
      InpatientPrescription compareInpatientPrescription);

  DocumentationTherapiesDto findTherapyGroupsForDocumentation(
      String patientId,
      Interval centralCaseEffective,
      List<InpatientPrescription> inpatientPrescriptions,
      boolean isOutpatient,
      DateTime when,
      Locale locale);

  DateTime findPreviousTaskForTherapy(
      String patientId,
      String compositionUid,
      String ehrOrderName,
      DateTime when);
}
