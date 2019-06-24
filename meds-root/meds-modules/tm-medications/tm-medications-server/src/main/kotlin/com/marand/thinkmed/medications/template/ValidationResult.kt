package com.marand.thinkmed.medications.template

/**
 * @author Nejc Korasa
 */

data class ValidationResult(
        var errors: List<ValidationError> = emptyList()) {

    constructor(error: ValidationError) : this() {

        errors = mutableListOf(error)
    }

    constructor(errorMessage: String, errorType: ValidationErrorType) : this() {

        errors = mutableListOf(ValidationError(errorMessage, errorType))
    }

    companion object {

        fun ok() = ValidationResult()

        fun join(results: List<ValidationResult>) = results.reduce { a, b -> ValidationResult(a.errors + b.errors) }
    }


    fun isValid() = errors.isEmpty()

    fun hasErrorType(errorType: ValidationErrorType) = errors.find { it.type == errorType } != null

    fun print() = errors.joinToString(", ") { it.print() }
}