/*
 * Copyright 2013-2017 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "gtest/gtest.h"
#include "code_generation_test/MessageHeader.h"
#include "code_generation_test/Car.h"

using namespace std;
using namespace code::generation::test;

namespace
{
struct StringTest : public testing::Test
{
    StringTest()
    {
        reset();
    }
    void reset()
    {
        // Issues are easier to detect if we initialise memory to something other than zero.
        memset(buf, 0xff, sizeof(buf));
        // Null terminate the buffer for safety though.
        buf[sizeof(buf) - 1] = '\0';
    }
    void putVehicleCode(const char* vehicleCode)
    {
        reset();
        in.wrapForEncode(buf, 0, sizeof(buf));
        in.putVehicleCode(vehicleCode);
        out.wrapForDecode(buf, 0, Car::sbeBlockLength(), Car::sbeSchemaVersion(), sizeof(buf));
    }
    void putVehicleCode(const string& vehicleCode)
    {
        reset();
        in.wrapForEncode(buf, 0, sizeof(buf));
        in.putVehicleCode(vehicleCode);
        out.wrapForDecode(buf, 0, Car::sbeBlockLength(), Car::sbeSchemaVersion(), sizeof(buf));
    }

    char buf[2048];
    Car in;
    Car out;
};

}

TEST_F(StringTest, shouldNullTerminateWhenNotFull)
{
    // The danger here is that, even though we're passing string "abc", the callee copies beyond the
    // null terminator or fails to zero pad.
    const char vc[] = { 'a', 'b', 'c', '\0', 'X', 'X' };

    putVehicleCode(vc);

    ASSERT_STREQ(out.vehicleCode(), "abc");
    ASSERT_EQ(out.vehicleCode()[4],  '\0');
    ASSERT_EQ(out.vehicleCode()[5],  '\0');
    ASSERT_EQ(out.vehicleCode()[6],  '\xff');

    // Similarly for std::string overload.
    reset();
    putVehicleCode(string(vc));

    ASSERT_STREQ(out.vehicleCode(), "abc");
    ASSERT_EQ(out.vehicleCode()[4],  '\0');
    ASSERT_EQ(out.vehicleCode()[5],  '\0');
    ASSERT_EQ(out.vehicleCode()[6],  '\xff');
}

TEST_F(StringTest, shouldNotNullTerminateWhenFull)
{
    putVehicleCode("abcdef");

    ASSERT_EQ(memcmp(out.vehicleCode(), "abcdef", 6), 0);
    ASSERT_EQ(out.vehicleCode()[6],  '\xff');

    // Similarly for std::string overload.
    reset();
    putVehicleCode(string("abcdef"));

    ASSERT_EQ(memcmp(out.vehicleCode(), "abcdef", 6), 0);
    ASSERT_EQ(out.vehicleCode()[6],  '\xff');
}

TEST_F(StringTest, shouldCopyToSmallerBuffer)
{
    putVehicleCode("abc");
    char vc[3] = { '\xff', '\xff', '\xff' };
    ASSERT_EQ(out.getVehicleCode(vc, 2), 2U);
    ASSERT_EQ(vc[0], 'a');
    ASSERT_EQ(vc[1], 'b');
    ASSERT_EQ(vc[2], '\xff');
}

TEST_F(StringTest, shouldCopyToLargerBuffer)
{
    putVehicleCode("abcdef");

    char vc[8];
    memset(vc, 0xff, sizeof(vc));
    ASSERT_EQ(out.getVehicleCode(vc, sizeof(vc)), 6U);
    ASSERT_STREQ(vc, "abcdef");
    ASSERT_EQ(vc[7], '\xff');
}

TEST_F(StringTest, shouldReturnActualStringSize)
{
    putVehicleCode(string("abc"));

    const string vc = out.getVehicleCodeAsString();
    ASSERT_EQ(vc,  "abc");
    ASSERT_EQ(vc.size(),  3U);
}
