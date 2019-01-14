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
package uk.co.real_logic.sbe.ir;

import org.junit.Test;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;
import uk.co.real_logic.sbe.xml.ParserOptions;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.co.real_logic.sbe.TestUtil.getLocalResource;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class ValueRefsTest
{
    @Test
    public void shouldGenerateValueRefToEnum()
        throws Exception
    {
        final MessageSchema schema = parse(getLocalResource("value-ref-schema.xml"), ParserOptions.DEFAULT);
        final IrGenerator irg = new IrGenerator();
        final Ir ir = irg.generate(schema);

        assertThat(ir.getMessage(1).get(1).encodedLength(), is(8));
        assertThat(ir.getMessage(2).get(1).encodedLength(), is(0));
        assertThat(ir.getMessage(3).get(1).encodedLength(), is(0));
        assertThat(ir.getMessage(4).get(1).encodedLength(), is(0));
        assertThat(ir.getMessage(5).get(1).encodedLength(), is(0));
    }
}
