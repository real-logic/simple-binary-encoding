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

    Group() : size_(0) {};
    virtual ~Group() {};

    const std::string &name() const
    {
        return name_;
    };

    int size() const
    {
        return size_;
    };

    // iterate over fields
    void begin();
    bool end() const;
    Field &current() const;
    void next();

protected:
    // set by Listener
    Group &name(std::string &name);

    Group &size(const int size)
    {
        size_ = size;
        return *this;
    };

    Group &reset()
    {
        size_ = 0;
        // TODO: remove all Fields
        return *this;
    };

private:
    std::string name_;
    Field cachedField_;
    int size_;

    friend class Listener;
};

} // namespace on_the_fly

} // namespace sbe

#endif /* _GROUP_H_ */
