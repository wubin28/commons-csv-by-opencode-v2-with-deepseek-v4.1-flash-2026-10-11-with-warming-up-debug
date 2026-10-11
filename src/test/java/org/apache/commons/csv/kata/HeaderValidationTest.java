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

package org.apache.commons.csv.kata;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.DuplicateHeaderMode;
import org.junit.jupiter.api.Test;

/**
 * Characterization tests for how duplicate and blank column names are handled.
 *
 * <p>
 * The interesting part is <em>where</em> the failure happens: some invalid headers are rejected while the
 * {@link CSVFormat} is built, others only when a parser is created and the header is actually read.
 * </p>
 */
class HeaderValidationTest {

    /**
     * {@code CSVFormat.DEFAULT} allows duplicate header names, and the last occurrence wins for name lookups.
     */
    @Test
    void defaultBuilder_allowsDuplicateHeaderNames() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.builder().setHeader("a", "a").get();
        assertEquals(DuplicateHeaderMode.ALLOW_ALL, format.getDuplicateHeaderMode());

        try (CSVParser parser = CSVParser.parse("1,2\n", format)) {
            // Two identical names are still reported in column order...
            assertEquals(Arrays.asList("a", "a"), parser.getHeaderNames());
            // ...but the name-to-index map can only keep one entry.
            assertEquals(1, parser.getHeaderMap().size());
            assertEquals(1, parser.getHeaderMap().get("a").intValue());

            final CSVRecord record = parser.iterator().next();
            assertEquals("2", record.get("a"));
        }
    }

    /**
     * With an explicit header, a duplicate is already rejected when the format is built.
     */
    @Test
    void explicitDuplicateHeader_withDisallow_failsWhenFormatIsBuilt() {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> CSVFormat.DEFAULT.builder().setHeader("a", "a").setDuplicateHeaderMode(DuplicateHeaderMode.DISALLOW).get());
        assertEquals("The header contains a duplicate name: \"a\" in [a, a]. "
                + "If this is valid then use CSVFormat.Builder.setDuplicateHeaderMode().", ex.getMessage());
    }

    /**
     * With an auto-detected header the duplicate cannot be seen while building, so the failure is deferred to parsing.
     */
    @Test
    void autoDetectedDuplicateHeader_withDisallow_failsWhenParsing() {
        final CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setDuplicateHeaderMode(DuplicateHeaderMode.DISALLOW).get();

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> CSVParser.parse("a,a\n" + "1,2\n", format));
        assertTrue(ex.getMessage().startsWith("The header contains a duplicate name: \"a\" in [a, a]."), ex.getMessage());
    }

    /**
     * A blank column name is rejected by default, no matter whether the header is explicit or auto-detected.
     */
    @Test
    void blankHeader_isRejectedByDefault() {
        final CSVFormat format = CSVFormat.DEFAULT.builder().setHeader("a", "").get();
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> CSVParser.parse("1,2\n", format));
        assertEquals("A header name is missing in [a, ]", ex.getMessage());
    }

    /**
     * {@code setAllowMissingColumnNames(true)} accepts the blank name and keeps it in the mapping.
     */
    @Test
    void blankHeader_isAcceptedWhenMissingColumnNamesAllowed() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.builder().setHeader("a", "").setAllowMissingColumnNames(true).get();

        try (CSVParser parser = CSVParser.parse("1,2\n", format)) {
            assertEquals(Arrays.asList("a", ""), parser.getHeaderNames());
            assertEquals(2, parser.getHeaderMap().size());
            assertEquals(1, parser.getHeaderMap().get("").intValue());

            final CSVRecord record = parser.iterator().next();
            assertEquals("1", record.get("a"));
            assertEquals("2", record.get(""));
        }
    }

    /**
     * {@code ALLOW_EMPTY} permits several blank names; only the last index survives in the map.
     */
    @Test
    void allowEmptyMode_permitsDuplicateBlankHeaders() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.builder().setHeader("", "").setAllowMissingColumnNames(true)
                .setDuplicateHeaderMode(DuplicateHeaderMode.ALLOW_EMPTY).get();

        try (CSVParser parser = CSVParser.parse("1,2\n", format)) {
            assertEquals(Arrays.asList("", ""), parser.getHeaderNames());
            assertEquals(1, parser.getHeaderMap().size());
            assertEquals(1, parser.getHeaderMap().get("").intValue());
        }
    }

    /**
     * {@code DISALLOW} rejects duplicate blank names as well, again while the format is built.
     */
    @Test
    void disallowMode_rejectsDuplicateBlankHeadersWhenBuilt() {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> CSVFormat.DEFAULT.builder().setHeader("", "").setAllowMissingColumnNames(true)
                        .setDuplicateHeaderMode(DuplicateHeaderMode.DISALLOW).get());
        assertEquals("The header contains a duplicate name: \"\" in [, ]. "
                + "If this is valid then use CSVFormat.Builder.setDuplicateHeaderMode().", ex.getMessage());
    }

    /**
     * The getter is a defensive copy: mutating it does not affect the format.
     */
    @Test
    void getHeader_returnsADefensiveCopy() {
        final CSVFormat format = CSVFormat.DEFAULT.builder().setHeader("a", "b").get();
        assertArrayEquals(new String[] { "a", "b" }, format.getHeader());
        final String[] header = format.getHeader();
        header[0] = "changed";
        assertEquals("a", format.getHeader()[0]);
    }
}
