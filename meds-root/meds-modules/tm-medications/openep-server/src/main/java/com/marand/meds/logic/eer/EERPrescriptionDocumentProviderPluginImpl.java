package com.marand.meds.logic.eer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.marand.maf.core.openehr.util.DvUtils;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.api.internal.dto.OutpatientPrescriptionStatus;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.document.TherapyDocumentDto;
import com.marand.thinkmed.medications.api.internal.dto.document.TherapyDocumentTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.eer.EERPrescriptionLocalDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.prescription.PrescriptionPackageDto;
import com.marand.thinkmed.medications.api.internal.dto.prescription.PrescriptionTherapyDto;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.document.TherapyDocumentProviderPlugin;
import com.marand.thinkmed.medications.ehr.model.MedicationAuthorisationSlovenia;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.model.OutpatientPrescription;
import com.marand.thinkmed.medications.therapy.converter.TherapyConverter;
import org.joda.time.DateTime;
import org.joda.time.base.AbstractInstant;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */
@Component
public class EERPrescriptionDocumentProviderPluginImpl implements TherapyDocumentProviderPlugin
{
  private TherapyDisplayProvider therapyDisplayProvider;
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private TherapyConverter therapyConverter;

  @Autowired
  public void setTherapyDisplayProvider(final TherapyDisplayProvider therapyDisplayProvider)
  {
    this.therapyDisplayProvider = therapyDisplayProvider;
  }

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Autowired
  public void setTherapyConverter(final TherapyConverter therapyConverter)
  {
    this.therapyConverter = therapyConverter;
  }

  @Override
  public Collection<TherapyDocumentTypeEnum> getPluginDocumentTypes()
  {
    return Collections.singletonList(TherapyDocumentTypeEnum.EER_PRESCRIPTION);
  }

  @Override
  public List<TherapyDocumentDto> getTherapyDocuments(
      final String patientId,
      final Integer numberOfResults,
      final DateTime when,
      final Locale locale)
  {
    final List<OutpatientPrescription> compositions = medicationsOpenEhrDao.findOutpatientPrescriptions(
        patientId,
        numberOfResults);

    final List<TherapyDocumentDto> documentsList = new ArrayList<>();
    for (final OutpatientPrescription composition : compositions)
    {
      final TherapyDocumentDto documentDto = convertCompositionToTherapyDocumentDto(locale, composition);
      documentsList.add(documentDto);
    }
    return documentsList;
  }

  @Override
  public TherapyDocumentDto getTherapyDocument(
      final String patientId,
      final String contentId,
      final Locale locale)
  {
    final OutpatientPrescription composition = medicationsOpenEhrDao.loadOutpatientPrescription(patientId, contentId);
    return convertCompositionToTherapyDocumentDto(locale, composition);
  }

  private TherapyDocumentDto convertCompositionToTherapyDocumentDto(
      final Locale locale,
      final OutpatientPrescription composition)
  {
    final TherapyDocumentDto documentDto = new TherapyDocumentDto();
    documentDto.setCreateTimestamp(DataValueUtils.getDateTime(composition.getContext().getStartTime()));
    documentDto.setCreator(new NamedExternalDto(composition.getComposer().getId(), composition.getComposer().getName()));
    documentDto.setDocumentType(TherapyDocumentTypeEnum.EER_PRESCRIPTION);

    final DvText careProvider = composition.getContext().getContextDetail().getDepartmentalPeriodOfCareIdentifier();
    if (careProvider instanceof DvCodedText)
    {
      documentDto.setCareProvider(new NamedExternalDto(
          ((DvCodedText)careProvider).getDefiningCode().getCodeString(),
          careProvider.getValue()));
    }

    final PrescriptionPackageDto prescriptionPackageDto = new PrescriptionPackageDto();
    prescriptionPackageDto.setCompositionUid(composition.getUid());
    documentDto.setContent(prescriptionPackageDto);

    for (final MedicationOrder medicationOrder : composition.getMedicationOrder())
    {
      final PrescriptionTherapyDto prescriptionTherapyDto = new PrescriptionTherapyDto();

      final TherapyDto therapyDto = therapyConverter.convertToTherapyDto(
          medicationOrder,
          composition.getUid(),
          DataValueUtils.getDateTime(composition.getContext().getStartTime()));

      if (therapyDto.getPrescriptionLocalDetails() instanceof EERPrescriptionLocalDetailsDto)
      {
        final MedicationAuthorisationSlovenia authorisationDetails = medicationOrder.getAuthorisationDirection();

        prescriptionTherapyDto.setPrescriptionStatus(OutpatientPrescriptionStatus.valueOf(authorisationDetails.getPrescriptionStatus()));

        prescriptionTherapyDto.setPrescriptionTherapyId(
            DvUtils.getString(authorisationDetails.getePrescriptionUniqueIdentifier()));

        prescriptionPackageDto.setPrescriptionPackageId(
            DvUtils.getString(authorisationDetails.getPackageEPrescriptionUniqueIdentifier()));
      }

      therapyDisplayProvider.fillDisplayValues(therapyDto, true, locale);

      prescriptionTherapyDto.setTherapy(therapyDto);
      prescriptionPackageDto.getPrescriptionTherapies().add(prescriptionTherapyDto);

      prescriptionPackageDto.setLastUpdateTimestamp(
          composition.getMedicationOrder().stream()
              .filter(m -> m.getAuthorisationDirection() != null)
              .map(m -> DataValueUtils.getDateTime(m.getAuthorisationDirection().getUpdateTimestamp()))
              .filter(Objects::nonNull)
              .max(AbstractInstant::compareTo)
              .orElse(null));
    }
    return documentDto;
  }
}
