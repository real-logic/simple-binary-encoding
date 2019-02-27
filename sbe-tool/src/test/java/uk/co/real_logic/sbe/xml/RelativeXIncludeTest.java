/*
 * Copyright 2013-2019 Real Logic Ltd.
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

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import org.xml.sax.InputSource;

import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class RelativeXIncludeTest
{
    @Test
    public void shouldParseFileInSubDir()
        throws Exception
    {
        final URL testResource = getClass().getClassLoader().getResource("sub/basic-schema.xml");
        Assert.assertNotNull(testResource);

        final InputSource is = new InputSource(testResource.openStream());
        final File file = new File(testResource.getFile());
        is.setSystemId(file.toPath().toAbsolutePath().getParent().toUri().toString());
        final MessageSchema messageSchema = parse(is, ParserOptions.DEFAULT);

        Assert.assertNotNull(messageSchema.getType("Symbol"));
    }
}