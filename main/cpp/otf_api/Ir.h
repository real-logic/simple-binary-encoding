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
#ifndef _IR_H_
#define _IR_H_

#define __STDC_LIMIT_MACROS 1
#include <stdint.h>
#include <string.h>

#include <string>

namespace sbe {
namespace on_the_fly {

/**
 * \brief Class that acts as an iterator and accessor over a serialized IR token list
 */
class Ir
{
public:
    /// Invalid message template ID
    static const ::int32_t INVALID_ID = -1;
    /// Value representing a variable length field (size)
    static const ::uint32_t VARIABLE_SIZE = -1;

    /// Constants used for holding Token signals
    enum TokenSignal
    {
        /// Begins a message. Is followed by a number of tokens in the message and terminated by an end message.
        BEGIN_MESSAGE = 1,
        /// Ends a message.
        END_MESSAGE = 2,
        /// Begins a composite. Is followed by a number of tokens in the composite and terminated by an end composite.
        BEGIN_COMPOSITE = 3,
        /// Ends a composite.
        END_COMPOSITE = 4,
        /// Begins a field. Is followed by a number of tokens in the field and terminated by an end field.
        BEGIN_FIELD = 5,
        /// Ends a field.
        END_FIELD = 6,
        /// Begins a repeating group. Is followed by a number of tokens in the group and terminated by an end group.
        BEGIN_GROUP = 7,
        /// Ends a repeating group.
        END_GROUP = 8,
        /// Begins an enumeration. Is followed by a number of tokens in the enumeration and terminated by an end enum.
        BEGIN_ENUM = 9,
        /// Indicates a valid value for an enumeration. Must appear between a begin/end enum pair.
        VALID_VALUE = 10,
        /// Ends an enumeration.
        END_ENUM = 11,
        /// Begins a bit set. Is followed by a number of tokens in the set and terminated by an end set
        BEGIN_SET = 12,
        /// Indicates a bit value in the bit set. Must appear between a begin/end set pair.
        CHOICE = 13,
        /// Ends a bit set.
        END_SET = 14,
        /// Begins a variable length data element. Is followed by a number of tokens in the element and terminated by an end var data.
        BEGIN_VAR_DATA = 15,
        /// Ends a variable length data element.
        END_VAR_DATA = 16,
        /// Indicates an encoding of a primitive element.
        ENCODING = 17
    };

    /// Constants used for representing byte order
    enum TokenByteOrder
    {
        /// little endian byte order
        SBE_LITTLE_ENDIAN = 0,
        /// big endian byte order
        SBE_BIG_ENDIAN = 1
    };

    /// Constants used for representing primitive types
    enum TokenPrimitiveType
    {
        /// Type is undefined or unknown
        NONE = 0,
        /// Type is a signed character
        CHAR = 1,
        /// Type is a signed 8-bit value
        INT8 = 2,
        /// Type is a signed 16-bit value
        INT16 = 3,
        /// Type is a signed 32-bit value
        INT32 = 4,
        /// Type is a signed 64-bit value
        INT64 = 5,
        /// Type is a unsigned 8-bit value
        UINT8 = 6,
        /// Type is a unsigned 16-bit value
        UINT16 = 7,
        /// Type is a unsigned 32-bit value
        UINT32 = 8,
        /// Type is a unsigned 64-bit value
        UINT64 = 9,
        /// Type is a 32-bit floating point value
        FLOAT = 10,
        /// Type is a 64-bit double floating point value
        DOUBLE = 11
    };

    /// Constants used for representing Presence
    enum TokenPresence
    {
        /// Field or encoding presence is required
        SBE_REQUIRED = 0,
        /// Field or encoding presence is optional
        SBE_OPTIONAL = 1,
        /// Field or encoding presence is constant and not encoded
        SBE_CONSTANT = 2
    };

    /**
     * \brief Interface for returning an Ir for a given templateId value.
     *
     * This interface is used by the decoder when it finds a field designated
     * by the user to be the dispatch point for messages. The value of the field
     * is passed to the Callback::irForTemplateId method and the overloaded
     * method must return an Ir for that message template ID.
     */
    class Callback
    {
    public:
        /**
         * \brief Method to be overloaded by subclasses that should return an Ir for
         * a given message template ID.
         *
         * \param templateId of the message
         * \param version of the message
         * \return Ir for the message
         */
        virtual Ir *irForTemplateId(const int templateId, const int version) = 0;

        virtual ~Callback() {}
    };

    // constructors and destructors

    /// Construct an Ir from a buffer with serialized tokens of len total size. Responsibility of buffer management is the applications.
    Ir(const char *buffer, const int len, const ::int64_t templateId, const ::int64_t schemaId, const ::int64_t schemaVersion = -1);

    /// Destroy an Ir. The buffer is NOT freed.
    virtual ~Ir();

    ::int64_t templateId(void) const
    {
        return templateId_;
    }

    ::int64_t id(void) const
    {
        return id_;
    }

    ::int64_t schemaVersion(void) const
    {
        return schemaVersion_;
    }

    // iterator methods for IrTokens

    /// Rewind Ir to beginning
    void begin();
    /// Increment Ir to next token in the list
    void next();
    /// Is the Ir setting at the end of the token list?
    bool end() const;

    // access methods for current IR Token

    /// Retrieve the offset value of the current token
    ::int32_t offset() const;
    /// Retrieve the size value of the current token
    ::int32_t size() const;
    /// Retrieve the Ir::TokenSignal of the current token
    TokenSignal signal() const;
    /// Retrieve the Ir::TokenByteOrder of the current token
    TokenByteOrder byteOrder() const;
    /// Retrieve the Ir::TokenPrimitiveType of the current token
    TokenPrimitiveType primitiveType() const;
    /// Retrieve the Ir::TokenPresence of the current token
    TokenPresence presence() const;
    /// Retrieve the ID set by the schema of the current token
    ::int32_t schemaId() const;
    /// Return the Ir::VALID_VALUE of an enumeration for the current token
    ::uint64_t validValue() const;
    /// Return the value of the current tokens bit set Ir::CHOICE value
    ::uint64_t choiceValue() const;
    /// Return the length of the name of the current token
    ::int64_t nameLen() const;
    /// Return the name of the current token
    std::string name() const;
    /// Return the length of the current tokens constant value in bytes
    ::int64_t constLen() const;
    /// Retrieve the current tokens constant value or NULL if not present
    const char *constValue() const;
    /// Return the length of the current tokens min value in bytes
    ::int64_t minLen() const;
    /// Return the current tokens min value or NULL if not present
    const char *minValue() const;
    /// Return the length of the current tokens max value in bytes
    ::int64_t maxLen() const;
    /// Return the current tokens max value or NULL if not present
    const char *maxValue() const;
    /// Return the length of the current tokens null value in bytes
    ::int64_t nullLen() const;
    /// Return the current tokens null value or NULL if not present
    const char *nullValue() const;
    /// Return the length of the current tokens characterEncoding value in bytes
    ::int64_t characterEncodingLen() const;
    /// Return the current tokens characterEncoding value or NULL if not present
    const char *characterEncoding() const;
    /// Return the length of the current tokens epoch value in bytes
    ::int64_t epochLen() const;
    /// Return the current tokens epoch value or NULL if not present
    const char *epoch() const;
    /// Return the length of the current tokens timeUnit value in bytes
    ::int64_t timeUnitLen() const;
    /// Return the current tokens timeUnit value or NULL if not present
    const char *timeUnit() const;
    /// Return the length of the current tokens semanticType value in bytes
    ::int64_t semanticTypeLen() const;
    /// Return the current tokens semanticType value or NULL if not present
    const char *semanticType() const;

    /// Retrieve position of current token
    int position() const;
    /// Rewind or fast-forward Ir to given position
    void position(int pos);

    // used by test fixtures to generate IR for tests - initial call allocates max sized buffer
    void addToken(::uint32_t offset,
                  ::uint32_t size,
                  TokenSignal signal,
                  TokenByteOrder byteOrder,
                  TokenPrimitiveType primitiveType,
                  ::uint16_t fieldId,
                  const std::string &name,
                  const char *constValue = NULL,
                  int constValLength = 0);

    // used to retrieve what would be the nominal size of a single element of a primitive type
    static ::int64_t size(TokenPrimitiveType type)
    {
        switch (type)
        {
        case CHAR:
        case INT8:
        case UINT8:
            return 1;
            break;
        case INT16:
        case UINT16:
            return 2;
            break;
        case INT32:
        case UINT32:
        case FLOAT:
            return 4;
            break;
        case INT64:
        case UINT64:
        case DOUBLE:
            return 8;
            break;
        case NONE:
        default:
            return 0;
            break;
        }
    }

private:
    void readTokenAtCurrentPosition();

    const char *buffer_;
    int len_;
    int cursorOffset_;
    ::int64_t templateId_;
    ::int64_t id_;
    ::int64_t schemaVersion_;

    struct Impl;

    struct Impl *impl_;
};

} // namespace on_the_fly
} // namespace sbe

#endif /* _IR_H_ */
