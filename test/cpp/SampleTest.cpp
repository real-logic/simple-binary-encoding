#include "gtest/gtest.h"
#include "sample.h"

TEST(SampleTest, Trivial)
{
    SomethingBasic *b = new SomethingBasic(5);
    EXPECT_EQ(5, b->val());
}
