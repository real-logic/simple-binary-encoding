#
# Copyright 2013-2025 Real Logic Limited.
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

function(sbe_test name)
    add_executable("${name}" "${name}.cpp")
    target_include_directories("${name}"
        PRIVATE ${GTEST_SOURCE_DIR}/googletest/include
        PRIVATE ${GTEST_SOURCE_DIR}/googlemock/include
        PRIVATE ${CXX_CODEC_TARGET_DIR}
    )
    target_compile_options("${name}" PRIVATE $<$<CXX_COMPILER_ID:AppleClang>:-Werror>)
    target_compile_options("${name}" PRIVATE $<$<CXX_COMPILER_ID:Clang>:-Werror>)
    target_compile_options("${name}" PRIVATE $<$<CXX_COMPILER_ID:GNU>:-Werror>)
    target_link_libraries("${name}" sbe ${GTEST_LIBS} ${CMAKE_THREAD_LIBS_INIT})
    add_test(NAME ${name} COMMAND ${name} WORKING_DIRECTORY ${CXX_CODEC_TARGET_DIR})
    add_dependencies(${name} gtest)
    if (${ARGC} GREATER 1)
        add_dependencies(${name} ${ARGV1})
    endif ()
endfunction()

find_package(Java REQUIRED)

set(CODE_GENERATION_SCHEMA ${CODEC_SCHEMA_DIR}/code-generation-schema.xml)
set(COMPOSITE_ELEMENTS_SCHEMA ${CODEC_SCHEMA_DIR}/composite-elements-schema.xml)
set(COMPOSITE_OFFSETS_SCHEMA ${CODEC_SCHEMA_DIR}/composite-offsets-schema.xml)
set(MESSAGE_BLOCK_LENGTH_TEST ${CODEC_SCHEMA_DIR}/message-block-length-test.xml)
set(GROUP_WITH_DATA_SCHEMA ${CODEC_SCHEMA_DIR}/group-with-data-schema.xml)
set(DTO_SCHEMA ${CODEC_SCHEMA_DIR}/dto-test-schema.xml)
set(ISSUE835_SCHEMA ${CODEC_SCHEMA_DIR}/issue835.xml)
set(ISSUE889_SCHEMA ${CODEC_SCHEMA_DIR}/issue889.xml)
set(ACCESS_ORDER_SCHEMA ${CODEC_SCHEMA_DIR}/field-order-check-schema.xml)
set(VERSIONED_MESSAGE_V1_SCHEMA ${CODEC_SCHEMA_DIR}/versioned-message-v1.xml)
set(VERSIONED_MESSAGE_V2_SCHEMA ${CODEC_SCHEMA_DIR}/versioned-message-v2.xml)

set(GENERATED_CODECS
    ${CXX_CODEC_TARGET_DIR}
)

add_custom_command(
    OUTPUT ${GENERATED_CODECS}
    DEPENDS ${CODE_GENERATION_SCHEMA}
        ${COMPOSITE_ELEMENTS_SCHEMA}
        ${COMPOSITE_OFFSETS_SCHEMA}
        ${MESSAGE_BLOCK_LENGTH_TEST}
        ${GROUP_WITH_DATA_SCHEMA}
        ${DTO_SCHEMA}
        ${ISSUE835_SCHEMA}
        ${ISSUE889_SCHEMA}
        ${ACCESS_ORDER_SCHEMA}
        ${VERSIONED_MESSAGE_V1_SCHEMA}
        ${VERSIONED_MESSAGE_V2_SCHEMA}
    sbe-jar ${SBE_JAR}
    COMMAND
        ${Java_JAVA_EXECUTABLE} --add-opens java.base/jdk.internal.misc=ALL-UNNAMED
            -Dsbe.output.dir=${CXX_CODEC_TARGET_DIR}
            -Dsbe.generate.ir="true"
            -Dsbe.target.language="cpp"
            -Dsbe.generate.precedence.checks="true"
            -Dsbe.precedence.checks.flag.name="SBE_ENABLE_PRECEDENCE_CHECKS_IN_TESTS"
            -Dsbe.cpp.disable.implicit.copying="true"
            -Dsbe.cpp.generate.dtos="true"
            -jar ${SBE_JAR}
            ${CODE_GENERATION_SCHEMA}
            ${COMPOSITE_OFFSETS_SCHEMA}
            ${MESSAGE_BLOCK_LENGTH_TEST}
            ${GROUP_WITH_DATA_SCHEMA}
            ${COMPOSITE_ELEMENTS_SCHEMA}
            ${DTO_SCHEMA}
            ${ISSUE835_SCHEMA}
            ${ISSUE889_SCHEMA}
            ${ACCESS_ORDER_SCHEMA}
            ${VERSIONED_MESSAGE_V1_SCHEMA}
            ${VERSIONED_MESSAGE_V2_SCHEMA}
)

add_custom_target(codecs DEPENDS ${GENERATED_CODECS})

# codec tests
sbe_test(BoundsCheckTest codecs)
sbe_test(CodeGenTest codecs)
sbe_test(CompositeOffsetsCodeGenTest codecs)
sbe_test(CompositeOffsetsIrTest codecs)
sbe_test(MessageBlockLengthTest codecs)
sbe_test(GroupWithDataTest codecs)
sbe_test(Rc3OtfFullIrTest codecs)
sbe_test(CompositeElementsTest codecs)
sbe_test(Issue835Test codecs)
sbe_test(Issue889Test codecs)
sbe_test(FieldAccessOrderCheckTest codecs)
target_compile_definitions(FieldAccessOrderCheckTest PRIVATE SBE_ENABLE_PRECEDENCE_CHECKS_IN_TESTS)
sbe_test(VersionedMessageTest codecs)

if (CMAKE_CXX_COMPILER_ID STREQUAL "GNU")
    # Check if the GCC version supports C++17
    if (CMAKE_CXX_COMPILER_VERSION VERSION_GREATER_EQUAL "7.0")
        sbe_test(DtoTest codecs)
        target_compile_features(DtoTest PRIVATE cxx_std_17)
    endif()

    # Check if the GCC version supports C++20
    if (CMAKE_CXX_COMPILER_VERSION VERSION_GREATER_EQUAL "11.0")
        target_compile_features(CodeGenTest PRIVATE cxx_std_20)
    endif()
endif()

if (CMAKE_CXX_COMPILER_ID STREQUAL "CLang")
    # Check if CLang version supports C++17
    if (CMAKE_CXX_COMPILER_VERSION VERSION_GREATER_EQUAL "4.0")
        sbe_test(DtoTest codecs)
        target_compile_features(DtoTest PRIVATE cxx_std_17)
    endif()

    # Check if CLang version supports C++20
    if (CMAKE_CXX_COMPILER_VERSION VERSION_GREATER_EQUAL "10.0")
        target_compile_features(CodeGenTest PRIVATE cxx_std_20)
    endif()
endif()

if (CMAKE_CXX_COMPILER_ID STREQUAL "MSVC")
    # Check if MSVC version supports C++17 / C++20
    if (CMAKE_CXX_COMPILER_VERSION VERSION_GREATER_EQUAL "19.14")
        sbe_test(DtoTest codecs)
        target_compile_options(DtoTest PRIVATE /std:c++17)
        target_compile_options(CodeGenTest PRIVATE /std:c++20)
    endif()
endif()
