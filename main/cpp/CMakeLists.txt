#
# Copyright 2015 Real Logic Ltd.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set(SOURCE
	otf_api/Ir.cpp
	otf_api/Listener.cpp
)

set(HEADERS
	otf_api/Field.h
	otf_api/Group.h
	otf_api/Ir.h
	otf_api/IrCollection.h
	otf_api/Listener.h
	otf_api/OnCompleted.h
	otf_api/OnError.h
	otf_api/OnNext.h
	sbe/sbe.hpp
)

add_library(sbe STATIC ${SOURCE} ${HEADERS})
