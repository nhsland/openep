package com.marand.thinkmed.medications.admission

import com.marand.thinkmed.medications.MedicationOrderActionEnum
import com.marand.thinkmed.medications.TherapyStatusEnum
import com.marand.thinkmed.medications.dto.MedicationActionEnum
import com.marand.thinkmed.medications.dto.MedicationActionEnum.ABORT
import com.marand.thinkmed.medications.dto.MedicationActionEnum.CANCEL
import com.marand.thinkmed.medications.dto.MedicationActionEnum.COMPLETE
import com.marand.thinkmed.medications.dto.MedicationActionEnum.MODIFY_EXISTING
import com.marand.thinkmed.medications.dto.MedicationActionEnum.SCHEDULE
import com.marand.thinkmed.medications.dto.MedicationActionEnum.SUSPEND
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionStatus
import com.marand.thinkmed.medications.ehr.model.MedicationManagement

/**
 * @author Nejc Korasa
 */

class AdmissionStatusMapper {

    companion object {

        fun mapToMedicationActionEnum(orderAction: MedicationOrderActionEnum): MedicationActionEnum = when (orderAction) {

            MedicationOrderActionEnum.ABORT -> ABORT
            MedicationOrderActionEnum.SUSPEND -> SUSPEND
            MedicationOrderActionEnum.SUSPEND_ADMISSION -> SUSPEND
            MedicationOrderActionEnum.PRESCRIBE -> SCHEDULE
            MedicationOrderActionEnum.PRESCRIBE_AND_ADMINISTER -> SCHEDULE
            MedicationOrderActionEnum.EDIT -> MODIFY_EXISTING
        }

        fun mapToAdmissionStatus(medicationAction: MedicationManagement): MedicationOnAdmissionStatus? = when (MedicationActionEnum.getActionEnum(medicationAction)) {

            null -> null
            ABORT -> MedicationOnAdmissionStatus.ABORTED
            SUSPEND -> MedicationOnAdmissionStatus.SUSPENDED
            SCHEDULE -> MedicationOnAdmissionStatus.PRESCRIBED
            MODIFY_EXISTING -> MedicationOnAdmissionStatus.EDITED_AND_PRESCRIBED
            else -> MedicationOnAdmissionStatus.PENDING
        }

        fun mapToTherapyStatus(medicationAction: MedicationManagement?): TherapyStatusEnum? {

            medicationAction ?: return null
            return mapToTherapyStatus(MedicationActionEnum.getActionEnum(medicationAction))
        }

        fun mapToTherapyStatus(actionEnum: MedicationActionEnum?): TherapyStatusEnum? {

            actionEnum ?: return null
            return when (actionEnum) {
                ABORT -> TherapyStatusEnum.ABORTED
                SUSPEND -> TherapyStatusEnum.SUSPENDED
                CANCEL -> TherapyStatusEnum.CANCELLED
                MODIFY_EXISTING -> TherapyStatusEnum.NORMAL
                COMPLETE-> TherapyStatusEnum.NORMAL
                else -> null
            }
        }

        fun mapToTherapyStatus(medicationOnAdmissionStatus: MedicationOnAdmissionStatus?): TherapyStatusEnum? {

            return when (medicationOnAdmissionStatus) {
                MedicationOnAdmissionStatus.PENDING -> TherapyStatusEnum.NORMAL
                MedicationOnAdmissionStatus.PRESCRIBED -> TherapyStatusEnum.NORMAL
                MedicationOnAdmissionStatus.EDITED_AND_PRESCRIBED -> TherapyStatusEnum.NORMAL
                MedicationOnAdmissionStatus.SUSPENDED -> TherapyStatusEnum.SUSPENDED
                MedicationOnAdmissionStatus.ABORTED -> TherapyStatusEnum.ABORTED
                null -> TherapyStatusEnum.NORMAL
            }
        }
    }
}