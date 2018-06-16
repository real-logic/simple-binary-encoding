#
# Copyright 2013-2018 Real Logic Ltd.
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

find_package(Java REQUIRED)

add_custom_target(
        ir_codecs
        ${Java_JAVA_EXECUTABLE}
        "-Dsbe.output.dir=${CMAKE_CURRENT_SOURCE_DIR}"
        "-Dsbe.target.language=CPP"
        "-jar" "${SBE_JAR}"
        "../resources/sbe-ir.xml"
        WORKING_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}"
        COMMENT "Generate IR codecs"
)

set(HEADERS
        sbe/sbe.h
    otf/IrDecoder.h
    otf/Token.h
    otf/Encoding.h
    otf/OtfMessageDecoder.h
    otf/OtfHeaderDecoder.h
)

add_library(sbe STATIC ${HEADERS} dummy.cpp)

add_dependencies(sbe ir_codecs)
