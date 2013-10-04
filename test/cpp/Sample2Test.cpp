#include "gtest/gtest.h"
#include "sample.h"

TEST(Sample2Test, Trivial)
{
    SomethingBasic *b = new SomethingBasic(3);
    EXPECT_EQ(3, b->val());
}
