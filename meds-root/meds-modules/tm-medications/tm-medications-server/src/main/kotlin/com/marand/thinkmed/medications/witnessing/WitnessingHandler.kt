package com.marand.thinkmed.medications.witnessing

import com.marand.thinkmed.medications.api.internal.dto.property.MedicationPropertyType
import com.marand.thinkmed.medications.connector.data.`object`.PatientDataForMedicationsDto
import com.marand.thinkmed.medications.dto.MedicationDataDto
import com.marand.thinkmed.medications.dto.overview.TherapyDayDto
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider
import org.joda.time.DateTime
import org.joda.time.Period
import org.springframework.stereotype.Component

/**
 * @author Nejc Korasa
 */

@Component
open class WitnessingHandler(
        private val witnessingProperties: WitnessingProperties,
        private val medicationsValueHolderProvider: MedicationsValueHolderProvider) {

    open fun isTherapyWitnessingRequired(therapyDay: TherapyDayDto): Boolean {
        return if (witnessingProperties.isEnabled) {
            therapyDay.therapy
                    ?.medications
                    ?.filter { it.id != null }
                    ?.any { isMedicationWitnessingRequired(medicationsValueHolderProvider.getMedicationData(it.id)) }
                    ?: false
        } else false
    }

    open fun isPatientWitnessingRequired(patientData: PatientDataForMedicationsDto): Boolean =
            witnessingProperties.ageLimit
                    ?.let { limit ->
                        patientData.birthDate
                                ?.let { birth -> Period(birth, DateTime.now()).years <= limit }
                                ?: false
                    }
                    ?: false

    private fun isMedicationWitnessingRequired(medication: MedicationDataDto) =
            medication.hasProperty(MedicationPropertyType.WITNESSING) || medication.isControlledDrug
}
