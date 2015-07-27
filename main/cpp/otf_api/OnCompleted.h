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
#ifndef _ONCOMPLETED_H_
#define _ONCOMPLETED_H_

namespace sbe {
namespace on_the_fly {

/**
 * \brief Interface used for indicating completion of decoding a message
 */
class OnCompleted
{
public:
    /**
     * \brief Method called when Listener successfully completes decoding a message
     *
     * \return 0 for success and -1 for failure
     */
    virtual int onCompleted(void) = 0;

    virtual ~OnCompleted() {}
};

} // namepsace on_the_fly

} // namespace sbe

#endif /* _ONCOMPLETED_H_ */
