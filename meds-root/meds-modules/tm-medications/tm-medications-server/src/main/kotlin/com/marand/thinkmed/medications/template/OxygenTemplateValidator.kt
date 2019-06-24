package com.marand.thinkmed.medications.template

import com.marand.thinkmed.medications.api.internal.dto.MedicationOrderFormType
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto
import com.marand.thinkmed.medications.dto.template.TherapyTemplateElementDto
import org.springframework.stereotype.Component

/**
 * @author Nejc Korasa
 */

@Component
class OxygenTemplateValidator : TemplateValidator {

    override fun isFor(therapy: TherapyDto?): Boolean = MedicationOrderFormType.OXYGEN == therapy?.medicationOrderFormType

    /**
     * Flow rate unit is fixed and always the same, therefore oxygen units are not validated
     */
    override fun validate(template: TherapyTemplateElementDto): ValidationResult = ValidationResult.ok()

    override fun clearUnits(template: TherapyTemplateElementDto) {}
}