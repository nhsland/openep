package com.marand.thinkmed.medications.dto.discharge;

/**
 * @author Nejc Korasa
 */

public enum MedicationOnDischargeStatus
{
  PRESCRIBED,
  EDITED_AND_PRESCRIBED, // modified and added to discharge list with MODIFY_EXISTING medication action
  NOT_PRESCRIBED // added to discharge list, with CANCEL medication action
}
