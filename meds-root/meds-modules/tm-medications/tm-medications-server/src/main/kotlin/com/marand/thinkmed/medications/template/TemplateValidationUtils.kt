package com.marand.thinkmed.medications.template

import com.marand.thinkmed.medications.dto.MedicationDataDto
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto
import com.marand.thinkmed.medications.template.ValidationErrorType.UNIT_MISMATCH
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider
import org.springframework.stereotype.Component

/**
 * @author Nejc Korasa
 */

@Component
open class TemplateValidationUtils(private val medicationsProvider: MedicationsValueHolderProvider) {

    open fun loadMedication(medication: MedicationDto): MedicationDataDto? {

        if (medication.id == null) return null
        return medicationsProvider.getMedicationData(medication.id)
    }

    open fun matchesNumeratorOrAdminUnit(unit: String?, medication: MedicationDataDto): ValidationResult {

        if (unit == null) return ValidationResult.ok()

        val hasPrescribingDose = medication.prescribingDose != null

        val comparedUnit: String?
        val comparedUnitName: String?

        if (hasPrescribingDose) {
            comparedUnit = medication.prescribingDose.numeratorUnit
            comparedUnitName = "Numerator prescribing unit"
        } else {
            comparedUnit = medication.administrationUnit
            comparedUnitName = "Administration unit"
        }

        if (unit == comparedUnit) return ValidationResult.ok()

        return ValidationResult(
                "$comparedUnitName mismatch: Template unit = $unit $comparedUnitName = $comparedUnit [Medication: ${medication.medication.id}]",
                UNIT_MISMATCH)
    }

    open fun matchesDenominator(unit: String?, medication: MedicationDataDto): ValidationResult {

        if (unit == null) return ValidationResult.ok()

        val hasPrescribingDose = medication.prescribingDose != null
        if (hasPrescribingDose) {

            val denominatorUnit = medication.prescribingDose.denominatorUnit
            return if (denominatorUnit == unit) ValidationResult.ok()
            else ValidationResult(
                    "Denominator unit mismatch: Template unit = $unit prescribing dose unit = $denominatorUnit [Medication: ${medication.medication.id}]",
                    UNIT_MISMATCH)
        }

        if (medication.administrationUnit != null) {

            return ValidationResult(
                    "Denominator unit mismatch: Medication has administration unit [Medication: ${medication.medication.id}]",
                    UNIT_MISMATCH)
        }

        return ValidationResult(
                "Medication has neither administration unit nor prescribing dose [Medication: ${medication.medication.id}]",
                UNIT_MISMATCH)
    }
}