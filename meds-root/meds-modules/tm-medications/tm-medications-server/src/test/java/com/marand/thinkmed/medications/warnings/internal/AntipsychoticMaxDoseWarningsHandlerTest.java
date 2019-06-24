package com.marand.thinkmed.medications.warnings.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.Opt;
import com.marand.maf.core.dictionary.MafDictionary;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.property.MedicationPropertyType;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import com.marand.thinkmed.medications.dto.property.MedicationPropertyDto;
import com.marand.thinkmed.medications.mentalhealth.impl.ConsentFormFromEhrProvider;
import com.marand.thinkmed.medications.service.WarningSeverity;
import com.marand.thinkmed.medications.service.WarningType;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.marand.thinkmed.medications.warnings.WarningTestUtils.buildTherapy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;

/**
 * @author Nejc Korasa
 */

@SuppressWarnings("unchecked")
@RunWith(SpringJUnit4ClassRunner.class)
public class AntipsychoticMaxDoseWarningsHandlerTest
{
  @InjectMocks
  private final AntipsychoticMaxDoseWarningsHandler antipsychoticsWarningsHandler = new AntipsychoticMaxDoseWarningsHandler();

  @Mock
  private ConsentFormFromEhrProvider consentFormFromEhrProvider;

  @Mock
  private MedicationsValueHolderProvider medicationsValueHolderProvider;

  @Before
  public void init()
  {
    Dictionary.setDelegate(new MafDictionary("Dictionary", "", new Locale("en")));

    final MedicationDataDto medication1 = new MedicationDataDto();
    medication1.setProperty(new MedicationPropertyDto(1L, MedicationPropertyType.ANTIPSYCHOTIC_TAG, "Anti"));
    medication1.setMedication(new MedicationDto(1L, "medication 1"));
    Mockito.when(medicationsValueHolderProvider.getMedicationData(1L)).thenReturn(medication1);

    final MedicationDataDto medication2 = new MedicationDataDto();
    medication2.setProperty(new MedicationPropertyDto(2L, MedicationPropertyType.ANTIPSYCHOTIC_TAG, "Anti"));
    medication2.setMedication(new MedicationDto(2L, "medication 2"));
    Mockito.when(medicationsValueHolderProvider.getMedicationData(2L)).thenReturn(medication2);

    final MedicationDataDto medication3 = new MedicationDataDto();
    medication3.setMedication(new MedicationDto(3L, "medication 3"));
    medication3.setProperty(new MedicationPropertyDto(3L, MedicationPropertyType.ANTIPSYCHOTIC_TAG, "Anti"));
    Mockito.when(medicationsValueHolderProvider.getMedicationData(3L)).thenReturn(medication3);

    final MedicationDataDto medication4 = new MedicationDataDto();
    medication4.setMedication(new MedicationDto(4L, "medication 4"));
    Mockito.when(medicationsValueHolderProvider.getMedicationData(4L)).thenReturn(medication4);

    Mockito
        .when(consentFormFromEhrProvider.getLatestMentalHealthDocument(anyString()))
        .thenReturn(Opt.of(buildConsentForm(null)));
  }

  private MentalHealthDocumentDto buildConsentForm(final Integer maxDosePercentage)
  {
    return new MentalHealthDocumentDto(null, null, null, null, null, null, maxDosePercentage, null, null);
  }

  @Test
  public void getWarnings()
  {
    final List<TherapyDto> therapies = Arrays.asList(
        buildTherapy(1L, "medication 1", 1L, 50),
        buildTherapy(2L, "medication 2", 1L, 50),
        buildTherapy(3L, "medication 3", 1L, 10),
        buildTherapy(4L, "medication 4", 1L, 100),
        buildTherapy(4L, "medication 4", 1L)
    );

    final List<MedicationsWarningDto> warnings = antipsychoticsWarningsHandler.getWarnings("1", therapies);
    final MedicationsWarningDto maxDoseWarning = warnings.get(0);

    assertEquals(1, warnings.size());
    assertEquals(3, maxDoseWarning.getMedications().size());
    assertTrue(maxDoseWarning.getMedications().stream().anyMatch(m -> "1".equals(m.getId())));
    assertTrue(maxDoseWarning.getMedications().stream().anyMatch(m -> "2".equals(m.getId())));
    assertTrue(maxDoseWarning.getMedications().stream().anyMatch(m -> "3".equals(m.getId())));
    assertTrue(maxDoseWarning.getMedications().stream().noneMatch(m -> "4".equals(m.getId())));

    assertTrue(maxDoseWarning.getDescription().contains("110%"));
    assertEquals(WarningSeverity.HIGH_OVERRIDE, maxDoseWarning.getSeverity());
    assertEquals(WarningType.MAX_DOSE, maxDoseWarning.getType());
  }

  @Test
  public void getWarningsNoAntipsychotics()
  {
    final List<MedicationsWarningDto> warnings = antipsychoticsWarningsHandler.getWarnings(
        "1",
        Collections.singletonList(buildTherapy(4L, "medication 4", 10, 150)));

    assertEquals(0, warnings.size());
  }

  @Test
  public void getWarningsBelow100()
  {
    final List<TherapyDto> therapies = Arrays.asList(
        buildTherapy(1L, "medication 1", 1L, 10),
        buildTherapy(2L, "medication 2", 1L, 10),
        buildTherapy(3L, "medication 3", 1L, 10),
        buildTherapy(4L, "medication 4", 1L, 100),
        buildTherapy(4L, "medication 4", 1L)
    );

    final List<MedicationsWarningDto> warnings = antipsychoticsWarningsHandler.getWarnings("1", therapies);

    assertEquals(0, warnings.size());
  }

  @Test
  public void buildWarningOverridePercentageExceeded()
  {
    final List<MedicationsWarningDto> medicationsWarningDtos = antipsychoticsWarningsHandler.buildMaxDoseWarnings(Collections.singletonList(new NamedExternalDto("1", "1")), 140, 130);
    assertEquals(WarningSeverity.HIGH_OVERRIDE, medicationsWarningDtos.get(0).getSeverity());
    assertTrue(medicationsWarningDtos.get(0).getDescription().startsWith("Cumulative dose of antipsychotic is 140%"));
    assertTrue(medicationsWarningDtos.get(0).getDescription().contains("For this patient, the upper limit of the cumulative dose was set to 130%."));
  }

  @Test
  public void buildWarningOverridePercentageNotExceeded()
  {
    final List<MedicationsWarningDto> medicationsWarningDtos = antipsychoticsWarningsHandler.buildMaxDoseWarnings(Collections.singletonList(new NamedExternalDto("1", "1")), 120, 130);
    assertEquals(WarningSeverity.HIGH, medicationsWarningDtos.get(0).getSeverity());
    assertTrue(medicationsWarningDtos.get(0).getDescription().startsWith("Cumulative dose of antipsychotic is 120%"));
    assertTrue(medicationsWarningDtos.get(0).getDescription().contains("For this patient, the upper limit of the cumulative dose was set to 130%."));
  }

  @Test
  public void buildWarningNoOverridePercentage()
  {
    final List<MedicationsWarningDto> medicationsWarningDtos = antipsychoticsWarningsHandler.buildMaxDoseWarnings(Collections.singletonList(new NamedExternalDto("1", "1")), 140, null);
    assertEquals(WarningSeverity.HIGH_OVERRIDE, medicationsWarningDtos.get(0).getSeverity());
    assertTrue(medicationsWarningDtos.get(0).getDescription().startsWith("Cumulative dose of antipsychotic is 140%"));
    assertFalse(medicationsWarningDtos.get(0).getDescription().contains("For this patient, the upper limit of the cumulative dose was set to"));
  }
}
