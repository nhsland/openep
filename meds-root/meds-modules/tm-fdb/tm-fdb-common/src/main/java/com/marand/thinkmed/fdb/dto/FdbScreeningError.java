package com.marand.thinkmed.fdb.dto;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Vid Kumse
 */
public class FdbScreeningError implements JsonSerializable
{
    private FdbNameValue ErrorType;
    private String Description;


    public FdbNameValue getErrorType() {
        return ErrorType;
    }

    public void setErrorType(final FdbNameValue errorType) {
        ErrorType = errorType;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(final String description) {
        Description = description;
    }
}
