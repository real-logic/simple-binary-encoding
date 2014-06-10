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

/**
 * \brief Encapsulation of a repeating group event and group information.
 *
 * Groups are markers in the event sequence of calls to OnNext::onNext. Groups
 * contain fields. When a group starts, OnNext::onNext(const Group &) is called
 * with a Group::Event type of Group::START, the name of the group, the iteration number
 * (starting at 0), and the expected number of iterations. After that, a set of calls
 * to OnNext(const Field &) should occur. A group is ended by a call to OnNext::onNext(const Group &)
 * with a Group::Event type of Group::END. Nested repeating groups are handled as one would
 * expect with Group::START and Group::END within an existing Group sequence.
 */
class Group
{
public:
    /// Group event type
    enum Event
    {
        /// Indicates a group is starting
        START = 1,
        /// Indicates a group is ending
        END = 2,
        /// Unknown event type
        NONE = 3
    };

    Group() {}

    Event event() const
    {
        return event_;
    }

    /// Return the name of the group as given in the schema and Ir
    const std::string &name() const
    {
        return name_;
    }

    /// Return the schema ID of the group as given in the schema and Ir
    ::int64_t schemaId() const
    {
        return schemaId_;
    }

    /// Return the iteration number. 0 based.
    int iteration() const
    {
        return iteration_;
    }

    /// Return the number of iterations of this group to expect.
    int numInGroup() const
    {
        return numInGroup_;
    }

protected:
    Group &name(const std::string &name)
    {
        name_ = name;
        return *this;
    }

    Group &schemaId(const ::int32_t id)
    {
        schemaId_ = id;
        return *this;
    }

    Group &iteration(const int iteration)
    {
        iteration_ = iteration;
        return *this;
    }

    Group &numInGroup(const int numInGroup)
    {
        numInGroup_ = numInGroup;
        return *this;
    }

    Group &event(Event event)
    {
        event_ = event;
        return *this;
    }

    Group &reset()
    {
        iteration_ = -1;
        numInGroup_ = 0;
        name_ = "";
        event_ = NONE;
        return *this;
    }

private:
    std::string name_;
    Event event_;
    int iteration_;
    int numInGroup_;
    ::int32_t schemaId_;

    friend class Listener;
};

} // namespace on_the_fly

} // namespace sbe

#endif /* _GROUP_H_ */
