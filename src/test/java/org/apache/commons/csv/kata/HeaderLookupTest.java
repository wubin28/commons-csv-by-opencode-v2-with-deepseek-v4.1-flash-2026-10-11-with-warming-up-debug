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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

/**
 * Characterization tests for accessing record values by column name or index, and for the errors raised when that is
 * not possible. These tests describe the very behavior the {@code setRequiredHeaders} feature would like to move
 * earlier in the pipeline.
 */
class HeaderLookupTest {

    /**
     * Records are produced successfully even when a business column is absent; the error only appears at lookup time.
     *
     * <p>
     * This is the exact pain point that motivates a future {@code setRequiredHeaders(...)}: the parser happily returns
     * records, and the failure surfaces later, in business code, as an {@code IllegalArgumentException}.
     * </p>
     */
    @Test
    void missingColumn_failsOnlyAtLookupTime_notAtParseTime() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.builder().setHeader("id", "amount").setSkipHeaderRecord(true).get();

        try (CSVParser parser = CSVParser.parse("id,amount\n" + "1,100\n", format)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            final CSVRecord record = records.get(0);
            assertTrue(record.isMapped("amount"));
            assertFalse(record.isMapped("currency"));

            final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> record.get("currency"));
            assertEquals("Mapping for currency not found, expected one of [id, amount]", ex.getMessage());
        }
    }

    /**
     * A short record (fewer values than columns) is inconsistent; a lookup that points past the end says so.
     */
    @Test
    void shortRecord_isInconsistentAndOutOfRangeLookupFails() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.builder().setHeader("a", "b", "c").setSkipHeaderRecord(true).get();

        try (CSVParser parser = CSVParser.parse("a,b,c\n" + "1,2\n", format)) {
            final CSVRecord record = parser.iterator().next();
            assertEquals(2, record.size());
            assertFalse(record.isConsistent());
            assertTrue(record.isMapped("c"));
            assertFalse(record.isSet("c"));
            assertTrue(record.isSet("b"));

            final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> record.get("c"));
            assertEquals("Index for header 'c' is 2 but CSVRecord only has 2 values!", ex.getMessage());
        }
    }

    /**
     * {@code isMapped}, {@code isSet} and {@code toMap} all reflect the header mapping.
     */
    @Test
    void toMapAndIsMapped_reflectTheHeaderMapping() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().get();

        try (CSVParser parser = CSVParser.parse("name,age\n" + "Alice,30\n", format)) {
            final CSVRecord record = parser.iterator().next();
            assertTrue(record.isMapped("name"));
            assertFalse(record.isMapped("nope"));
            assertEquals("Alice", record.get("name"));

            final Map<String, String> map = record.toMap();
            assertEquals(2, map.size());
            assertEquals("Alice", map.get("name"));
            assertEquals("30", map.get("age"));
        }
    }

    /**
     * Without a header mapping, {@code toMap()} is empty and index access still works.
     */
    @Test
    void toMap_isEmptyWithoutHeader() throws IOException {
        try (CSVParser parser = CSVParser.parse("1,2\n", CSVFormat.DEFAULT)) {
            final CSVRecord record = parser.iterator().next();
            assertTrue(record.toMap().isEmpty());
            assertEquals("1", record.get(0));
            assertEquals("2", record.get(1));
        }
    }
}
