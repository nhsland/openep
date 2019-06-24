package com.marand.thinkmed.medications.therapy.report;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.maf.core.dictionary.MafDictionary;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.api.externals.data.object.ExternalCatalogDto;
import com.marand.thinkmed.medications.TherapyReportStatusEnum;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.api.internal.dto.CodedNameDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.OxygenTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.config.MedsProperties;
import com.marand.thinkmed.medications.connector.data.object.AllergiesStatus;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationResultEnum;
import com.marand.thinkmed.medications.dto.administration.BolusAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.DoseAdministration;
import com.marand.thinkmed.medications.dto.administration.InfusionSetChangeDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StartOxygenAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StopAdministrationDto;
import com.marand.thinkmed.medications.dto.overview.ContinuousInfusionTherapyRowDtoDto;
import com.marand.thinkmed.medications.dto.overview.OxygenTherapyRowDtoDto;
import com.marand.thinkmed.medications.dto.overview.TherapyRowDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayReportAdministrationDateGroupDto;
import com.marand.thinkmed.medications.dto.report.TherapyReportHourDoseTimeDto;
import com.marand.thinkmed.medications.dto.report.TherapySurgeryReportDto;
import com.marand.thinkmed.medications.dto.report.TherapySurgeryReportElementDto;
import com.marand.thinkmed.medications.therapy.util.TherapyBuilderUtils;
import org.assertj.core.util.Lists;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"Duplicates", "TooBroadScope"})
@RunWith(SpringJUnit4ClassRunner.class)
public class TherapyReportDataProviderTest
{
  @InjectMocks
  private final TherapyReportDataProviderImpl therapyReportDataProviderImpl = new TherapyReportDataProviderImpl();

  @Before
  public void setup()
  {
    Dictionary.setDelegate(new MafDictionary("Dictionary", "", new Locale("en")));
    final MedsProperties medsProperties = new MedsProperties();
    medsProperties.setAntimicrobialDaysCountStartsWithOne(true);
    therapyReportDataProviderImpl.setMedsProperties(medsProperties);
    therapyReportDataProviderImpl.setTherapyDisplayProvider(new TherapyDisplayProvider());
  }

  @Test
  public void mapAdministrationsTest()
  {
    final AdministrationDto administration1 = new StartOxygenAdministrationDto();
    administration1.setAdministrationTime(new DateTime(2018, 10, 17, 15, 0));

    final AdministrationDto administration2 = new BolusAdministrationDto();
    administration2.setAdministrationTime(new DateTime(2018, 10, 16, 15, 0));
    final TherapyDoseDto therapyDose2 = new TherapyDoseDto();
    therapyDose2.setNumerator(22.0);
    therapyDose2.setNumeratorUnit("mg");
    therapyDose2.setDenominator(10.0);
    therapyDose2.setDenominatorUnit("ml");
    ((DoseAdministration)administration2).setAdministeredDose(therapyDose2);

    final AdministrationDto administration3 = new StopAdministrationDto();
    administration3.setAdministrationTime(new DateTime(2018, 10, 15, 15, 0));
    administration3.setComment("blablabla");

    final AdministrationDto administration4 = new AdjustInfusionAdministrationDto();
    administration4.setAdministrationTime(new DateTime(2018, 10, 15, 16, 0));

    final List<AdministrationDto> administrations = Lists.newArrayList();
    administrations.add(administration1);
    administrations.add(administration2);
    administrations.add(administration3);
    administrations.add(administration4);

    final List<AdministrationDto> administrationsSlo = Lists.newArrayList();
    administrationsSlo.add(administration3);

    final Locale locale = new Locale("en");

    final Locale localeSlo = new Locale("sl");

    final List<TherapyDayReportAdministrationDateGroupDto> result = therapyReportDataProviderImpl.mapAdministrations(
        administrations,
        locale);

    final List<TherapyDayReportAdministrationDateGroupDto> resultSlo = therapyReportDataProviderImpl.mapAdministrations(
        administrationsSlo,
        localeSlo);

    final TherapyDayReportAdministrationDateGroupDto result1 = result.get(0);
    assertThat(result1.getDate()).isEqualTo("17-Oct");
    assertThat(result1.getTherapyDayReportAdministrationDtos().get(0).getComment()).isEqualTo("");
    assertThat(result1.getTherapyDayReportAdministrationDtos().get(0).getTimeDose()).isEqualTo("15:00");
    assertThat(result1.getTherapyDayReportAdministrationDtos().size()).isEqualTo(1);

    final TherapyDayReportAdministrationDateGroupDto result2 = result.get(1);
    assertThat(result2.getDate()).isEqualTo("16-Oct");
    assertThat(result2.getTherapyDayReportAdministrationDtos().get(0).getComment()).isEqualTo("");
    assertThat(result2.getTherapyDayReportAdministrationDtos().get(0).getTimeDose()).isEqualTo("15:00 - 22mg");
    assertThat(result2.getTherapyDayReportAdministrationDtos().size()).isEqualTo(1);

    final TherapyDayReportAdministrationDateGroupDto result3 = result.get(2);
    assertThat(result3.getDate()).isEqualTo("15-Oct");
    assertThat(result3.getTherapyDayReportAdministrationDtos().get(0).getComment()).isEqualTo("blablabla");
    assertThat(result3.getTherapyDayReportAdministrationDtos().get(0).getTimeDose()).isEqualTo("15:00 - Stopped");
    assertThat(result3.getTherapyDayReportAdministrationDtos().get(1).getComment()).isEqualTo("");
    assertThat(result3.getTherapyDayReportAdministrationDtos().get(1).getTimeDose()).isEqualTo("16:00");
    assertThat(result3.getTherapyDayReportAdministrationDtos().size()).isEqualTo(2);

    assertThat(result.size()).isEqualTo(3);

    assertThat(resultSlo.get(0).getDate()).isEqualTo("15.10");
    assertThat(resultSlo.get(0).getTherapyDayReportAdministrationDtos().get(0).getTimeDose()).isEqualTo("15:00 - Ukinjena");
    assertThat(resultSlo.size()).isEqualTo(1);
    assertThat(resultSlo.get(0).getTherapyDayReportAdministrationDtos().size()).isEqualTo(1);
  }

  @Test
  public void mapToTimeDoseForTherapySurgeryReportGivenTest()
  {
    final Locale locale = new Locale("en");

    final AdministrationDto administration1 = createStartAdministrationForTherapy(
        "1",
        new DateTime(22018, 3, 3, 2, 8),
        AdministrationResultEnum.GIVEN);

    final String mappedAdministration1 = therapyReportDataProviderImpl.mapToTimeDoseForTherapySurgeryReport(
        administration1,
        locale);

    assertThat(mappedAdministration1).isEqualTo("02:08 - 33 mg");
  }

  @Test
  public void mapToTimeDoseForTherapySurgeryReportNotGivenTest()
  {
    final Locale locale = new Locale("en");

    final AdministrationDto administration2 = createStartAdministrationForTherapy(
        "2",
        new DateTime(2018, 3, 3, 2, 8),
        AdministrationResultEnum.NOT_GIVEN);

    final String mappedAdministration2 = therapyReportDataProviderImpl.mapToTimeDoseForTherapySurgeryReport(
        administration2,
        locale);

    assertThat(mappedAdministration2).isEqualTo("02:08 - NOT GIVEN");
  }

  @Test
  public void mapToTimeDoseForTherapySurgeryReportSelfAdministeredTest()
  {
    final Locale locale = new Locale("en");

    final AdministrationDto administration3 = createStartAdministrationForTherapy(
        "3",
        new DateTime(2018, 3, 3, 2, 8),
        AdministrationResultEnum.SELF_ADMINISTERED);

    final String mappedAdministration3 = therapyReportDataProviderImpl.mapToTimeDoseForTherapySurgeryReport(
        administration3,
        locale);

    assertThat(mappedAdministration3).isEqualTo("02:08 - 33 mg");
  }

  @Test
  public void mapToTimeDoseForTherapySurgeryReportDeferredTest()
  {
    final Locale locale = new Locale("en");

    final AdministrationDto administration4 = createStartAdministrationForTherapy(
        "4",
        new DateTime(2018, 3, 3, 2, 8),
        AdministrationResultEnum.DEFER);

    final String mappedAdministration4 = therapyReportDataProviderImpl.mapToTimeDoseForTherapySurgeryReport(
        administration4,
        locale);

    assertThat(mappedAdministration4).isEqualTo("02:08 - DEFERRED");
  }

  @Test
  public void mapToTimeDoseForTherapySurgeryReportDeferredWithCommentTest()
  {

    final AdministrationDto administration5 = createStartAdministrationForTherapy(
        "5",
        new DateTime(2018, 3, 3, 2, 8),
        AdministrationResultEnum.DEFER);
    administration5.setComment("comment1");

    final Locale locale = new Locale("en");
    final String mappedAdministration5 = therapyReportDataProviderImpl.mapToTimeDoseForTherapySurgeryReport(
        administration5,
        locale);

    assertThat(mappedAdministration5).isEqualTo("02:08 - DEFERRED - comment1");
  }

  @Test
  public void mapToTimeDoseForTherapySurgeryReportNotGiveReasonTest()
  {
    final Locale locale = new Locale("en");

    final AdministrationDto administration6 = createStartAdministrationForTherapy(
        "6",
        new DateTime(2018, 3, 3, 2, 8),
        AdministrationResultEnum.NOT_GIVEN);
    administration6.setNotAdministeredReason(new CodedNameDto("1", "reason1"));

    final String mappedAdministration6 = therapyReportDataProviderImpl.mapToTimeDoseForTherapySurgeryReport(
        administration6,
        locale);

    assertThat(mappedAdministration6).isEqualTo("02:08 - NOT GIVEN - reason1");
  }

  @Test
  public void mapToTimeDoseForTherapySurgeryReportGivenEmptyDoseTest()
  {
    final Locale locale = new Locale("en");

    final StartAdministrationDto administration7 = new StartAdministrationDto();
    administration7.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administration7.setAdministeredDose(new TherapyDoseDto());
    administration7.setAdministrationTime(new DateTime(2018, 3, 4, 5, 12));

    final String mappedAdministration7 = therapyReportDataProviderImpl.mapToTimeDoseForTherapySurgeryReport(
        administration7,
        locale);

    assertThat(mappedAdministration7).isEqualTo("05:12");
  }

  @Test
  public void mapToTimeDoseForTherapySurgeryReportDescriptiveTherapyTest()
  {
    final Locale locale = new Locale("en");

    final StartAdministrationDto administration8 = new StartAdministrationDto();
    administration8.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administration8.setAdministrationTime(new DateTime(2018, 3, 4, 5, 12));

    final String mappedAdministration8 = therapyReportDataProviderImpl.mapToTimeDoseForTherapySurgeryReport(
        administration8,
        locale);

    assertThat(mappedAdministration8).isEqualTo("05:12");
  }

  @Test
  public void mapToTimeDoseForTherapySurgeryReportNotDoseAdministrationTest()
  {
    final Locale locale = new Locale("en");

    final InfusionSetChangeDto administration9 = new InfusionSetChangeDto();
    administration9.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administration9.setAdministrationTime(new DateTime(2018, 3, 4, 5, 12));

    final String mappedAdministration9 = therapyReportDataProviderImpl.mapToTimeDoseForTherapySurgeryReport(
        administration9,
        locale);

    assertThat(mappedAdministration9).isEqualTo("");
  }

  @Test
  public void extractOnlyLastAdministrationTest()
  {
    final AdministrationDto administration1 = new StartAdministrationDto();
    administration1.setAdministrationTime(new DateTime(2018, 2, 1, 1, 10));
    administration1.setAdministrationId("extractOnlyLastAdministrationTest1");

    final AdministrationDto administrationEdit1 = new AdjustInfusionAdministrationDto();
    administrationEdit1.setAdministrationTime(new DateTime(2018, 2, 1, 1, 12));
    administrationEdit1.setAdministrationId("extractOnlyLastAdministrationTestEdit1");

    final AdministrationDto administrationEdit2 = new AdjustInfusionAdministrationDto();
    administrationEdit2.setAdministrationTime(new DateTime(2018, 2, 1, 1, 14));
    administrationEdit2.setAdministrationId("extractOnlyLastAdministrationTestEdit2");

    final AdministrationDto administration2 = new StopAdministrationDto();
    administration2.setAdministrationTime(new DateTime(2018, 2, 2, 2, 10));
    administration2.setAdministrationId("extractOnlyLastAdministrationTest2");

    final List<AdministrationDto> administrations1 = Lists.newArrayList();
    administrations1.add(administration1);
    administrations1.add(administration2);
    administrations1.add(administrationEdit1);
    administrations1.add(administrationEdit2);

    final List<AdministrationDto> result = therapyReportDataProviderImpl.extractOnlyLastAdministration(administrations1);

    assertThat(result.size()).isEqualTo(1);
    assertThat(result.get(0).getAdministrationId()).isEqualTo("extractOnlyLastAdministrationTestEdit2");
  }

  @Test
  public void extractOnlyLastAdministrationTestEmptyTest()
  {
    final List<AdministrationDto> result = therapyReportDataProviderImpl.extractOnlyLastAdministration(Lists.emptyList());

    assertThat(result).isNotNull();
    assertThat(result.size()).isEqualTo(0);
  }

  @Test
  public void mapConstantComplexTherapyTimeDosesCITest()
  {
    final Locale locale = new Locale("en");

    final ConstantComplexTherapyDto contionousInfusion = TherapyBuilderUtils.createFullConstantComplexTherapyContinuousInfusion("CI");
    contionousInfusion.setSpeedDisplay("30 mL/h");
    final List<TherapyReportHourDoseTimeDto> result = therapyReportDataProviderImpl.mapConstantComplexTherapyTimeDoses(contionousInfusion,
                                                                                                                       locale);

    assertThat("").isEqualTo(result.get(0).getDoseTimeDisplay());
    assertThat("").isEqualTo(result.get(1).getDoseTimeDisplay());
    assertThat("").isEqualTo(result.get(2).getDoseTimeDisplay());
  }

  @Test
  public void mapConstantComplexTherapyTimeDosesWithSpeedDisplayTest()
  {
    final Locale locale = new Locale("en");

    final ConstantComplexTherapyDto speedDisplayTherapy = TherapyBuilderUtils.createMinimalConstantComplexTherapy("SpeedDisplay");
    speedDisplayTherapy.setSpeedDisplay("30 mL/h");
    speedDisplayTherapy.getDoseTimes().add(new HourMinuteDto(12, 15));
    speedDisplayTherapy.getDoseTimes().add(new HourMinuteDto(0, 15));
    final List<TherapyReportHourDoseTimeDto> result = therapyReportDataProviderImpl.mapConstantComplexTherapyTimeDoses(speedDisplayTherapy,
                                                                                                 locale);

    assertThat("30 mL/h" + "\n" + "12:15").isEqualTo(result.get(0).getDoseTimeDisplay());
    assertThat("30 mL/h" + "\n" + "00:15").isEqualTo(result.get(1).getDoseTimeDisplay());
    assertThat("").isEqualTo(result.get(2).getDoseTimeDisplay());
  }

  @Test
  public void mapConstantComplexTherapyTimeDosesWithVolumeSumDisplayTest()
  {
    final Locale locale = new Locale("en");

    final ConstantComplexTherapyDto volumeSumDsiplayTherapy = TherapyBuilderUtils.createMinimalConstantComplexTherapy("VolumeSumDisplay");
    volumeSumDsiplayTherapy.setVolumeSumDisplay("50 mL");
    volumeSumDsiplayTherapy.getIngredientsList().add(new InfusionIngredientDto());
    volumeSumDsiplayTherapy.getDoseTimes().add(new HourMinuteDto(10, 0));
    volumeSumDsiplayTherapy.getDoseTimes().add(new HourMinuteDto(20, 0));

    final List<TherapyReportHourDoseTimeDto> result = therapyReportDataProviderImpl.mapConstantComplexTherapyTimeDoses(volumeSumDsiplayTherapy,
                                                                                                 locale);

    assertThat("50 mL" + "\n" + "10:00").isEqualTo(result.get(0).getDoseTimeDisplay());
    assertThat("50 mL" + "\n" + "20:00").isEqualTo(result.get(1).getDoseTimeDisplay());
    assertThat("").isEqualTo(result.get(2).getDoseTimeDisplay());
  }

  @Test
  public void mapConstantComplexTherapyTimeDosesWithOneIngridientTest()
  {
    final Locale locale = new Locale("en");

    final ConstantComplexTherapyDto oneIngridientTherapy = TherapyBuilderUtils.createMinimalConstantComplexTherapy("OneIngridient");
    oneIngridientTherapy.getDoseTimes().add(new HourMinuteDto(10, 0));
    oneIngridientTherapy.getDoseTimes().add(new HourMinuteDto(20, 0));
    oneIngridientTherapy.getIngredientsList().get(0).setQuantityDisplay("100 mL");

    final List<TherapyReportHourDoseTimeDto> result = therapyReportDataProviderImpl.mapConstantComplexTherapyTimeDoses(oneIngridientTherapy, locale);

    assertThat("100 mL" + "\n" + "10:00").isEqualTo(result.get(0).getDoseTimeDisplay());
    assertThat("100 mL" + "\n" + "20:00").isEqualTo(result.get(1).getDoseTimeDisplay());
    assertThat("").isEqualTo(result.get(2).getDoseTimeDisplay());
  }

  @Test
  public void mapConstantComplexTherapyTimeDosesWithManyIngridientsTest()
  {
    final Locale locale = new Locale("en");

    final ConstantComplexTherapyDto manyIngridientsTherapy = TherapyBuilderUtils.createMinimalConstantComplexTherapy("manyIngridients");
    manyIngridientsTherapy.getDoseTimes().add(new HourMinuteDto(10, 0));
    manyIngridientsTherapy.getDoseTimes().add(new HourMinuteDto(20, 0));
    final InfusionIngredientDto dopamine = new InfusionIngredientDto();
    dopamine.setQuantity(100.0);
    dopamine.setQuantityUnit("mL");
    dopamine.setQuantityDisplay("100 mL");
    final InfusionIngredientDto glucose = new InfusionIngredientDto();
    glucose.setQuantity(50.0);
    glucose.setQuantityUnit("mL");
    glucose.setQuantityDisplay("50 mL");
    manyIngridientsTherapy.getIngredientsList().add(dopamine);
    manyIngridientsTherapy.getIngredientsList().add(glucose);

    final List<TherapyReportHourDoseTimeDto> result = therapyReportDataProviderImpl.mapConstantComplexTherapyTimeDoses(manyIngridientsTherapy, locale);

    assertThat("10:00").isEqualTo(result.get(0).getDoseTimeDisplay());
    assertThat("20:00").isEqualTo(result.get(1).getDoseTimeDisplay());
    assertThat("").isEqualTo(result.get(2).getDoseTimeDisplay());
  }

  @Test
  public void mapTherapyRowDtoToTherapySurgeryReportDtoEmptyInputTest()
  {
    final Locale locale = new Locale("en");
    final DateTime when = new DateTime(2018, 10, 10, 10, 10);

    final List<TherapyRowDto> therapies = Lists.emptyList();

    final PatientDataForTherapyReportDto patientDataForTherapyReportDto = createMockedPatientData();

    final TherapySurgeryReportDto result = therapyReportDataProviderImpl.mapTherapyRowDtoToTherapySurgeryReportDto(
        therapies,
        locale,
        when,
        patientDataForTherapyReportDto);

    assertThat(result).isNotNull();
    assertThat(result.getPatientData()).isNotNull();
    assertThat(result.getCurrentDate()).isEqualTo("10-Oct-2018 10:10");
  }

  //@Test
  //public void mapTherapyRowDtoToTherapySurgeryReportDtoTest()
  //{
  //  final ConstantComplexTherapyDto minimalConstantComplexTherapyWithRate = TherapyBuilderUtils.createMinimalConstantComplexTherapyWithRate(
  //      "minimalConstantComplexTherapyWithRate");
  //  final ConstantSimpleTherapyDto minimalSimpleTherapyDto = TherapyBuilderUtils.createMinimalSimpleTherapyDto(
  //      "minimalSimpleTherapyDto");
  //  final ConstantComplexTherapyDto fullConstantComplexTherapy = TherapyBuilderUtils.createFullConstantComplexTherapy(
  //      "fullConstantComplexTherapy");
  //  final ConstantSimpleTherapyDto fullConstantSimpleTherapyDoseRange = TherapyBuilderUtils.createFullConstantSimpleTherapyDoseRange(
  //      "fullConstantSimpleTherapyDoseRange");
  //  final ConstantComplexTherapyDto minimalConstantComplexTherapyContinuousInfusion = TherapyBuilderUtils.createMinimalConstantComplexTherapyContinuousInfusion(
  //      "minimalConstantComplexTherapyContinuousInfusion");
  //  final VariableSimpleTherapyDto fullVariableSimpleTherapy = TherapyBuilderUtils.createFullVariableSimpleTherapy(
  //      "fullVariableSimpleTherapy");
  //  final VariableSimpleTherapyDto fullVariableSimpleTherapyDischargeProtocol = TherapyBuilderUtils.createFullVariableSimpleTherapyDischargeProtocol(
  //      "fullVariableSimpleTherapyDischargeProtocol");
  //  final VariableComplexTherapyDto fullVariableComplexTherapy = TherapyBuilderUtils.createFullVariableComplexTherapy(
  //      "fullVariableComplexTherapy");
  //  final VariableSimpleTherapyDto minimalVariableSimpleTherapy = TherapyBuilderUtils.createMinimalVariableSimpleTherapy(
  //      "minimalVariableSimpleTherapy");
  //  final ConstantComplexTherapyDto minimalConstantComplexTherapy = TherapyBuilderUtils.createMinimalConstantComplexTherapy(
  //      "minimalConstantComplexTherapy");
  //  final ConstantSimpleTherapyDto fullConstantSimpleTherapy = TherapyBuilderUtils.createFullConstantSimpleTherapy(
  //      "fullConstantSimpleTherapy");
  //  final OxygenTherapyDto oxygenTherapy = TherapyBuilderUtils.createOxygenTherapy("oxygenTherapy");
  //  final VariableComplexTherapyDto fullVariableComplexTherapyContinuousInfusion = TherapyBuilderUtils.createFullVariableComplexTherapyContinuousInfusion(
  //      "fullVariableComplexTherapyContinuousInfusion");
  //  final ConstantComplexTherapyDto fullConstantComplexTherapyContinuousInfusion = TherapyBuilderUtils.createFullConstantComplexTherapyContinuousInfusion(
  //      "fullConstantComplexTherapyContinuousInfusion");
  //  final ConstantComplexTherapyDto fullConstantComplexTherapyWithRate = TherapyBuilderUtils.createFullConstantComplexTherapyWithRate(
  //      "fullConstantComplexTherapyWithRate");
  //
  //  final List<DateTime> dateTimes = Lists.newArrayList();
  //  dateTimes.add(new DateTime(2018, 11, 18, 12, 0));
  //  dateTimes.add(new DateTime(2018, 11, 25, 12, 0));
  //  dateTimes.add(new DateTime(2018, 11, 16, 12, 0));
  //  dateTimes.add(new DateTime(2018, 11, 23, 12, 0));
  //  dateTimes.add(new DateTime(2018, 11, 17, 12, 0));
  //  dateTimes.add(new DateTime(2018, 11, 21, 12, 0));
  //  dateTimes.add(new DateTime(2018, 11, 20, 12, 0));
  //  dateTimes.add(new DateTime(2018, 11, 13, 12, 0));
  //  dateTimes.add(new DateTime(2018, 11, 22, 12, 0));
  //  dateTimes.add(new DateTime(2018, 11, 19, 12, 0));
  //  dateTimes.add(new DateTime(2018, 11, 24, 12, 0));
  //  dateTimes.add(new DateTime(2018, 11, 11, 12, 0));
  //  dateTimes.add(new DateTime(2018, 11, 12, 12, 0));
  //  dateTimes.add(new DateTime(2018, 11, 14, 12, 0));
  //  dateTimes.add(new DateTime(2018, 11, 15, 12, 0));
  //
  //  final List<TherapyDto> therapies = Lists.newArrayList();
  //  therapies.add(minimalConstantComplexTherapyWithRate);
  //  therapies.add(minimalSimpleTherapyDto);
  //  therapies.add(fullConstantComplexTherapy);
  //  therapies.add(fullConstantSimpleTherapyDoseRange);
  //  therapies.add(minimalConstantComplexTherapyContinuousInfusion);
  //  therapies.add(fullVariableSimpleTherapy);
  //  therapies.add(fullVariableSimpleTherapyDischargeProtocol);
  //  therapies.add(fullVariableComplexTherapy);
  //  therapies.add(minimalVariableSimpleTherapy);
  //  therapies.add(minimalConstantComplexTherapy);
  //  therapies.add(fullConstantSimpleTherapy);
  //  therapies.add(oxygenTherapy);
  //  therapies.add(fullVariableComplexTherapyContinuousInfusion);
  //  therapies.add(fullConstantComplexTherapyContinuousInfusion);
  //  therapies.add(fullConstantComplexTherapyWithRate);
  //
  //  final List<TherapyRowDto> therapyRows = Lists.newArrayList();
  //
  //  for (final TherapyDto therapy : therapies)
  //  {
  //    final DateTime lastAdminTime = dateTimes.get(therapies.indexOf(therapy));
  //    final TherapyRowDto therapyRow = createTherapyRowDto(therapy, Lists.newArrayList(createStartAdministrationForTherapy(
  //        therapy.getCompositionUid(),
  //        lastAdminTime,
  //        AdministrationResultEnum.GIVEN)));
  //    therapyRows.add(therapyRow);
  //  }
  //
  //  final Locale locale = new Locale("en");
  //  final DateTime when = new DateTime(2018, 10, 10, 10, 10);
  //
  //  final PatientDataForTherapyReportDto patientDataForTherapyReportDto = createMockedPatientData();
  //
  //
  //
  //  final TherapySurgeryReportDto result = therapyReportDataProviderImpl.mapTherapyRowDtoToTherapySurgeryReportDto(
  //      therapyRows,
  //      locale,
  //      when,
  //      patientDataForTherapyReportDto);
  //
  //  final TherapySurgeryReportElementDto minimalConstantComplexTherapyWithRateReportElement = new TherapySurgeryReportElementDto(
  //      "12:00 - 33mg",
  //      "18-Nov-2018",
  //      minimalConstantComplexTherapyWithRate,
  //      "06-Oct-2018 12:00",
  //      null,
  //      null,
  //      TherapyReportStatusEnum.SUSPENDED,
  //      "0",
  //      false,
  //      "");
  //
  //  final TherapySurgeryReportElementDto minimalSimpleTherapyDtoReportElement = new TherapySurgeryReportElementDto(
  //      "12:00 - 33mg",
  //      "25-Nov-2018",
  //      minimalSimpleTherapyDto,
  //      "06-Oct-2018 12:00",
  //      null,
  //      null,
  //      TherapyReportStatusEnum.ACTIVE,
  //      "0",
  //      false,
  //      "");
  //
  //  final TherapySurgeryReportElementDto fullConstantComplexTherapyReportElement = new TherapySurgeryReportElementDto(
  //      "12:00 - 33mg",
  //      "16-Nov-2018",
  //      fullConstantComplexTherapy,
  //      "06-Oct-2018 12:00",
  //      "20-Oct-2018 12:00",
  //      null,
  //      TherapyReportStatusEnum.ACTIVE,
  //      "0",
  //      false,
  //      "");
  //
  //  final TherapySurgeryReportElementDto fullConstantSimpleTherapyDoseRangeReportElement = new TherapySurgeryReportElementDto(
  //      "12:00 - 33mg",
  //      "23-Nov-2018",
  //      fullConstantSimpleTherapyDoseRange,
  //      "06-Oct-2018 12:00",
  //      "20-Oct-2018 12:00",
  //      null,
  //      TherapyReportStatusEnum.ACTIVE,
  //      "0",
  //      false,
  //      "");
  //
  //  final TherapySurgeryReportElementDto minimalConstantComplexTherapyContinuousInfusionReportElement = new TherapySurgeryReportElementDto(
  //      "12:00 - 33mg",
  //      "17-Nov-2018",
  //      minimalConstantComplexTherapyContinuousInfusion,
  //      "06-Oct-2018 12:00",
  //      null,
  //      null,
  //      TherapyReportStatusEnum.ACTIVE,
  //      "0",
  //      false,
  //      "");
  //
  //  final TherapySurgeryReportElementDto fullVariableSimpleTherapyReportElement = new TherapySurgeryReportElementDto(
  //      "12:00 - 33mg",
  //      "21-Nov-2018",
  //      fullVariableSimpleTherapy,
  //      "06-Oct-2018 12:00",
  //      "20-Oct-2018 12:00",
  //      null,
  //      TherapyReportStatusEnum.FINISHED,
  //      "0",
  //      false,
  //      "");
  //
  //  final TherapySurgeryReportElementDto fullVariableSimpleTherapyDischargeProtocolReportElement = new TherapySurgeryReportElementDto(
  //      "12:00 - 33mg",
  //      "20-Nov-2018",
  //      fullVariableSimpleTherapyDischargeProtocol,
  //      "06-Oct-2018 12:00",
  //      "20-Oct-2018 12:00",
  //      null,
  //      TherapyReportStatusEnum.ACTIVE,
  //      "0",
  //      false,
  //      "");
  //
  //  final TherapySurgeryReportElementDto fullVariableComplexTherapyReportElement = new TherapySurgeryReportElementDto(
  //      "12:00 - 33mg",
  //      "13-Nov-2018",
  //      fullVariableComplexTherapy,
  //      "06-Oct-2018 12:00",
  //      "20-Oct-2018 12:00",
  //      null,
  //      TherapyReportStatusEnum.ACTIVE,
  //      "0",
  //      false,
  //      "");
  //
  //  final TherapySurgeryReportElementDto minimalVariableSimpleTherapyReportElement = new TherapySurgeryReportElementDto(
  //      "12:00 - 33mg",
  //      "22-Nov-2018",
  //      minimalVariableSimpleTherapy,
  //      "06-Oct-2018 12:00",
  //      null,
  //      null,
  //      TherapyReportStatusEnum.ACTIVE,
  //      "0",
  //      false,
  //      "");
  //
  //  final TherapySurgeryReportElementDto minimalConstantComplexTherapyReportElement = new TherapySurgeryReportElementDto(
  //      "12:00 - 33mg",
  //      "19-Nov-2018",
  //      minimalConstantComplexTherapy,
  //      "06-Oct-2018 12:00",
  //      null,
  //      null,
  //      TherapyReportStatusEnum.ACTIVE,
  //      "0",
  //      false,
  //      "");
  //
  //  final TherapySurgeryReportElementDto fullConstantSimpleTherapyReportElement = new TherapySurgeryReportElementDto(
  //      "12:00 - 33mg",
  //      "24-Nov-2018",
  //      fullConstantSimpleTherapy,
  //      "06-Oct-2018 12:00",
  //      "20-Oct-2018 12:00",
  //      null,
  //      TherapyReportStatusEnum.ACTIVE,
  //      "0",
  //      false,
  //      "");
  //
  //  final TherapySurgeryReportElementDto oxygenTherapyReportElement = new TherapySurgeryReportElementDto(
  //      "12:00 - 33mg",
  //      "11-Nov-2018",
  //      oxygenTherapy,
  //      "06-Oct-2018 12:00",
  //      null,
  //      "22",
  //      TherapyReportStatusEnum.ACTIVE,
  //      "5",
  //      true,
  //      "");
  //
  //  final TherapySurgeryReportElementDto fullVariableComplexTherapyContinuousInfusionReportElement = new TherapySurgeryReportElementDto(
  //      "12:00 - 33mg",
  //      "12-Nov-2018",
  //      fullVariableComplexTherapyContinuousInfusion,
  //      "06-Oct-2018 12:00",
  //      "20-Oct-2018 12:00",
  //      null,
  //      TherapyReportStatusEnum.ACTIVE,
  //      "0",
  //      false,
  //      "");
  //
  //  final TherapySurgeryReportElementDto fullConstantComplexTherapyContinuousInfusionReportElement = new TherapySurgeryReportElementDto(
  //      "12:00 - 33mg",
  //      "14-Nov-2018",
  //      fullConstantComplexTherapyContinuousInfusion,
  //      "06-Oct-2018 12:00",
  //      "20-Oct-2018 12:00",
  //      null,
  //      TherapyReportStatusEnum.ACTIVE,
  //      "0",
  //      false,
  //      "");
  //
  //  final TherapySurgeryReportElementDto fullConstantComplexTherapyWithRateReportElement = new TherapySurgeryReportElementDto(
  //      "12:00 - 33mg",
  //      "15-Nov-2018",
  //      fullConstantComplexTherapyWithRate,
  //      "06-Oct-2018 12:00",
  //      "20-Oct-2018 12:00",
  //      "11",
  //      TherapyReportStatusEnum.FINISHED,
  //      "0",
  //      false,
  //      "");
  //
  //  final TherapySurgeryReportDto expectedResult = new TherapySurgeryReportDto(
  //      patientDataForTherapyReportDto,
  //      Lists.newArrayList(
  //          minimalSimpleTherapyDtoReportElement,
  //          fullConstantSimpleTherapyDoseRangeReportElement,
  //          fullVariableSimpleTherapyReportElement,
  //          fullVariableSimpleTherapyDischargeProtocolReportElement,
  //          minimalVariableSimpleTherapyReportElement,
  //          fullConstantSimpleTherapyReportElement,
  //          oxygenTherapyReportElement),
  //      Lists.newArrayList(minimalConstantComplexTherapyWithRateReportElement,
  //                         fullConstantComplexTherapyReportElement,
  //                         minimalConstantComplexTherapyContinuousInfusionReportElement,
  //                         fullVariableComplexTherapyReportElement,
  //                         minimalConstantComplexTherapyReportElement,
  //                         fullVariableComplexTherapyContinuousInfusionReportElement,
  //                         fullConstantComplexTherapyContinuousInfusionReportElement,
  //                         fullConstantComplexTherapyWithRateReportElement),
  //      "10-okt.-2018");
  //
  //
  //  assertThat(expectedResult.getSimpleElements().get(0).getTimeDose())
  //      .isEqualTo(result.getSimpleElements().get(0).getTimeDose());
  //  assertThat(expectedResult.getSimpleElements().get(0).getDate())
  //      .isEqualTo(result.getSimpleElements().get(0).getDate());
  //  assertThat(expectedResult.getSimpleElements().get(0).getTherapy())
  //      .isEqualTo(result.getSimpleElements().get(0).getTherapy());
  //  assertThat(expectedResult.getSimpleElements().get(0).getTherapyStart())
  //      .isEqualTo(result.getSimpleElements().get(0).getTherapyStart());
  //  assertThat(expectedResult.getSimpleElements().get(0).getTherapyEnd())
  //      .isEqualTo(result.getSimpleElements().get(0).getTherapyEnd());
  //  assertThat(result.getSimpleElements().get(0).getCurrentRate())
  //      .isNull();
  //  assertThat(expectedResult.getSimpleElements().get(0).getTherapyReportStatusEnum())
  //      .isEqualTo(result.getSimpleElements().get(0).getTherapyReportStatusEnum());
  //  assertThat(expectedResult.getSimpleElements().get(0).getTherapyConsecutiveDay())
  //      .isEqualTo(result.getSimpleElements().get(0).getTherapyConsecutiveDay());
  //  assertThat(expectedResult.getSimpleElements().get(0).isShowTherapyConsecutiveDay())
  //      .isEqualTo(result.getSimpleElements().get(0).isShowTherapyConsecutiveDay());
  //
  //  assertThat(expectedResult.getSimpleElements().get(1).getTimeDose())
  //      .isEqualTo(result.getSimpleElements().get(1).getTimeDose());
  //  assertThat(expectedResult.getSimpleElements().get(1).getDate())
  //      .isEqualTo(result.getSimpleElements().get(1).getDate());
  //  assertThat(expectedResult.getSimpleElements().get(1).getTherapy())
  //      .isEqualTo(result.getSimpleElements().get(1).getTherapy());
  //  assertThat(expectedResult.getSimpleElements().get(1).getTherapyStart())
  //      .isEqualTo(result.getSimpleElements().get(1).getTherapyStart());
  //  assertThat(expectedResult.getSimpleElements().get(1).getTherapyEnd())
  //      .isEqualTo(result.getSimpleElements().get(1).getTherapyEnd());
  //  assertThat(result.getSimpleElements().get(1).getCurrentRate())
  //      .isNull();
  //  assertThat(expectedResult.getSimpleElements().get(1).getTherapyReportStatusEnum())
  //      .isEqualTo(result.getSimpleElements().get(1).getTherapyReportStatusEnum());
  //  assertThat(expectedResult.getSimpleElements().get(1).getTherapyConsecutiveDay())
  //      .isEqualTo(result.getSimpleElements().get(1).getTherapyConsecutiveDay());
  //  assertThat(expectedResult.getSimpleElements().get(1).isShowTherapyConsecutiveDay())
  //      .isEqualTo(result.getSimpleElements().get(1).isShowTherapyConsecutiveDay());
  //
  //  assertThat(expectedResult.getSimpleElements().get(2).getTimeDose())
  //      .isEqualTo(result.getSimpleElements().get(2).getTimeDose());
  //  assertThat(expectedResult.getSimpleElements().get(2).getDate())
  //      .isEqualTo(result.getSimpleElements().get(2).getDate());
  //  assertThat(expectedResult.getSimpleElements().get(2).getTherapy())
  //      .isEqualTo(result.getSimpleElements().get(2).getTherapy());
  //  assertThat(expectedResult.getSimpleElements().get(2).getTherapyStart())
  //      .isEqualTo(result.getSimpleElements().get(2).getTherapyStart());
  //  assertThat(expectedResult.getSimpleElements().get(2).getTherapyEnd())
  //      .isEqualTo(result.getSimpleElements().get(2).getTherapyEnd());
  //  assertThat(result.getSimpleElements().get(2).getCurrentRate())
  //      .isNull();
  //  assertThat(expectedResult.getSimpleElements().get(2).getTherapyReportStatusEnum())
  //      .isEqualTo(result.getSimpleElements().get(2).getTherapyReportStatusEnum());
  //  assertThat(expectedResult.getSimpleElements().get(2).getTherapyConsecutiveDay())
  //      .isEqualTo(result.getSimpleElements().get(2).getTherapyConsecutiveDay());
  //  assertThat(expectedResult.getSimpleElements().get(2).isShowTherapyConsecutiveDay())
  //      .isEqualTo(result.getSimpleElements().get(2).isShowTherapyConsecutiveDay());
  //
  //  assertThat(expectedResult.getSimpleElements().get(3).getTimeDose())
  //      .isEqualTo(result.getSimpleElements().get(3).getTimeDose());
  //  assertThat(expectedResult.getSimpleElements().get(3).getDate())
  //      .isEqualTo(result.getSimpleElements().get(3).getDate());
  //  assertThat(expectedResult.getSimpleElements().get(3).getTherapy())
  //      .isEqualTo(result.getSimpleElements().get(3).getTherapy());
  //  assertThat(expectedResult.getSimpleElements().get(3).getTherapyStart())
  //      .isEqualTo(result.getSimpleElements().get(3).getTherapyStart());
  //  assertThat(expectedResult.getSimpleElements().get(3).getTherapyEnd())
  //      .isEqualTo(result.getSimpleElements().get(3).getTherapyEnd());
  //  assertThat(result.getSimpleElements().get(3).getCurrentRate())
  //      .isNull();
  //  assertThat(expectedResult.getSimpleElements().get(3).getTherapyReportStatusEnum())
  //      .isEqualTo(result.getSimpleElements().get(3).getTherapyReportStatusEnum());
  //  assertThat(expectedResult.getSimpleElements().get(3).getTherapyConsecutiveDay())
  //      .isEqualTo(result.getSimpleElements().get(3).getTherapyConsecutiveDay());
  //  assertThat(expectedResult.getSimpleElements().get(3).isShowTherapyConsecutiveDay())
  //      .isEqualTo(result.getSimpleElements().get(3).isShowTherapyConsecutiveDay());
  //
  //  assertThat(expectedResult.getSimpleElements().get(4).getTimeDose())
  //      .isEqualTo(result.getSimpleElements().get(4).getTimeDose());
  //  assertThat(expectedResult.getSimpleElements().get(4).getDate())
  //      .isEqualTo(result.getSimpleElements().get(4).getDate());
  //  assertThat(expectedResult.getSimpleElements().get(4).getTherapy())
  //      .isEqualTo(result.getSimpleElements().get(4).getTherapy());
  //  assertThat(expectedResult.getSimpleElements().get(4).getTherapyStart())
  //      .isEqualTo(result.getSimpleElements().get(4).getTherapyStart());
  //  assertThat(expectedResult.getSimpleElements().get(4).getTherapyEnd())
  //      .isEqualTo(result.getSimpleElements().get(4).getTherapyEnd());
  //  assertThat(result.getSimpleElements().get(4).getCurrentRate())
  //      .isNull();
  //  assertThat(expectedResult.getSimpleElements().get(4).getTherapyReportStatusEnum())
  //      .isEqualTo(result.getSimpleElements().get(4).getTherapyReportStatusEnum());
  //  assertThat(expectedResult.getSimpleElements().get(4).getTherapyConsecutiveDay())
  //      .isEqualTo(result.getSimpleElements().get(4).getTherapyConsecutiveDay());
  //  assertThat(expectedResult.getSimpleElements().get(4).isShowTherapyConsecutiveDay())
  //      .isEqualTo(result.getSimpleElements().get(4).isShowTherapyConsecutiveDay());
  //
  //  assertThat(expectedResult.getSimpleElements().get(5).getTimeDose())
  //      .isEqualTo(result.getSimpleElements().get(5).getTimeDose());
  //  assertThat(expectedResult.getSimpleElements().get(5).getDate())
  //      .isEqualTo(result.getSimpleElements().get(5).getDate());
  //  assertThat(expectedResult.getSimpleElements().get(5).getTherapy())
  //      .isEqualTo(result.getSimpleElements().get(5).getTherapy());
  //  assertThat(expectedResult.getSimpleElements().get(5).getTherapyStart())
  //      .isEqualTo(result.getSimpleElements().get(5).getTherapyStart());
  //  assertThat(expectedResult.getSimpleElements().get(5).getTherapyEnd())
  //      .isEqualTo(result.getSimpleElements().get(5).getTherapyEnd());
  //  assertThat(result.getSimpleElements().get(5).getCurrentRate())
  //      .isNull();
  //  assertThat(expectedResult.getSimpleElements().get(5).getTherapyReportStatusEnum())
  //      .isEqualTo(result.getSimpleElements().get(5).getTherapyReportStatusEnum());
  //  assertThat(expectedResult.getSimpleElements().get(5).getTherapyConsecutiveDay())
  //      .isEqualTo(result.getSimpleElements().get(5).getTherapyConsecutiveDay());
  //  assertThat(expectedResult.getSimpleElements().get(5).isShowTherapyConsecutiveDay())
  //      .isEqualTo(result.getSimpleElements().get(5).isShowTherapyConsecutiveDay());
  //
  //  assertThat(expectedResult.getSimpleElements().get(6).getTimeDose())
  //      .isEqualTo(result.getSimpleElements().get(6).getTimeDose());
  //  assertThat(expectedResult.getSimpleElements().get(6).getDate())
  //      .isEqualTo(result.getSimpleElements().get(6).getDate());
  //  assertThat(expectedResult.getSimpleElements().get(6).getTherapy())
  //      .isEqualTo(result.getSimpleElements().get(6).getTherapy());
  //  assertThat(expectedResult.getSimpleElements().get(6).getTherapyStart())
  //      .isEqualTo(result.getSimpleElements().get(6).getTherapyStart());
  //  assertThat(expectedResult.getSimpleElements().get(6).getTherapyEnd())
  //      .isEqualTo(result.getSimpleElements().get(6).getTherapyEnd());
  //  assertThat(result.getSimpleElements().get(6).getCurrentRate())
  //      .isEqualTo(expectedResult.getSimpleElements().get(6).getCurrentRate());
  //  assertThat(expectedResult.getSimpleElements().get(6).getTherapyReportStatusEnum())
  //      .isEqualTo(result.getSimpleElements().get(6).getTherapyReportStatusEnum());
  //  assertThat(expectedResult.getSimpleElements().get(6).getTherapyConsecutiveDay())
  //      .isEqualTo(result.getSimpleElements().get(6).getTherapyConsecutiveDay());
  //  assertThat(expectedResult.getSimpleElements().get(6).isShowTherapyConsecutiveDay())
  //      .isEqualTo(result.getSimpleElements().get(6).isShowTherapyConsecutiveDay());
  //
  //  assertThat(expectedResult.getComplexElements().get(0).getTimeDose())
  //      .isEqualTo(result.getComplexElements().get(0).getTimeDose());
  //  assertThat(expectedResult.getComplexElements().get(0).getDate())
  //      .isEqualTo(result.getComplexElements().get(0).getDate());
  //  assertThat(expectedResult.getComplexElements().get(0).getTherapy())
  //      .isEqualTo(result.getComplexElements().get(0).getTherapy());
  //  assertThat(expectedResult.getComplexElements().get(0).getTherapyStart())
  //      .isEqualTo(result.getComplexElements().get(0).getTherapyStart());
  //  assertThat(expectedResult.getComplexElements().get(0).getTherapyEnd())
  //      .isEqualTo(result.getComplexElements().get(0).getTherapyEnd());
  //  assertThat(result.getComplexElements().get(0).getCurrentRate())
  //      .isNull();
  //  assertThat(expectedResult.getComplexElements().get(0).getTherapyReportStatusEnum())
  //      .isEqualTo(result.getComplexElements().get(0).getTherapyReportStatusEnum());
  //  assertThat(expectedResult.getComplexElements().get(0).getTherapyConsecutiveDay())
  //      .isEqualTo(result.getComplexElements().get(0).getTherapyConsecutiveDay());
  //  assertThat(expectedResult.getComplexElements().get(0).isShowTherapyConsecutiveDay())
  //      .isEqualTo(result.getComplexElements().get(0).isShowTherapyConsecutiveDay());
  //
  //  assertThat(expectedResult.getComplexElements().get(1).getTimeDose())
  //      .isEqualTo(result.getComplexElements().get(1).getTimeDose());
  //  assertThat(expectedResult.getComplexElements().get(1).getDate())
  //      .isEqualTo(result.getComplexElements().get(1).getDate());
  //  assertThat(expectedResult.getComplexElements().get(1).getTherapy())
  //      .isEqualTo(result.getComplexElements().get(1).getTherapy());
  //  assertThat(expectedResult.getComplexElements().get(1).getTherapyStart())
  //      .isEqualTo(result.getComplexElements().get(1).getTherapyStart());
  //  assertThat(expectedResult.getComplexElements().get(1).getTherapyEnd())
  //      .isEqualTo(result.getComplexElements().get(1).getTherapyEnd());
  //  assertThat(result.getComplexElements().get(1).getCurrentRate())
  //      .isNull();
  //  assertThat(expectedResult.getComplexElements().get(1).getTherapyReportStatusEnum())
  //      .isEqualTo(result.getComplexElements().get(1).getTherapyReportStatusEnum());
  //  assertThat(expectedResult.getComplexElements().get(1).getTherapyConsecutiveDay())
  //      .isEqualTo(result.getComplexElements().get(1).getTherapyConsecutiveDay());
  //  assertThat(expectedResult.getComplexElements().get(1).isShowTherapyConsecutiveDay())
  //      .isEqualTo(result.getComplexElements().get(1).isShowTherapyConsecutiveDay());
  //
  //  assertThat(expectedResult.getComplexElements().get(2).getTimeDose())
  //      .isEqualTo(result.getComplexElements().get(2).getTimeDose());
  //  assertThat(expectedResult.getComplexElements().get(2).getDate())
  //      .isEqualTo(result.getComplexElements().get(2).getDate());
  //  assertThat(expectedResult.getComplexElements().get(2).getTherapy())
  //      .isEqualTo(result.getComplexElements().get(2).getTherapy());
  //  assertThat(expectedResult.getComplexElements().get(2).getTherapyStart())
  //      .isEqualTo(result.getComplexElements().get(2).getTherapyStart());
  //  assertThat(expectedResult.getComplexElements().get(2).getTherapyEnd())
  //      .isEqualTo(result.getComplexElements().get(2).getTherapyEnd());
  //  assertThat(result.getComplexElements().get(2).getCurrentRate())
  //      .isNull();
  //  assertThat(expectedResult.getComplexElements().get(2).getTherapyReportStatusEnum())
  //      .isEqualTo(result.getComplexElements().get(2).getTherapyReportStatusEnum());
  //  assertThat(expectedResult.getComplexElements().get(2).getTherapyConsecutiveDay())
  //      .isEqualTo(result.getComplexElements().get(2).getTherapyConsecutiveDay());
  //  assertThat(expectedResult.getComplexElements().get(2).isShowTherapyConsecutiveDay())
  //      .isEqualTo(result.getComplexElements().get(2).isShowTherapyConsecutiveDay());
  //
  //  assertThat(expectedResult.getComplexElements().get(3).getTimeDose())
  //      .isEqualTo(result.getComplexElements().get(3).getTimeDose());
  //  assertThat(expectedResult.getComplexElements().get(3).getDate())
  //      .isEqualTo(result.getComplexElements().get(3).getDate());
  //  assertThat(expectedResult.getComplexElements().get(3).getTherapy())
  //      .isEqualTo(result.getComplexElements().get(3).getTherapy());
  //  assertThat(expectedResult.getComplexElements().get(3).getTherapyStart())
  //      .isEqualTo(result.getComplexElements().get(3).getTherapyStart());
  //  assertThat(expectedResult.getComplexElements().get(3).getTherapyEnd())
  //      .isEqualTo(result.getComplexElements().get(3).getTherapyEnd());
  //  assertThat(result.getComplexElements().get(3).getCurrentRate())
  //      .isNull();
  //  assertThat(expectedResult.getComplexElements().get(3).getTherapyReportStatusEnum())
  //      .isEqualTo(result.getComplexElements().get(3).getTherapyReportStatusEnum());
  //  assertThat(expectedResult.getComplexElements().get(3).getTherapyConsecutiveDay())
  //      .isEqualTo(result.getComplexElements().get(3).getTherapyConsecutiveDay());
  //  assertThat(expectedResult.getComplexElements().get(3).isShowTherapyConsecutiveDay())
  //      .isEqualTo(result.getComplexElements().get(3).isShowTherapyConsecutiveDay());
  //
  //  assertThat(expectedResult.getComplexElements().get(4).getTimeDose())
  //      .isEqualTo(result.getComplexElements().get(4).getTimeDose());
  //  assertThat(expectedResult.getComplexElements().get(4).getDate())
  //      .isEqualTo(result.getComplexElements().get(4).getDate());
  //  assertThat(expectedResult.getComplexElements().get(4).getTherapy())
  //      .isEqualTo(result.getComplexElements().get(4).getTherapy());
  //  assertThat(expectedResult.getComplexElements().get(4).getTherapyStart())
  //      .isEqualTo(result.getComplexElements().get(4).getTherapyStart());
  //  assertThat(expectedResult.getComplexElements().get(4).getTherapyEnd())
  //      .isEqualTo(result.getComplexElements().get(4).getTherapyEnd());
  //  assertThat(result.getComplexElements().get(4).getCurrentRate())
  //      .isNull();
  //  assertThat(expectedResult.getComplexElements().get(4).getTherapyReportStatusEnum())
  //      .isEqualTo(result.getComplexElements().get(4).getTherapyReportStatusEnum());
  //  assertThat(expectedResult.getComplexElements().get(4).getTherapyConsecutiveDay())
  //      .isEqualTo(result.getComplexElements().get(4).getTherapyConsecutiveDay());
  //  assertThat(expectedResult.getComplexElements().get(4).isShowTherapyConsecutiveDay())
  //      .isEqualTo(result.getComplexElements().get(4).isShowTherapyConsecutiveDay());
  //
  //  assertThat(expectedResult.getComplexElements().get(5).getTimeDose())
  //      .isEqualTo(result.getComplexElements().get(5).getTimeDose());
  //  assertThat(expectedResult.getComplexElements().get(5).getDate())
  //      .isEqualTo(result.getComplexElements().get(5).getDate());
  //  assertThat(expectedResult.getComplexElements().get(5).getTherapy())
  //      .isEqualTo(result.getComplexElements().get(5).getTherapy());
  //  assertThat(expectedResult.getComplexElements().get(5).getTherapyStart())
  //      .isEqualTo(result.getComplexElements().get(5).getTherapyStart());
  //  assertThat(expectedResult.getComplexElements().get(5).getTherapyEnd())
  //      .isEqualTo(result.getComplexElements().get(5).getTherapyEnd());
  //  assertThat(result.getComplexElements().get(5).getCurrentRate())
  //      .isNull();
  //  assertThat(expectedResult.getComplexElements().get(5).getTherapyReportStatusEnum())
  //      .isEqualTo(result.getComplexElements().get(5).getTherapyReportStatusEnum());
  //  assertThat(expectedResult.getComplexElements().get(5).getTherapyConsecutiveDay())
  //      .isEqualTo(result.getComplexElements().get(5).getTherapyConsecutiveDay());
  //  assertThat(expectedResult.getComplexElements().get(5).isShowTherapyConsecutiveDay())
  //      .isEqualTo(result.getComplexElements().get(5).isShowTherapyConsecutiveDay());
  //
  //  assertThat(expectedResult.getComplexElements().get(6).getTimeDose())
  //      .isEqualTo(result.getComplexElements().get(6).getTimeDose());
  //  assertThat(expectedResult.getComplexElements().get(6).getDate())
  //      .isEqualTo(result.getComplexElements().get(6).getDate());
  //  assertThat(expectedResult.getComplexElements().get(6).getTherapy())
  //      .isEqualTo(result.getComplexElements().get(6).getTherapy());
  //  assertThat(expectedResult.getComplexElements().get(6).getTherapyStart())
  //      .isEqualTo(result.getComplexElements().get(6).getTherapyStart());
  //  assertThat(expectedResult.getComplexElements().get(6).getTherapyEnd())
  //      .isEqualTo(result.getComplexElements().get(6).getTherapyEnd());
  //  assertThat(result.getComplexElements().get(6).getCurrentRate())
  //      .isNull();
  //  assertThat(expectedResult.getComplexElements().get(6).getTherapyReportStatusEnum())
  //      .isEqualTo(result.getComplexElements().get(6).getTherapyReportStatusEnum());
  //  assertThat(expectedResult.getComplexElements().get(6).getTherapyConsecutiveDay())
  //      .isEqualTo(result.getComplexElements().get(6).getTherapyConsecutiveDay());
  //  assertThat(expectedResult.getComplexElements().get(6).isShowTherapyConsecutiveDay())
  //      .isEqualTo(result.getComplexElements().get(6).isShowTherapyConsecutiveDay());
  //
  //  assertThat(expectedResult.getComplexElements().get(7).getTimeDose())
  //      .isEqualTo(result.getComplexElements().get(7).getTimeDose());
  //  assertThat(expectedResult.getComplexElements().get(7).getDate())
  //      .isEqualTo(result.getComplexElements().get(7).getDate());
  //  assertThat(expectedResult.getComplexElements().get(7).getTherapy())
  //      .isEqualTo(result.getComplexElements().get(7).getTherapy());
  //  assertThat(expectedResult.getComplexElements().get(7).getTherapyStart())
  //      .isEqualTo(result.getComplexElements().get(7).getTherapyStart());
  //  assertThat(expectedResult.getComplexElements().get(7).getTherapyEnd())
  //      .isEqualTo(result.getComplexElements().get(7).getTherapyEnd());
  //  assertThat(result.getComplexElements().get(7).getCurrentRate())
  //      .isEqualTo(expectedResult.getComplexElements().get(7).getCurrentRate());
  //  assertThat(expectedResult.getComplexElements().get(7).getTherapyReportStatusEnum())
  //      .isEqualTo(result.getComplexElements().get(7).getTherapyReportStatusEnum());
  //  assertThat(expectedResult.getComplexElements().get(7).getTherapyConsecutiveDay())
  //      .isEqualTo(result.getComplexElements().get(7).getTherapyConsecutiveDay());
  //  assertThat(expectedResult.getComplexElements().get(7).isShowTherapyConsecutiveDay())
  //      .isEqualTo(result.getComplexElements().get(7).isShowTherapyConsecutiveDay());
  //
  //  assertThat(result.getSimpleElements().size()).isEqualTo(7);
  //  assertThat(result.getComplexElements().size()).isEqualTo(8);
  //}

  private AdministrationDto createStartAdministrationForTherapy(
      final String compositionUid, final DateTime administrationTime,
      final AdministrationResultEnum administrationResultEnum)
  {
    final StartAdministrationDto administration = new StartAdministrationDto();
    administration.setAdministrationTime(administrationTime);
    administration.setAdministrationId(compositionUid);
    administration.setAdministrationResult(administrationResultEnum);
    final TherapyDoseDto therapyDose = new TherapyDoseDto();
    therapyDose.setNumerator(33.0);
    therapyDose.setNumeratorUnit("mg");
    administration.setAdministeredDose(therapyDose);

    return administration;
  }

  private TherapyRowDto createTherapyRowDto(final TherapyDto therapy, final List<AdministrationDto> administrations)
  {
    if ("oxygenTherapy".equals(therapy.getCompositionUid()))
    {
      final OxygenTherapyRowDtoDto oxygenTherapyRowDtoDto = new OxygenTherapyRowDtoDto();
      oxygenTherapyRowDtoDto.setTherapy(therapy);
      oxygenTherapyRowDtoDto.getAdministrations().addAll(administrations);
      oxygenTherapyRowDtoDto.setCurrentInfusionRate(22.0);
      oxygenTherapyRowDtoDto.setTherapyStatus(TherapyStatusEnum.NORMAL);
      oxygenTherapyRowDtoDto.setConsecutiveDay(5);
      oxygenTherapyRowDtoDto.setShowConsecutiveDay(true);
      return oxygenTherapyRowDtoDto;
    }

    if ("fullConstantComplexTherapyWithRate".equals(therapy.getCompositionUid()))
    {
      final ContinuousInfusionTherapyRowDtoDto continuousInfusionTherapyRowDtoDto = new ContinuousInfusionTherapyRowDtoDto();
      continuousInfusionTherapyRowDtoDto.setTherapy(therapy);
      continuousInfusionTherapyRowDtoDto.getAdministrations().addAll(administrations);
      continuousInfusionTherapyRowDtoDto.setCurrentInfusionRate(11.0);
      continuousInfusionTherapyRowDtoDto.setTherapyStatus(TherapyStatusEnum.ABORTED);
      return continuousInfusionTherapyRowDtoDto;
    }

    final TherapyRowDto therapyRow = new TherapyRowDto();

    if ("fullVariableSimpleTherapy".equals(therapy.getCompositionUid()))
    {
      therapyRow.setTherapyStatus(TherapyStatusEnum.CANCELLED);
    }

    if ("minimalConstantComplexTherapyWithRate".equals(therapy.getCompositionUid()))
    {
      therapyRow.setTherapyStatus(TherapyStatusEnum.SUSPENDED);
    }

    if ("minimalSimpleTherapyDto".equals(therapy.getCompositionUid()))
    {
      therapyRow.setTherapyStatus(TherapyStatusEnum.VERY_LATE);
    }


    therapyRow.setTherapy(therapy);
    therapyRow.getAdministrations().addAll(administrations);

    return therapyRow;
  }

  private PatientDataForTherapyReportDto createMockedPatientData()
  {
    return new PatientDataForTherapyReportDto(
        true,
        "Janez",
        "11-Oct-2018",
        Gender.FEMALE,
        "BIS",
        "12345as",
        "23",
        "Klinični Center",
        "UKC",
        "Dr. Boris",
        "132",
        "14-Oct-2018",
        45,
        Collections.singletonList(new ExternalCatalogDto("1", "Gripa", "1000")),
        "67",
        Collections.emptyList(),
        AllergiesStatus.NO_KNOWN_ALLERGY,
        "ZZZS",
        "Ljubljana");
  }
}