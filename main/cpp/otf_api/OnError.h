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
#ifndef _ONERROR_H_
#define _ONERROR_H_

#include "Field.h"

namespace sbe {
namespace on_the_fly {

/**
 * \brief Encapsulation of an error encountered while decoding
 */
class Error
{
public:
    /// Construct an error object with given error message string
    Error(const char *errorMsg) : errorMsg_(errorMsg) {}
    /// Construct an error object with given error message string as std::string
    Error(const std::string errorMsg) : errorMsg_(errorMsg) {}
    virtual ~Error() {};
    /// Return error message
    const std::string &message() const { return errorMsg_; }
private:
    std::string errorMsg_;
};

/**
 * \brief Interface used for indicating an error condition while decoding. Ends decoding of current message.
 */
class OnError
{
public:
    /**
     * \brief Method called when Listener encounters an error while decoding
     *
     * \param error encountered while decoding
     * \return 0 for success and -1 for failure
     * \sa Error
     */
    virtual int onError(const Error &error) = 0;

    virtual ~OnError() {}
};

} // namepsace on_the_fly

} // namespace sbe

#endif /* _ONERROR_H_ */
