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
#ifndef _FIELD_H_
#define _FIELD_H_

#include <vector>

#include "Ir.h"

namespace sbe {
namespace on_the_fly {

/**
 * \brief Encapsulation of a field
 *
 * During decoding a Listener will call OnNext::onNext(const Field &) and pass encountered fields to the
 * application. These fields may be of varying types, including composites (or structs), enumerations,
 * bit sets, or variable length data. All of these types may be accessed via this class.
 */
class Field
{
public:
    /// Invalid Schema ID value
    static const ::int32_t INVALID_ID = -1;
    /// Index for the Field itself
    static const int FIELD_INDEX = -1;

    /// Type of Field
    enum Type
    {
        NOTSET = 0,
        /// Field is a composite
        COMPOSITE = 1,
        /// Field is an encoding of a primitive type
        ENCODING = 2,
        /// Field is an enumeration
        ENUM = 3,
        /// Field is a bit set
        SET = 4,
        /// Field is variable length data
        VAR_DATA = 5
    };

    class EncodingValue
    {
    public:
        EncodingValue(const ::int64_t value) : int64Value_(value) {};
        EncodingValue(const ::uint64_t value) : uint64Value_(value) {};
        EncodingValue(const double value) : doubleValue_(value) {};
        EncodingValue(const char *value) : arrayValue_((char *)value) {};

        ::int64_t int64Value_;
        ::uint64_t uint64Value_;
        double doubleValue_;
        char *arrayValue_;   // this holds a pointer into the buffer. We don't alloc our own copy since Field is reused.
    };

    Field()
    {
        reset();
    }

    virtual ~Field() {};

    /// Retrieve the type of Field this is. \sa Field::Type
    Type type() const
    {
        return type_;
    }

    /// Return the number of encodings this Field contains. This is usually 1. However, Field::COMPOSITE can have more than one.
    int numEncodings() const
    {
        return numEncodings_;
    }

    /// Return whether the Field is a Field::COMPOSITE or not
    bool isComposite() const { return (COMPOSITE == type_) ? true : false; };
    /// Return whether the Field is a Field::ENUM or not
    bool isEnum() const { return (ENUM == type_) ? true : false; };
    /// Return whether the Field is a Field::SET or not
    bool isSet() const {return (SET == type_) ? true : false; };
    /// Return whether the Field is a Field::VAR_DATA or not
    bool isVariableData() const { return (VAR_DATA == type_) ? true : false; };

    /// Return the ID assigned by the schema for this Field. May be set to Ir::INVALID_ID to indicate no ID assigned.
    ::int32_t schemaId() const
    {
        return schemaId_;
    }

    /// Return the name of the Field. Can be empty if field has no name.
    const std::string &fieldName() const
    {
        return name_;
    }

    /// Return the name of the composite if Field is a Field::COMPOSITE or empty if not.
    const std::string &compositeName() const
    {
        return compositeName_;
    }

    /** \brief Return the name of the encoding for the given index
     *
     * \param index of the encoding to return the name of
     * \return the name of the encoding for the given index
     */
    const std::string &encodingName(const int index) const
    {
        return encodingNames_[index];
    }

    /** \brief Return the Ir::TokenPrimitiveType of the encoding for the given index
     *
     * \param index of the encoding to return the primitive type of. May be Field::FIELD_INDEX if only a single encoding.
     * \return the primitive type of the encoding
     */
    Ir::TokenPrimitiveType primitiveType(const int index = FIELD_INDEX) const
    {
        return (index == FIELD_INDEX) ? primitiveTypes_[0] : primitiveTypes_[index];
    }

    /** \brief Return the Ir::TokenPresence of the encoding for the given index
     *
     * \param index of the encoding to return the presence of. May be Field::FIELD_INDEX if only a single encoding.
     * \return the presence of the encoding
     */
    Ir::TokenPresence presence(const int index = FIELD_INDEX) const
    {
        return (index == FIELD_INDEX) ? presence_[0] : presence_[index];
    }

    /** \brief Return the length in primitive type units of the encoding for the given index
     *
     * \param index of the encoding to return the length of. May be Field::FIELD_INDEX if only a single encoding.
     * \return the length in primitive type units of the encoding
     */
    int length(const int index = FIELD_INDEX) const
    {
        return (index == FIELD_INDEX) ? encodingLengths_[0] : encodingLengths_[index];
    }

    // encoding values. index = -1 means only 1 encoding (for set, enum, encoding) and exceptions on composite
    // enums and sets can have values as well. So, could grab encoding values.

    /** \brief Return the signed integer value of the encoding for the given index
     *
     * \warning
     * If the encoding is not a signed integer type, this value may be garbage.
     *
     * \param index of the encoding to return the value of. May be Field::FIELD_INDEX if only a single encoding.
     * \return the signed integer value of the encoding
     */
    ::int64_t getInt(const int index = FIELD_INDEX) const
    {
        return (index == FIELD_INDEX) ? encodingValues_[0].int64Value_ : encodingValues_[index].int64Value_;
    }

    /** \brief Return the unsigned integer value of the encoding for the given index
     *
     * \warning
     * If the encoding is not an unsigned integer type, this value may be garbage.
     *
     * \param index of the encoding to return the value of. May be Field::FIELD_INDEX if only a single encoding.
     * \return the unsigned integer value of the encoding
     */
    ::uint64_t getUInt(const int index = FIELD_INDEX) const
    {
        return (index == FIELD_INDEX) ? encodingValues_[0].uint64Value_ : encodingValues_[index].uint64Value_;
    }

    /** \brief Return the floating point value of the encoding for the given index
     *
     * \warning
     * If the encoding is not a floating point type, this value may be garbage.
     *
     * \param index of the encoding to return the value of. May be Field::FIELD_INDEX if only a single encoding.
     * \return the floating point value of the encoding
     */
    double getDouble(const int index = FIELD_INDEX) const
    {
        return (index == FIELD_INDEX) ? encodingValues_[0].doubleValue_ : encodingValues_[index].doubleValue_;
    }

    /** \brief Retrieve the byte array value of the encoding for the given index
     *
     * \warning
     * If the encoding is not a variable length data type or static array, this value may be garbage.
     * This method does not bounds check.
     *
     * \param index of the encoding to return the value of. May be Field::FIELD_INDEX if only a single encoding.
     * \param dst to copy the byte array into
     * \param offset in primitive type units to start the copy from
     * \param length in primitive type units to copy
     */
    void getArray(const int index, char *dst, const int offset, const int length) const
    {
        ::memcpy(dst,
                 encodingValues_[index].arrayValue_ + (Ir::size(primitiveTypes_[index]) * offset),
                 Ir::size(primitiveTypes_[index]) * length);
    }

    /** \brief Retrieve the name of the valid value that matches the value of this enumeration
     *
     * This field may be empty to signify that no valid value matches.
     *
     * \warning
     * This string will be empty if the Field is not an enumeration.
     *
     * \return the name of the valid value
     */
    const std::string &validValue() const
    {
        return validValue_;
    }

    /** \brief Retrieve the list of names of the set bits in the bit set
     *
     * This list may be empty if no bits matched.
     *
     * \warning
     * This list will be empty if the Field is not a bit set.
     *
     * \return vector of strings matching set bits in the bit set.
     */
    const std::vector<std::string> &choices() const
    {
        return choiceValues_;
    }

    /// Type of Meta Attribute
    enum MetaAttribute
    {
        /// epoch attribute
        EPOCH,
        /// timeUnit attribute
        TIME_UNIT,
        /// semanticType attribute
        SEMANTIC_TYPE
    };

    /** \brief Return the value of the meta attribute of the encoding for the given index
     *
     * \param attr to return
     * \param index of the encoding to return the value of. May be Field::FIELD_INDEX if only a single encoding.
     * \return string holding the attribute value or an empty string if not set and not a defined default.
     */
    const std::string getMetaAttribute(const MetaAttribute attr, const int index = FIELD_INDEX) const
    {
        int i = (FIELD_INDEX == index) ? 0 : index;

        switch (attr)
        {
            case EPOCH:
                return std::string(metaEpoch_[i].first, metaEpoch_[i].second);
                break;

            case TIME_UNIT:
                return std::string(metaTimeUnit_[i].first, metaTimeUnit_[i].second);
                break;

            case SEMANTIC_TYPE:
                return std::string(metaSemanticType_[i].first, metaSemanticType_[i].second);
                break;

            default:
                break;
        }
        return std::string("");
    }

protected:
    // builder-ish pattern - set by Listener
    Field &numEncodings(const ::uint16_t numEncodings)
    {
        numEncodings_ = numEncodings;
        return *this;
    }

    Field &type(const Type type)
    {
        type_ = type;
        return *this;
    }

    Field &fieldName(const std::string &name)
    {
        name_ = name;
        return *this;
    }

    Field &compositeName(const std::string &name)
    {
        compositeName_ = name;
        return *this;
    }

    Field &name(const int index, const std::string &name)
    {
        encodingNames_[index] = name;
        return *this;
    }

    Field &schemaId(const ::uint16_t id)
    {
        schemaId_ = id;
        return *this;
    }

    void addMeta(const Ir *ir)
    {
        metaEpoch_.push_back(std::pair<const char *, ::int64_t>(ir->epoch(), ir->epochLen()));
        metaTimeUnit_.push_back(std::pair<const char *, ::int64_t>(ir->timeUnit(), ir->timeUnitLen()));
        metaSemanticType_.push_back(std::pair<const char *, ::int64_t>(ir->semanticType(), ir->semanticTypeLen()));
    }

    Field &addEncoding(const std::string &name, const Ir::TokenPrimitiveType type,
                       const ::int64_t value, const Ir *ir)
    {
        encodingNames_.push_back(name);
        primitiveTypes_.push_back(type);
        encodingValues_.push_back(EncodingValue(value));
        encodingLengths_.push_back(1);
        presence_.push_back(ir->presence());
        addMeta(ir);
        numEncodings_++;
        return *this;
    }

    Field &addEncoding(const std::string &name, const Ir::TokenPrimitiveType type,
                       const ::uint64_t value, const Ir *ir)
    {
        encodingNames_.push_back(name);
        primitiveTypes_.push_back(type);
        encodingValues_.push_back(EncodingValue(value));
        encodingLengths_.push_back(1);
        presence_.push_back(ir->presence());
        addMeta(ir);
        numEncodings_++;
        return *this;        
    }

    Field &addEncoding(const std::string &name, const Ir::TokenPrimitiveType type,
                       const double value, const Ir *ir)
    {
        encodingNames_.push_back(name);
        primitiveTypes_.push_back(type);
        encodingValues_.push_back(EncodingValue(value));
        encodingLengths_.push_back(1);
        presence_.push_back(ir->presence());
        addMeta(ir);
        numEncodings_++;
        return *this;
    }

    Field &addEncoding(const std::string &name, const Ir::TokenPrimitiveType type,
                       const char *array, const int size, const Ir *ir)
    {
        encodingNames_.push_back(name);
        primitiveTypes_.push_back(type);
        encodingValues_.push_back(EncodingValue(array));
        encodingLengths_.push_back(size / Ir::size(type));
        presence_.push_back(ir->presence());
        addMeta(ir);
        numEncodings_++;
        return *this;
    }

    Field &addValidValue(const std::string value)
    {
        validValue_ = value;
        return *this;
    }

    Field &addChoice(const std::string value)
    {
        choiceValues_.push_back(value);
        return *this;
    }

    Field &varDataLength(const ::uint64_t value)
    {
        varDataLength_ = value;
        return *this;
    }

    ::uint64_t varDataLength(void)
    {
        return varDataLength_;
    }

    Field &reset()
    {
        type_ = Field::NOTSET;
        name_ = "";
        compositeName_ = "";
        schemaId_ = INVALID_ID;
        numEncodings_ = 0;
        varDataLength_ = 0;
        encodingNames_.clear();
        primitiveTypes_.clear();
        encodingValues_.clear();
        encodingLengths_.clear();
        presence_.clear();
        choiceValues_.clear();
        metaEpoch_.clear();
        metaTimeUnit_.clear();
        metaSemanticType_.clear();
        validValue_ = "";
        return *this;
    }

private:
    Type type_;
    std::string name_;
    std::string compositeName_;
    std::string validValue_;
    ::int32_t schemaId_;
    ::uint16_t numEncodings_;
    ::uint64_t varDataLength_;

    std::vector<std::string> encodingNames_;
    std::vector<Ir::TokenPrimitiveType> primitiveTypes_;
    std::vector<EncodingValue> encodingValues_;
    std::vector<int> encodingLengths_;
    std::vector<Ir::TokenPresence> presence_;
    std::vector<std::pair<const char *, ::int64_t> > metaEpoch_;
    std::vector<std::pair<const char *, ::int64_t> > metaTimeUnit_;
    std::vector<std::pair<const char *, ::int64_t> > metaSemanticType_;

    std::vector<std::string> choiceValues_;

    friend class Listener;
};

} // namespace on_the_fly

} // namespace sbe

#endif /* _FIELD_H_ */
