package com.marand.thinkmed.medications.mentalhealth.impl;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentType;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthMedicationDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTemplateDto;
import com.marand.thinkmed.medications.ehr.model.consentform.MedicationConsent;
import com.marand.thinkmed.medications.ehr.model.consentform.MedicationConsentForm;
import com.marand.thinkmed.medications.ehr.model.consentform.MedicationConsentItem;
import com.marand.thinkmed.medications.ehr.model.consentform.MedicationItem;
import com.marand.thinkmed.medications.ehr.model.consentform.MedicationList;
import com.marand.thinkmed.medications.ehr.model.consentform.ConsentType;
import com.marand.thinkmed.medications.ehr.model.consentform.MedicationType;
import com.marand.thinkmed.medications.ehr.utils.EhrContextVisitor;
import com.marand.thinkmed.medications.task.MedicationsTasksHandler;
import com.marand.thinkmed.request.user.RequestUser;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Vid Kumse
 */

@Component
public class ConsentFormToEhrSaver
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private MedicationsTasksHandler medicationsTasksHandler;

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Autowired
  public void setMedicationsTasksHandler(final MedicationsTasksHandler medicationsTasksHandler)
  {
    this.medicationsTasksHandler = medicationsTasksHandler;
  }

  public void saveNewMentalHealthForm(
      final @NonNull MentalHealthDocumentDto mentalHealthDocumentDto,
      final NamedExternalDto careProvider,
      final @NonNull DateTime when)
  {
    final boolean medicationListExists = !mentalHealthDocumentDto.getMentalHealthMedicationDtoList().isEmpty()
        || !mentalHealthDocumentDto.getMentalHealthTemplateDtoList().isEmpty();

    Preconditions.checkArgument(medicationListExists, "Medication or template list is required");

    final MedicationConsentForm medicationConsentForm = new MedicationConsentForm();
    medicationConsentForm.setMedicationConsent(extractMedicationConsent(mentalHealthDocumentDto));
    medicationConsentForm.setMedicationList(extractMedicationList(mentalHealthDocumentDto));

    medicationsTasksHandler.createCheckMentalHealthMedsTask(mentalHealthDocumentDto.getPatientId(), when);

    new EhrContextVisitor(medicationConsentForm)
        .withCareProvider(careProvider != null ? careProvider.getId() : null)
        .withComposer(RequestUser.getId(), RequestUser.getFullName())
        .withStartTime(when)
        .visit();

    medicationsOpenEhrDao.saveComposition(
        mentalHealthDocumentDto.getPatientId(),
        medicationConsentForm,
        null);
  }

  private MedicationConsent extractMedicationConsent(final MentalHealthDocumentDto mentalHealthDocumentDto)
  {
    final MedicationConsent medicationConsent = new MedicationConsent();
    medicationConsent.setConsentType(mentalHealthDocumentDto.getMentalHealthDocumentType() == MentalHealthDocumentType.T2 ?
                                     ConsentType.T2.getDvCodedText() :
                                     ConsentType.T3.getDvCodedText());

    final Integer maxDosePercentage = mentalHealthDocumentDto.getMaxDosePercentage();
    if (maxDosePercentage != null)
    {
      medicationConsent.setMaximumCumulativeDose(DataValueUtils.getQuantity(maxDosePercentage, "%"));
    }

    return medicationConsent;
  }

  private MedicationList extractMedicationList(final MentalHealthDocumentDto mentalHealthDocumentDto)
  {
    final MedicationList medicationList = new MedicationList();
    final Collection<MentalHealthMedicationDto> mentalHealthMedicationDtos = mentalHealthDocumentDto.getMentalHealthMedicationDtoList();

    final List<MedicationConsentItem> medicationConsentItems = Lists.newArrayList();

    medicationConsentItems.addAll(mentalHealthMedicationDtos.stream()
                                      .map(this::mapMentalHealthMedication)
                                      .collect(Collectors.toList()));

    final Collection<MentalHealthTemplateDto> mentalHealthTemplateDtos = mentalHealthDocumentDto.getMentalHealthTemplateDtoList();
    medicationConsentItems.addAll(mentalHealthTemplateDtos.stream()
                                      .map(this::mapMentalHealthTemplate)
                                      .collect(Collectors.toList()));

    medicationList.setMedicationConsentItem(medicationConsentItems);
    return medicationList;
  }

  private MedicationConsentItem mapMentalHealthMedication(final MentalHealthMedicationDto mentalHealthMedicationDto)
  {
    final MedicationItem medicationItem = new MedicationItem();
    medicationItem.setType(MedicationType.MEDICATION.getDvCodedText());

    if (mentalHealthMedicationDto.getName() != null)
    {
      medicationItem.setName(DataValueUtils.getLocalCodedText(
          String.valueOf(mentalHealthMedicationDto.getId()),
          mentalHealthMedicationDto.getName()));
    }

    final MedicationConsentItem medicationConsentItem = new MedicationConsentItem();

    final MedicationRouteDto route = mentalHealthMedicationDto.getRoute();
    if (route != null)
    {
      medicationConsentItem.setRoute(DataValueUtils.getLocalCodedText(String.valueOf(route.getId()), route.getName()));
    }

    medicationConsentItem.setMedicationItem(medicationItem);

    return medicationConsentItem;
  }

  private MedicationConsentItem mapMentalHealthTemplate(final MentalHealthTemplateDto mentalHealthTemplateDto)
  {
    final MedicationItem medicationItem = new MedicationItem();
    medicationItem.setType(MedicationType.MEDICATION_GROUP.getDvCodedText());

    if (mentalHealthTemplateDto.getName() != null)
    {
      medicationItem.setName(DataValueUtils.getLocalCodedText(
          String.valueOf(mentalHealthTemplateDto.getId()),
          mentalHealthTemplateDto.getName()));
    }
    final MedicationConsentItem medicationConsentItem = new MedicationConsentItem();

    final MedicationRouteDto route = mentalHealthTemplateDto.getRoute();
    if (route != null)
    {
      medicationConsentItem.setRoute(DataValueUtils.getLocalCodedText(String.valueOf(route.getId()), route.getName()));
    }

    medicationConsentItem.setMedicationItem(medicationItem);

    return medicationConsentItem;
  }
}
