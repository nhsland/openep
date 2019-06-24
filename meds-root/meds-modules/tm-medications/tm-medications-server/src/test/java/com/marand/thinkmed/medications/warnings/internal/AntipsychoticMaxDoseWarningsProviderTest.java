package com.marand.thinkmed.medications.warnings.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.dictionary.MafDictionary;
import com.marand.thinkmed.medications.config.MedsProperties;
import com.marand.thinkmed.medications.service.WarningSeverity;
import com.marand.thinkmed.medications.service.WarningType;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import com.marand.thinkmed.medications.therapy.util.TherapyBuilderUtils;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author Nejc Korasa
 */

@RunWith(SpringJUnit4ClassRunner.class)
public class AntipsychoticMaxDoseWarningsProviderTest
{
  @InjectMocks
  private final AntipsychoticMaxDoseWarningsProvider antipsychoticMaxDoseWarningsProvider = new AntipsychoticMaxDoseWarningsProvider();

  @Mock
  private AntipsychoticMaxDoseWarningsHandler antipsychoticMaxDoseWarningsHandler;

  @Mock
  private MedsProperties medsProperties;

  @Before
  public void init()
  {
    Dictionary.setDelegate(new MafDictionary("Dictionary", "", new Locale("en")));

    Mockito.when(antipsychoticMaxDoseWarningsHandler.getWarnings(Matchers.any(), Matchers.any()))
        .thenReturn(Arrays.asList(
            new MedicationsWarningDto("w1", WarningSeverity.HIGH, WarningType.MAX_DOSE),
            new MedicationsWarningDto("w2", WarningSeverity.HIGH_OVERRIDE, WarningType.MAX_DOSE)
        ));
  }

  @Test
  public void getWarningsNotEnabled()
  {
    Mockito.when(medsProperties.getCumulativeAntipsychoticDoseEnabled()).thenReturn(false);

    final List<MedicationsWarningDto> warnings = antipsychoticMaxDoseWarningsProvider.getWarnings(
        "patient",
        Collections.singletonList(TherapyBuilderUtils.createFullConstantComplexTherapy("t1")),
        Collections.singletonList(TherapyBuilderUtils.createFullConstantComplexTherapy("t1")),
        new DateTime(2018, 10, 6, 12, 0, 0),
        new DateTime(2018, 1, 1,2 ,1 ,1 ),
        50.0,
        null);

    Assert.assertTrue(warnings.isEmpty());
  }

  @Test
  public void getWarningsHasNoAntipsychotics()
  {
    Mockito.when(medsProperties.getCumulativeAntipsychoticDoseEnabled()).thenReturn(true);
    Mockito.when(antipsychoticMaxDoseWarningsHandler.hasAntipsychotics(Matchers.any())).thenReturn(false);

    final List<MedicationsWarningDto> warnings = antipsychoticMaxDoseWarningsProvider.getWarnings(
        "patient",
        Collections.singletonList(TherapyBuilderUtils.createFullConstantComplexTherapy("t1")),
        Collections.singletonList(TherapyBuilderUtils.createFullConstantComplexTherapy("t1")),
        new DateTime(2018, 10, 6, 12, 0, 0),
        new DateTime(2018, 1, 1,2 ,1 ,1 ),
        50.0,
        null);

    Assert.assertTrue(warnings.isEmpty());
  }

  @Test
  public void getWarningsEnabledAndHasAntipsychotics()
  {
    Mockito.when(medsProperties.getCumulativeAntipsychoticDoseEnabled()).thenReturn(true);
    Mockito.when(antipsychoticMaxDoseWarningsHandler.hasAntipsychotics(Matchers.any())).thenReturn(true);

    final List<MedicationsWarningDto> warnings = antipsychoticMaxDoseWarningsProvider.getWarnings(
        "patient",
        Collections.singletonList(TherapyBuilderUtils.createFullConstantComplexTherapy("t1")),
        Collections.singletonList(TherapyBuilderUtils.createFullConstantComplexTherapy("t1")),
        new DateTime(2018, 10, 6, 12, 0, 0),
        new DateTime(2018, 1, 1,2 ,1 ,1 ),
        50.0,
        null);

    Assert.assertFalse(warnings.isEmpty());
  }
}