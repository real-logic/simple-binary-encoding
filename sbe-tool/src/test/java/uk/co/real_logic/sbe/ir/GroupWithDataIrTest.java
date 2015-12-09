/*
 * Copyright 2015 Real Logic Ltd.
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

import org.junit.Ignore;
import org.junit.Test;
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;
import uk.co.real_logic.sbe.xml.ParserOptions;

import java.util.List;

import static java.lang.Integer.valueOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.co.real_logic.sbe.TestUtil.getLocalResource;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class GroupWithDataIrTest
{
    @Test
    public void shouldGenerateCorrectIrForVarDataInRepeatingGroup()
        throws Exception
    {
        final MessageSchema schema = parse(getLocalResource("group-with-data-schema.xml"), ParserOptions.DEFAULT);
        final IrGenerator irg = new IrGenerator();
        final Ir ir = irg.generate(schema);

        /* 0=msg, 1=field, 2=enc, 3=fieldend, 4=group, 5=comp, 6=enc, 7=enc, 8=compend, ... */
        final int groupIdx = 4;
        final int dimensionsCompIdx = 5;
        final int dimensionsBlEncIdx = 6;
        final int varDataFieldIdx = 15;
        final int lengthEncIdx = 17;
        final int dataEncIdx = 18;

        final List<Token> tokens = ir.getMessage(1);

        /* assert on the group token */
        assertThat(tokens.get(groupIdx).signal(), is(Signal.BEGIN_GROUP));
        assertThat(tokens.get(groupIdx).name(), is("Entries"));
        assertThat(valueOf(tokens.get(groupIdx).id()), is(valueOf(2)));

        /* assert on the comp token for dimensions */
        assertThat(tokens.get(dimensionsCompIdx).signal(), is(Signal.BEGIN_COMPOSITE));
        assertThat(tokens.get(dimensionsCompIdx).name(), is("groupSizeEncoding"));

        /* assert on the enc token for dimensions blockLength */
        assertThat(tokens.get(dimensionsBlEncIdx).signal(), is(Signal.ENCODING));
        assertThat(tokens.get(dimensionsBlEncIdx).name(), is("blockLength"));
        // TODO: value is variable as var data is present

        assertThat(tokens.get(varDataFieldIdx).signal(), is(Signal.BEGIN_VAR_DATA));
        assertThat(tokens.get(varDataFieldIdx).name(), is("varDataField"));
        assertThat(valueOf(tokens.get(varDataFieldIdx).id()), is(valueOf(5)));

        assertThat(tokens.get(lengthEncIdx).signal(), is(Signal.ENCODING));
        assertThat(tokens.get(lengthEncIdx).encoding().primitiveType(), is(PrimitiveType.UINT8));

        /* assert the group node has the right IrId and xRefIrId, etc. */
        assertThat(tokens.get(dataEncIdx).signal(), is(Signal.ENCODING));
        assertThat(tokens.get(dataEncIdx).encoding().primitiveType(), is(PrimitiveType.CHAR));
    }

    @Ignore
    @Test
    public void shouldGenerateCorrectIrForVarDataInNestedRepeatingGroup()
        throws Exception
    {
        final MessageSchema schema = parse(getLocalResource("group-with-data-schema.xml"), ParserOptions.DEFAULT);
        final IrGenerator irg = new IrGenerator();
        final Ir ir = irg.generate(schema);

        /* 0=msg, 1=field, 2=enc, 3=fieldend, 4=group, 5=comp, 6=enc, 7=enc, 8=compend, ... */
        final int groupIdx = 4;
        final int dimensionsCompIdx = 5;
        final int dimensionsBlEncIdx = 6;

        final List<Token> tokens = ir.getMessage(2);

        // TODO: complete
    }
}
