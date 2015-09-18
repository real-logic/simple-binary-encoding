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
#ifndef _ONNEXT_H_
#define _ONNEXT_H_

#include "Field.h"
#include "Group.h"

namespace sbe {
namespace on_the_fly {

/**
 * \brief Interface used for indicating decoding of a field and/or a group indication
 */
class OnNext
{
public:
    /**
     * \brief Method called when Listener finishes decoding a field
     *
     * \param field encountered while decoding
     * \return 0 for success and -1 for failure
     * \sa Field
     */
    virtual int onNext(const Field &field) = 0;

    /**
     * \brief Method called when Listener encounters a group element
     *
     * \param group encountered while decoding
     * \return 0 for success and -1 for failure
     * \sa Group
     */
    virtual int onNext(const Group &group) = 0;

    virtual ~OnNext() {}
};

} // namepsace on_the_fly

} // namespace sbe

#endif /* _ONNEXT_H_ */
