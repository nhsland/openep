package com.marand.thinkmed.medications.automatic.create;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.google.common.base.Stopwatch;
import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.AutomaticAdministrationTaskCreatorDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.utils.PrescriptionsEhrUtils;
import com.marand.thinkmed.medications.event.MedsEventProducer;
import com.marand.thinkmed.request.user.StaticAuth;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nejc Korasa
 */

public class AdministrationAutoTaskCreator
{
  private static final Logger LOG = LoggerFactory.getLogger(AdministrationAutoTaskCreator.class);

  /**
   * Stores time millis of latest successfully executed task creator
   */
  private final AtomicLong lastSuccessMillis = new AtomicLong(-1);

  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private AdministrationAutoTaskCreatorHandler administrationAutoTaskCreatorHandler;
  private MedsEventProducer medsEventProducer;

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Autowired
  public void setAdministrationAutoTaskCreatorHandler(final AdministrationAutoTaskCreatorHandler administrationAutoTaskCreatorHandler)
  {
    this.administrationAutoTaskCreatorHandler = administrationAutoTaskCreatorHandler;
  }

  @Autowired
  public void setMedsEventProducer(final MedsEventProducer medsEventProducer)
  {
    this.medsEventProducer = medsEventProducer;
  }

  @Transactional
  @Scheduled(cron = "${meds.auto-task-creator-cron}")
  public void run()
  {
    SecurityContextHolder.getContext().setAuthentication(new StaticAuth("Think!Meds", "Think!Meds,", "Think!Meds,"));
    createAdministrationTasks(new DateTime());
  }

  private void createAdministrationTasks(final DateTime time)
  {
    final Stopwatch sw = Stopwatch.createStarted();
    LOG.info("Starting task creator");

    // run only if not yet successful for the day
    final DateTime lastSuccess = new DateTime(lastSuccessMillis.get());
    if (lastSuccess.getDayOfYear() == time.getDayOfYear())
    {
      LOG.info("Skipping task creator, ran successfully at " + lastSuccess);
      return;
    }

    // load prescriptions
    final Map<InpatientPrescription, String> activePrescriptionsMap = medicationsOpenEhrDao.getActiveInpatientPrescriptionsWithPatientIds(time);

    final Map<InpatientPrescription, String> nonSuspendedPrescriptionsMap = activePrescriptionsMap.entrySet()
        .stream()
        .filter(entry -> !PrescriptionsEhrUtils.isTherapySuspended(entry.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    LOG.info("Retrieved " + nonSuspendedPrescriptionsMap.size() + " prescriptions from EHR - TOOK: " + sw.elapsed(TimeUnit.MILLISECONDS) + " ms");

    // validate count
    final boolean validCount = validateActiveInstructionsCount(time, activePrescriptionsMap.size());

    final List<AutomaticAdministrationTaskCreatorDto> autoTaskCreatorDtos = administrationAutoTaskCreatorHandler.getAutoAdministrationTaskCreatorDtos(
        time,
        nonSuspendedPrescriptionsMap);

    final Set<String> processedPatientIds = new HashSet<>();
    int successCount = 0;
    for (final AutomaticAdministrationTaskCreatorDto dto : autoTaskCreatorDtos)
    {
      try
      {
        administrationAutoTaskCreatorHandler.createAdministrationTasksOnAutoCreate(dto, time);
        processedPatientIds.add(dto.getPatientId());
        successCount++;
      }
      catch (final Throwable t)
      {
        final String compUid = Opt.resolve(() -> dto.getTherapyDto().getCompositionUid()).get();
        LOG.error("Failed creating tasks - patientId: " + dto.getPatientId() + " compositionUid:" + compUid + "\n" + ExceptionUtils.getFullStackTrace(t));
      }
    }

    for (final String patientId : processedPatientIds)
    {
      try
      {
        medsEventProducer.triggerAdministrationChanged(patientId);
      }
      catch (final Throwable t)
      {
        LOG.error("Failed invalidation patient cache - patientId : " + patientId);
      }
    }

    sw.stop();
    LOG.info(String.format(
        "Successfully created administration tasks for %d therapies and %d patients - TOOK: %d ms = %d seconds",
        successCount,
        processedPatientIds.size(),
        sw.elapsed(TimeUnit.MILLISECONDS),
        sw.elapsed(TimeUnit.SECONDS)));

    // update last success
    if (validCount)
    {
      lastSuccessMillis.set(time.getMillis());
      LOG.info("Setting last success time to " + time);
    }
  }

  @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
  private boolean validateActiveInstructionsCount(final DateTime time, final int activeInstructionsCount)
  {
    final int actualCount = medicationsOpenEhrDao.countActiveMedicationOrdersWithPatientIds(time);
    final boolean countValid = actualCount == activeInstructionsCount;
    if (!countValid)
    {
      LOG.error(String.format(
          "Invalid count, loaded %d active medication instructions with streaming query, actual count is %d, difference: %d",
          activeInstructionsCount,
          actualCount,
          actualCount - activeInstructionsCount));
    }
    return countValid;
  }
}
