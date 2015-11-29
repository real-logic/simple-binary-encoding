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
#ifndef _OTFMESSAGETESTCBS_H_
#define _OTFMESSAGETESTCBS_H_

/*
 * Base class for CBs to track seen callbacks
 */
class OtfMessageTestCBs : public OnNext, public OnError, public OnCompleted
{
public:
OtfMessageTestCBs() : numFieldsSeen_(0), numErrorsSeen_(0), numCompletedsSeen_(0), numGroupsSeen_(0) {};

    virtual int onNext(const Field &f)
    {
        numFieldsSeen_++;
        return 0;
    };

    virtual int onNext(const Group &g)
    {
        numGroupsSeen_++;
        return 0;
    };

    virtual int onError(const Error &e)
    {
        numErrorsSeen_++;
        return 0;
    };

    virtual int onCompleted()
    {
        numCompletedsSeen_++;
        return 0;
    };

    int numFieldsSeen_;
    int numErrorsSeen_;
    int numCompletedsSeen_;
    int numGroupsSeen_;
};

#endif /* _OTFMESSAGETESTCBS_H_ */
