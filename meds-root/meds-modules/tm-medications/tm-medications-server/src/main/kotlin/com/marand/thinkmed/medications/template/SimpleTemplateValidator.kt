package com.marand.thinkmed.medications.template

import com.marand.thinkmed.medications.api.internal.dto.MedicationOrderFormType
import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto
import com.marand.thinkmed.medications.api.internal.dto.SimpleTherapyDto
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto
import com.marand.thinkmed.medications.api.internal.dto.VariableSimpleTherapyDto
import com.marand.thinkmed.medications.dto.template.TherapyTemplateElementDto
import com.marand.thinkmed.medications.template.ValidationErrorType.NO_DATA
import org.springframework.stereotype.Component

/**
 * @author Nejc Korasa
 */

@Component
class SimpleTemplateValidator(private val validationUtils: TemplateValidationUtils) : TemplateValidator {

    override fun isFor(therapy: TherapyDto?): Boolean =
            therapy?.medicationOrderFormType
                    ?.let { MedicationOrderFormType.SIMPLE_ORDERS.contains(it) }
                    ?: false

    override fun validate(template: TherapyTemplateElementDto): ValidationResult {

        val therapy = template.therapy as? SimpleTherapyDto
        therapy ?: return ValidationResult("Therapy is null", NO_DATA)

        val actualMed = validationUtils.loadMedication(therapy.medication) ?: return ValidationResult.ok()

        val results = listOf(
                validationUtils.matchesNumeratorOrAdminUnit(therapy.quantityUnit, actualMed),
                validationUtils.matchesDenominator(therapy.quantityDenominatorUnit, actualMed)
        )
        return ValidationResult.join(results)
    }

    override fun clearUnits(template: TherapyTemplateElementDto) {

        val therapy = template.therapy
        therapy ?: return

        if (therapy is ConstantSimpleTherapyDto) clearUnitsConstantTherapy(therapy)
        if (therapy is VariableSimpleTherapyDto) clearUnitsVariableTherapy(therapy)
    }

    private fun clearUnitsConstantTherapy(therapy: ConstantSimpleTherapyDto) {

        clearUnitsSimpleTherapy(therapy)

        therapy.doseElement.quantity = null
        therapy.doseElement.quantityDenominator = null
        therapy.doseElement.doseDescription = null
        therapy.doseElement.doseRange = null
    }

    private fun clearUnitsVariableTherapy(therapy: VariableSimpleTherapyDto) {

        clearUnitsSimpleTherapy(therapy)

        therapy.timedDoseElements = emptyList()

        therapy.dosingFrequency = null
    }

    private fun clearUnitsSimpleTherapy(therapy: SimpleTherapyDto) {

        therapy.quantityUnit = null
        therapy.quantityDenominatorUnit = null
        therapy.quantityDisplay = null
    }
}