/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.airlift.resolver;

import org.codehaus.plexus.logging.Logger;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

/**
 * Adapt JDK logger to a Plexus logger, ignoring Plexus logger API parts that are not classical and
 * probably not really used. This is based on Slf4jLogger by Json van Zyl.
 */
class JdkLogger
        implements Logger
{
    private final java.util.logging.Logger logger;

    public JdkLogger(java.util.logging.Logger logger)
    {
        this.logger = logger;
    }

    @Override
    public void debug(String message)
    {
        logger.log(FINE, message);
    }

    @Override
    public void debug(String message, Throwable throwable)
    {
        logger.log(FINE, message, throwable);
    }

    @Override
    public boolean isDebugEnabled()
    {
        return logger.isLoggable(FINE);
    }

    @Override
    public void info(String message)
    {
        logger.log(INFO, message);
    }

    @Override
    public void info(String message, Throwable throwable)
    {
        logger.log(INFO, message, throwable);
    }

    @Override
    public boolean isInfoEnabled()
    {
        return logger.isLoggable(INFO);
    }

    @Override
    public void warn(String message)
    {
        logger.log(WARNING, message);
    }

    @Override
    public void warn(String message, Throwable throwable)
    {
        logger.log(WARNING, message, throwable);
    }

    @Override
    public boolean isWarnEnabled()
    {
        return logger.isLoggable(WARNING);
    }

    @Override
    public void error(String message)
    {
        logger.log(SEVERE, message);
    }

    @Override
    public void error(String message, Throwable throwable)
    {
        logger.log(SEVERE, message, throwable);
    }

    @Override
    public boolean isErrorEnabled()
    {
        return logger.isLoggable(SEVERE);
    }

    @Override
    public void fatalError(String message)
    {
        error(message);
    }

    @Override
    public void fatalError(String message, Throwable throwable)
    {
        error(message, throwable);
    }

    @Override
    public boolean isFatalErrorEnabled()
    {
        return isErrorEnabled();
    }

    /**
     * <b>Warning</b>: ignored (always return <code>0 == Logger.LEVEL_DEBUG</code>).
     */
    @Override
    public int getThreshold()
    {
        return 0;
    }

    /**
     * <b>Warning</b>: ignored.
     */
    @Override
    public void setThreshold(int threshold)
    {
    }

    /**
     * <b>Warning</b>: ignored (always return <code>null</code>).
     */
    @Override
    public Logger getChildLogger(String name)
    {
        return null;
    }

    @Override
    public String getName()
    {
        return logger.getName();
    }
}
