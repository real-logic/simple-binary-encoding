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
#ifndef _SBE_HPP_
#define _SBE_HPP_

/*
 * Types used by C++ codec. Might have to be platform specific at some stage.
 */
typedef char sbe_char_t;
typedef int8_t sbe_int8_t;
typedef int16_t sbe_int16_t;
typedef int32_t sbe_int32_t;
typedef int64_t sbe_int64_t;
typedef uint8_t sbe_uint8_t;
typedef uint16_t sbe_uint16_t;
typedef uint32_t sbe_uint32_t;
typedef uint64_t sbe_uint64_t;
typedef float sbe_float_t;
typedef double sbe_double_t;

namespace sbe {

/// Interface for FixedFlyweight
class FixedFlyweight
{
public:
    virtual void reset(char *buffer, const int offset) = 0;
};

/// Interface for MessageFlyweight
class MessageFlyweight
{
public:
    virtual void reset(char *buffer, const int offset) = 0;
    virtual uint64_t blockLength(void) const = 0;
    virtual uint64_t offset(void) const = 0;
    virtual uint64_t position(void) const = 0;
    virtual void position(const uint64_t position) = 0;
    virtual char *buffer(void) = 0;
};

/// Interface for GroupFlyweight
class GroupFlyweight
{
public:
    virtual void resetForDecode(MessageFlyweight *message) = 0;
    virtual void resetForEncode(MessageFlyweight *message, const int size) = 0;
    virtual int size(void) const = 0;
    virtual bool next(void) = 0;
};

}

#endif /* _SBE_HPP_ */
