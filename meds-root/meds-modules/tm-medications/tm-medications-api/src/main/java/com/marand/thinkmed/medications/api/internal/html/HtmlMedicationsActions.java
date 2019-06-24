package com.marand.thinkmed.medications.api.internal.html;

/**
 * @author Bostjan Vester
 */
public interface HtmlMedicationsActions
{
  String PRINT_SURGERY_THERAPY_REPORT = "printSurgeryTherapyReport";
  String SAVE_CONTEXT = "SAVE_CONTEXT";
  String OPEN_PATIENT = "openPatient";
  String OUTPATIENT_PRESCRIPTION = "outpatientPrescription";
  String CANCEL_PRESCRIPTION = "cancelPrescription";
  String UPDATE_OUTPATIENT_PRESCRIPTION = "updateOutpatientPrescription";
  String GET_EXTERNAL_OUTPATIENT_PRESCRIPTIONS = "getExternalOutpatientPrescription";
  String PRINT_PERFUSION_SYRINGE_LABEL = "perfusionSyringeLabelPrint";
  String DELETE_OUTPATIENT_PRESCRIPTION = "deleteOutpatientPrescription";
  String AUTHORIZE_OUTPATIENT_PRESCRIPTION = "authorizeOutpatientPrescription";
  String AUTHENTICATE_ADMINISTRATION_WITNESS = "authenticateAdministrationWitness";
  String CREATE_AND_OPEN_FILE = "createAndOpenFile";
}
