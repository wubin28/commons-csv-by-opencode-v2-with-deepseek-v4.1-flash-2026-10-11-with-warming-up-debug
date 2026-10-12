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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Reads the markdown structure of a fixture into an {@link ApprovalFixture}. This class knows nothing about CSV parsing or assertions.
 */
final class ApprovalFixtureParser {

    private static final String MISSING_PHASE_DEFAULT = "parse";

    private ApprovalFixtureParser() {
        // No instances.
    }

    /**
     * Parses a fixture.
     *
     * @param resourcePath path relative to the {@code approved/} resource root.
     * @param lines        the fixture lines.
     * @return the parsed fixture.
     * @throws IllegalArgumentException when a required section or key is missing or malformed.
     */
    static ApprovalFixture parse(final String resourcePath, final List<String> lines) {
        String title = "";
        final Map<String, String> meta = new LinkedHashMap<>();
        final Map<String, String> input = new LinkedHashMap<>();
        final StringBuilder data = new StringBuilder();
        final Map<String, String> expected = new LinkedHashMap<>();
        final List<String> notes = new ArrayList<>();
        String section = null;
        boolean inFence = false;
        for (final String raw : lines) {
            final String line = raw;
            final String trimmed = line.trim();
            if (trimmed.startsWith("# ")) {
                title = trimmed.substring(2).trim();
                continue;
            }
            if (trimmed.startsWith("## ")) {
                section = trimmed.substring(3).trim().toLowerCase(Locale.ROOT);
                inFence = false;
                continue;
            }
            if (trimmed.startsWith("Note:")) {
                notes.add(trimmed.substring("Note:".length()).trim());
                continue;
            }
            if (section == null) {
                continue;
            }
            switch (section) {
            case "meta":
                putKeyValue(meta, line, resourcePath);
                break;
            case "input":
                putKeyValue(input, line, resourcePath);
                break;
            case "data":
                if (trimmed.startsWith("```")) {
                    inFence = !inFence;
                } else if (inFence) {
                    data.append(line).append('\n');
                }
                break;
            case "expected":
                putKeyValue(expected, line, resourcePath);
                break;
            case "notes":
                if (!trimmed.isEmpty()) {
                    notes.add(trimmed);
                }
                break;
            default:
                break;
            }
        }
        final String outcome = required(expected, "outcome", resourcePath);
        final String phase = "failure".equals(outcome) ? expected.getOrDefault("phase", MISSING_PHASE_DEFAULT) : null;
        final String throwsClass = expected.get("throws");
        final String message = expected.get("message");
        final Map<String, String> assertions = new LinkedHashMap<>(expected);
        assertions.remove("outcome");
        assertions.remove("phase");
        assertions.remove("throws");
        assertions.remove("message");
        if (!"success".equals(outcome) && !"failure".equals(outcome) && !"config".equals(outcome)) {
            throw new IllegalArgumentException("Invalid outcome '" + outcome + "' in " + resourcePath);
        }
        if ("failure".equals(outcome) && !"build".equals(phase) && !"parse".equals(phase)) {
            throw new IllegalArgumentException("Invalid phase '" + phase + "' in " + resourcePath);
        }
        if (title.isEmpty()) {
            throw new IllegalArgumentException("Missing H1 title in " + resourcePath);
        }
        return new ApprovalFixture(resourcePath, title, meta, input, data.toString(), outcome, phase, throwsClass, message, assertions, notes);
    }

    private static void putKeyValue(final Map<String, String> target, final String line, final String resourcePath) {
        final String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        final int colon = trimmed.indexOf(':');
        if (colon < 0) {
            throw new IllegalArgumentException("Malformed line (expected 'key: value') in " + resourcePath + ": " + trimmed);
        }
        final String key = trimmed.substring(0, colon).trim();
        final String value = trimmed.substring(colon + 1).trim();
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Empty key in " + resourcePath + ": " + trimmed);
        }
        target.put(key, value);
    }

    private static String required(final Map<String, String> map, final String key, final String resourcePath) {
        final String value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required key '" + key + "' in " + resourcePath);
        }
        return value;
    }
}
