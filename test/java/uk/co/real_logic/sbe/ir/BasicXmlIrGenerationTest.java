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
package uk.co.real_logic.sbe.ir;

import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;
import uk.co.real_logic.sbe.xml.XmlSchemaParser;

import java.io.InputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import static java.lang.Integer.*;
import static java.lang.Boolean.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class BasicXmlIrGenerationTest
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
    public void shouldGenerateCorrectIrForMessageHeader()
        throws Exception
    {
        MessageSchema schema = XmlSchemaParser.parseXmlAndGenerateMessageSchema(getLocalResource("BasicSchemaFileTest.xml"));
        IrGenerator irg = new IrGenerator();

        List<IrNode> ir = irg.generateForHeader(schema);
 
       /*
        * assert the basic structure of the messageHeader IR
        */
        assertThat(valueOf(ir.size()), is(valueOf(6)));

        /* assert all elements of node 0 */
        assertThat(ir.get(0).getMetaData().getFlag(), is(IrNode.Flag.STRUCT_START));
        assertThat(ir.get(0).getMetaData().getName(), is("messageHeader"));
        assertThat(valueOf(ir.get(0).getMetaData().getId()), is(valueOf(IrNode.MetaData.INVALID_ID)));
        assertThat(valueOf(ir.get(0).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(0).getOffset()), is(valueOf(0)));

        /* assert all elements of node 1 */
        assertThat(ir.get(1).getMetaData().getFlag(), is(IrNode.Flag.NONE));
        assertThat(ir.get(1).getMetaData().getName(), is("blockLength"));
        assertThat(valueOf(ir.get(1).getMetaData().getId()), is(valueOf(IrNode.MetaData.INVALID_ID)));
        assertThat(valueOf(ir.get(1).size()), is(valueOf(2)));
        assertThat(valueOf(ir.get(1).getOffset()), is(valueOf(0)));

        /* assert all elements of node 2 */
        assertThat(ir.get(2).getMetaData().getFlag(), is(IrNode.Flag.NONE));
        assertThat(ir.get(2).getMetaData().getName(), is("templateId"));
        assertThat(valueOf(ir.get(2).getMetaData().getId()), is(valueOf(IrNode.MetaData.INVALID_ID)));
        assertThat(valueOf(ir.get(2).size()), is(valueOf(2)));
        assertThat(valueOf(ir.get(2).getOffset()), is(valueOf(2)));

        /* assert all elements of node 3 */
        assertThat(ir.get(3).getMetaData().getFlag(), is(IrNode.Flag.NONE));
        assertThat(ir.get(3).getMetaData().getName(), is("version"));
        assertThat(valueOf(ir.get(3).getMetaData().getId()), is(valueOf(IrNode.MetaData.INVALID_ID)));
        assertThat(valueOf(ir.get(3).size()), is(valueOf(1)));
        assertThat(valueOf(ir.get(3).getOffset()), is(valueOf(4)));

        /* assert all elements of node 4 */
        assertThat(ir.get(4).getMetaData().getFlag(), is(IrNode.Flag.NONE));
        assertThat(ir.get(4).getMetaData().getName(), is("reserved"));
        assertThat(valueOf(ir.get(4).getMetaData().getId()), is(valueOf(IrNode.MetaData.INVALID_ID)));
        assertThat(valueOf(ir.get(4).size()), is(valueOf(1)));
        assertThat(valueOf(ir.get(4).getOffset()), is(valueOf(5)));

        /* assert all elements of node 5 */
        assertThat(ir.get(5).getMetaData().getFlag(), is(IrNode.Flag.STRUCT_END));
        assertThat(ir.get(5).getMetaData().getName(), is("messageHeader"));
        assertThat(valueOf(ir.get(5).getMetaData().getId()), is(valueOf(IrNode.MetaData.INVALID_ID)));
        assertThat(valueOf(ir.get(5).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(5).getOffset()), is(valueOf(0)));
    }

    @Test
    public void shouldGenerateCorrectIrForBasicMessage()
        throws Exception
    {
        MessageSchema schema = XmlSchemaParser.parseXmlAndGenerateMessageSchema(getLocalResource("BasicSchemaFileTest.xml"));
        IrGenerator irg = new IrGenerator();

        List<IrNode> ir = irg.generateForMessage(schema.getMessage(new Long(50001)));
 
       /*
        * assert the basic structure of the message IR
        */
        assertThat(valueOf(ir.size()), is(valueOf(3)));

        /* assert all elements of node 0 */
        assertThat(ir.get(0).getMetaData().getFlag(), is(IrNode.Flag.FIELD_START));
        assertThat(ir.get(0).getMetaData().getName(), is("Tag40001"));
        assertThat(valueOf(ir.get(0).getMetaData().getId()), is(valueOf(40001)));
        assertThat(valueOf(ir.get(0).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(0).getOffset()), is(valueOf(0)));

        /* assert all elements of node 1 */
        assertThat(ir.get(1).getMetaData().getFlag(), is(IrNode.Flag.NONE));
        assertThat(ir.get(1).getMetaData().getName(), is("uint32"));
        assertThat(valueOf(ir.get(1).getMetaData().getId()), is(valueOf(IrNode.MetaData.INVALID_ID)));
        assertThat(valueOf(ir.get(1).size()), is(valueOf(4)));
        assertThat(valueOf(ir.get(1).getOffset()), is(valueOf(0)));

        /* assert all elements of node 2 */
        assertThat(ir.get(2).getMetaData().getFlag(), is(IrNode.Flag.FIELD_END));
        assertThat(ir.get(2).getMetaData().getName(), is("Tag40001"));
        assertThat(valueOf(ir.get(2).getMetaData().getId()), is(valueOf(40001)));
        assertThat(valueOf(ir.get(2).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(2).getOffset()), is(valueOf(0)));
    }

}