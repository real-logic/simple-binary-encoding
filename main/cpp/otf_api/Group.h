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
#ifndef _GROUP_H_
#define _GROUP_H_

namespace sbe {
namespace on_the_fly {

class Group
{
public:
    enum Event
    {
        START = 1,
        END = 2,
        NONE = 3
    };

    Group() {};

    Event event() const
    {
        return event_;
    };

    const std::string &name() const
    {
        return name_;
    };

    int iteration() const
    {
        return iteration_;
    };

    int numInGroup() const
    {
        return numInGroup_;
    };

protected:
    Group &name(const std::string &name)
    {
        name_ = name;
        return *this;
    };

    Group &iteration(const int iteration)
    {
        iteration_ = iteration;
        return *this;
    };

    Group &numInGroup(const int numInGroup)
    {
        numInGroup_ = numInGroup;
        return *this;
    };

    Group &event(Event event)
    {
        event_ = event;
        return *this;
    };

    Group &reset()
    {
        iteration_ = -1;
        numInGroup_ = 0;
        name_ = "";
        event_ = NONE;
        return *this;
    };

private:
    std::string name_;
    Event event_;
    int iteration_;
    int numInGroup_;

    friend class Listener;
};

} // namespace on_the_fly

} // namespace sbe

#endif /* _GROUP_H_ */
