package com.marand.thinkmed.medications.ehr.utils;

import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.ehr.model.Composer;
import com.marand.thinkmed.medications.ehr.model.Identifier;
import com.marand.thinkmed.medications.ehr.model.PrescriptionIdentifierType;
import com.marand.thinkmed.medications.ehr.model.composition.EhrComposition;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */

public class EhrContextVisitor
{
  private final EhrComposition composition;

  private String centralCaseId;
  private String careProviderId;
  private Composer composer;
  private DateTime startTime;

  // only for PRESCRIPTION composition
  private Identifier prescriptionIdentifier;

  // only for REPORT composition
  private String status;

  public EhrContextVisitor(final EhrComposition composition)
  {
    this.composition = composition;
  }

  public EhrContextVisitor withStartTime(final DateTime startTime)
  {
    this.startTime = startTime;
    return this;
  }

  public EhrContextVisitor withCentralCaseId(final String centralCaseId)
  {
    this.centralCaseId = centralCaseId;
    return this;
  }

  public EhrContextVisitor withCareProvider(final String careProviderId)
  {
    this.careProviderId = careProviderId;
    return this;
  }

  public EhrContextVisitor withComposer(final Composer composer)
  {
    this.composer = composer;
    return this;
  }

  public EhrContextVisitor withComposer(final NamedExternalDto namedExternal)
  {
    return withComposer(namedExternal.getId(), namedExternal.getName());
  }

  public EhrContextVisitor withComposer(final String composerId, final String composerName)
  {
    final Composer comp = new Composer();
    comp.setName(composerName);
    comp.setId(composerId);
    return withComposer(comp);
  }

  public EhrContextVisitor withPrescriptionIdentifier(final PrescriptionIdentifierType type, final String id)
  {
    prescriptionIdentifier = type.buildIdentifier(id);
    return this;
  }

  public EhrContextVisitor withStatus(final String status)
  {
    this.status = status;
    return this;
  }

  public void visit()
  {
    ContextEhrUtils.fillContext(
        composition,
        centralCaseId,
        careProviderId,
        composer,
        prescriptionIdentifier,
        status,
        startTime);
  }
}
