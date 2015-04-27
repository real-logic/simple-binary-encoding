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
cmake_minimum_required(VERSION 2.8 FATAL_ERROR)

include(ExternalProject)

enable_testing()

ExternalProject_Add(
	gtest
	URL ${CMAKE_CURRENT_SOURCE_DIR}/test/lib/gtest-1.7.0.zip
	URL_MD5 2d6ec8ccdf5c46b05ba54a9fd1d130d7
	INSTALL_COMMAND ""
	LOG_CONFIGURE ON
	LOG_BUILD ON
)

ExternalProject_Get_Property(gtest source_dir)
set(GTEST_SOURCE_DIR ${source_dir})
ExternalProject_Get_Property(gtest binary_dir)
set(GTEST_BINARY_DIR ${binary_dir})

find_package(Threads)

if(UNIX)
	set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++98 -fexceptions -g -Wall -Ofast -DNDEBUG")
endif()

if(APPLE)
	add_definitions(-DDarwin)
elseif(WIN32)
	add_definitions(-DWIN32)

    set(CMAKE_CXX_FLAGS_DEBUG "/MTd /Od /Zi")
    set(CMAKE_CXX_FLAGS_RELEASE "/MT")
else()
	add_definitions(-D_GNU_SOURCE)
endif()

set(LIBPREFIX "${CMAKE_STATIC_LIBRARY_PREFIX}")
set(LIBSUFFIX "${CMAKE_STATIC_LIBRARY_SUFFIX}")

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/binaries")
set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/lib")

file(GLOB SBE_JAR ${CMAKE_CURRENT_BINARY_DIR}/libs/sbe-all-*-SNAPSHOT.jar ${CMAKE_CURRENT_BINARY_DIR}/libs/sbe-all-*-RC2.jar)
if(SBE_JAR)
	message("latest SBE jar found: ${SBE_JAR}")
else()
	message(FATAL_ERROR "could not find latest SBE jar in ${CMAKE_CURRENT_BINARY_DIR}/libs")
endif()

set(CODEC_TARGET_DIR "${CMAKE_CURRENT_BINARY_DIR}/codecs")
set(CODEC_SCHEMA_DIR "${CMAKE_CURRENT_SOURCE_DIR}/test/resources")

find_package(Threads)

include_directories(
	${GTEST_SOURCE_DIR}/include
	${CMAKE_CURRENT_SOURCE_DIR}/main/cpp
	${CODEC_TARGET_DIR}
)

add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/main/cpp)
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/test/cpp)

