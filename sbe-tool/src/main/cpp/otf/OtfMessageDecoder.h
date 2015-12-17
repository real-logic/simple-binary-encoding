/*
 * Copyright 2015 Real Logic Ltd.
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
#ifndef _OTF_MESSAGEDECODER_H
#define _OTF_MESSAGEDECODER_H

#include <functional>
#include <vector>

#include "Token.h"

namespace sbe {
namespace otf {
namespace OtfMessageDecoder {

typedef std::function<void(Token&)> on_begin_message_t;
typedef std::function<void(Token&)> on_end_message_t;

// Builder allows us to avoid vtable overhead and leverage some ease of use
class TokenListenerBuilder
{
public:

    inline TokenListenerBuilder& onBeginMessage(on_begin_message_t onBeginMessage)
    {
        m_onBeginMessage = onBeginMessage;
        return *this;
    }

    inline on_begin_message_t onBeginMessage()
    {
        return m_onBeginMessage;
    }

private:
    on_begin_message_t m_onBeginMessage = [](Token&) { /* no op */ };
};

int decode(
    const char *buffer,
    int actingVersion,
    int blockLength,
    std::shared_ptr<std::vector<Token>> msgTokens,
    TokenListenerBuilder& listener)
{
    listener.onBeginMessage()(msgTokens->at(0));

}

}}}

#endif
