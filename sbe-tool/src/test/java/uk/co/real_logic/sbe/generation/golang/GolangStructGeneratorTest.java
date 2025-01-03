/*
 * Copyright 2013-2025 Real Logic Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.sbe.generation.golang;

import org.agrona.generation.StringWriterOutputManager;
import org.junit.jupiter.api.Test;
import uk.co.real_logic.sbe.Tests;
import uk.co.real_logic.sbe.generation.golang.struct.GolangGenerator;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;
import uk.co.real_logic.sbe.xml.ParserOptions;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

class GolangStructGeneratorTest
{
    @Test
    @SuppressWarnings("MethodLength")
    void shouldUseUpperCaseTypeNamesWhenReferencingEnumValues() throws Exception
    {
        try (InputStream in = Tests.getLocalResource("value-ref-with-lower-case-enum.xml"))
        {
            final ParserOptions options = ParserOptions.builder().stopOnError(true).build();
            final MessageSchema schema = parse(in, options);
            final IrGenerator irg = new IrGenerator();
            final Ir ir = irg.generate(schema);
            final StringWriterOutputManager outputManager = new StringWriterOutputManager();
            outputManager.setPackageName(ir.applicableNamespace());

            final GolangGenerator generator = new GolangGenerator(ir, outputManager);
            generator.generate();

            final String engineTypeSource = outputManager.getSource("issue505.EngineType").toString();
            assertEquals("// Generated SBE (Simple Binary Encoding) message codec\n" +
                "\n" +
                "package issue505\n" +
                "\n" +
                "import (\n" +
                "\t\"fmt\"\n" +
                "\t\"io\"\n" +
                "\t\"reflect\"\n" +
                ")\n" +
                "\n" +
                "type EngineTypeEnum uint8\n" +
                "type EngineTypeValues struct {\n" +
                "\tGas       EngineTypeEnum\n" +
                "\tOIl       EngineTypeEnum\n" +
                "\tWinD      EngineTypeEnum\n" +
                "\tSOLAR     EngineTypeEnum\n" +
                "\tNullValue EngineTypeEnum\n" +
                "}\n" +
                "\n" +
                "var EngineType = EngineTypeValues{0, 1, 2, 3, 255}\n" +
                "\n" +
                "func (e EngineTypeEnum) Encode(_m *SbeGoMarshaller, _w io.Writer) error {\n" +
                "\tif err := _m.WriteUint8(_w, uint8(e)); err != nil {\n" +
                "\t\treturn err\n" +
                "\t}\n" +
                "\treturn nil\n" +
                "}\n" +
                "\n" +
                "func (e *EngineTypeEnum) Decode(_m *SbeGoMarshaller, _r io.Reader, actingVersion uint16) error {\n" +
                "\tif err := _m.ReadUint8(_r, (*uint8)(e)); err != nil {\n" +
                "\t\treturn err\n" +
                "\t}\n" +
                "\treturn nil\n" +
                "}\n" +
                "\n" +
                "func (e EngineTypeEnum) RangeCheck(actingVersion uint16, schemaVersion uint16) error {\n" +
                "\tif actingVersion > schemaVersion {\n" +
                "\t\treturn nil\n" +
                "\t}\n" +
                "\tvalue := reflect.ValueOf(EngineType)\n" +
                "\tfor idx := 0; idx < value.NumField(); idx++ {\n" +
                "\t\tif e == value.Field(idx).Interface() {\n" +
                "\t\t\treturn nil\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\treturn fmt.Errorf(\"Range check failed on EngineType, unknown enumeration value %d\", e)\n" +
                "}\n" +
                "\n" +
                "func (*EngineTypeEnum) EncodedLength() int64 {\n" +
                "\treturn 1\n" +
                "}\n" +
                "\n" +
                "func (*EngineTypeEnum) GasSinceVersion() uint16 {\n" +
                "\treturn 0\n" +
                "}\n" +
                "\n" +
                "func (e *EngineTypeEnum) GasInActingVersion(actingVersion uint16) bool {\n" +
                "\treturn actingVersion >= e.GasSinceVersion()\n" +
                "}\n" +
                "\n" +
                "func (*EngineTypeEnum) GasDeprecated() uint16 {\n" +
                "\treturn 0\n" +
                "}\n" +
                "\n" +
                "func (*EngineTypeEnum) OIlSinceVersion() uint16 {\n" +
                "\treturn 0\n" +
                "}\n" +
                "\n" +
                "func (e *EngineTypeEnum) OIlInActingVersion(actingVersion uint16) bool {\n" +
                "\treturn actingVersion >= e.OIlSinceVersion()\n" +
                "}\n" +
                "\n" +
                "func (*EngineTypeEnum) OIlDeprecated() uint16 {\n" +
                "\treturn 0\n" +
                "}\n" +
                "\n" +
                "func (*EngineTypeEnum) WinDSinceVersion() uint16 {\n" +
                "\treturn 0\n" +
                "}\n" +
                "\n" +
                "func (e *EngineTypeEnum) WinDInActingVersion(actingVersion uint16) bool {\n" +
                "\treturn actingVersion >= e.WinDSinceVersion()\n" +
                "}\n" +
                "\n" +
                "func (*EngineTypeEnum) WinDDeprecated() uint16 {\n" +
                "\treturn 0\n" +
                "}\n" +
                "\n" +
                "func (*EngineTypeEnum) SOLARSinceVersion() uint16 {\n" +
                "\treturn 0\n" +
                "}\n" +
                "\n" +
                "func (e *EngineTypeEnum) SOLARInActingVersion(actingVersion uint16) bool {\n" +
                "\treturn actingVersion >= e.SOLARSinceVersion()\n" +
                "}\n" +
                "\n" +
                "func (*EngineTypeEnum) SOLARDeprecated() uint16 {\n" +
                "\treturn 0\n" +
                "}\n", engineTypeSource);

            final String messageSource = outputManager.getSource("issue505.SomeMessage").toString();
            assertEquals("// Generated SBE (Simple Binary Encoding) message codec\n" +
                "\n" +
                "package issue505\n" +
                "\n" +
                "import (\n" +
                "\t\"io\"\n" +
                "\t\"io/ioutil\"\n" +
                ")\n" +
                "\n" +
                "type SomeMessage struct {\n" +
                "\tEngineType EngineTypeEnum\n" +
                "}\n" +
                "\n" +
                "func (s *SomeMessage) Encode(_m *SbeGoMarshaller, _w io.Writer, doRangeCheck bool) error {\n" +
                "\tif doRangeCheck {\n" +
                "\t\tif err := s.RangeCheck(s.SbeSchemaVersion(), s.SbeSchemaVersion()); err != nil {\n" +
                "\t\t\treturn err\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\treturn nil\n" +
                "}\n" +
                "\n" +
                "func (s *SomeMessage) Decode(_m *SbeGoMarshaller, _r io.Reader, actingVersion uint16, " +
                "blockLength uint16, doRangeCheck bool) error {\n" +
                "\ts.EngineType = EngineType.Gas\n" +
                "\tif actingVersion > s.SbeSchemaVersion() && blockLength > s.SbeBlockLength() {\n" +
                "\t\tio.CopyN(ioutil.Discard, _r, int64(blockLength-s.SbeBlockLength()))\n" +
                "\t}\n" +
                "\tif doRangeCheck {\n" +
                "\t\tif err := s.RangeCheck(actingVersion, s.SbeSchemaVersion()); err != nil {\n" +
                "\t\t\treturn err\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\treturn nil\n" +
                "}\n" +
                "\n" +
                "func (s *SomeMessage) RangeCheck(actingVersion uint16, schemaVersion uint16) error {\n" +
                "\tif err := s.EngineType.RangeCheck(actingVersion, schemaVersion); err != nil {\n" +
                "\t\treturn err\n" +
                "\t}\n" +
                "\treturn nil\n" +
                "}\n" +
                "\n" +
                "func SomeMessageInit(s *SomeMessage) {\n" +
                "\ts.EngineType = EngineType.Gas\n" +
                "\treturn\n" +
                "}\n" +
                "\n" +
                "func (*SomeMessage) SbeBlockLength() (blockLength uint16) {\n" +
                "\treturn 0\n" +
                "}\n" +
                "\n" +
                "func (*SomeMessage) SbeTemplateId() (templateId uint16) {\n" +
                "\treturn 1\n" +
                "}\n" +
                "\n" +
                "func (*SomeMessage) SbeSchemaId() (schemaId uint16) {\n" +
                "\treturn 505\n" +
                "}\n" +
                "\n" +
                "func (*SomeMessage) SbeSchemaVersion() (schemaVersion uint16) {\n" +
                "\treturn 0\n" +
                "}\n" +
                "\n" +
                "func (*SomeMessage) SbeSemanticType() (semanticType []byte) {\n" +
                "\treturn []byte(\"\")\n" +
                "}\n" +
                "\n" +
                "func (*SomeMessage) SbeSemanticVersion() (semanticVersion string) {\n" +
                "\treturn \"1.0\"\n" +
                "}\n" +
                "\n" +
                "func (*SomeMessage) EngineTypeId() uint16 {\n" +
                "\treturn 1\n" +
                "}\n" +
                "\n" +
                "func (*SomeMessage) EngineTypeSinceVersion() uint16 {\n" +
                "\treturn 0\n" +
                "}\n" +
                "\n" +
                "func (s *SomeMessage) EngineTypeInActingVersion(actingVersion uint16) bool {\n" +
                "\treturn actingVersion >= s.EngineTypeSinceVersion()\n" +
                "}\n" +
                "\n" +
                "func (*SomeMessage) EngineTypeDeprecated() uint16 {\n" +
                "\treturn 0\n" +
                "}\n" +
                "\n" +
                "func (*SomeMessage) EngineTypeMetaAttribute(meta int) string {\n" +
                "\tswitch meta {\n" +
                "\tcase 1:\n" +
                "\t\treturn \"\"\n" +
                "\tcase 2:\n" +
                "\t\treturn \"\"\n" +
                "\tcase 3:\n" +
                "\t\treturn \"\"\n" +
                "\tcase 4:\n" +
                "\t\treturn \"constant\"\n" +
                "\t}\n" +
                "\treturn \"\"\n" +
                "}\n", messageSource);
        }
    }

    @Test
    @SuppressWarnings("MethodLength")
    void shouldUseUpperCaseTypeNamesWhenReferencingBitSet() throws Exception
    {
        try (InputStream in = Tests.getLocalResource("message-with-lower-case-bitset.xml"))
        {
            final ParserOptions options = ParserOptions.builder().stopOnError(true).build();
            final MessageSchema schema = parse(in, options);
            final IrGenerator irg = new IrGenerator();
            final Ir ir = irg.generate(schema);
            final StringWriterOutputManager outputManager = new StringWriterOutputManager();
            outputManager.setPackageName(ir.applicableNamespace());

            final GolangGenerator generator = new GolangGenerator(ir, outputManager);
            generator.generate();

            final String eventTypeSource = outputManager.getSource("test973.EventType").toString();
            assertEquals("// Generated SBE (Simple Binary Encoding) message codec\n" +
                "\n" +
                "package test973\n" +
                "\n" +
                "import (\n" +
                "\t\"io\"\n" +
                ")\n" +
                "\n" +
                "type EventType [8]bool\n" +
                "type EventTypeChoiceValue uint8\n" +
                "type EventTypeChoiceValues struct {\n" +
                "\tA     EventTypeChoiceValue\n" +
                "\tBb    EventTypeChoiceValue\n" +
                "\tCcc   EventTypeChoiceValue\n" +
                "\tD     EventTypeChoiceValue\n" +
                "\tEeEee EventTypeChoiceValue\n" +
                "}\n" +
                "\n" +
                "var EventTypeChoice = EventTypeChoiceValues{0, 1, 2, 3, 4}\n" +
                "\n" +
                "func (e *EventType) Encode(_m *SbeGoMarshaller, _w io.Writer) error {\n" +
                "\tvar wireval uint8 = 0\n" +
                "\tfor k, v := range e {\n" +
                "\t\tif v {\n" +
                "\t\t\twireval |= (1 << uint(k))\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\treturn _m.WriteUint8(_w, wireval)\n" +
                "}\n" +
                "\n" +
                "func (e *EventType) Decode(_m *SbeGoMarshaller, _r io.Reader, actingVersion uint16) error {\n" +
                "\tvar wireval uint8\n" +
                "\n" +
                "\tif err := _m.ReadUint8(_r, &wireval); err != nil {\n" +
                "\t\treturn err\n" +
                "\t}\n" +
                "\n" +
                "\tvar idx uint\n" +
                "\tfor idx = 0; idx < 8; idx++ {\n" +
                "\t\te[idx] = (wireval & (1 << idx)) > 0\n" +
                "\t}\n" +
                "\treturn nil\n" +
                "}\n" +
                "\n" +
                "func (EventType) EncodedLength() int64 {\n" +
                "\treturn 1\n" +
                "}\n" +
                "\n" +
                "func (*EventType) ASinceVersion() uint16 {\n" +
                "\treturn 0\n" +
                "}\n" +
                "\n" +
                "func (e *EventType) AInActingVersion(actingVersion uint16) bool {\n" +
                "\treturn actingVersion >= e.ASinceVersion()\n" +
                "}\n" +
                "\n" +
                "func (*EventType) ADeprecated() uint16 {\n" +
                "\treturn 0\n" +
                "}\n" +
                "\n" +
                "func (*EventType) BbSinceVersion() uint16 {\n" +
                "\treturn 0\n" +
                "}\n" +
                "\n" +
                "func (e *EventType) BbInActingVersion(actingVersion uint16) bool {\n" +
                "\treturn actingVersion >= e.BbSinceVersion()\n" +
                "}\n" +
                "\n" +
                "func (*EventType) BbDeprecated() uint16 {\n" +
                "\treturn 0\n" +
                "}\n" +
                "\n" +
                "func (*EventType) CccSinceVersion() uint16 {\n" +
                "\treturn 0\n" +
                "}\n" +
                "\n" +
                "func (e *EventType) CccInActingVersion(actingVersion uint16) bool {\n" +
                "\treturn actingVersion >= e.CccSinceVersion()\n" +
                "}\n" +
                "\n" +
                "func (*EventType) CccDeprecated() uint16 {\n" +
                "\treturn 0\n" +
                "}\n" +
                "\n" +
                "func (*EventType) DSinceVersion() uint16 {\n" +
                "\treturn 0\n" +
                "}\n" +
                "\n" +
                "func (e *EventType) DInActingVersion(actingVersion uint16) bool {\n" +
                "\treturn actingVersion >= e.DSinceVersion()\n" +
                "}\n" +
                "\n" +
                "func (*EventType) DDeprecated() uint16 {\n" +
                "\treturn 0\n" +
                "}\n" +
                "\n" +
                "func (*EventType) EeEeeSinceVersion() uint16 {\n" +
                "\treturn 0\n" +
                "}\n" +
                "\n" +
                "func (e *EventType) EeEeeInActingVersion(actingVersion uint16) bool {\n" +
                "\treturn actingVersion >= e.EeEeeSinceVersion()\n" +
                "}\n" +
                "\n" +
                "func (*EventType) EeEeeDeprecated() uint16 {\n" +
                "\treturn 0\n" +
                "}\n", eventTypeSource);

            final String messageSource = outputManager.getSource("test973.SomeMessage").toString();
            assertEquals("// Generated SBE (Simple Binary Encoding) message codec\n" +
                "\n" +
                "package test973\n" +
                "\n" +
                "import (\n" +
                "\t\"io\"\n" +
                "\t\"io/ioutil\"\n" +
                ")\n" +
                "\n" +
                "type SomeMessage struct {\n" +
                "\tMyEvent EventType\n" +
                "}\n" +
                "\n" +
                "func (s *SomeMessage) Encode(_m *SbeGoMarshaller, _w io.Writer, doRangeCheck bool) error {\n" +
                "\tif doRangeCheck {\n" +
                "\t\tif err := s.RangeCheck(s.SbeSchemaVersion(), s.SbeSchemaVersion()); err != nil {\n" +
                "\t\t\treturn err\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\tif err := s.MyEvent.Encode(_m, _w); err != nil {\n" +
                "\t\treturn err\n" +
                "\t}\n" +
                "\treturn nil\n" +
                "}\n" +
                "\n" +
                "func (s *SomeMessage) Decode(_m *SbeGoMarshaller, _r io.Reader, actingVersion uint16, " +
                "blockLength uint16, doRangeCheck bool) error {\n" +
                "\tif s.MyEventInActingVersion(actingVersion) {\n" +
                "\t\tif err := s.MyEvent.Decode(_m, _r, actingVersion); err != nil {\n" +
                "\t\t\treturn err\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\tif actingVersion > s.SbeSchemaVersion() && blockLength > s.SbeBlockLength() {\n" +
                "\t\tio.CopyN(ioutil.Discard, _r, int64(blockLength-s.SbeBlockLength()))\n" +
                "\t}\n" +
                "\tif doRangeCheck {\n" +
                "\t\tif err := s.RangeCheck(actingVersion, s.SbeSchemaVersion()); err != nil {\n" +
                "\t\t\treturn err\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\treturn nil\n" +
                "}\n" +
                "\n" +
                "func (s *SomeMessage) RangeCheck(actingVersion uint16, schemaVersion uint16) error {\n" +
                "\treturn nil\n" +
                "}\n" +
                "\n" +
                "func SomeMessageInit(s *SomeMessage) {\n" +
                "\treturn\n" +
                "}\n" +
                "\n" +
                "func (*SomeMessage) SbeBlockLength() (blockLength uint16) {\n" +
                "\treturn 1\n" +
                "}\n" +
                "\n" +
                "func (*SomeMessage) SbeTemplateId() (templateId uint16) {\n" +
                "\treturn 1\n" +
                "}\n" +
                "\n" +
                "func (*SomeMessage) SbeSchemaId() (schemaId uint16) {\n" +
                "\treturn 973\n" +
                "}\n" +
                "\n" +
                "func (*SomeMessage) SbeSchemaVersion() (schemaVersion uint16) {\n" +
                "\treturn 0\n" +
                "}\n" +
                "\n" +
                "func (*SomeMessage) SbeSemanticType() (semanticType []byte) {\n" +
                "\treturn []byte(\"\")\n" +
                "}\n" +
                "\n" +
                "func (*SomeMessage) SbeSemanticVersion() (semanticVersion string) {\n" +
                "\treturn \"1.0\"\n" +
                "}\n" +
                "\n" +
                "func (*SomeMessage) MyEventId() uint16 {\n" +
                "\treturn 1\n" +
                "}\n" +
                "\n" +
                "func (*SomeMessage) MyEventSinceVersion() uint16 {\n" +
                "\treturn 0\n" +
                "}\n" +
                "\n" +
                "func (s *SomeMessage) MyEventInActingVersion(actingVersion uint16) bool {\n" +
                "\treturn actingVersion >= s.MyEventSinceVersion()\n" +
                "}\n" +
                "\n" +
                "func (*SomeMessage) MyEventDeprecated() uint16 {\n" +
                "\treturn 0\n" +
                "}\n" +
                "\n" +
                "func (*SomeMessage) MyEventMetaAttribute(meta int) string {\n" +
                "\tswitch meta {\n" +
                "\tcase 1:\n" +
                "\t\treturn \"\"\n" +
                "\tcase 2:\n" +
                "\t\treturn \"\"\n" +
                "\tcase 3:\n" +
                "\t\treturn \"\"\n" +
                "\tcase 4:\n" +
                "\t\treturn \"required\"\n" +
                "\t}\n" +
                "\treturn \"\"\n" +
                "}\n", messageSource);
        }
    }
}
