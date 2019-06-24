package com.marand.thinkmed.medications.dose

import com.marand.thinkmed.medications.MedicationLevelEnum
import com.marand.thinkmed.medications.dto.MedicationDataDto
import com.marand.thinkmed.medications.dto.MedicationIngredientDto
import com.marand.thinkmed.medications.api.internal.dto.dose.PrescribingDoseDto
import com.marand.thinkmed.medications.units.converter.UnitsConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
open class DoseUtils @Autowired constructor(private val unitsConverter: UnitsConverter) {

    /**
     * Builds prescribing medication dose for {@param medication} using medication ingredients - main ingredients if they exist,
     * or all ingredients if no main ingredient is defined
     *
     * @return prescribing medication dose or null if administration unit is defined or DESCRIPTIVE order form type is set
     * @throws IllegalStateException if medication has no administration unit order form type is not DESCRIPTIVE and ingredients are empty
     */
    open fun buildPrescribingDose(medication: MedicationDataDto): PrescribingDoseDto? {

        if (medication.administrationUnit != null) return null
        if (medication.isDescriptiveDose) return null
        if (medication.medicationIngredients.isEmpty()) throw IllegalStateException("Either Administration unit or ingredients must be set, both cannot be empty!")

        if (medication.medicationLevel == MedicationLevelEnum.VTM && medication.medicationIngredients.size > 1) {
            return PrescribingDoseDto(
                    1.0,
                    medication.medicationIngredients[0].strengthNumeratorUnit,
                    null,
                    null)
        }

        val mainIngredients = medication.medicationIngredients.filter { it.isMain }
        return buildDoseFromIngredients(if (mainIngredients.isEmpty()) medication.medicationIngredients else mainIngredients)
    }

    /**
     * Builds medication dose for {@param ingredients}
     *
     * @return dose for ingredients or null if {@param ingredients} is empty
     */
    open fun buildDoseFromIngredients(ingredients: List<MedicationIngredientDto>): PrescribingDoseDto? {

        if (ingredients.isEmpty()) return null

        val first = ingredients[0]
        val mainNumeratorUnit = first.strengthNumeratorUnit

        return ingredients
                .map { buildDoseFromIngredient(mainNumeratorUnit, it) }
                .reduce { d1, d2 ->
                    if ((d1.denominator == null) xor (d2.denominator == null)) throw IllegalStateException("Either all, or non of ingredients denominators must exist!")

                    PrescribingDoseDto(
                            d1.numerator + d2.numerator,
                            d1.numeratorUnit,
                            d1.denominator,
                            d1.denominatorUnit)
                }
    }

    private fun buildDoseFromIngredient(toNumeratorUnit: String, ingredient: MedicationIngredientDto) = PrescribingDoseDto(
            unitsConverter.convert(ingredient.strengthNumerator!!, ingredient.strengthNumeratorUnit, toNumeratorUnit),
            toNumeratorUnit,
            ingredient.strengthDenominator,
            ingredient.strengthDenominatorUnit)
}
