package com.marand.thinkmed.medications.template

import com.marand.thinkmed.medications.api.internal.dto.TherapyDto
import com.marand.thinkmed.medications.dto.template.TherapyTemplateElementDto

/**
 * @author Nejc Korasa
 */

interface TemplateValidator {

    fun isFor(therapy: TherapyDto?): Boolean

    fun validate(template: TherapyTemplateElementDto): ValidationResult

    fun clearUnits(template: TherapyTemplateElementDto)
}