import unittest

from tests.gen.issue483 import *


class TestIssue483(unittest.TestCase):

    def test_presense(self):
        # Check  attributes for their presence meta attribute
        self.assertEqual(Issue483.unset_meta_attribute(MetaAttribute.Presence), "required")
        self.assertEqual(Issue483.required_meta_attribute(MetaAttribute.Presence), "required")
        self.assertEqual(Issue483.constant_meta_attribute(MetaAttribute.Presence), "constant")
        self.assertEqual(Issue483.optional_meta_attribute(MetaAttribute.Presence), "optional")
