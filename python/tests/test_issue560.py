import unittest

from tests.gen.issue560 import Model
from tests.gen.issue560.issue560_encoder import Issue560Encoder
from tests.gen.issue560.issue560_decoder import Issue560Decoder
from tests.gen.issue560.message_header_encoder import MessageHeaderEncoder


class TestIssue560(unittest.TestCase):

    def setUp(self) -> None:
        self._buffer = bytearray(4096)
        self._issue560 = Issue560Encoder()
        self._issue560_decoder = Issue560Decoder()
        self._header = MessageHeaderEncoder()
        self._header.wrap(self._buffer, 0)
        self._header.blockLength(self._issue560.BLOCK_LENGTH)
        self._header.schemaId(self._issue560.SCHEMA_ID)
        self._header.templateId(self._issue560.TEMPLATE_ID)
        self._header.version(self._issue560.SCHEMA_VERSION)

    def test_const_enum(self):
        self.assertEqual(self._issue560_decoder.discountedModel(), Model.C, "Incorrect const enum valueref")