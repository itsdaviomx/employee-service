package com.employee.employeeservice.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.chrono.IsoEra;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.regex.Pattern;

public class BirthDateDeserializer extends JsonDeserializer<LocalDate> {

    private static final DateTimeFormatter FORMAT = new DateTimeFormatterBuilder()
            .appendPattern(ApiDateFormat.PATTERN)
            .parseDefaulting(ChronoField.ERA, IsoEra.CE.getValue())
            .toFormatter(Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);
    private static final Pattern SHAPE = Pattern.compile(ApiDateFormat.REGEX);

    @Override
    public LocalDate deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.hasToken(JsonToken.VALUE_STRING)) {
            String text = parser.getText();
            if (SHAPE.matcher(text).matches()) {
                try {
                    return LocalDate.parse(text, FORMAT);
                } catch (DateTimeParseException ignored) {
                    // Correct shape but an impossible calendar date.
                }
            }
        }
        return context.reportInputMismatch(LocalDate.class, "birthDate must be a valid date in dd-MM-yyyy format");
    }
}
