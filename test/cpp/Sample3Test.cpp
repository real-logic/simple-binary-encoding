#include "gtest/gtest.h"
#include "sample.h"

TEST(Sample3Test, Trivial)
{
    SomethingBasic *b = new SomethingBasic(5);
    EXPECT_EQ(1, b->val());
}
