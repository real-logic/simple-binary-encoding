/*
 * Copyright 2013 Real Logic Ltd.
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
#include <iostream>

#include "uk_co_real_logic_sbe_examples/MessageHeader.hpp"
// #include "uk_co_real_logic_sbe_examples/Car.hpp"

using namespace std;
using namespace uk_co_real_logic_sbe_examples;

void encodeHdr(MessageHeader &hdr, char *buffer)
{
    hdr.resetForEncode(buffer, 0);

    hdr.blockLength(10);
    hdr.templateId(100);
    hdr.version(1);
    hdr.reserved(0);
}

void decodeHdr(MessageHeader &hdr, const char *buffer)
{
    hdr.resetForDecode(buffer, 0);

    cout << "messageHeader.blockLength=" << hdr.blockLength() << endl;
    cout << "messageHeader.templateId=" << hdr.templateId() << endl;
    cout << "messageHeader.version=" << (sbe_uint32_t)hdr.version() << endl;
    cout << "messageHeader.reserved=" << (sbe_uint32_t)hdr.reserved() << endl;
}

int main(int argc, const char* argv[])
{
    char buffer[2048];
    MessageHeader hdr;

    encodeHdr(hdr, buffer);
    decodeHdr(hdr, buffer);
    return 0;
}