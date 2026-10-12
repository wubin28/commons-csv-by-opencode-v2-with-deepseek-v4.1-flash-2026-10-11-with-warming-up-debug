/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.commons.csv.approved;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.DuplicateHeaderMode;

/**
 * Turns a parsed {@link ApprovalFixture} into real API calls and assertions. Knows nothing about markdown.
 */
final class ApprovalScenarioExecutor {

    static final String NONE = "(none)";

    static final String EMPTY = "(empty)";

    static final String NULL = "(null)";

    static final String DEFAULT = "(default)";

    static final String BLANK = "(blank)";

    private static final Pattern RECORD_ASSERTION = Pattern.compile("record\\[(\\d+)]\\.(number|isMapped\\((.+)\\)|get\\((.+)\\))");

    private ApprovalScenarioExecutor() {
        // No instances.
    }

    /**
     * Runs one fixture: either asserts it, or (with {@code -Dapproved.regen=true}) rewrites its expected block.
     *
     * @param fixture the fixture to run.
     */
    static void execute(final ApprovalFixture fixture) {
        final Result result = run(fixture);
        if (Boolean.parseBoolean(System.getProperty("approved.regen", "false"))) {
            ApprovalFixtureRewriter.rewrite(fixture, result);
            return;
        }
        assertResult(fixture, result);
    }

    private static Result run(final ApprovalFixture fixture) {
        final Result result = new Result();
        final CSVFormat format;
        try {
            format = configure(fixture).get();
        } catch (final IllegalArgumentException e) {
            result.outcome = "failure";
            result.phase = "build";
            result.throwsClass = e.getClass().getName();
            result.message = e.getMessage();
            return result;
        }
        result.format = format;
        if ("config".equals(fixture.outcome)) {
            result.outcome = "config";
            result.values = evaluateConfig(fixture, format);
            return result;
        }
        try (CSVParser parser = CSVParser.parse(fixture.data, format)) {
            result.outcome = "success";
            result.values = evaluateSuccess(fixture, format, parser);
            return result;
        } catch (final IllegalArgumentException e) {
            result.outcome = "failure";
            result.phase = "parse";
            result.throwsClass = e.getClass().getName();
            result.message = e.getMessage();
            return result;
        } catch (final IOException e) {
            throw new UncheckedIOException("I/O error while parsing " + fixture.resourcePath, e);
        }
    }

    private static CSVFormat.Builder configure(final ApprovalFixture fixture) {
        final CSVFormat.Builder builder = CSVFormat.DEFAULT.builder();
        final String requiredHeaders = fixture.input.get("requiredHeaders");
        if (requiredHeaders != null && !NONE.equals(requiredHeaders)) {
            if (NULL.equals(requiredHeaders)) {
                builder.setRequiredHeaders((String[]) null);
            } else if (EMPTY.equals(requiredHeaders)) {
                builder.setRequiredHeaders();
            } else {
                builder.setRequiredHeaders(parseList(requiredHeaders));
            }
        }
        final String header = fixture.input.get("header");
        if (header != null && !NONE.equals(header)) {
            if (NULL.equals(header)) {
                builder.setHeader((String[]) null);
            } else if (EMPTY.equals(header)) {
                builder.setHeader();
            } else {
                builder.setHeader(parseList(header));
            }
        }
        applyBoolean(fixture.input.get("ignoreHeaderCase"), "ignoreHeaderCase", builder);
        applyBoolean(fixture.input.get("allowMissingColumnNames"), "allowMissingColumnNames", builder);
        applyBoolean(fixture.input.get("skipHeaderRecord"), "skipHeaderRecord", builder);
        applyBoolean(fixture.input.get("trim"), "trim", builder);
        final String duplicateHeaderMode = fixture.input.get("duplicateHeaderMode");
        if (duplicateHeaderMode != null && !DEFAULT.equals(duplicateHeaderMode)) {
            builder.setDuplicateHeaderMode(DuplicateHeaderMode.valueOf(duplicateHeaderMode.trim().toUpperCase(Locale.ROOT)));
        }
        applyCharacter(fixture.input.get("commentMarker"), "commentMarker", builder);
        applyCharacter(fixture.input.get("delimiter"), "delimiter", builder);
        applyCharacter(fixture.input.get("quote"), "quote", builder);
        final String nullString = fixture.input.get("nullString");
        if (nullString != null && !DEFAULT.equals(nullString) && !NONE.equals(nullString)) {
            builder.setNullString(nullString);
        }
        return builder;
    }

    private static void applyBoolean(final String value, final String name, final CSVFormat.Builder builder) {
        if (value == null || DEFAULT.equals(value)) {
            return;
        }
        final boolean flag = Boolean.parseBoolean(value);
        switch (name) {
        case "ignoreHeaderCase":
            builder.setIgnoreHeaderCase(flag);
            break;
        case "allowMissingColumnNames":
            builder.setAllowMissingColumnNames(flag);
            break;
        case "skipHeaderRecord":
            builder.setSkipHeaderRecord(flag);
            break;
        case "trim":
            builder.setTrim(flag);
            break;
        default:
            throw new IllegalArgumentException("Unknown boolean setting: " + name);
        }
    }

    private static void applyCharacter(final String value, final String name, final CSVFormat.Builder builder) {
        if (value == null || DEFAULT.equals(value) || NONE.equals(value)) {
            return;
        }
        final char c = charOf(value, name);
        switch (name) {
        case "commentMarker":
            builder.setCommentMarker(c);
            break;
        case "delimiter":
            builder.setDelimiter(c);
            break;
        case "quote":
            builder.setQuote(c);
            break;
        default:
            throw new IllegalArgumentException("Unknown character setting: " + name);
        }
    }

    private static char charOf(final String value, final String name) {
        if ("\\t".equals(value)) {
            return '\t';
        }
        if (value.length() == 1) {
            return value.charAt(0);
        }
        if (value.length() == 3 && value.charAt(0) == '\'' && value.charAt(2) == '\'') {
            return value.charAt(1);
        }
        throw new IllegalArgumentException("Cannot parse a single character for '" + name + "': " + value);
    }

    private static String[] parseList(final String value) {
        final String[] parts = value.split(",", -1);
        final String[] result = new String[parts.length];
        for (int i = 0; i < parts.length; i++) {
            final String part = parts[i].trim();
            if (EMPTY.equals(part)) {
                result[i] = "";
            } else if (NULL.equals(part)) {
                result[i] = null;
            } else if (BLANK.equals(part)) {
                result[i] = " ";
            } else {
                result[i] = part;
            }
        }
        return result;
    }

    private static Map<String, String> evaluateSuccess(final ApprovalFixture fixture, final CSVFormat format, final CSVParser parser) {
        final List<CSVRecord> records = parser.getRecords();
        final Map<String, String> values = new LinkedHashMap<>();
        for (final String key : fixture.assertions.keySet()) {
            values.put(key, actualValue(fixture, key, format, parser, records));
        }
        return values;
    }

    private static Map<String, String> evaluateConfig(final ApprovalFixture fixture, final CSVFormat format) {
        final Map<String, String> values = new LinkedHashMap<>();
        for (final String key : fixture.assertions.keySet()) {
            switch (key) {
            case "formatRequiredHeaders":
                values.put(key, join(Arrays.asList(format.getRequiredHeaders())));
                break;
            case "defensiveCopyHolds": {
                final String[] first = format.getRequiredHeaders();
                final String before = first.length > 0 ? first[0] : null;
                if (first.length > 0) {
                    first[0] = "MUTATED";
                }
                final String[] second = format.getRequiredHeaders();
                final String after = second.length > 0 ? second[0] : null;
                values.put(key, String.valueOf(Objects.equals(before, after)));
                break;
            }
            case "copyPreservesRequiredHeaders": {
                final boolean copy = Arrays.equals(format.getRequiredHeaders(), format.builder().get().getRequiredHeaders());
                values.put(key, String.valueOf(copy));
                break;
            }
            case "equalsDiffersOnRequiredHeaders": {
                final CSVFormat a = CSVFormat.DEFAULT.builder().setRequiredHeaders("a").get();
                final CSVFormat b = CSVFormat.DEFAULT.builder().setRequiredHeaders("b").get();
                values.put(key, String.valueOf(!a.equals(b) && a.hashCode() != b.hashCode()));
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown config assertion '" + key + "' in " + fixture.resourcePath);
            }
        }
        return values;
    }

    private static String actualValue(final ApprovalFixture fixture, final String key, final CSVFormat format, final CSVParser parser,
            final List<CSVRecord> records) {
        if ("formatRequiredHeaders".equals(key)) {
            return join(Arrays.asList(format.getRequiredHeaders()));
        }
        if ("headerNames".equals(key)) {
            return join(parser.getHeaderNames());
        }
        if ("headerMapSize".equals(key)) {
            final Map<String, Integer> map = parser.getHeaderMap();
            return String.valueOf(map == null ? 0 : map.size());
        }
        if ("recordCount".equals(key)) {
            return String.valueOf(records.size());
        }
        if (key.startsWith("headerIndex(") && key.endsWith(")")) {
            final String name = key.substring("headerIndex(".length(), key.length() - 1);
            final Map<String, Integer> map = parser.getHeaderMap();
            final Integer index = map == null ? null : map.get(name);
            return String.valueOf(index);
        }
        final Matcher matcher = RECORD_ASSERTION.matcher(key);
        if (matcher.matches()) {
            final CSVRecord record = records.get(Integer.parseInt(matcher.group(1)));
            final String operation = matcher.group(2);
            if ("number".equals(operation)) {
                return String.valueOf(record.getRecordNumber());
            }
            if (operation.startsWith("isMapped(")) {
                return String.valueOf(record.isMapped(matcher.group(3)));
            }
            if (operation.startsWith("get(")) {
                return display(record.get(matcher.group(4)));
            }
        }
        throw new IllegalArgumentException("Unknown assertion key '" + key + "' in " + fixture.resourcePath);
    }

    private static String join(final Collection<String> values) {
        final StringBuilder builder = new StringBuilder();
        for (final String value : values) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(display(value));
        }
        return builder.length() == 0 ? EMPTY : builder.toString();
    }

    private static String display(final String value) {
        if (value == null) {
            return NULL;
        }
        if (value.isEmpty()) {
            return EMPTY;
        }
        return value;
    }

    private static void assertResult(final ApprovalFixture fixture, final Result result) {
        assertEquals(fixture.outcome, result.outcome, () -> "outcome mismatch in " + fixture.resourcePath);
        if ("failure".equals(fixture.outcome)) {
            assertEquals(fixture.phase, result.phase, () -> "phase mismatch in " + fixture.resourcePath);
            assertEquals(fixture.throwsClass, result.throwsClass, () -> "exception type mismatch in " + fixture.resourcePath);
            assertEquals(fixture.message, result.message, () -> "message mismatch in " + fixture.resourcePath);
            return;
        }
        for (final Map.Entry<String, String> entry : fixture.assertions.entrySet()) {
            assertEquals(entry.getValue(), result.values.get(entry.getKey()),
                    () -> "assertion '" + entry.getKey() + "' mismatch in " + fixture.resourcePath);
        }
    }

    /** The actual outcome of a run, used both for assertion and for {@code regen}. */
    static final class Result {

        String outcome;

        String phase;

        String throwsClass;

        String message;

        CSVFormat format;

        Map<String, String> values = new LinkedHashMap<>();
    }
}
