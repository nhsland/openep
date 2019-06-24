package com.marand.thinkmed.medications.template

import com.marand.thinkmed.medications.api.internal.dto.TherapyDto
import com.marand.thinkmed.medications.dto.ValidationIssueEnum
import com.marand.thinkmed.medications.dto.template.TherapyTemplateElementDto
import com.marand.thinkmed.medications.dto.template.TherapyTemplatesDto
import com.marand.thinkmed.medications.template.ValidationErrorType.NOT_VALIDATED
import com.marand.thinkmed.medications.template.ValidationErrorType.UNEXPECTED
import org.springframework.stereotype.Component

/**
 * @author Nejc Korasa
 */

@Component
open class TemplateValidationManager(private val validators: List<TemplateValidator>) {

    open fun validate(template: TherapyTemplateElementDto, print: Boolean = false): ValidationResult {

        return try {

            findValidator(template.therapy)
                    ?.let {
                        val result: ValidationResult = it.validate(template)
                        if (print && !result.isValid()) printValidationResult(template, result)
                        return result
                    }
                    ?: ValidationResult(error = ValidationError(type = NOT_VALIDATED))

        } catch (e: Exception) {

            val result = ValidationResult(error = ValidationError(e.message, UNEXPECTED))
            if (print) printValidationResult(template, result)
            result
        }
    }

    open fun validateAll(templatesDto: TherapyTemplatesDto) {

        println("--------------- STARTING validation ---------------")

        templatesDto.apply {

            println("\n ---------- User Templates ---------- \n")
            userTemplates
                    .flatMap { it.templateElements }
                    .forEach { validate(it, true) }

            println("\n ---------- Org Templates ---------- \n")
            organizationTemplates
                    .flatMap { it.templateElements }
                    .forEach { validate(it, true) }

            println("\n ---------- Patient Templates ---------- \n")
            patientTemplates
                    .flatMap { it.templateElements }
                    .forEach { validate(it, true) }

            println("\n ---------- Custom Templates ---------- \n")
            customTemplateGroups
                    .flatMap { it.customTemplates.flatMap { it.templateElements } }
                    .forEach { validate(it, true) }

        }

        println("\n --------------- ENDING validation ---------------")
    }

    open fun clearUnitsIfNotValid(template: TherapyTemplateElementDto) {

        val result = validate(template)
        if (!result.isValid()) {

            clearUnits(template)
            template.validationIssues.add(ValidationIssueEnum.UNITS_CHANGED)
        }
    }

    private fun clearUnits(template: TherapyTemplateElementDto) = findValidator(template.therapy)?.clearUnits(template)

    private fun findValidator(therapy: TherapyDto): TemplateValidator? = validators.find { it.isFor(therapy) }

    private fun printValidationResult(tmp: TherapyTemplateElementDto, result: ValidationResult) = println("${tmp.id} - ${result.print()}")
}