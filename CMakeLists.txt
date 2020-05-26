#
# Copyright 2013-2020 Real Logic Limited.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

cmake_minimum_required(VERSION 3.1.3 FATAL_ERROR)
cmake_policy(VERSION 3.1.3)

file(STRINGS version.txt SBE_VERSION_TXT LIMIT_COUNT 1 REGEX "^[0-9]+(\\.[0-9]+)+")
string(REGEX REPLACE "^([0-9]+(\\.[0-9]+)+).*$" "\\1" SBE_VERSION_NUMERIC "${SBE_VERSION_TXT}")

project("sbe" VERSION "${SBE_VERSION_NUMERIC}")

include(ExternalProject)
include(CheckLibraryExists)

if (${CMAKE_SOURCE_DIR} STREQUAL ${CMAKE_CURRENT_SOURCE_DIR})
    set(STANDALONE_BUILD TRUE)
endif ()

option(SBE_TESTS "Enable tests" ${STANDALONE_BUILD})
option(SBE_BUILD_SAMPLES "Enable building the sample projects" ${STANDALONE_BUILD})
option(SBE_BUILD_BENCHMARKS "Enable building the benchmarks" ${STANDALONE_BUILD})

unset(STANDALONE_BUILD)

if (SBE_TESTS)
    enable_testing()
endif ()

# default built type is Release
if (NOT CMAKE_BUILD_TYPE)
    set(CMAKE_BUILD_TYPE "Release" CACHE STRING "Choose the type of build" FORCE)
endif (NOT CMAKE_BUILD_TYPE)

if (NOT DEFINED CMAKE_CXX_STANDARD)
    set(CMAKE_CXX_STANDARD 11)
endif ()

if (NOT DEFINED CMAKE_CXX_EXTENSIONS)
    set(CMAKE_CXX_EXTENSIONS OFF)
endif ()

if (NOT DEFINED CMAKE_C_STANDARD)
    set(CMAKE_C_STANDARD 11)
endif ()

if (NOT DEFINED CMAKE_C_EXTENSIONS)
    set(CMAKE_C_EXTENSIONS OFF)
endif ()

function (aeron_extract)
    set(TMP_EXTRACTED ${ARGV0})
    set(TMP_EXTRACTED_VALUE ${${TMP_EXTRACTED}})
    message(STATUS "Extract ${ARGV1} with ${TMP_EXTRACTED}: ${TMP_EXTRACTED_VALUE}")
    if (NOT TMP_EXTRACTED_VALUE)
        file(REMOVE_RECURSE ${ARGV2}/${ARGV3})
        file(MAKE_DIRECTORY ${ARGV2})
        execute_process(COMMAND
            ${CMAKE_COMMAND} -E tar -xf ${ARGV1}
            WORKING_DIRECTORY ${ARGV2})
        set(${TMP_EXTRACTED} ON CACHE BOOL "")
    endif ()
endfunction()

if (SBE_TESTS)
    aeron_extract(GOOGLETEST_EXTRACTED ${CMAKE_CURRENT_SOURCE_DIR}/cppbuild/googletest-release-1.10.0.zip ${CMAKE_CURRENT_BINARY_DIR}/cppbuild googletest-release-1.10.0)
    set(GTEST_SOURCE_DIR ${CMAKE_CURRENT_BINARY_DIR}/cppbuild/googletest-release-1.10.0)
    set(gtest_force_shared_crt ON CACHE BOOL "")
    add_subdirectory(${GTEST_SOURCE_DIR} ${CMAKE_CURRENT_BINARY_DIR}/3rd/gmock EXCLUDE_FROM_ALL)
    set(GTEST_LIBS gmock gtest gtest_main)
endif ()

find_package(Threads)

if (UNIX)
    add_compile_options(-Wall -Wpedantic -Wextra -Wno-unused-parameter)
endif ()

if (APPLE)
    # -Wall doesn't enable everything we want to see
    add_compile_options(-Wsign-compare)
    add_definitions(-DDarwin)
elseif (MSVC)
    add_definitions(-DWIN32)

    set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -Od -Zi -wd4996")
    set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -wd4996")
else ()
    add_definitions(-D_GNU_SOURCE)
endif ()

set(LIBPREFIX "${CMAKE_STATIC_LIBRARY_PREFIX}")
set(LIBSUFFIX "${CMAKE_STATIC_LIBRARY_SUFFIX}")

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/binaries")
set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/lib")

set(SBE_JAR "${CMAKE_CURRENT_SOURCE_DIR}/sbe-all/build/libs/sbe-all-${SBE_VERSION_TXT}.jar")

file(GLOB_RECURSE SBE_SOURCES
    "${CMAKE_CURRENT_SOURCE_DIR}/sbe-tool/src/main/java/*.java"
)

add_custom_command(OUTPUT ${SBE_JAR}
    COMMAND ./gradlew assemble -x javadoc
    DEPENDS ${SBE_SOURCES}
    WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
    COMMENT "Generating SBE jar"
)

add_custom_target(sbe-jar
    DEPENDS ${SBE_JAR}
)

set(CODEC_TARGET_DIR "${CMAKE_CURRENT_BINARY_DIR}/generated")
set(C_CODEC_TARGET_DIR "${CODEC_TARGET_DIR}/c")
set(CXX_CODEC_TARGET_DIR "${CODEC_TARGET_DIR}/cpp")
set(CODEC_SCHEMA_DIR "${CMAKE_CURRENT_SOURCE_DIR}/sbe-tool/src/test/resources")
set(CODEC_PERF_SCHEMA_DIR "${CMAKE_CURRENT_SOURCE_DIR}/sbe-benchmarks/src/main/resources")
set(CODEC_EXAMPLES_SCHEMA_DIR "${CMAKE_CURRENT_SOURCE_DIR}/sbe-samples/src/main/resources")

add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/sbe-tool/src/main/cpp)

if (SBE_TESTS)
    add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/sbe-tool/src/test/c)
    add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/sbe-tool/src/test/cpp)
endif ()

if (SBE_BUILD_SAMPLES)
    add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/sbe-samples/src/main/cpp)
endif ()

if (SBE_BUILD_BENCHMARKS)
    add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/sbe-benchmarks/src/main/cpp)
endif ()
