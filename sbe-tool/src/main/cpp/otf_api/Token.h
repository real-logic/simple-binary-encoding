/*
 * Copyright 2015 Real Logic Ltd.
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
#ifndef _TOKEN_H
#define _TOKEN_H

#include "Ir.h"

namespace sbe {
namespace on_the_fly {

/*
 * Hold the state for a single token in the IR
 */
class Token
{
public:
    Token(int index, const Ir *ir);

private:
    int offset_;
    int index_;
};

}}

#endif //TOKEN_H
