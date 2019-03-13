function(sbe_test name)
    add_executable("C${name}" "${name}.cpp")
    target_include_directories("C${name}"
        PRIVATE ${GTEST_SOURCE_DIR}/googletest/include
        PRIVATE ${C_CODEC_TARGET_DIR}
    )
    target_link_libraries("C${name}" ${GTEST_LIBS} ${CMAKE_THREAD_LIBS_INIT})
    add_test(NAME C${name} COMMAND C${name} WORKING_DIRECTORY ${C_CODEC_TARGET_DIR})
    add_dependencies(C${name} gtest)
    if(${ARGC} GREATER 1)
        add_dependencies(C${name} ${ARGV1})
    endif()
endfunction()

find_package(Java REQUIRED)

set(CODE_GENERATION_SCHEMA ${CODEC_SCHEMA_DIR}/code-generation-schema.xml)
set(GROUP_WITH_DATA_SCHEMA ${CODEC_SCHEMA_DIR}/group-with-data-schema.xml)

set(GENERATED_CODECS
    ${C_CODEC_TARGET_DIR}
)

add_custom_command(
    OUTPUT ${GENERATED_CODECS}
    DEPENDS sbe-jar ${SBE_JAR} ${CODE_GENERATION_SCHEMA} ${GROUP_WITH_DATA_SCHEMA}
    COMMAND ${Java_JAVA_EXECUTABLE}
        -Dsbe.output.dir=${C_CODEC_TARGET_DIR} -Dsbe.target.language="C" -jar ${SBE_JAR}
        ${CODE_GENERATION_SCHEMA}
        ${GROUP_WITH_DATA_SCHEMA}
)

add_custom_target(c_codecs DEPENDS ${GENERATED_CODECS})

# codec tests
sbe_test(BoundsCheckTest c_codecs)
sbe_test(CodeGenTest c_codecs)
sbe_test(GroupWithDataTest c_codecs)
