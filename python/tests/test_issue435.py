import unittest

from tests.gen.issue435.message_header_encoder import *
from tests.gen.issue435.message_header_decoder import *
from tests.gen.issue435.issue435_encoder import *
from tests.gen.issue435.issue435_decoder import *


class TestIssue435(unittest.TestCase):

    def setUp(self) -> None:
        self._buffer = bytearray(4096)
        self._messageHeader = MessageHeaderEncoder()
        self._messageHeaderDecoder = MessageHeaderDecoder()
        self._issue435 = Issue435Encoder()
        self._issue435_decoder = Issue435Decoder()
        self._messageHeader.wrap(self._buffer, 0)
        self._messageHeader.version(self._issue435.sbeSchemaVersion)
        self._messageHeader.blockLength(self._issue435.sbeBlockLength)
        self._messageHeader.schemaId(self._issue435.sbeSchemaId)
        self._messageHeader.templateId(self._issue435.sbeTemplateId)

        #< ref > element in non - standard
        set_s = self._messageHeader.s()
        set_s.one(True)

    def test_non_standard_header_size(self):
        self.assertEqual(9, self._messageHeader.encodedLength)

    def test_issue435_ref_test(self):
        self._issue435.wrap(self._buffer, self._messageHeader.encodedLength)
        ex = self._issue435.example()
        ex.e(EnumRef.Two)
        self.assertEqual(1, self._issue435.sbeBlockLength)
        self._messageHeaderDecoder.wrap(self._buffer, 0)
        self._messageHeader.wrap(self._buffer, 0)
        self.assertEqual(self._issue435.sbeBlockLength, self._messageHeaderDecoder.blockLength(), "Incorrect BlockLength")
        self.assertEqual(self._issue435.sbeSchemaId, self._messageHeaderDecoder.schemaId(), "Incorrect SchemaId")
        self.assertEqual(self._issue435.sbeTemplateId, self._messageHeaderDecoder.templateId(), "Incorrect TemplateId")
        self.assertEqual(self._issue435.sbeSchemaVersion, self._messageHeaderDecoder.version(), "Incorrect SchemaVersion")
        self.assertTrue(self._messageHeaderDecoder.s().one(), "Incorrect SetRef.One")

        self._issue435_decoder.wrap(self._buffer, self._messageHeader.encodedLength, self._messageHeaderDecoder.blockLength(),
                                self._messageHeaderDecoder.version())

        self.assertEqual(EnumRef.Two, self._issue435_decoder.example().e(), "Incorrect EnuRef");
