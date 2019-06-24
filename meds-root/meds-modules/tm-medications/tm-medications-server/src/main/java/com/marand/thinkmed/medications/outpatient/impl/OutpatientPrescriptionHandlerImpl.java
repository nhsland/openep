package com.marand.thinkmed.medications.outpatient.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.marand.ispek.print.common.PrintContext;
import com.marand.ispek.print.common.ReportAction;
import com.marand.ispek.print.jasperreports.JasperReportPrintParameters;
import com.marand.ispek.print.jasperreports.JasperReportsUtils;
import com.marand.maf.core.Opt;
import com.marand.maf.core.StringUtils;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkehr.session.EhrSessioned;
import com.marand.thinkehr.util.ConversionUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.api.internal.dto.prescription.PrescriptionPackageDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.eer.EERPrescriptionPackageDto;
import com.marand.thinkmed.medications.api.internal.dto.prescription.PrescriptionPackageDto;
import com.marand.thinkmed.medications.api.internal.dto.prescription.PrescriptionTherapyDto;
import com.marand.thinkmed.medications.connector.MedicationsConnector;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.report.OutpatientPrescriptionPrintoutDto;
import com.marand.thinkmed.medications.dto.report.PrescriptionForPrintoutDto;
import com.marand.thinkmed.medications.ehr.model.MedicationAuthorisationSlovenia;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.model.OutpatientPrescription;
import com.marand.thinkmed.medications.ehr.utils.EhrContextVisitor;
import com.marand.thinkmed.medications.ehr.utils.MedicationsEhrUtils;
import com.marand.thinkmed.medications.outpatient.OutpatientPrescriptionHandler;
import com.marand.thinkmed.medications.report.MedicationsReports;
import com.marand.thinkmed.medications.therapy.converter.TherapyConverter;
import com.marand.thinkmed.request.user.RequestUser;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Mitja Lapajne
 */

public abstract class OutpatientPrescriptionHandlerImpl implements OutpatientPrescriptionHandler
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private TherapyConverter therapyConverter;
  private MedicationsConnector medicationsConnector;

  protected MedicationsOpenEhrDao getEhrMedicationsDao()
  {
    return medicationsOpenEhrDao;
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

  @Autowired
  public void setMedicationsConnector(final MedicationsConnector medicationsConnector)
  {
    this.medicationsConnector = medicationsConnector;
  }

  @Override
  @EhrSessioned
  public String savePrescription(
      final String patientId,
      final PrescriptionPackageDto prescriptionPackageDto,
      final DateTime when)
  {
    final OutpatientPrescription composition = new OutpatientPrescription();

    composition.setMedicationOrder(buildMedicationOrders(prescriptionPackageDto));
    addContext(prescriptionPackageDto, when, composition);

    return medicationsOpenEhrDao.saveComposition(patientId, composition, prescriptionPackageDto.getCompositionUid());
  }

  private List<MedicationOrder> buildMedicationOrders(final PrescriptionPackageDto prescriptionPackageDto)
  {
    final List<MedicationOrder> medicationOrders = new ArrayList<>();
    for (final PrescriptionTherapyDto prescriptionDto : prescriptionPackageDto.getPrescriptionTherapies())
    {
      final TherapyDto therapy = prescriptionDto.getTherapy();
      final MedicationOrder medicationOrder = therapyConverter.convertToMedicationOrder(therapy);
      fillAuthorisationData(medicationOrder, prescriptionDto, prescriptionPackageDto);
      medicationOrders.add(medicationOrder);
    }
    return medicationOrders;
  }

  private void addContext(
      final PrescriptionPackageDto prescriptionPackageDto,
      final DateTime when,
      final OutpatientPrescription composition)
  {
    final EhrContextVisitor contextVisitor = new EhrContextVisitor(composition)
        .withStartTime(when);

    if (prescriptionPackageDto.getComposer() != null)
    {
      contextVisitor.withComposer(prescriptionPackageDto.getComposer());
    }
    else
    {
      contextVisitor.withComposer(RequestUser.getId(), RequestUser.getFullName());
    }

    contextVisitor.visit();

    final NamedExternalDto careProvider = extractCareProvider(prescriptionPackageDto);
    if (careProvider != null)
    {
      composition.getContext().getContextDetail().setDepartmentalPeriodOfCareIdentifier(
          DataValueUtils.getLocalCodedText(careProvider.getId(), careProvider.getName()));
    }
  }

  private NamedExternalDto extractCareProvider(final PrescriptionPackageDto prescriptionPackageDto)
  {
    return prescriptionPackageDto instanceof EERPrescriptionPackageDto
           ? ((EERPrescriptionPackageDto)prescriptionPackageDto).getCareProvider()
           : null;
  }

  protected abstract void fillAuthorisationData(
      MedicationOrder medicationOrder,
      PrescriptionTherapyDto prescriptionDto,
      PrescriptionPackageDto prescriptionPackage);

  @Override
  public byte[] getOutpatientPrescriptionPrintout(
      final @NonNull String patientId,
      final @NonNull String compositionUid,
      final @NonNull Locale locale,
      final @NonNull DateTime when)
  {
    final PatientDataForMedicationsDto patientData = medicationsConnector.getPatientData(patientId, null, when);

    final OutpatientPrescriptionPrintoutDto printoutDto = new OutpatientPrescriptionPrintoutDto();

    printoutDto.setPatientName(patientData.getPatientName());
    printoutDto.setBirthDate(formatDateTime(patientData.getBirthDate()));
    printoutDto.setGender(patientData.getGender());

    final OutpatientPrescription composition = medicationsOpenEhrDao.loadOutpatientPrescription(patientId, compositionUid);

    printoutDto.setDateOfPrescription(formatDateTime(ConversionUtils.toDateTime(composition.getContext().getStartTime())));
    printoutDto.setPrescriber(composition.getComposer().getName());
    printoutDto.setWard(
        Opt.resolve(() -> composition.getContext().getContextDetail().getDepartmentalPeriodOfCareIdentifier().getValue())
            .orElse(null));

    for (final MedicationOrder order : composition.getMedicationOrder())
    {
      final MedicationAuthorisationSlovenia authorization = order.getAuthorisationDirection();

      if (authorization.getPackageEPrescriptionUniqueIdentifier() != null)
      {
        printoutDto.setPrescriptionsPackageId(authorization.getPackageEPrescriptionUniqueIdentifier().getValue());
      }

      printoutDto.getPrescriptions().add(buildPrescriptionForPrintoutDto(order));
    }

    final JasperReportPrintParameters parameters =
        new JasperReportPrintParameters(
            MedicationsReports.OUTPATIENT_PRESCRIPTIONS,
            Collections.singleton(printoutDto),
            ReportAction.PDF,
            PrintContext.INSTANCE.getValuesProvider().getLoggedUserName(),
            false);

    return JasperReportsUtils.createPdfByteArray(parameters);
  }

  private String formatDateTime(final DateTime dateTime)
  {
    return dateTime.getDayOfMonth() + "." + dateTime.getMonthOfYear() + "." + dateTime.getYear();
  }

  private PrescriptionForPrintoutDto buildPrescriptionForPrintoutDto(final MedicationOrder order)
  {
    final PrescriptionForPrintoutDto prescriptionDto = new PrescriptionForPrintoutDto();

    final MedicationAuthorisationSlovenia authorisation = order.getAuthorisationDirection();
    if (authorisation.getePrescriptionUniqueIdentifier() != null)
    {
      prescriptionDto.setPrescriptionId(authorisation.getePrescriptionUniqueIdentifier().getValue());
    }

    if (MedicationsEhrUtils.isSimpleTherapy(order))
    {
      prescriptionDto.setMedicationName(order.getMedicationItem().getValue());
    }
    else
    {
      prescriptionDto.setMedicationName(order.getPreparationDetails().getComponentName().getValue());
    }


    return prescriptionDto;
  }
}
