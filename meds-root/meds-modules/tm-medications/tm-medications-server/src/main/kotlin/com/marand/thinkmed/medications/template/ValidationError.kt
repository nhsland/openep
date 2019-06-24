package com.marand.thinkmed.medications.template

/**
 * @author Nejc Korasa
 */

data class ValidationError(
        val message: String? = null,
        val type: ValidationErrorType) {

    fun print() = "type = $type message = $message"
}