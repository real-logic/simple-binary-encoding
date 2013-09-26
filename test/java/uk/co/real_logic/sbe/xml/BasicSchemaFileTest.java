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

import org.junit.Test;
import uk.co.real_logic.sbe.TestUtil;

import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class BasicSchemaFileTest
{
    @Test
    public void shouldHandleBasicFile()
        throws Exception
    {
        parse(TestUtil.getLocalResource("BasicSchemaFileTest.xml"));
    }

    @Test
    public void shouldHandleBasicFileWithGroup()
        throws Exception
    {
        parse(TestUtil.getLocalResource("BasicGroupSchemaFileTest.xml"));
    }

    @Test
    public void shouldHandleBasicFileWithVariableLengthData()
        throws Exception
    {
        parse(TestUtil.getLocalResource("BasicVariableLengthSchemaFileTest.xml"));
    }
}