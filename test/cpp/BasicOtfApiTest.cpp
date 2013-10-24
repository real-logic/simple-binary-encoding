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
#include <deque>
#include <iostream>

#include "gtest/gtest.h"
#include "otf_api/Listener.h"

using namespace sbe::on_the_fly;
using ::std::cout;
using ::std::endl;

TEST(BasicOtfApi, shouldHandleListenerCreation)
{
    Listener listener;
}

class TestListener : public Listener
{
private:
    std::deque<Field> fieldQ_;
public:
    TestListener() : Listener() {};

    virtual int process(void)
    {
        while (!fieldQ_.empty())
        {
            Field f = fieldQ_.front();
            Listener::deliver(f);
            fieldQ_.pop_front();
        }
        return 0;
    };

    TestListener &add(const Field &f)
    {
        fieldQ_.push_back(f);
        return *this;
    };
};

class OnNextCounter : public OnNext
{
private:
    int count_;
public:
    OnNextCounter() : count_(0) {};

    virtual int onNext(const Field &)
    {
        count_++;
        return 0;
    };

    virtual int onNext(const Group &)
    {
        return 0;
    };

    int count(void)
    {
        return count_;
    };
};

/*
 * Could have made this a fixture, but want to use it directly.
 */
TEST(BasicOtfApi, shouldHandleOnNext)
{
    Field f1, f2;
    OnNextCounter counter;
    TestListener listener;

    listener.add(f1).add(f2).subscribe(&counter);
    EXPECT_EQ(counter.count(), 2);
}
