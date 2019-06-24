package com.marand.thinkmed.medications.template

import com.marand.thinkmed.medications.api.internal.dto.MedicationOrderFormType
import com.marand.thinkmed.medications.api.internal.dto.ComplexTherapyDto
import com.marand.thinkmed.medications.api.internal.dto.ConstantComplexTherapyDto
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto
import com.marand.thinkmed.medications.api.internal.dto.VariableComplexTherapyDto
import com.marand.thinkmed.medications.api.internal.dto.InfusionIngredientDto
import com.marand.thinkmed.medications.dto.template.TherapyTemplateElementDto
import com.marand.thinkmed.medications.template.ValidationErrorType.NO_DATA
import org.springframework.stereotype.Component

/**
 * @author Nejc Korasa
 */

@Component
class ComplexTemplateValidator(private val validationUtils: TemplateValidationUtils) : TemplateValidator {

    override fun isFor(therapy: TherapyDto?): Boolean = MedicationOrderFormType.COMPLEX == therapy?.medicationOrderFormType

    override fun validate(template: TherapyTemplateElementDto): ValidationResult {

        val therapy = template.therapy as? ComplexTherapyDto
        therapy ?: return ValidationResult("Therapy is null", NO_DATA)

        val results = therapy.ingredientsList.map { validateInfusionIngredient(it) }
        return ValidationResult.join(results)
    }

    private fun validateInfusionIngredient(infIngredient: InfusionIngredientDto): ValidationResult {

        val actualMed = validationUtils.loadMedication(infIngredient.medication) ?: return ValidationResult.ok()

        val results = listOf(
                validationUtils.matchesNumeratorOrAdminUnit(infIngredient.quantityUnit, actualMed),
                validationUtils.matchesDenominator(infIngredient.quantityDenominatorUnit, actualMed)
        )
        return ValidationResult.join(results)
    }

    override fun clearUnits(template: TherapyTemplateElementDto) {

        val therapy = template.therapy
        therapy ?: return

        if (therapy is ConstantComplexTherapyDto) clearUnitsConstantTherapy(therapy)
        if (therapy is VariableComplexTherapyDto) clearUnitVariableTherapyUnits(therapy)
    }

    private fun clearUnitsConstantTherapy(therapy: ConstantComplexTherapyDto) {

        clearUnitsComplexTherapy(therapy)

        therapy.rateString = null
        therapy.doseElement = null
        therapy.durationDisplay = null
    }

    private fun clearUnitVariableTherapyUnits(therapy: VariableComplexTherapyDto) {

        clearUnitsComplexTherapy(therapy)

        therapy.timedDoseElements = emptyList()

        therapy.dosingFrequency = null
    }

    private fun clearUnitsComplexTherapy(therapy: ComplexTherapyDto) {

        therapy.ingredientsList.forEach {

            it.quantity = null
            it.quantityUnit = null
            it.quantityDenominator = null
            it.quantityDenominatorUnit = null
            it.quantityDisplay = null
        }

        therapy.baselineInfusionDisplay = null
        therapy.speedFormulaDisplay = null
        therapy.speedDisplay = null
        therapy.volumeSumUnit = null
        therapy.volumeSum = null
    }
}