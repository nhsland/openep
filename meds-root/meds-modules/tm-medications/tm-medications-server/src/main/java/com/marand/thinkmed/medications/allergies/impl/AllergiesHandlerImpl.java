package com.marand.thinkmed.medications.allergies.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.marand.thinkehr.session.EhrSession;
import com.marand.thinkehr.session.EhrSessionType;
import com.marand.thinkehr.session.EhrSessioned;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.allergies.AllergiesHandler;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import com.marand.thinkmed.medications.connector.impl.provider.AllergiesProvider;
import com.marand.thinkmed.medications.event.MedsEventProducer;
import com.marand.thinkmed.medications.service.WarningSeverity;
import com.marand.thinkmed.medications.service.WarningType;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import com.marand.thinkmed.medications.task.MedicationsTasksHandler;
import com.marand.thinkmed.medications.warnings.TherapyWarningsProvider;
import com.marand.thinkmed.request.time.RequestDateTimeHolder;
import edu.emory.mathcs.backport.java.util.Collections;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nejc Korasa
 */

@Component
public class AllergiesHandlerImpl implements AllergiesHandler
{
  private TherapyWarningsProvider therapyWarningsProvider;
  private MedicationsTasksHandler medicationsTasksHandler;
  private RequestDateTimeHolder requestDateTimeHolder;
  private AllergiesProvider allergiesProvider;
  private MedsEventProducer medsEventProducer;

  private EhrSession ehrSession;

  @Autowired
  public void setRequestDateTimeHolder(final RequestDateTimeHolder requestDateTimeHolder)
  {
    this.requestDateTimeHolder = requestDateTimeHolder;
  }

  @Autowired
  public void setTherapyWarningsProvider(final TherapyWarningsProvider therapyWarningsProvider)
  {
    this.therapyWarningsProvider = therapyWarningsProvider;
  }

  @Autowired
  public void setMedicationsTasksHandler(final MedicationsTasksHandler medicationsTasksHandler)
  {
    this.medicationsTasksHandler = medicationsTasksHandler;
  }

  @Autowired
  public void setAllergiesProvider(final AllergiesProvider allergiesProvider)
  {
    this.allergiesProvider = allergiesProvider;
  }

  @Autowired
  public void setEhrSession(final EhrSession ehrSession)
  {
    this.ehrSession = ehrSession;
  }

  @Autowired
  public void setMedsEventProducer(final MedsEventProducer medsEventProducer)
  {
    this.medsEventProducer = medsEventProducer;
  }

  @Override
  public void handleNewAllergies(
      final @NonNull String patientId,
      final @NonNull Collection<IdNameDto> newAllergies,
      final @NonNull DateTime when)
  {
    medsEventProducer.triggerAllergiesChanged(patientId);
    if (!getAllergyWarnings(patientId, newAllergies, when).isEmpty())
    {
      medicationsTasksHandler.createCheckNewAllergiesTask(patientId, newAllergies, when);
    }
  }

  @Transactional
  @EhrSessioned(value = EhrSessionType.READ_ONLY)
  @Override
  public void handleNewAllergies(final @NonNull String ehrId, final String oldCompositionUId, final String newCompositionUId)
  {
    Preconditions.checkArgument(
        oldCompositionUId != null || newCompositionUId != null,
        "Both, old and new composition UIDs are null");

    // Ignore cases for deleted compositions
    if (newCompositionUId == null)
    {
      return;
    }

    final List<IdNameDto> latestAllergens = allergiesProvider.getAllergies(ehrId, newCompositionUId).getAllergens();

    if (oldCompositionUId == null) // CREATED
    {
      final String previousComp = allergiesProvider.getPreviousAllergyUId(ehrId, newCompositionUId);
      if (previousComp != null)
      {
        latestAllergens.removeAll(allergiesProvider.getAllergies(ehrId, previousComp).getAllergens());
      }
    }
    else // UPDATED
    {
      latestAllergens.removeAll(allergiesProvider.getAllergies(ehrId, oldCompositionUId).getAllergens());
    }

    handleNewAllergies(ehrId, latestAllergens);
  }

  private void handleNewAllergies(final @NonNull String ehrId, final List<IdNameDto> allergies)
  {
    final String patientId = ehrSession.getSubjectId(ehrId);
    final DateTime requestTime = requestDateTimeHolder.getRequestTimestamp();
    handleNewAllergies(patientId, allergies, requestTime);
  }

  @Override
  public List<MedicationsWarningDto> getAllergyWarnings(
      final @NonNull String patientId,
      final @NonNull Collection<IdNameDto> allergies,
      final @NonNull DateTime when)
  {

    if (allergies.isEmpty())
    {
      return new ArrayList<>();
    }

    // screening for allergy warnings does not require patient data. DateOfBirth is mocked.
    // noinspection unchecked
    final List<MedicationsWarningDto> medicationWarnings = therapyWarningsProvider.findCurrentTherapiesWarnings(
        patientId,
        new DateTime(1993, 12, 26, 0, 0), // mocked data
        null,
        null,
        Gender.NOT_KNOWN,
        Collections.emptyList(),
        new ArrayList<>(allergies),
        false,
        requestDateTimeHolder.getRequestTimestamp(),
        null);

    return medicationWarnings
        .stream()
        .filter(m -> m.getType() == WarningType.ALLERGY)
        .filter(m -> m.getSeverity() == WarningSeverity.HIGH_OVERRIDE || m.getSeverity() == WarningSeverity.HIGH)
        .collect(Collectors.toList());
  }
}
