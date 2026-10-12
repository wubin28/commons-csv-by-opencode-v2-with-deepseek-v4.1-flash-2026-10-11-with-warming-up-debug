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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.approved.ApprovalScenarioExecutor.Result;

/**
 * Rewrites only the {@code ## Expected} block of a fixture from an actual run. Enabled with {@code -Dapproved.regen=true};
 * the other blocks ({@code ## Meta} / {@code ## Input} / {@code ## Data}) are never touched. Review the result with {@code git diff}.
 */
final class ApprovalFixtureRewriter {

    private static final String SOURCE_ROOT = "src/test/resources/org/apache/commons/csv/approved";

    private ApprovalFixtureRewriter() {
        // No instances.
    }

    /**
     * Rewrites the expected block of the fixture on disk.
     *
     * @param fixture the fixture that was run.
     * @param result  the actual result.
     */
    static void rewrite(final ApprovalFixture fixture, final Result result) {
        final Path source = locateSource(fixture.resourcePath);
        if (source == null) {
            System.out.println("[approved.regen] cannot locate the source fixture for " + fixture.resourcePath + "; skipped");
            return;
        }
        try {
            final List<String> lines = Files.readAllLines(source, StandardCharsets.UTF_8);
            final List<String> rewritten = new ArrayList<>(lines.size());
            boolean inExpected = false;
            for (final String line : lines) {
                final String trimmed = line.trim();
                if (trimmed.startsWith("## ")) {
                    if (inExpected) {
                        rewritten.addAll(expectedLines(fixture, result));
                        inExpected = false;
                    }
                    rewritten.add(line);
                    if ("## expected".equalsIgnoreCase(trimmed)) {
                        inExpected = true;
                    }
                    continue;
                }
                if (!inExpected) {
                    rewritten.add(line);
                }
            }
            if (inExpected) {
                rewritten.addAll(expectedLines(fixture, result));
            }
            Files.write(source, rewritten, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to rewrite " + source, e);
        }
    }

    private static List<String> expectedLines(final ApprovalFixture fixture, final Result result) {
        final List<String> lines = new ArrayList<>();
        lines.add("outcome: " + result.outcome);
        if ("failure".equals(result.outcome)) {
            lines.add("phase: " + result.phase);
            lines.add("throws: " + result.throwsClass);
            lines.add("message: " + result.message);
        } else {
            for (final Map.Entry<String, String> entry : fixture.assertions.entrySet()) {
                lines.add(entry.getKey() + ": " + result.values.get(entry.getKey()));
            }
        }
        return lines;
    }

    private static Path locateSource(final String relativePath) {
        for (final String prefix : new String[] { "", "commons-csv/" }) {
            final Path candidate = Paths.get(prefix + SOURCE_ROOT + "/" + relativePath);
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        return null;
    }
}
