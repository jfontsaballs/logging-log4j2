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
package org.apache.logging.log4j.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.io.internal.InternalBufferedReader;
import org.apache.logging.log4j.spi.ExtendedLogger;

/**
 *
 * @since 2.1
 */
public class LoggerBufferedReader extends BufferedReader {
    private static final String FQCN = LoggerBufferedReader.class.getName();
    private final InternalBufferedReader reader;

    protected LoggerBufferedReader(final Reader reader, final ExtendedLogger logger, final String fqcn,
                                   final Level level, final Marker marker) {
        super(reader);
        this.reader = new InternalBufferedReader(reader, logger, fqcn == null ? FQCN : fqcn, level, marker);
    }

    protected LoggerBufferedReader(final Reader reader, final int size, final ExtendedLogger logger, final String fqcn,
                                   final Level level, final Marker marker) {
        super(reader);
        this.reader = new InternalBufferedReader(reader, size, logger, fqcn == null ? FQCN : fqcn, level, marker);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    @Override
    public int read() throws IOException {
        return reader.read();
    }

    @Override
    public int read(final char[] cbuf) throws IOException {
        return reader.read(cbuf);
    }

    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        return reader.read(cbuf, off, len);
    }

    @Override
    public int read(final CharBuffer target) throws IOException {
        return reader.read(target);
    }

    @Override
    public String readLine() throws IOException {
        return reader.readLine();
    }
}
