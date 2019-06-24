package com.marand.thinkmed.medications.therapy.ehr;

import java.util.ArrayList;
import java.util.List;

import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.openehr.util.OpenEhrRefUtils;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.composition.EhrComposition;
import com.marand.thinkmed.medications.ehr.utils.LinksEhrUtils;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.DvEhrUri;
import org.openehr.jaxb.rm.Link;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class TherapyEhrHandler
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  /**
   * @return list of all previous therapyIds from update links (including {@param therapyId})
   */
  public List<String> getAllPreviousTherapyIds(final @NonNull String patientId, final @NonNull String therapyId)
  {
    final InpatientPrescription prescription = medicationsOpenEhrDao.loadInpatientPrescription(
        patientId,
        TherapyIdUtils.extractCompositionUid(therapyId));

    List<Link> updateLinks = LinksEhrUtils.getLinksOfType(prescription, EhrLinkType.UPDATE);

    final List<String> therapyIds = new ArrayList<>();
    therapyIds.add(therapyId);

    while (!updateLinks.isEmpty())
    {
      final InpatientPrescription previousPrescription = getPrescriptionFromLink(patientId, updateLinks.get(0), true);
      therapyIds.add(TherapyIdUtils.createTherapyId(previousPrescription));
      updateLinks = LinksEhrUtils.getLinksOfType(previousPrescription, EhrLinkType.UPDATE);
    }

    return therapyIds;
  }

  public InpatientPrescription getPrescriptionFromLink(
      final @NonNull String patientId,
      final @NonNull EhrComposition composition,
      final @NonNull EhrLinkType linkType,
      final boolean getLatestVersion)
  {
    final List<Link> updateLinks = LinksEhrUtils.getLinksOfType(composition, linkType);
    return updateLinks.isEmpty() ? null : getPrescriptionFromLink(patientId, updateLinks.get(0), getLatestVersion);
  }

  public InpatientPrescription getPrescriptionFromLink(final String patientId, final Link link, final boolean getLatestVersion)
  {
    final String targetCompositionId = LinksEhrUtils.getTargetCompositionIdFromLink(link);
    final String compositionId = getLatestVersion
                                 ? TherapyIdUtils.getCompositionUidWithoutVersion(targetCompositionId)
                                 : targetCompositionId;

    return medicationsOpenEhrDao.loadInpatientPrescription(patientId, compositionId);
  }

  public DateTime getOriginalTherapyStart(
      final @NonNull String patientId,
      final @NonNull InpatientPrescription inpatientPrescription)
  {
    final String originalTherapyId = getOriginalTherapyId(inpatientPrescription);
    return medicationsOpenEhrDao.getInpatientPrescriptionStart(
        patientId,
        TherapyIdUtils.parseTherapyId(originalTherapyId).getFirst());
  }

  public String getOriginalTherapyId(final String patientId, final String compositionUid)
  {
    return getOriginalTherapyId(medicationsOpenEhrDao.loadInpatientPrescription(patientId, compositionUid));
  }

  public String getOriginalTherapyId(final @NonNull InpatientPrescription prescription)
  {
    final List<Link> originLinks = LinksEhrUtils.getLinksOfType(prescription.getLinks(), EhrLinkType.ORIGIN);
    if (!originLinks.isEmpty())
    {
      final DvEhrUri target = originLinks.get(0).getTarget();
      final OpenEhrRefUtils.EhrUriComponents ehrUri = OpenEhrRefUtils.parseEhrUri(target.getValue());
      return TherapyIdUtils.createTherapyId(ehrUri.getCompositionId());
    }
    return TherapyIdUtils.createTherapyId(prescription.getUid());
  }
}
