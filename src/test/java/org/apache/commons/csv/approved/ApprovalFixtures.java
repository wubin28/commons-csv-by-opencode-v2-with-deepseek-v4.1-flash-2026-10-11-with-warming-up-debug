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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

/**
 * Loads every {@code *.approved.md} fixture of a decision-table group from the test classpath, wrapping each one in a
 * {@link Named} argument so JUnit renders it as an individual, debuggable parameterized invocation.
 */
final class ApprovalFixtures {

    private static final String ROOT = "org/apache/commons/csv/approved";

    private ApprovalFixtures() {
        // No instances.
    }

    /**
     * Loads the fixtures of one group.
     *
     * @param group the sub-directory under {@code approved/}, e.g. {@code table-a}.
     * @return one {@link Arguments} per fixture, named for display.
     */
    static Stream<Arguments> load(final String group) {
        final Path dir = resolve(group);
        try (Stream<Path> files = Files.walk(dir)) {
            return files.filter(path -> path.getFileName().toString().endsWith(".approved.md")).sorted().map(path -> {
                final ApprovalFixture fixture = read(dir, path);
                return Arguments.of(Named.of(fixture.displayName(), fixture));
            }).collect(Collectors.toList()).stream();
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to load fixtures from " + dir, e);
        }
    }

    private static Path resolve(final String group) {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(ROOT + "/" + group);
        if (url == null) {
            throw new IllegalStateException("Fixture directory not found on the classpath: " + ROOT + "/" + group);
        }
        try {
            return Paths.get(url.toURI());
        } catch (final URISyntaxException e) {
            throw new IllegalStateException("Invalid fixture directory URL: " + url, e);
        }
    }

    private static ApprovalFixture read(final Path dir, final Path file) {
        try {
            final List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            return ApprovalFixtureParser.parse(dir.relativize(file).toString(), lines);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read fixture " + file, e);
        }
    }
}
