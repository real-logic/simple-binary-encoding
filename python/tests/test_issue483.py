import unittest

from tests.gen.issue483.issue483_encoder import *
from tests.gen.issue483.issue483_decoder import *


class TestIssue483(unittest.TestCase):

    def test_presense(self):
        # Check  attributes for their presence meta attribute
        self.assertEqual(Issue483Encoder.unsetMetaAttribute(MetaAttribute.Presence), "required")
        self.assertEqual(Issue483Encoder.requiredMetaAttribute(MetaAttribute.Presence), "required")
        self.assertEqual(Issue483Encoder.constantMetaAttribute(MetaAttribute.Presence), "constant")
        self.assertEqual(Issue483Encoder.optionalMetaAttribute(MetaAttribute.Presence), "optional")
