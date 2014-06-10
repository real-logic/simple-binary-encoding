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
cmake_minimum_required(VERSION 2.8)

option(USE_PROTOBUF "Build Protobuf installed in PROTOBUF_HOME." OFF)
option(RETRIEVE_AND_BUILD_PROTOBUF "Retrieve and build protobuf in PROTOBUF_HOME. Not Windows!!" OFF)

if(UNIX)
	# added -DNDEBUG from #128
	set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Ofast -DNDEBUG")
endif()

if(APPLE)
	add_definitions(-DDarwin)
elseif(WIN32)
	add_definitions(-DWIN32)
else()
	add_definitions(-D_GNU_SOURCE)
endif()

include_directories(
	# the generated code directory
	${CMAKE_CURRENT_BINARY_DIR}/cpp
	# the benchmark code directory
	${CMAKE_CURRENT_SOURCE_DIR}/../../main/cpp
)

set(SRCS_BENCHLET_MAIN
	benchlet-main.cpp
)

set(SBE_CAR_SCHEMA ${CMAKE_CURRENT_SOURCE_DIR}/../resources/sbe/car-c.xml)
set(SBE_MD_SCHEMA ${CMAKE_CURRENT_SOURCE_DIR}/../resources/sbe/fix-message-samples.xml)
set(SBE_TARGET_DIR ${CMAKE_CURRENT_BINARY_DIR}/cpp)
set(SBE_JAR ${CMAKE_CURRENT_BINARY_DIR}/../dist/sbe.jar)
set(SBE_MD_GENERATED_HEADERS
	${SBE_TARGET_DIR}/uk_co_real_logic_sbe_samples_fix/MarketDataIncrementalRefreshTrades.hpp
	${SBE_TARGET_DIR}/uk_co_real_logic_sbe_samples_fix/MessageHeader.hpp
)
set(SBE_CAR_GENERATED_HEADERS
	${SBE_TARGET_DIR}/uk_co_real_logic_sbe_examples_car/Car.hpp
	${SBE_TARGET_DIR}/uk_co_real_logic_sbe_examples_car/MessageHeader.hpp
)

#
# Generation of the C++ codecs are done here as well as in the ant build as this might be used standalone
#

find_package(Java)

add_custom_command(
	OUTPUT ${SBE_MD_GENERATED_HEADERS}
	DEPENDS ${SBE_MD_SCHEMA}
	COMMAND ${Java_JAVA_EXECUTABLE} -Dsbe.output.dir=${SBE_TARGET_DIR} -Dsbe.target.language="cpp98" -jar ${SBE_JAR} ${SBE_MD_SCHEMA}
)
add_custom_target(generate_sbe_md DEPENDS ${SBE_MD_GENERATED_HEADERS})

add_custom_command(
	OUTPUT ${SBE_CAR_GENERATED_HEADERS}
	DEPENDS ${SBE_CAR_SCHEMA}
	COMMAND ${Java_JAVA_EXECUTABLE} -Dsbe.output.dir=${SBE_TARGET_DIR} -Dsbe.target.language="cpp98" -jar ${SBE_JAR} ${SBE_CAR_SCHEMA}
)
add_custom_target(generate_sbe_car DEPENDS ${SBE_CAR_GENERATED_HEADERS})

add_executable(benchlet-sbe-car-runner ${SRCS_BENCHLET_MAIN} CarBench.cpp)
add_executable(benchlet-sbe-md-runner ${SRCS_BENCHLET_MAIN} MarketDataBench.cpp)
add_dependencies(benchlet-sbe-md-runner generate_sbe_md)
add_dependencies(benchlet-sbe-car-runner generate_sbe_car)

set(DEV_DIR $ENV{HOME}/dev CACHE PATH "Path to dependencies")

set(PROTOBUF_VSN "protobuf-2.5.0")
set(PROTOBUF_HOME ${DEV_DIR}/${PROTOBUF_VSN} CACHE PATH "Path to protobuf install directory")
set(PROTOBUF_TARGET_DIR ${CMAKE_CURRENT_BINARY_DIR}/cpp)

if(RETRIEVE_AND_BUILD_PROTOBUF)
	if(EXISTS ${PROTOBUF_HOME})
		message("PROTUBUF_HOME exists.")
	else()
		file(MAKE_DIRECTORY ${DEV_DIR}/deps)
		file(DOWNLOAD https://protobuf.googlecode.com/files/${PROTOBUF_VSN}.tar.gz ${DEV_DIR}/deps/${PROTOBUF_VSN}.tar.gz)
		execute_process(
			WORKING_DIRECTORY ${DEV_DIR}/deps
			COMMAND tar xzf ${DEV_DIR}/deps/${PROTOBUF_VSN}.tar.gz			
		)
		execute_process(
			WORKING_DIRECTORY ${DEV_DIR}/deps/${PROTOBUF_VSN}
			OUTPUT_FILE ${DEV_DIR}/deps/${PROTOBUF_VSN}.configure.out
			ERROR_FILE ${DEV_DIR}/deps/${PROTOBUF_VSN}.configure.err
			COMMAND ./configure --prefix=${PROTOBUF_HOME}
		)
		execute_process(
			WORKING_DIRECTORY ${DEV_DIR}/deps/${PROTOBUF_VSN}
			OUTPUT_FILE ${DEV_DIR}/deps/${PROTOBUF_VSN}.make.out
			ERROR_FILE ${DEV_DIR}/deps/${PROTOBUF_VSN}.make.err
			COMMAND make
		)
		execute_process(
			WORKING_DIRECTORY ${DEV_DIR}/deps/${PROTOBUF_VSN}
			OUTPUT_FILE ${DEV_DIR}/deps/${PROTOBUF_VSN}.make-install.out
			ERROR_FILE ${DEV_DIR}/deps/${PROTOBUF_VSN}.make-install.err
			COMMAND make install
		)
	endif()
endif()

if(USE_PROTOBUF)
	set(PROTOBUF_CAR_SCHEMA ${CMAKE_CURRENT_SOURCE_DIR}/../resources/protobuf/car.proto)
	set(PROTOBUF_MD_SCHEMA ${CMAKE_CURRENT_SOURCE_DIR}/../resources/protobuf/fix-messages.proto)
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
