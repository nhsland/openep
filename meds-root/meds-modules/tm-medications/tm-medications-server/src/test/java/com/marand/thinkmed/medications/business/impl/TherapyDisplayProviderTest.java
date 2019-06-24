package com.marand.thinkmed.medications.business.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.maf.core.dictionary.MafDictionary;
import com.marand.maf.core.server.util.DefinedLocaleHolder;
import com.marand.thinkmed.api.core.GrammaticalGender;
import com.marand.thinkmed.medications.api.internal.dto.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.MedicationStartCriterionEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.TitrationType;
import com.marand.thinkmed.medications.api.internal.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.api.internal.dto.IndicationDto;
import com.marand.thinkmed.medications.api.internal.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseType;
import com.marand.thinkmed.medications.api.internal.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.DispenseDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.ComplexDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.SimpleDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.TimedSimpleDoseElementDto;
import com.marand.thinkmed.medications.units.provider.TestUnitsProviderImpl;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings("TooBroadScope")
public class TherapyDisplayProviderTest
{
  private final TherapyDisplayProvider therapyDisplayProvider = new TherapyDisplayProvider();

  private final ReleaseDetailsDisplayProvider releaseDetailsDisplayProvider = new ReleaseDetailsDisplayProvider();

  @Before
  public void setUp()
  {
    Dictionary.setDelegate(new MafDictionary("Dictionary", "", new Locale("en")));
    DefinedLocaleHolder.INSTANCE.setLocale(new Locale("en"));

    therapyDisplayProvider.setUnitsProvider(new TestUnitsProviderImpl());
    therapyDisplayProvider.setReleaseDetailsDisplayProvider(releaseDetailsDisplayProvider);
  }

  @Test
  public void testConstantSimpleTherapyDisplay()
  {
    final Locale locale = new Locale("en");
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setReleaseDetails(new ReleaseDetailsDto(ReleaseType.MODIFIED_RELEASE, 24));
    final DosingFrequencyDto frequency = new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 3.0);
    fillSimpleTherapyCommonValues(therapy, frequency);
    therapy.setDosingDaysFrequency(2);
    therapy.setDoseElement(createSimpleDoseElement(10.0, 1.0));
    therapy.setWhenNeeded(true);
    therapy.setTargetInr(5.0);
    therapy.setStart(new DateTime(2017, 1, 1, 0, 0));
    therapy.setEnd(new DateTime(2017, 2, 7, 11, 0));

    therapyDisplayProvider.fillDisplayValues(therapy, true, false, true, locale, false);

    assertEquals("Paracetamol (Lekadol)", therapy.getMedication().getDisplayName());
    assertEquals("3X " + Dictionary.getEntry("per.day", GrammaticalGender.UNDEFINED, locale),
        therapy.getFrequencyDisplay());
    assertEquals("10 mg / 1 mL", therapy.getQuantityDisplay());
    assertEquals("When needed", therapy.getWhenNeededDisplay());
    assertEquals("Every 2 days", therapy.getDaysFrequencyDisplay());

    final String formattedDescription = therapy.getFormattedTherapyDisplay();
    assertEquals(
        "<span class='GenericName TextDataBold'>Paracetamol </span>" +
            "<span class='MedicationName TextData'>(Lekadol) </span> - MR24<br>" +
            "<span class='DoseLabel TextLabel MedicationLabel'>DOSE </span>" +
            "<span class='Quantity TextDataBold'>10 mg / 1 mL </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Frequency TextData'>3X per day </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='DaysOfWeek TextData'>Every Mon, Fri </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='FrequencyDisplay TextData'>Every 2 days </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='WhenNeeded TextData'>When needed </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Route TextData'>Oral </span><br>" +
            "<span class='CommentLabel TextLabel MedicationLabel'>COMMENT </span>" +
            "<span class='Comment TextData'>Comment </span><br>" +
            "<span class='TargetInrLabel TextLabel MedicationLabel'>TARGET INR </span>" +
            "<span class='TargetInr TextData'>5.0 </span>" +
            "<span class='TherapyDuration'><br><span class='DurationLabel TextLabel MedicationLabel'>Duration </span>" +
            "<span class='Duration TextData'>5 weeks, 2 days, 11 hours </span></span>",
        formattedDescription);
  }

  @Test
  public void testConstantSimpleTherapyDisplayNoDurationNoSupply()
  {
    final Locale locale = new Locale("en");
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    final DosingFrequencyDto frequency = new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 3.0);
    therapy.setMedication(createMedicationDto(1L, "Lekadol", "Paracetamol", MedicationTypeEnum.MEDICATION));
    therapy.setDosingFrequency(frequency);
    final MedicationRouteDto routeDto = new MedicationRouteDto();
    routeDto.setCode("1");
    routeDto.setName("Oral");
    therapy.setRoutes(Collections.singletonList(routeDto));
    therapy.setQuantityUnit("mg");
    therapy.setQuantityDenominatorUnit("mL");
    therapy.setDispenseDetails(new DispenseDetailsDto());
    therapy.setDoseElement(createSimpleDoseElement(10.0, 1.0));
    therapy.setClinicalIndication(new IndicationDto("1", "Pain"));

    therapyDisplayProvider.fillDisplayValues(therapy, true, false, true, locale, false);

    assertEquals("Paracetamol (Lekadol)", therapy.getMedication().getDisplayName());
    assertEquals(
        "3X " + Dictionary.getEntry("per.day", GrammaticalGender.UNDEFINED, locale),
        therapy.getFrequencyDisplay());
    assertEquals("10 mg / 1 mL", therapy.getQuantityDisplay());

    final String formattedDescription = therapy.getFormattedTherapyDisplay();
    assertEquals(
        "<span class='GenericName TextDataBold'>Paracetamol </span>" +
            "<span class='MedicationName TextData'>(Lekadol) </span><br>" +
            "<span class='DoseLabel TextLabel MedicationLabel'>DOSE </span>" +
            "<span class='Quantity TextDataBold'>10 mg / 1 mL </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Frequency TextData'>3X per day </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Route TextData'>Oral </span><br>" +
            "<span class='IndicationLabel TextLabel MedicationLabel'>INDICATION </span>" +
            "<span class='Indication TextData'>Pain </span>",
        formattedDescription);
  }

  @Test
  public void testTitratedSimpleTherapyDisplay()
  {
    final Locale locale = new Locale("en");
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    final DosingFrequencyDto frequency = new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 3.0);
    fillSimpleTherapyCommonValues(therapy, frequency);
    therapy.setTitration(TitrationType.BLOOD_SUGAR);

    therapyDisplayProvider.fillDisplayValues(therapy, true, false, true, locale, false);

    assertEquals("Paracetamol (Lekadol)", therapy.getMedication().getDisplayName());
    assertEquals(
        "3X " + Dictionary.getEntry("per.day", GrammaticalGender.UNDEFINED, locale), therapy.getFrequencyDisplay());
    assertEquals("Titrate", therapy.getQuantityDisplay());
    assertNull(therapy.getWhenNeededDisplay());
    assertNull(therapy.getDaysFrequencyDisplay());

    final String formattedDescription = therapy.getFormattedTherapyDisplay();
    assertEquals(
        "<span class='GenericName TextDataBold'>Paracetamol </span>" +
            "<span class='MedicationName TextData'>(Lekadol) </span><br>" +
            "<span class='DoseLabel TextLabel MedicationLabel'>DOSE </span>" +
            "<span class='Quantity TextDataBold'>Titrate </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Frequency TextData'>3X per day </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='DaysOfWeek TextData'>Every Mon, Fri </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Route TextData'>Oral </span><br>" +
            "<span class='CommentLabel TextLabel MedicationLabel'>COMMENT </span>" +
            "<span class='Comment TextData'>Comment </span>",
        formattedDescription);
  }

  @Test
  public void testConstantSimpleTherapyShortDisplay()
  {
    final Locale locale = new Locale("en");
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    final DosingFrequencyDto frequency = new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 1.0);
    fillSimpleTherapyCommonValues(therapy, frequency);
    therapy.setDosingDaysFrequency(2);
    therapy.setDoseElement(createSimpleDoseElement(10.0, 1.0));
    therapy.setWhenNeeded(true);
    therapy.setTargetInr(5.0);

    therapyDisplayProvider.fillDisplayValues(therapy, false, locale);

    assertEquals("Paracetamol (Lekadol)", therapy.getMedication().getDisplayName());
    assertNull(therapy.getFrequencyDisplay());
    assertEquals("10 mg / 1 mL", therapy.getQuantityDisplay());
    assertEquals("When needed", therapy.getWhenNeededDisplay());
    assertEquals("Every 2 days", therapy.getDaysFrequencyDisplay());

    final String formattedDescription = therapy.getFormattedTherapyDisplay();
    assertEquals(
        "<span class='GenericName TextDataBold'>Paracetamol </span>" +
            "<span class='MedicationName TextData'>(Lekadol) </span><br>" +
            "<span class='Quantity TextDataBold'>10 mg / 1 mL </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='DaysOfWeek TextData'>Every Mon, Fri </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='FrequencyDisplay TextData'>Every 2 days </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='WhenNeeded TextData'>When needed </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Route TextData'>Oral </span><br>" +
            "<span class='TargetInrLabel TextLabel MedicationLabel'>TARGET INR </span>" +
            "<span class='TargetInr TextData'>5.0 </span>",
        formattedDescription);
  }

  @Test
  public void testVariableSimpleTherapyDisplay()
  {
    final Locale locale = new Locale("en");
    final VariableSimpleTherapyDto therapy = new VariableSimpleTherapyDto();
    final DosingFrequencyDto frequency = new DosingFrequencyDto(DosingFrequencyTypeEnum.BETWEEN_DOSES, 8.0);
    fillSimpleTherapyCommonValues(therapy, frequency);
    final List<TimedSimpleDoseElementDto> timedDoseElements = new ArrayList<>();
    final TimedSimpleDoseElementDto doseElement1 = new TimedSimpleDoseElementDto();
    doseElement1.setDoseElement(createSimpleDoseElement(10.0, 1.0));
    doseElement1.setDoseTime(new HourMinuteDto(9, 0));
    timedDoseElements.add(doseElement1);
    final TimedSimpleDoseElementDto doseElement2 = new TimedSimpleDoseElementDto();
    doseElement2.setDoseElement(createSimpleDoseElement(15.0, 1.5));
    doseElement2.setDoseTime(new HourMinuteDto(12, 0));
    timedDoseElements.add(doseElement2);
    final TimedSimpleDoseElementDto doseElement3 = new TimedSimpleDoseElementDto();
    doseElement3.setDoseElement(createSimpleDoseElement(20.0, 2.0));
    doseElement3.setDoseTime(new HourMinuteDto(18, 30));
    timedDoseElements.add(doseElement3);
    therapy.setTimedDoseElements(timedDoseElements);
    therapy.setStartCriterion(MedicationStartCriterionEnum.BY_DOCTOR_ORDERS.name());

    therapyDisplayProvider.fillDisplayValues(therapy, true, locale);

    assertEquals("Paracetamol (Lekadol)", therapy.getMedication().getDisplayName());
    assertEquals(
        Dictionary.getEntry("once.every", GrammaticalGender.UNDEFINED, locale) + " 8 " + Dictionary.getEntry(
            "hours.accusative",
            GrammaticalGender.UNDEFINED,
            locale),
        therapy.getFrequencyDisplay());
    assertEquals("Variable dose", therapy.getQuantityDisplay());
    assertEquals("10 mg / 1 mL", therapy.getTimedDoseElements().get(0).getQuantityDisplay());
    assertEquals("09:00", therapy.getTimedDoseElements().get(0).getTimeDisplay());
    assertEquals("15 mg / 1.5 mL", therapy.getTimedDoseElements().get(1).getQuantityDisplay());
    assertEquals("12:00", therapy.getTimedDoseElements().get(1).getTimeDisplay());
    assertEquals("20 mg / 2 mL", therapy.getTimedDoseElements().get(2).getQuantityDisplay());
    assertEquals("18:30", therapy.getTimedDoseElements().get(2).getTimeDisplay());

    assertEquals("By doctor orders", therapy.getStartCriterionDisplay());
    assertNull(therapy.getDaysFrequencyDisplay());

    final String formattedDescription = therapy.getFormattedTherapyDisplay();

    assertEquals(
        "<span class='GenericName TextDataBold'>Paracetamol </span>" +
            "<span class='MedicationName TextData'>(Lekadol) </span><br>" +
            "<span class='DoseLabel TextLabel MedicationLabel'>DOSE </span>" +
            "<span class='Quantity TextDataBold'>Variable dose </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Frequency TextData'>Every 8 hours </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='DaysOfWeek TextData'>Every Mon, Fri </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='StartCriterion TextData'>By doctor orders </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Route TextData'>Oral </span><br>" +
            "<span class='CommentLabel TextLabel MedicationLabel'>COMMENT </span>" +
            "<span class='Comment TextData'>Comment </span>",
        formattedDescription);
  }

  @Test
  public void testConstantComplexTherapyDisplay()
  {
    final Locale locale = new Locale("en");
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    fillComplexTherapyCommonValues(therapy);
    therapy.setDoseElement(createComplexDoseElement(120, 50.0, "mL/h"));
    therapy.setWhenNeeded(false);
    therapy.setStart(new DateTime(2017, 1, 1, 0, 0));

    therapyDisplayProvider.fillDisplayValues(therapy, true, false, true, locale, false);

    assertEquals("Ketoprofen (Ketonal)", therapy.getIngredientsList().get(0).getMedication().getDisplayName());
    assertEquals("100 mg / 1 mL", therapy.getIngredientsList().get(0).getQuantityDisplay());
    assertEquals("Glukoza 10%", therapy.getIngredientsList().get(1).getMedication().getDisplayName());
    assertEquals("200 mL", therapy.getIngredientsList().get(1).getQuantityDisplay());
    assertEquals(
        Dictionary.getEntry("once.every", GrammaticalGender.UNDEFINED, locale) + " 8 " + Dictionary.getEntry(
            "hours.accusative",
            GrammaticalGender.UNDEFINED,
            locale),
        therapy.getFrequencyDisplay());
    assertEquals("50 mL/h", therapy.getSpeedDisplay());
    assertNull(therapy.getWhenNeededDisplay());

    final String formattedDescription = therapy.getFormattedTherapyDisplay();

    assertEquals(
        "<span class='GenericName TextDataBold'>Ketoprofen </span>" +
            "<span class='MedicationName TextData'>(Ketonal) </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Quantity TextDataBold'>100 mg / 1 mL </span><br>" +
            "<span class='MedicationName TextData'>Glukoza 10% </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Quantity TextDataBold'>200 mL </span><br>" +
            "<span class='VolumeSumLabel TextLabel MedicationLabel'>TOTAL VOLUME </span>" +
            "<span class='VolumeSum TextData'>201 mL </span><br>" +
            "<span class='SpeedLabel TextLabel MedicationLabel'>RATE </span>" +
            "<span class='Speed TextData'>50 mL/h </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='DurationLabel TextLabel MedicationLabel'>DURATION </span>" +
            "<span class='Duration TextData'>2h </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='FrequencyLabel TextLabel MedicationLabel'>DOSING INTERVAL </span><span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Frequency TextData'>Every 8 hours </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='DaysOfWeek TextData'>Every Mon, Fri </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Route TextData'>Oral </span><br>" +
            "<span class='CommentLabel TextLabel MedicationLabel'>COMMENT </span>" +
            "<span class='Comment TextData'>Comment </span>",
        formattedDescription);
  }

  @Test
  public void testConstantComplexNoRateTherapyDisplay()
  {
    final Locale locale = new Locale("en");
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    fillComplexTherapyCommonValues(therapy);
    therapy.setDoseElement(createComplexDoseElement(null, null, "mL/h"));
    therapy.setWhenNeeded(false);

    therapyDisplayProvider.fillDisplayValues(therapy, true, locale);

    assertEquals("Ketoprofen (Ketonal)", therapy.getIngredientsList().get(0).getMedication().getDisplayName());
    assertEquals("100 mg / 1 mL", therapy.getIngredientsList().get(0).getQuantityDisplay());
    assertEquals("Glukoza 10%", therapy.getIngredientsList().get(1).getMedication().getDisplayName());
    assertEquals("200 mL", therapy.getIngredientsList().get(1).getQuantityDisplay());
    assertNull(therapy.getWhenNeededDisplay());

    final String formattedDescription = therapy.getFormattedTherapyDisplay();

    assertEquals(
        "<span class='GenericName TextDataBold'>Ketoprofen </span><span class='MedicationName TextData'>(Ketonal) " +
            "</span><br><span class='BaseLineInfusion TextLabel MedicationLabel'>DOSE </span>" +
            "<span class='Quantity TextDataBold'>100 mg / 1 mL </span><br><span class='MedicationName TextData'>Glukoza 10% </span><span class='Delimiter TextData'>" +
            "<span> &ndash; </span> </span><span class='Quantity TextDataBold'>200 mL </span><br>" +
            "<span class='VolumeSumLabel TextLabel MedicationLabel'>TOTAL VOLUME </span>" +
            "<span class='VolumeSum TextData'>201 mL </span><br><span class='FrequencyLabel TextLabel MedicationLabel'>DOSING INTERVAL </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Frequency TextData'>Every 8 hours </span><span class='Delimiter TextData'><span> &ndash; " +
            "</span> </span><span class='DaysOfWeek TextData'>Every Mon, Fri </span><span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Route TextData'>Oral </span><br><span class='CommentLabel TextLabel MedicationLabel'>COMMENT </span>" +
            "<span class='Comment TextData'>Comment </span>",
        formattedDescription);
  }

  @Test
  public void testConstantComplexTherapyShortDisplay()
  {
    final Locale locale = new Locale("en");
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    fillComplexTherapyCommonValues(therapy);
    therapy.setDoseElement(createComplexDoseElement(120, 50.0, "mL/h"));
    therapy.setWhenNeeded(false);

    therapyDisplayProvider.fillDisplayValues(therapy, false, locale);

    assertEquals("Ketoprofen (Ketonal)", therapy.getIngredientsList().get(0).getMedication().getDisplayName());
    assertEquals("100 mg / 1 mL", therapy.getIngredientsList().get(0).getQuantityDisplay());
    assertEquals("Glukoza 10%", therapy.getIngredientsList().get(1).getMedication().getDisplayName());
    assertEquals("200 mL", therapy.getIngredientsList().get(1).getQuantityDisplay());
    assertEquals(
        Dictionary.getEntry("once.every", GrammaticalGender.UNDEFINED, locale) + " 8 " + Dictionary.getEntry(
            "hours.accusative",
            GrammaticalGender.UNDEFINED,
            locale),
        therapy.getFrequencyDisplay());
    assertEquals("50 mL/h", therapy.getSpeedDisplay());
    assertNull(therapy.getWhenNeededDisplay());

    final String formattedDescription = therapy.getFormattedTherapyDisplay();

    assertEquals(
        "<span class='MedicationName TextData'>Ketonal </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Quantity TextDataBold'>100 mg / 1 mL </span><br>" +
            "<span class='MedicationName TextData'>Glukoza 10% </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Quantity TextDataBold'>200 mL </span><br>" +
            "<span class='VolumeSum TextData'>201 mL </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Speed TextData'>50 mL/h </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Duration TextData'>2h </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Frequency TextData'>Every 8 hours </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Route TextData'>Oral </span>",
        formattedDescription);
  }

  @Test
  public void testVariableComplexTherapyDisplay()
  {
    final Locale locale = new Locale("en");
    final VariableComplexTherapyDto therapy = new VariableComplexTherapyDto();
    fillComplexTherapyCommonValues(therapy);
    final List<TimedComplexDoseElementDto> timedDoseElements = new ArrayList<>();
    final TimedComplexDoseElementDto doseElement1 = new TimedComplexDoseElementDto();
    doseElement1.setDoseElement(createComplexDoseElement(15, 50.0, "mL/h"));
    doseElement1.setDoseTime(new HourMinuteDto(10, 0));
    timedDoseElements.add(doseElement1);
    final TimedComplexDoseElementDto doseElement2 = new TimedComplexDoseElementDto();
    doseElement2.setDoseElement(createComplexDoseElement(15, 100.0, "mL/h"));
    doseElement2.setDoseTime(new HourMinuteDto(10, 15));
    timedDoseElements.add(doseElement2);
    final TimedComplexDoseElementDto doseElement3 = new TimedComplexDoseElementDto();
    doseElement3.setDoseElement(createComplexDoseElement(null, 200.0, "mL/h"));
    doseElement3.setDoseTime(new HourMinuteDto(10, 30));
    timedDoseElements.add(doseElement3);

    therapy.setTimedDoseElements(timedDoseElements);

    therapyDisplayProvider.fillDisplayValues(therapy, true, locale);

    assertEquals("Ketoprofen (Ketonal)", therapy.getIngredientsList().get(0).getMedication().getDisplayName());
    assertEquals("100 mg / 1 mL", therapy.getIngredientsList().get(0).getQuantityDisplay());
    assertEquals("Glukoza 10%", therapy.getIngredientsList().get(1).getMedication().getDisplayName());
    assertEquals("200 mL", therapy.getIngredientsList().get(1).getQuantityDisplay());
    assertEquals(
        Dictionary.getEntry("once.every", GrammaticalGender.UNDEFINED, locale) + " 8 " + Dictionary.getEntry(
            "hours.accusative",
            GrammaticalGender.UNDEFINED,
            locale),
        therapy.getFrequencyDisplay());
    assertEquals("50-100-200 mL/h", therapy.getSpeedDisplay());
    assertEquals("50 mL/h", therapy.getTimedDoseElements().get(0).getSpeedDisplay());
    assertEquals("10:00–10:15", therapy.getTimedDoseElements().get(0).getIntervalDisplay());
    assertEquals("100 mL/h", therapy.getTimedDoseElements().get(1).getSpeedDisplay());
    assertEquals("10:15–10:30", therapy.getTimedDoseElements().get(1).getIntervalDisplay());
    assertEquals("200 mL/h", therapy.getTimedDoseElements().get(2).getSpeedDisplay());
    assertEquals("10:30–...", therapy.getTimedDoseElements().get(2).getIntervalDisplay());

    final String formattedDescription = therapy.getFormattedTherapyDisplay();

    assertEquals(
        "<span class='GenericName TextDataBold'>Ketoprofen </span>" +
            "<span class='MedicationName TextData'>(Ketonal) </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Quantity TextDataBold'>100 mg / 1 mL </span><br>" +
            "<span class='MedicationName TextData'>Glukoza 10% </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Quantity TextDataBold'>200 mL </span><br>" +
            "<span class='VolumeSumLabel TextLabel MedicationLabel'>TOTAL VOLUME </span>" +
            "<span class='VolumeSum TextData'>201 mL </span><br>" +
            "<span class='SpeedFormulaLabel TextLabel MedicationLabel'>DOSE </span>" +
            "<span class='SpeedFormula TextData'>Variable rate </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='FrequencyLabel TextLabel MedicationLabel'>DOSING INTERVAL </span><span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Frequency TextData'>Every 8 hours </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='DaysOfWeek TextData'>Every Mon, Fri </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Route TextData'>Oral </span><br>" +
            "<span class='CommentLabel TextLabel MedicationLabel'>COMMENT </span>" +
            "<span class='Comment TextData'>Comment </span>",
        formattedDescription);
  }
  @Test
  public void testGetFormattedDecimalValue()
  {
    final Locale locale = new Locale("en");
    final String value1 = "3.652";
    final String value2 = "3.652 ml";
    final String value3 = "3 ml";
    final String value4 = "150";
    final String value5 = "volume sum equals 150.654 ml";
    final String value6 = "volume sum equals 500 ml";
    final String value7 = "150.654ml";
    final String value8 = "300ml";

    final String formattedValue1 = therapyDisplayProvider.getFormattedDecimalValue(value1, locale, false);
    final String formattedValue2 = therapyDisplayProvider.getFormattedDecimalValue(value2, locale, false);
    final String formattedValue3 = therapyDisplayProvider.getFormattedDecimalValue(value3, locale, false);
    final String formattedValue4 = therapyDisplayProvider.getFormattedDecimalValue(value4, locale, false);
    final String formattedValue5 = therapyDisplayProvider.getFormattedDecimalValue(value5, locale, false);
    final String formattedValue6 = therapyDisplayProvider.getFormattedDecimalValue(value6, locale, false);
    final String formattedValue7 = therapyDisplayProvider.getFormattedDecimalValue(value7, locale, false);
    final String formattedValue8 = therapyDisplayProvider.getFormattedDecimalValue(value8, locale, false);

    assertEquals("3.<span class='TextDataSmallerDecimal'>652</span>", formattedValue1);
    assertEquals("3.<span class='TextDataSmallerDecimal'>652</span> ml", formattedValue2);
    assertEquals("3 ml", formattedValue3);
    assertEquals("150", formattedValue4);
    assertEquals("volume sum equals 150.<span class='TextDataSmallerDecimal'>654</span> ml", formattedValue5);
    assertEquals("volume sum equals 500 ml", formattedValue6);
    assertEquals("150.<span class='TextDataSmallerDecimal'>654</span>ml", formattedValue7);
    assertEquals("300ml", formattedValue8);
  }

  @Test
  public void testdecimalToStringEN() throws ParseException
  {
    final Locale locale = new Locale("en");

    assertEquals("11.667", therapyDisplayProvider.decimalToString(11.66666, locale));
    assertEquals("1.667", therapyDisplayProvider.decimalToString(1.66666, locale));
    assertEquals("0.667", therapyDisplayProvider.decimalToString(0.66666, locale));
    assertEquals("0.0667", therapyDisplayProvider.decimalToString(0.06666, locale));
    assertEquals("0.00667", therapyDisplayProvider.decimalToString(0.006666, locale));
    assertEquals("0.000667", therapyDisplayProvider.decimalToString(0.0006666, locale));
    assertEquals("1", therapyDisplayProvider.decimalToString(1.0000006, locale));
    assertEquals("1.007", therapyDisplayProvider.decimalToString(1.006666, locale));
    assertEquals("0", therapyDisplayProvider.decimalToString(0.0, locale));
    assertEquals("", therapyDisplayProvider.decimalToString(null, locale));
  }

  @Test
  public void testdecimalToStringSLO() throws ParseException
  {
    final Locale locale = new Locale("sl");

    assertEquals("11,667", therapyDisplayProvider.decimalToString(11.66666, locale));
    assertEquals("1,667", therapyDisplayProvider.decimalToString(1.66666, locale));
    assertEquals("0,667", therapyDisplayProvider.decimalToString(0.66666, locale));
    assertEquals("0,0667", therapyDisplayProvider.decimalToString(0.06666, locale));
    assertEquals("0,00667", therapyDisplayProvider.decimalToString(0.006666, locale));
    assertEquals("0,000667", therapyDisplayProvider.decimalToString(0.0006666, locale));
    assertEquals("1", therapyDisplayProvider.decimalToString(1.0000006, locale));
    assertEquals("1,007", therapyDisplayProvider.decimalToString(1.006666, locale));
    assertEquals("0", therapyDisplayProvider.decimalToString(0.0, locale));
    assertEquals("", therapyDisplayProvider.decimalToString(null, locale));
  }

  @Test
  public void testGetTherapyDurationDisplay()
  {
    final Locale locale = new Locale("sl");

    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setStart(new DateTime(2016, 12, 10, 8, 0));
    therapy.setEnd(new DateTime(2016, 12, 15, 16, 0));
    assertEquals("<span class='TherapyDuration'><br>" +
                     "<span class='DurationLabel TextLabel MedicationLabel'>Trajanje </span>" +
                     "<span class='Duration TextData'>5 dni, 8 ur </span></span>",
                 therapyDisplayProvider.getTherapyDurationDisplay(therapy, locale));
    therapy.setEnd(new DateTime(2016, 12, 11, 9, 1));
    assertEquals("<span class='TherapyDuration'><br>" +
                     "<span class='DurationLabel TextLabel MedicationLabel'>Trajanje </span>" +
                     "<span class='Duration TextData'>1 dan, 1 ura, 1 min </span></span>",
                 therapyDisplayProvider.getTherapyDurationDisplay(therapy, locale));
    therapy.setEnd(new DateTime(2016, 12, 10, 10, 15));
    assertEquals("<span class='TherapyDuration'><br>" +
                     "<span class='DurationLabel TextLabel MedicationLabel'>Trajanje </span>" +
                     "<span class='Duration TextData'>2 ur, 15 min </span></span>",
                 therapyDisplayProvider.getTherapyDurationDisplay(therapy, locale));
    therapy.setEnd(new DateTime(2016, 12, 10, 8, 15));
    assertEquals("<span class='TherapyDuration'><br>" +
                     "<span class='DurationLabel TextLabel MedicationLabel'>Trajanje </span>" +
                     "<span class='Duration TextData'>15 min </span></span>",
                 therapyDisplayProvider.getTherapyDurationDisplay(therapy, locale));
    therapy.setEnd(new DateTime(2016, 12, 25, 6, 0));
    assertEquals("<span class='TherapyDuration'><br>" +
                     "<span class='DurationLabel TextLabel MedicationLabel'>Trajanje </span>" +
                     "<span class='Duration TextData'>2 tednov, 22 ur </span></span>",
                 therapyDisplayProvider.getTherapyDurationDisplay(therapy, locale));
    therapy.setEnd(new DateTime(2016, 12, 11, 8, 0));
    assertEquals("<span class='TherapyDuration'><br>" +
                     "<span class='DurationLabel TextLabel MedicationLabel'>Trajanje </span>" +
                     "<span class='Duration TextData'>1 dan </span></span>",
                 therapyDisplayProvider.getTherapyDurationDisplay(therapy, locale));
  }

  @Test
  public void fillDisplayValuesCharacterEscapingTest()
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();

    therapy.setCompositionUid("<script>");
    therapy.setEhrOrderName("<Script>");
    therapy.setTherapyDescription("<script>");
    therapy.setDaysOfWeek(Lists.newArrayList("<script>"));
    therapy.setComment("<script>");
    therapy.setPrescriberName("<script>");
    therapy.setComposerName("<script>");
    final IndicationDto clinicalIndication = new IndicationDto("<script>", "<script>");
    therapy.setClinicalIndication(clinicalIndication);
    therapy.setLinkName("<script>");
    therapy.setCriticalWarnings(Lists.newArrayList("<script>"));
    final MedicationDto aspirin = new MedicationDto();
    aspirin.setName("<script>");
    aspirin.setGenericName("<script>");
    therapy.setMedication(aspirin);
    final DispenseDetailsDto dispenseDetailsDto = new DispenseDetailsDto();
    dispenseDetailsDto.setUnit("<script>");
    dispenseDetailsDto.setDaysDuration(10);
    therapy.setDispenseDetails(dispenseDetailsDto);
    therapy.setQuantityUnit("<script>");
    therapy.setQuantityDenominatorUnit("<script>");
    final SimpleDoseElementDto simpleDoseElement = new SimpleDoseElementDto();
    simpleDoseElement.setDoseDescription("<script>");
    therapy.setDoseElement(simpleDoseElement);

    therapyDisplayProvider.fillDisplayValues(therapy, true, new Locale("en"));

    assertEquals("<span class='GenericName TextDataBold'>&lt;script&gt; </span>" +
                     "<span class='MedicationName TextData'>(&lt;script&gt;) </span><br>" +
                     "<span class='DoseLabel TextLabel MedicationLabel'>DOSE </span>" +
                     "<span class='DescriptiveQuantity TextDataBold'>&lt;script&gt; </span>" +
                     "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
                     "<span class='DaysOfWeek TextData'>Every &lt;script&gt;.f3l </span><br><span class='DispenseDetails" +
                     " TextLabel MedicationLabel'>SUPPLY FOR DAYS </span><span class='DispenseDetails TextData'>" +
                     "10 </span><br><span class='CommentLabel TextLabel MedicationLabel'>COMMENT </span><span" +
                     " class='Comment TextData'>&lt;script&gt; </span><span class='IndicationLabel TextLabel" +
                     " MedicationLabel'>INDICATION </span><span class='Indication TextData'>&lt;script&gt; </span>",
                 therapy.getFormattedTherapyDisplay());
  }

  @Test
  public void fillDisplayValuesCharacterNonEscapingTest()
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();

    therapy.setCompositionUid("234");
    therapy.setEhrOrderName("OrderName");
    therapy.setTherapyDescription("This is therapy");
    therapy.setDaysOfWeek(Lists.newArrayList("Monday"));
    therapy.setComment("This is comment");
    therapy.setPrescriberName("Luka");
    therapy.setComposerName("Joze");
    final IndicationDto clinicalIndication = new IndicationDto("Ind1", "Ind2");
    therapy.setClinicalIndication(clinicalIndication);
    therapy.setLinkName("Linkname");
    therapy.setCriticalWarnings(Lists.newArrayList("This is critical warning!"));
    final MedicationDto aspirin = new MedicationDto();
    aspirin.setName("Aspirin 25mg");
    aspirin.setGenericName("Aspirin");
    therapy.setMedication(aspirin);
    final DispenseDetailsDto dispenseDetails = new DispenseDetailsDto();
    dispenseDetails.setUnit("mL");
    dispenseDetails.setDaysDuration(20);
    therapy.setDispenseDetails(dispenseDetails);
    therapy.setQuantityUnit("mg");
    therapy.setQuantityDenominatorUnit("g");
    final SimpleDoseElementDto simpleDoseElement = new SimpleDoseElementDto();
    simpleDoseElement.setDoseDescription("small dosage");
    therapy.setDoseElement(simpleDoseElement);

    therapyDisplayProvider.fillDisplayValues(therapy, true, new Locale("en"));

    assertEquals("<span class='GenericName TextDataBold'>Aspirin </span><span class='MedicationName TextData'>" +
                     "(Aspirin 25mg) </span><br><span class='DoseLabel TextLabel MedicationLabel'>DOSE </span>" +
                     "<span class='DescriptiveQuantity TextDataBold'>small dosage </span><span class='Delimiter TextData'>" +
                     "<span> &ndash; </span> </span><span class='DaysOfWeek TextData'>Every Mon </span><br><span " +
                     "class='DispenseDetails TextLabel MedicationLabel'>SUPPLY FOR DAYS </span><span " +
                     "class='DispenseDetails TextData'>20 </span><br><span class='CommentLabel TextLabel " +
                     "MedicationLabel'>COMMENT </span><span class='Comment TextData'>This is comment </span>" +
                     "<span class='IndicationLabel TextLabel MedicationLabel'>INDICATION </span><span class='Indication" +
                     " TextData'>Ind2 </span>",
                 therapy.getFormattedTherapyDisplay());
  }

  private void fillSimpleTherapyCommonValues(final SimpleTherapyDto therapy, final DosingFrequencyDto frequency)
  {
    therapy.setMedication(createMedicationDto(1L, "Lekadol", "Paracetamol", MedicationTypeEnum.MEDICATION));
    therapy.setDosingFrequency(frequency);
    therapy.setDaysOfWeek(createDaysList("MONDAY", "FRIDAY"));
    therapy.setPrescriberName("Tadej Avčin");
    therapy.setComment("Comment");
    final MedicationRouteDto routeDto = new MedicationRouteDto();
    routeDto.setCode("1");
    routeDto.setName("Oral");
    therapy.setRoutes(Collections.singletonList(routeDto));
    therapy.setStart(new DateTime(2013, 3, 3, 0, 0));
    therapy.setQuantityUnit("mg");
    therapy.setQuantityDenominatorUnit("mL");
  }

  private void fillComplexTherapyCommonValues(final ComplexTherapyDto therapy)
  {
    final List<InfusionIngredientDto> ingredientsList = new ArrayList<>();
    final InfusionIngredientDto ingredient1 = new InfusionIngredientDto();
    ingredient1.setMedication(createMedicationDto(1L, "Ketonal", "Ketoprofen", MedicationTypeEnum.MEDICATION));
    ingredient1.setQuantity(100.0);
    ingredient1.setQuantityUnit("mg");
    ingredient1.setQuantityDenominator(1.0);
    ingredient1.setQuantityDenominatorUnit("mL");
    ingredientsList.add(ingredient1);
    final InfusionIngredientDto ingredient2 = new InfusionIngredientDto();
    ingredient2.setMedication(createMedicationDto(1L, "Glukoza 10%", "Glukoza", MedicationTypeEnum.DILUENT));
    ingredient2.setQuantityDenominator(200.0);
    ingredient2.setQuantityDenominatorUnit("mL");
    ingredientsList.add(ingredient2);
    therapy.setIngredientsList(ingredientsList);

    therapy.setVolumeSum(201.0);
    therapy.setVolumeSumUnit("mL");
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.BETWEEN_DOSES, 8.0));
    therapy.setDaysOfWeek(createDaysList("MONDAY", "FRIDAY"));
    therapy.setPrescriberName("Tadej Avčin");
    therapy.setComment("Comment");
    final MedicationRouteDto routeDto = new MedicationRouteDto();
    routeDto.setCode("1");
    routeDto.setName("Oral");
    therapy.setRoutes(Collections.singletonList(routeDto));
    therapy.setStart(new DateTime(2013, 3, 3, 0, 0));
  }

  private MedicationDto createMedicationDto(
      final long id,
      final String name,
      final String genericName,
      final MedicationTypeEnum type)
  {
    final MedicationDto medicationDto = new MedicationDto();
    medicationDto.setId(id);
    medicationDto.setName(name);
    medicationDto.setGenericName(genericName);
    medicationDto.setMedicationType(type);
    return medicationDto;
  }

  private List<String> createDaysList(final String... days)
  {
    return Arrays.asList(days);
  }

  private SimpleDoseElementDto createSimpleDoseElement(
      final Double quantity,
      final Double quantityDenominator)
  {
    final SimpleDoseElementDto doseElementDto = new SimpleDoseElementDto();
    doseElementDto.setQuantity(quantity);
    doseElementDto.setQuantityDenominator(quantityDenominator);
    return doseElementDto;
  }

  private ComplexDoseElementDto createComplexDoseElement(
      final Integer duration,
      final Double speed,
      final String speedUnit)
  {
    final ComplexDoseElementDto doseElementDto = new ComplexDoseElementDto();
    doseElementDto.setDuration(duration);
    doseElementDto.setRate(speed);
    doseElementDto.setRateUnit(speedUnit);
    return doseElementDto;
  }
}
