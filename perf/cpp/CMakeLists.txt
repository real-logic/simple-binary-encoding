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
find_package(Protobuf)

CHECK_LIBRARY_EXISTS(rt clock_gettime "time.h" HAVE_CLOCK_GETTIME_RT)

if(CMAKE_BUILD_TYPE STREQUAL "Release")
	message("Build type(${CMAKE_BUILD_TYPE}): perf tests turning OFF bounds checks")
	add_definitions(-DNDEBUG)
	add_definitions(-DSBE_NO_BOUNDS_CHECK)
else()
	message("Build type(${CMAKE_BUILD_TYPE}): perf tests turning ON bounds checks")
endif()

set(SRCS_BENCHLET_MAIN benchlet-main.cpp)

set(GENERATED_CODECS 
	${CODEC_TARGET_DIR}/uk_co_real_logic_sbe_examples_car
	${CODEC_TARGET_DIR}/uk_co_real_logic_sbe_samples_fix
)

set(SBE_CAR_SCHEMA ${CODEC_PERF_SCHEMA_DIR}/sbe/car-c.xml)
set(SBE_MD_SCHEMA ${CODEC_PERF_SCHEMA_DIR}/sbe/fix-message-samples.xml)

add_custom_command(
	OUTPUT ${GENERATED_CODECS}
	DEPENDS ${SBE_CAR_SCHEMA} ${SBE_MD_SCHEMA}
	COMMAND ${Java_JAVA_EXECUTABLE} -Dsbe.output.dir=${CODEC_TARGET_DIR} -Dsbe.target.language="cpp98" -jar ${SBE_JAR} ${SBE_CAR_SCHEMA} ${SBE_MD_SCHEMA}
)
add_custom_target(perf_codecs DEPENDS ${GENERATED_CODECS})

add_executable(benchlet-sbe-car-runner ${SRCS_BENCHLET_MAIN} CarBench.cpp)
add_executable(benchlet-sbe-md-runner ${SRCS_BENCHLET_MAIN} MarketDataBench.cpp)
add_dependencies(benchlet-sbe-md-runner perf_codecs)
add_dependencies(benchlet-sbe-car-runner perf_codecs)

if (HAVE_CLOCK_GETTIME_RT)
  target_link_libraries(benchlet-sbe-md-runner rt)
  target_link_libraries(benchlet-sbe-car-runner rt)
endif (HAVE_CLOCK_GETTIME_RT)

if(USE_PROTOBUF)
	set(PROTOBUF_CAR_SCHEMA ${CODEC_TARGET_DIR}/protobuf/car.proto)
	set(PROTOBUF_MD_SCHEMA ${CODEC_TARGET_DIR}/protobuf/fix-messages.proto)
	set(PROTOBUF_MD_GENERATED_HEADERS
		${CMAKE_CURRENT_BINARY_DIR}/cpp/fix-messages.pb.h
	)
	set(PROTOBUF_MD_GENERATED_SOURCES
		${CMAKE_CURRENT_BINARY_DIR}/cpp/fix-messages.pb.cc
	)
	file(MAKE_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/cpp)
	add_custom_command(
		OUTPUT ${PROTOBUF_MD_GENERATED_HEADERS} ${PROTOBUF_MD_GENERATED_SOURCES}
		DEPENDS ${PROTOBUF_MD_SCHEMA}
		COMMAND ${PROTOBUF_HOME}/bin/protoc -I${CMAKE_CURRENT_SOURCE_DIR}/../resources/protobuf --cpp_out ${PROTOBUF_TARGET_DIR} ${PROTOBUF_MD_SCHEMA}
	)
	include_directories(AFTER ${PROTOBUF_HOME}/include)
	find_library(PROTOBUF_LIBS protobuf ${PROTOBUF_HOME}/lib)
	add_custom_target(generate_protobuf_md DEPENDS ${PROTOBUF_MD_GENERATED_HEADERS} ${PROTOBUF_MD_GENERATED_SOURCES})
	add_executable(benchlet-pb-md-runner ${SRCS_BENCHLET_MAIN} ${PROTOBUF_MD_GENERATED_SOURCES} PbMarketDataBench.cpp)
	add_dependencies(benchlet-pb-md-runner generate_protobuf_md)
	target_link_libraries(benchlet-pb-md-runner ${PROTOBUF_LIBS})
endif()
