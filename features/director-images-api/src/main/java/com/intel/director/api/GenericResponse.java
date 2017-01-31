package com.intel.director.api;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class GenericResponse {

    public String status;
    public String details;
    public String error;
    @JsonSerialize(using = ErrorCodeSerializer.class) 
    public ErrorCode errorCode;

    public String getDetails() {
	return details;
    }

    public void setDetails(String details) {
	this.details = details;
    }

    public String getStatus() {
	return status;
    }

    public void setStatus(String status) {
	this.status = status;
    }

    public String getError() {
	return error;
    }

    public void setError(String error) {
	this.error = error;
    }

    public ErrorCode getErrorCode() {
	return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
	this.errorCode = errorCode;
    }

}
