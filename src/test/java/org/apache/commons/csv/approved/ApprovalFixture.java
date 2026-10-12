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

import java.util.List;
import java.util.Map;

/**
 * A parsed {@code *.approved.md} fixture: the single input the approval tests read and execute.
 */
final class ApprovalFixture {

    /** Path relative to the {@code approved/} resource root, e.g. {@code table-a/tc-a2-v1-....approved.md}. */
    final String resourcePath;

    final String title;

    final Map<String, String> meta;

    final Map<String, String> input;

    /** Raw CSV data; empty for an empty file. */
    final String data;

    /** {@code success}, {@code failure} or {@code config}. */
    final String outcome;

    /** {@code build} or {@code parse}; only meaningful for a failure. */
    final String phase;

    final String throwsClass;

    final String message;

    /** Success/config assertion keys in declaration order. */
    final Map<String, String> assertions;

    final List<String> notes;

    ApprovalFixture(final String resourcePath, final String title, final Map<String, String> meta, final Map<String, String> input,
            final String data, final String outcome, final String phase, final String throwsClass, final String message,
            final Map<String, String> assertions, final List<String> notes) {
        this.resourcePath = resourcePath;
        this.title = title;
        this.meta = meta;
        this.input = input;
        this.data = data;
        this.outcome = outcome;
        this.phase = phase;
        this.throwsClass = throwsClass;
        this.message = message;
        this.assertions = assertions;
        this.notes = notes;
    }

    /**
     * Builds the display name shown for this invocation in the VSCode Testing panel.
     *
     * @return the display name.
     */
    String displayName() {
        return "[" + meta.getOrDefault("table", "?") + "/" + meta.getOrDefault("rule", "?") + "] " + meta.getOrDefault("case", "?") + " "
                + meta.getOrDefault("variant", "?") + " - " + title;
    }
}
