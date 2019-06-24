package com.marand.thinkmed.medications.service;

/**
 * @author Klavdij Lapajne
 */
public enum WarningType
{
  UNMATCHED,
  MAX_DOSE,
  PARACETAMOL,
  MENTAL_HEALTH,
  CUSTOM,
  FAILED, //from external provider
  ALLERGY,  //from external provider
  INTERACTION,  //from external provider
  DUPLICATE,  //from external provider
  PATIENT_CHECK //from external provider
}