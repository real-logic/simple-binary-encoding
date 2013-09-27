/* -*- mode: java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil -*- */
/*
 * Copyright 2013 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.sbe.xml;

import uk.co.real_logic.sbe.SbeTool;

import java.io.PrintStream;

/**
 * class to hold error handling state
 */
public class ErrorHandler
{
    private final PrintStream out;
    private final boolean throwOnErr;
    private final boolean suppressOut;
    private final boolean warningsFatal;
    private int errors = 0;
    private int warnings = 0;

    public ErrorHandler(final PrintStream stream)
    {
        out = stream;
        throwOnErr = Boolean.parseBoolean(System.getProperty(SbeTool.SBE_VALIDATE_EXCEPTION));
        suppressOut = Boolean.parseBoolean(System.getProperty(SbeTool.SBE_VALIDATE_OUTPUT_SUPPRESS));
        warningsFatal = Boolean.parseBoolean(System.getProperty(SbeTool.SBE_VALIDATE_WARNINGS_FATAL));
    }

    public ErrorHandler()
    {
        this(System.err);
    }

    public void error(final String msg)
    {
        errors++;

        if (!suppressOut)
        {
            out.println("ERROR: " + msg);
        }

        if (throwOnErr)
        {
            throw new IllegalArgumentException(msg);
        }
    }

    public void warning(final String msg)
    {
        warnings++;

        if (!suppressOut)
        {
            out.println("WARNING: " + msg);
        }

        if (warningsFatal && throwOnErr)
        {
            throw new IllegalArgumentException(msg);
        }
    }

    public void checkIfShouldExit()
    {
        if (errors > 0)
        {
            throw new IllegalArgumentException("had " + errors + " errors");
        }
        else if (warnings > 0 && warningsFatal)
        {
            throw new IllegalArgumentException("had " + warnings + " warnings");
        }
    }

    public int getErrors()
    {
        return errors;
    }

    public int getWarnings()
    {
        return warnings;
    }

    public String toString()
    {
        return "ErrorHandler{" +
            "out=" + out +
            ", throwOnErr=" + throwOnErr +
            ", suppressOut=" + suppressOut +
            ", warningsFatal=" + warningsFatal +
            ", errors=" + errors +
            ", warnings=" + warnings +
            '}';
    }
}
