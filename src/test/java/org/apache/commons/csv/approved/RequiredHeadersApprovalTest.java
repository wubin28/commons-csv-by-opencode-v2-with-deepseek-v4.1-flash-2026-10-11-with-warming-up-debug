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

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Fixture-driven approval tests for {@code CSVFormat.Builder.setRequiredHeaders(String...)}.
 *
 * <p>
 * Every {@code *.approved.md} fixture under {@code src/test/resources/org/apache/commons/csv/approved} is a single,
 * independently debuggable parameterized invocation. The fixtures are grouped by decision table so the VSCode Testing
 * panel can be folded by table.
 * </p>
 */
class RequiredHeadersApprovalTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("tableC")
    void tableC(final ApprovalFixture fixture) {
        ApprovalScenarioExecutor.execute(fixture);
    }

    static Stream<Arguments> tableC() {
        return ApprovalFixtures.load("table-c");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("tableA")
    void tableA(final ApprovalFixture fixture) {
        ApprovalScenarioExecutor.execute(fixture);
    }

    static Stream<Arguments> tableA() {
        return ApprovalFixtures.load("table-a");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("tableB")
    void tableB(final ApprovalFixture fixture) {
        ApprovalScenarioExecutor.execute(fixture);
    }

    static Stream<Arguments> tableB() {
        return ApprovalFixtures.load("table-b");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("interaction")
    void interaction(final ApprovalFixture fixture) {
        ApprovalScenarioExecutor.execute(fixture);
    }

    static Stream<Arguments> interaction() {
        return ApprovalFixtures.load("interaction");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("config")
    void config(final ApprovalFixture fixture) {
        ApprovalScenarioExecutor.execute(fixture);
    }

    static Stream<Arguments> config() {
        return ApprovalFixtures.load("config");
    }
}
