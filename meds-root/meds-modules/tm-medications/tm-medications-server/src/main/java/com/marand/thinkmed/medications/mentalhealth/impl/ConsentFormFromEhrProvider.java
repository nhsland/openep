package com.marand.thinkmed.medications.mentalhealth.impl;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.marand.maf.core.Opt;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentType;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthMedicationDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTemplateDto;
import com.marand.thinkmed.medications.ehr.model.consentform.ConsentType;
import com.marand.thinkmed.medications.ehr.model.consentform.MedicationConsentForm;
import com.marand.thinkmed.medications.ehr.model.consentform.MedicationConsentItem;
import com.marand.thinkmed.medications.ehr.model.consentform.MedicationType;
import com.marand.thinkmed.medications.mentalhealth.MentalHealthFormProvider;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolder;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConsentFormFromEhrProvider implements MentalHealthFormProvider
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private MedicationsValueHolder medicationsValueHolder;

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Autowired
  public void setMedicationsValueHolder(final MedicationsValueHolder medicationsValueHolder)
  {
    this.medicationsValueHolder = medicationsValueHolder;
  }

  @Override
  public Opt<MentalHealthDocumentDto> getLatestMentalHealthDocument(final @NonNull String patientId)
  {
    final MedicationConsentForm medicationConsentForm = medicationsOpenEhrDao.findLatestConsentForm(patientId).get();

    return Opt.of(buildMentalHealthDocumentDto(medicationConsentForm, patientId));
  }

  @Override
  public MentalHealthDocumentDto getMentalHealthDocument(
      final @NonNull String patientId,
      final @NonNull String compositionUId)
  {
    return buildMentalHealthDocumentDto(
        medicationsOpenEhrDao.loadConsentFormComposition(patientId, compositionUId),
        patientId);
  }

  @Override
  public Collection<MentalHealthDocumentDto> getMentalHealthDocuments(
      final @NonNull String patientId,
      final Interval interval,
      final Integer fetchCount)
  {
    return medicationsOpenEhrDao.findMedicationConsentFormCompositions(patientId, interval, fetchCount)
        .stream()
        .map(form -> buildMentalHealthDocumentDto(form, patientId))
        .collect(Collectors.toList());
  }

  private MentalHealthDocumentDto buildMentalHealthDocumentDto(
      final MedicationConsentForm consentForm,
      final String patientId)
  {
    if (consentForm != null)
    {
      final NamedExternalDto composer = extractComposer(consentForm);
      final MentalHealthDocumentType consentType = extractConsentType(consentForm);
      final Integer maximumCumulativeDose = extractMaximumCumulativeDose(consentForm);
      final List<MentalHealthMedicationDto> mentalHealthMedications = extractMentalHealthMedications(consentForm);
      final List<MentalHealthTemplateDto> mentalHealthTemplateDtos = extractMentalHealthTemplates(consentForm);
      final DateTime startTime = DataValueUtils.getDateTime(consentForm.getContext().getStartTime());

      return new MentalHealthDocumentDto(
          consentForm.getUid(),
          startTime,
          composer,
          patientId,
          composer,
          consentType,
          maximumCumulativeDose,
          mentalHealthMedications,
          mentalHealthTemplateDtos);
    }
    return null;
  }

  private NamedExternalDto extractComposer(final MedicationConsentForm consentForm)
  {
    return Opt.resolve(() -> new NamedExternalDto(consentForm.getComposer().getId(), consentForm.getComposer().getName()))
        .orElse(null);
  }

  private MentalHealthDocumentType extractConsentType(final MedicationConsentForm consentForm)
  {
    if (consentForm != null)
    {
      if (consentForm.getMedicationConsent() != null)
      {
        if (consentForm.getMedicationConsent().getConsentType() != null)
        {
          if (consentForm.getMedicationConsent().getConsentType().equals(ConsentType.T2.getDvCodedText()))
          {
            return MentalHealthDocumentType.T2;
          }

          if (consentForm.getMedicationConsent().getConsentType().equals(ConsentType.T3.getDvCodedText()))
          {
            return MentalHealthDocumentType.T3;
          }
        }
      }
    }
    return null;
  }

  @SuppressWarnings("NumericCastThatLosesPrecision")
  private Integer extractMaximumCumulativeDose(final MedicationConsentForm consentForm)
  {
    return Opt.resolve(() -> (int)consentForm.getMedicationConsent().getMaximumCumulativeDose().getMagnitude())
        .orElse(null);
  }

  private List<MentalHealthMedicationDto> extractMentalHealthMedications(final MedicationConsentForm consentForm)
  {
    if (consentForm != null)
    {
      if (consentForm.getMedicationList() != null)
      {
        if (consentForm.getMedicationList().getMedicationConsentItem() != null)
        {
          return consentForm.getMedicationList().getMedicationConsentItem().stream()
              .filter(m -> MedicationType.valueOf(m.getMedicationItem().getType()) == MedicationType.MEDICATION)
              .map(this::getMentalHealthMedicationDto)
              .collect(Collectors.toList());
        }
      }
    }
    return null;
  }

  private MentalHealthMedicationDto getMentalHealthMedicationDto(final MedicationConsentItem item)
  {
    final DvCodedText name = (DvCodedText)item.getMedicationItem().getName();
    final Long medicationId = Long.valueOf(name.getDefiningCode().getCodeString());
    return new MentalHealthMedicationDto(
        medicationId,
        name.getValue(),
        extractGenericName(medicationId),
        extractRoute(item));
  }

  private List<MentalHealthTemplateDto> extractMentalHealthTemplates(final MedicationConsentForm consentForm)
  {
    if (consentForm != null)
    {
      if (consentForm.getMedicationList() != null)
      {
        if (consentForm.getMedicationList().getMedicationConsentItem() != null)
        {
          return consentForm.getMedicationList().getMedicationConsentItem().stream()
              .filter(m -> MedicationType.valueOf(m.getMedicationItem().getType()) == MedicationType.MEDICATION_GROUP)
              .map(m -> new MentalHealthTemplateDto(
                  Long.valueOf(((DvCodedText)m.getMedicationItem().getName()).getDefiningCode().getCodeString()),
                  m.getMedicationItem().getName().getValue(),
                  extractRoute(m)))
              .collect(Collectors.toList());
        }
      }
    }
    return null;
  }

  private String extractGenericName(final Long medicationId)
  {
    final MedicationDataDto medicationData = medicationsValueHolder.getMedications().get(medicationId);
    return medicationData.getMedication().getGenericName();
  }

  private MedicationRouteDto extractRoute(final MedicationConsentItem medicationConsentItem)
  {
    if (medicationConsentItem.getRoute() != null)
    {
      final DvText route = medicationConsentItem.getRoute();
      final MedicationRouteDto medicationRouteDto = new MedicationRouteDto();
      medicationRouteDto.setId(Long.valueOf(((DvCodedText)route).getDefiningCode().getCodeString()));
      medicationRouteDto.setName(route.getValue());
      return medicationRouteDto;
    }
    return null;
  }
}
