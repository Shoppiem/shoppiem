package com.shoppiem.api.utils.mapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.OffsetDateTime;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 */
public class OffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {
    @Override
    public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        return OffsetDateTime.parse(jsonParser.getValueAsString(), OffsetDateTimeSerializer.FORMATTER);
    }
}
