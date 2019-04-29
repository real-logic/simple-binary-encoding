import unittest

from tests.gen.issue435 import *


class TestIssue435(unittest.TestCase):

    def setUp(self) -> None:
        self._buffer = bytearray(4096)
        self._messageHeader = MessageHeader()
        self._issue435 = Issue435()
        self._messageHeader.wrap(self._buffer, 0, Issue435.SCHEMA_VERSION)
        self._messageHeader.set_version(Issue435.SCHEMA_VERSION)

        self._messageHeader.set_block_length(Issue435.BLOCK_LENGTH)
        self._messageHeader.set_schema_id(Issue435.SCHEMA_ID)
        self._messageHeader.set_template_id(Issue435.TEMPLATE_ID)
        self._messageHeader.set_version(Issue435.SCHEMA_VERSION)

        #< ref > element in non - standard
        set_s = self._messageHeader.set_s()
        set_s.set_one(True)

    def test_non_standard_header_size(self):
        self.assertEqual(9, self._messageHeader.SIZE)

    def test_issue435_ref_test(self):
        self._issue435.wrap_encode(self._buffer, MessageHeader.SIZE)
        ex = self._issue435.set_example()
        ex.set_e(EnumRef.Two)
        self.assertEqual(1, Issue435.BLOCK_LENGTH)
        self._messageHeader.wrap(self._buffer, 0, Issue435.SCHEMA_VERSION)
        self.assertEqual(Issue435.BLOCK_LENGTH, self._messageHeader.get_block_length(), "Incorrect BlockLength");
        self.assertEqual(Issue435.SCHEMA_ID, self._messageHeader.get_schema_id(), "Incorrect SchemaId");
        self.assertEqual(Issue435.TEMPLATE_ID, self._messageHeader.get_template_id(), "Incorrect TemplateId");
        self.assertEqual(Issue435.SCHEMA_VERSION, self._messageHeader.get_version(), "Incorrect SchemaVersion");
        self.assertTrue(self._messageHeader.get_s().has_one(), "Incorrect SetRef.One");

        self._issue435.wrap_decode(self._buffer, MessageHeader.SIZE, self._messageHeader.get_block_length(),
                                self._messageHeader.get_version())

        self.assertEqual(EnumRef.Two, self._issue435.get_example().get_e(), "Incorrect EnuRef");
