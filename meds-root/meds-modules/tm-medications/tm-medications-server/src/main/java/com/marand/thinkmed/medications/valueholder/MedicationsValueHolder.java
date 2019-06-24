package com.marand.thinkmed.medications.valueholder;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.base.Stopwatch;
import com.marand.maf.core.valueholder.ValueHolder;
import com.marand.thinkmed.medications.api.internal.dto.DoseFormDto;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Mitja Lapajne
 */

@Component
public class MedicationsValueHolder extends ValueHolder<MedicationsValueHolder.MedicationsHolderDo>
{
  private static final Logger LOG = LoggerFactory.getLogger(MedicationsValueHolder.class);

  private UnitsValueHolder unitsValueHolder;
  private MedicationsDao medicationsDao;

  @Autowired
  public void setUnitsValueHolder(final UnitsValueHolder unitsValueHolder)
  {
    this.unitsValueHolder = unitsValueHolder;
  }

  @Autowired
  public void setMedicationsDao(final MedicationsDao medicationsDao)
  {
    this.medicationsDao = medicationsDao;
  }

  @Override
  protected String getKey()
  {
    return "MEDICATIONS";
  }

  public Map<Long, MedicationDataDto> getMedications()
  {
    return getValue().getMedications();
  }

  public Map<Long, DoseFormDto> getDoseForms()
  {
    return getValue().getDoseForms();
  }

  @Override
  protected MedicationsHolderDo loadValue()
  {
    final Stopwatch sw = Stopwatch.createStarted();

    if (unitsValueHolder.hasNewVersion() && !unitsValueHolder.isRunning())
    {
      LOG.info("Units holder value changed, reloading units before loading medications");
      unitsValueHolder.run(false);
    }

    final Map<Long, MedicationDataDto> medicationsMap = medicationsDao.loadMedicationsMap(new DateTime());

    sw.stop();
    LOG.info(String.format("Meds value holder loaded - took: %d ms = %d seconds", sw.elapsed(MILLISECONDS), sw.elapsed(SECONDS)));

    return new MedicationsHolderDo(medicationsMap);
  }

  public static class MedicationsHolderDo
  {
    private final Map<Long, MedicationDataDto> medications;
    private final Map<Long, DoseFormDto> doseForms;

    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
    public MedicationsHolderDo(final Map<Long, MedicationDataDto> medications)
    {
      this.medications = medications;
      doseForms = medications.values().stream()
          .map(MedicationDataDto::getDoseForm)
          .filter(Objects::nonNull)
          .filter(d -> d.getId() > 0)
          .distinct()
          .collect(Collectors.toMap(DoseFormDto::getId, d -> d));
    }

    public Map<Long, MedicationDataDto> getMedications()
    {
      return Collections.unmodifiableMap(medications);
    }

    public Map<Long, DoseFormDto> getDoseForms()
    {
      return Collections.unmodifiableMap(doseForms);
    }
  }
}
