package com.intel.director.api;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ErrorCodeSerializer extends JsonSerializer<ErrorCode> {

    @Override
    public void serialize(ErrorCode value, JsonGenerator generator, SerializerProvider serializers)
	    throws IOException, JsonProcessingException {
	generator.writeStartObject();
	generator.writeFieldName("errorCode");
	generator.writeNumber(value.getErrorCode());
	generator.writeFieldName("errorDescription");
	generator.writeString(value.getErrorDescription());
	generator.writeEndObject();
    }
}
