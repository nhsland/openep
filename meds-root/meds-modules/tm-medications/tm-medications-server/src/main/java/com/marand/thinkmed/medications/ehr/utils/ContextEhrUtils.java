package com.marand.thinkmed.medications.ehr.utils;

import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.ehr.model.Composer;
import com.marand.thinkmed.medications.ehr.model.ContextDetail;
import com.marand.thinkmed.medications.ehr.model.Identifier;
import com.marand.thinkmed.medications.ehr.model.composition.EhrComposition;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */

public class ContextEhrUtils
{
  private ContextEhrUtils() { }

  public static void fillContext(
      final EhrComposition composition,
      final String centralCaseId,
      final String careProviderId,
      final Composer composer,
      final Identifier prescriptionIdentifier,
      final String status,
      final DateTime startTime)
  {
    if (composer != null)
    {
      composition.setComposer(composer);
    }
    if (startTime != null)
    {
      composition.getContext().setStartTime(DataValueUtils.getDateTime(startTime));
    }
    if (prescriptionIdentifier != null)
    {
      composition.getContext().setPrescriptionIdentifier(prescriptionIdentifier);
    }
    if (status != null)
    {
      composition.getContext().setStatus(EhrValueUtils.getText(status));
    }

    composition.getContext().setContextDetail(buildContextDetail(centralCaseId, careProviderId));
    composition.getContext().setSetting(DataValueUtils.getOpenEhrCodedText("238", "other care"));
  }

  public static ContextDetail buildContextDetail(final String centralCaseId, final String careProviderId)
  {
    final ContextDetail contextDetail = new ContextDetail();

    if (centralCaseId != null)
    {
      contextDetail.setPeriodOfCareIdentifier(DataValueUtils.getText(centralCaseId));
    }
    if (careProviderId != null)
    {
      contextDetail.setDepartmentalPeriodOfCareIdentifier(DataValueUtils.getText(careProviderId));
    }

    return contextDetail;
  }
}
