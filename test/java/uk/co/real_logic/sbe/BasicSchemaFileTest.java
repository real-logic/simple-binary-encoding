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
package uk.co.real_logic.sbe;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import static java.lang.Integer.*;
import static java.lang.Boolean.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class BasicSchemaFileTest
{
    /**
     * Return an InputStream suitable for XML parsing from a given filename. Will search CP.
     *
     * @param name of the XML file
     * @return {@link java.io.InputStream} of the file
     * @throws RuntimeException if file not found
     */
    public InputStream getLocalResource(final String name)
    {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(name);

        if (in == null)
        {
            throw new RuntimeException("could not find " + name);
        }

        return in;
    }

    @Test
    public void shouldHandleBasicFile()
        throws Exception
    {
        XmlSchemaParser.parseXmlAndGenerateIr(getLocalResource("BasicSchemaFileTest.xml"));
    }

    @Test
    public void shouldHandleBasicFileWithGroup()
        throws Exception
    {
        XmlSchemaParser.parseXmlAndGenerateIr(getLocalResource("BasicGroupSchemaFileTest.xml"));
    }

}