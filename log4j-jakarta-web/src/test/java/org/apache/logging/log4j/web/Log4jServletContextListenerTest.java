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
package org.apache.logging.log4j.web;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
public class Log4jServletContextListenerTest {
    /* event and servletContext are marked lenient because they aren't used in the
     * testDestroyWithNoInit but are only accessed during initialization
     */
    @Mock(lenient = true)
    private ServletContextEvent event;
    @Mock(lenient = true)
    private ServletContext servletContext;
    @Mock
    private Log4jWebLifeCycle initializer;

    private Log4jServletContextListener listener;

    @BeforeEach
    public void setUp() {
        this.listener = new Log4jServletContextListener();
        given(event.getServletContext()).willReturn(servletContext);
        given(servletContext.getAttribute(Log4jWebSupport.SUPPORT_ATTRIBUTE)).willReturn(initializer);
    }

    @Test
    public void testInitAndDestroy() throws Exception {
        this.listener.contextInitialized(this.event);

        then(initializer).should().start();
        then(initializer).should().setLoggerContext();

        this.listener.contextDestroyed(this.event);

        then(initializer).should().clearLoggerContext();
        then(initializer).should().stop();
    }

    @Test
    public void testInitFailure() throws Exception {
        willThrow(new IllegalStateException(Strings.EMPTY)).given(initializer).start();

        try {
            this.listener.contextInitialized(this.event);
            fail("Expected a RuntimeException.");
        } catch (final RuntimeException e) {
            assertEquals("Failed to initialize Log4j properly.", e.getMessage(), "The message is not correct.");
        }
    }

    @Test
    public void initializingLog4jServletContextListenerShouldFaileWhenAutoShutdownIsTrue() throws Exception {
        given(servletContext.getInitParameter(eq(Log4jWebSupport.IS_LOG4J_AUTO_SHUTDOWN_DISABLED)))
                .willReturn("true");
        ensureInitializingFailsWhenAuthShutdownIsEnabled();
    }

    @Test
    public void initializingLog4jServletContextListenerShouldFaileWhenAutoShutdownIsTRUE() throws Exception {
        given(servletContext.getInitParameter(eq(Log4jWebSupport.IS_LOG4J_AUTO_SHUTDOWN_DISABLED)))
                .willReturn("TRUE");
        ensureInitializingFailsWhenAuthShutdownIsEnabled();
    }

    private void ensureInitializingFailsWhenAuthShutdownIsEnabled() {
        try {
            this.listener.contextInitialized(this.event);
            fail("Expected a RuntimeException.");
        } catch (final RuntimeException e) {
            final String expectedMessage =
                    "Do not use " + Log4jServletContextListener.class.getSimpleName() + " when "
                            + Log4jWebSupport.IS_LOG4J_AUTO_SHUTDOWN_DISABLED + " is true. Please use "
                            + Log4jShutdownOnContextDestroyedListener.class.getSimpleName() + " instead of "
                            + Log4jServletContextListener.class.getSimpleName() + ".";

            assertEquals(expectedMessage, e.getMessage(), "The message is not correct");
        }
    }
}
