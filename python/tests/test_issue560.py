import unittest

from tests.gen.issue560 import *


class TestIssue560(unittest.TestCase):

    def setUp(self) -> None:
        self._buffer = bytearray(4096)
        self._issue560 = Issue560()
        self._header = MessageHeader()
        self._header.wrap(self._buffer, 0, Issue560.SCHEMA_VERSION)
        self._header.set_block_length(Issue560.BLOCK_LENGTH)
        self._header.set_schema_id(Issue560.SCHEMA_ID)
        self._header.set_template_id(Issue560.TEMPLATE_ID)
        self._header.set_version(Issue560.SCHEMA_VERSION)

    def test_const_enum(self):
        self.assertEqual(self._issue560.get_discounted_model(), Model.C, "Incorrect const enum valueref")

