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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

/**
 * Characterization tests for how a {@link CSVFormat} decides where the header comes from.
 *
 * <p>
 * These tests pin down the <em>current</em> behavior of Apache Commons CSV. They are part of a warm-up exercise and are
 * deliberately written against the public API only (the {@code kata} sub-package is not an internal package).
 * </p>
 */
class HeaderResolutionTest {

    /**
     * With a format that has no header at all, values can only be accessed by index.
     */
    @Test
    void noHeader_indexAccessWorksButNameAccessThrows() throws IOException {
        try (CSVParser parser = CSVParser.parse("name,age\n" + "Alice,30\n", CSVFormat.DEFAULT)) {
            assertNull(parser.getHeaderMap());
            assertEquals(0, parser.getHeaderNames().size());

            final CSVRecord first = parser.iterator().next();
            assertEquals("name", first.get(0));
            assertEquals("age", first.get(1));

            final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> first.get("name"));
            assertEquals("No header mapping was specified, the record values can't be accessed by name", ex.getMessage());

            assertFalse(first.isMapped("name"));
            assertTrue(first.isConsistent());
        }
    }

    /**
     * {@code setHeader()} with no arguments means "read the header from the first record of the input".
     */
    @Test
    void emptyHeader_autoDetectsFirstRecordAsHeader() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().get();

        try (CSVParser parser = CSVParser.parse("name,age\n" + "Alice,30\n" + "Bob,25\n", format)) {
            assertEquals(Arrays.asList("name", "age"), parser.getHeaderNames());
            assertEquals(0, parser.getHeaderMap().get("name").intValue());
            assertEquals(1, parser.getHeaderMap().get("age").intValue());

            final List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("Alice", records.get(0).get("name"));
            // NOTE: even though the header row is consumed while the parser is constructed, the first *data*
            // record is numbered 1, not 2. Verified behavior: createHeaders() runs at CSVParser.java:528, but
            // this.recordNumber is only assigned afterwards at :531, discarding the header's increment.
            assertEquals(1, records.get(0).getRecordNumber());
            assertEquals(2, records.get(1).getRecordNumber());
            assertEquals("25", records.get(1).get("age"));
        }
    }

    /**
     * Handing an explicit header to the builder does NOT consume the first line unless the caller also skips it.
     */
    @Test
    void explicitHeaderWithoutSkip_reReadsFirstLineAsData() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.builder().setHeader("name", "age").get();

        try (CSVParser parser = CSVParser.parse("name,age\n" + "Alice,30\n", format)) {
            assertEquals(Arrays.asList("name", "age"), parser.getHeaderNames());

            final List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            // The header line itself is re-read as the first data record.
            assertEquals("name", records.get(0).get("name"));
            assertEquals(1, records.get(0).getRecordNumber());
            assertEquals("Alice", records.get(1).get("name"));
        }
    }

    /**
     * The documented recipe for "my file has a header line, use it": explicit header + {@code setSkipHeaderRecord(true)}.
     */
    @Test
    void explicitHeaderWithSkip_consumesFirstLineAndStartsAtData() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.builder().setHeader("name", "age").setSkipHeaderRecord(true).get();

        try (CSVParser parser = CSVParser.parse("name,age\n" + "Alice,30\n", format)) {
            assertEquals(Arrays.asList("name", "age"), parser.getHeaderNames());

            final List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("Alice", records.get(0).get("name"));
            // Same reset quirk as above: the skipped header row does not advance the data record numbering.
            assertEquals(1, records.get(0).getRecordNumber());
        }
    }

    /**
     * When the header is auto-detected, {@code skipHeaderRecord} must NOT skip a second line.
     */
    @Test
    void skipHeaderRecord_hasNoExtraEffectWhenHeaderIsAutoDetected() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get();
        assertTrue(format.getSkipHeaderRecord());

        try (CSVParser parser = CSVParser.parse("name,age\n" + "Alice,30\n", format)) {
            assertEquals(Arrays.asList("name", "age"), parser.getHeaderNames());
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("Alice", records.get(0).get("name"));
        }
    }

    /**
     * {@code setIgnoreHeaderCase(true)} makes name lookups case-insensitive while keeping the original column names.
     */
    @Test
    void ignoreHeaderCase_makesNameLookupCaseInsensitive() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.builder().setHeader("Name", "Age").setIgnoreHeaderCase(true).get();
        assertTrue(format.getIgnoreHeaderCase());

        try (CSVParser parser = CSVParser.parse("Alice,30\n", format)) {
            assertEquals(Arrays.asList("Name", "Age"), parser.getHeaderNames());
            final CSVRecord record = parser.iterator().next();
            assertEquals("Alice", record.get("NAME"));
            assertEquals("30", record.get("age"));
            assertTrue(record.isMapped("nAmE"));
        }
    }
}
