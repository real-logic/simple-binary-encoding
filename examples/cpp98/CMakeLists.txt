#
# Copyright 2013 Real Logic Ltd.
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

set(GENERATED_CODECS 
	${CODEC_TARGET_DIR}/baseline
)

set(EXAMPLES_SCHEMA ${CODEC_EXAMPLES_SCHEMA_DIR}/example-schema.xml)

add_custom_command(
	OUTPUT ${GENERATED_CODECS}
	DEPENDS ${EXAMPLES_SCHEMA}
	COMMAND ${Java_JAVA_EXECUTABLE} -Dsbe.output.dir=${CODEC_TARGET_DIR} -Dsbe.generate.ir="true" -Dsbe.target.language="cpp98" -jar ${SBE_JAR} ${EXAMPLES_SCHEMA}
)
add_custom_target(examples_codecs DEPENDS ${GENERATED_CODECS})

add_executable(sbeExample SbeExample.cpp)
add_executable(sbeOtfDecoder SbeOtfDecoder.cpp)
target_link_libraries(sbeOtfDecoder sbe)

add_dependencies(sbeExample examples_codecs)
add_dependencies(sbeOtfDecoder examples_codecs)
