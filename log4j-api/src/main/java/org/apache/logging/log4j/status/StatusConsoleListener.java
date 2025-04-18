/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.logging.log4j.status;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Objects;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedNoReferenceMessageFactory;

/**
 * {@link StatusListener} that writes to the console.
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class StatusConsoleListener implements StatusListener {

    private Level level;

    private String[] filters;

    private final PrintStream stream;

    private final Logger logger;

    /**
     * Constructs a {@link StatusConsoleListener} instance writing to {@link System#out} using the supplied level.
     *
     * @param level the level of status messages that should appear on the console
     * @throws NullPointerException on null {@code level}
     */
    public StatusConsoleListener(final Level level) {
        this(level, System.out);
    }

    /**
     * Constructs a {@link StatusConsoleListener} instance using the supplied level and stream.
     * <p>
     * Make sure not to use a logger stream of some sort to avoid creating an infinite loop of indirection!
     * </p>
     *
     * @param level the level of status messages that should appear on the console
     * @param stream the stream to write to
     * @throws NullPointerException on null {@code level} or {@code stream}
     */
    public StatusConsoleListener(final Level level, final PrintStream stream) {
        this(level, stream, SimpleLoggerFactory.getInstance());
    }

    StatusConsoleListener(
            final Level level,
            final PrintStream stream,
            final SimpleLoggerFactory loggerFactory) {
        this.level = Objects.requireNonNull(level, "level");
        this.stream = Objects.requireNonNull(stream, "stream");
        this.logger = Objects
                .requireNonNull(loggerFactory, "loggerFactory")
                .createSimpleLogger(
                        "StatusConsoleListener",
                        level,
                        ParameterizedNoReferenceMessageFactory.INSTANCE,
                        stream);
    }

    /**
     * Sets the level to a new value.
     * @param level The new Level.
     */
    public void setLevel(final Level level) {
        this.level = level;
    }

    /**
     * Return the Log Level for which the Listener should receive events.
     * @return the Log Level.
     */
    @Override
    public Level getStatusLevel() {
        return this.level;
    }

    /**
     * Writes status messages to the console.
     * @param data The StatusData.
     */
    @Override
    public void log(final StatusData data) {
        final boolean filtered = filtered(data);
        if (!filtered) {
            logger
                    // Logging using _only_ the following 4 fields set by `StatusLogger#logMessage()`:
                    .atLevel(data.getLevel())
                    .withThrowable(data.getThrowable())
                    .withLocation(data.getStackTraceElement())
                    .log(data.getMessage());
        }
    }

    /**
     * Adds package name filters to exclude.
     * @param filters An array of package names to exclude.
     */
    public void setFilters(final String... filters) {
        this.filters = filters;
    }

    private boolean filtered(final StatusData data) {
        if (filters == null) {
            return false;
        }
        final String caller = data.getStackTraceElement().getClassName();
        for (final String filter : filters) {
            if (caller.startsWith(filter)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        // only want to close non-system streams
        if (this.stream != System.out && this.stream != System.err) {
            this.stream.close();
        }
    }

}
