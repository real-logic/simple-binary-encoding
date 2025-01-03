/*
 * Copyright 2013-2025 Real Logic Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.sbe.generation.rust;

import uk.co.real_logic.sbe.ir.Ir;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.stream.Stream;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static uk.co.real_logic.sbe.generation.rust.RustGenerator.BUF_LIFETIME;
import static uk.co.real_logic.sbe.generation.rust.RustGenerator.READ_BUF_TYPE;
import static uk.co.real_logic.sbe.generation.rust.RustGenerator.WRITE_BUF_TYPE;
import static uk.co.real_logic.sbe.generation.rust.RustUtil.TYPE_NAME_BY_PRIMITIVE_TYPE_MAP;
import static uk.co.real_logic.sbe.generation.rust.RustUtil.indent;
import static uk.co.real_logic.sbe.generation.rust.RustUtil.rustTypeName;
import static uk.co.real_logic.sbe.generation.rust.RustUtil.toLowerSnakeCase;

/**
 * Generates `lib.rs` specific code.
 */
class LibRsDef
{
    private final RustOutputManager outputManager;
    private final ByteOrder byteOrder;
    private final String schemaVersionType;

    /**
     * Create a new 'lib.rs' for the library being generated.
     *
     * @param outputManager     for generating the codecs to.
     * @param byteOrder         for the Encoding.
     * @param schemaVersionType for acting_version type.
     */
    LibRsDef(
        final RustOutputManager outputManager,
        final ByteOrder byteOrder,
        final String schemaVersionType)
    {
        this.outputManager = outputManager;
        this.byteOrder = byteOrder;
        this.schemaVersionType = schemaVersionType;
    }

    void generate(final Ir ir) throws IOException
    {
        try (Writer libRs = outputManager.createOutput("lib"))
        {
            indent(libRs, 0, "#![forbid(unsafe_code)]\n");
            indent(libRs, 0, "#![allow(clippy::all)]\n");
            indent(libRs, 0, "#![allow(non_camel_case_types)]\n\n");
            indent(libRs, 0, "#![allow(ambiguous_glob_reexports)]\n\n");
            indent(libRs, 0, "use ::core::{convert::TryInto};\n\n");

            final ArrayList<String> modules = new ArrayList<>();
            try (Stream<Path> walk = Files.walk(outputManager.getSrcDirPath()))
            {
                walk
                    .filter(Files::isRegularFile)
                    .map((path) -> path.getFileName().toString())
                    .filter((fileName) -> fileName.endsWith(".rs"))
                    .filter((fileName) -> !fileName.equals("lib.rs"))
                    .map((fileName) -> fileName.substring(0, fileName.length() - 3))
                    .sorted()
                    .forEach(modules::add);
            }

            // add modules
            for (final String mod : modules)
            {
                indent(libRs, 0, "pub mod %s;\n", toLowerSnakeCase(mod));
            }
            indent(libRs, 0, "\n");

            generateSbeSchemaConsts(libRs, ir);

            generateSbeErrorEnum(libRs);
            generateEitherEnum(libRs);

            generateEncoderTraits(libRs);
            generateDecoderTraits(schemaVersionType, libRs);

            generateReadBuf(libRs, byteOrder);
            generateWriteBuf(libRs, byteOrder);
        }
    }

    static void generateEncoderTraits(final Writer writer) throws IOException
    {
        indent(writer, 0, "pub trait Writer<'a>: Sized {\n");
        indent(writer, 1, "fn get_buf_mut(&mut self) -> &mut WriteBuf<'a>;\n");
        indent(writer, 0, "}\n\n");

        indent(writer, 0, "pub trait Encoder<'a>: Writer<'a> {\n");
        indent(writer, 1, "fn get_limit(&self) -> usize;\n");
        indent(writer, 1, "fn set_limit(&mut self, limit: usize);\n");
        indent(writer, 0, "}\n\n");
    }

    static void generateDecoderTraits(final String schemaVersionType, final Writer writer) throws IOException
    {
        indent(writer, 0, "pub trait ActingVersion {\n");
        indent(writer, 1, "fn acting_version(&self) -> %s;\n", schemaVersionType);
        indent(writer, 0, "}\n\n");

        indent(writer, 0, "pub trait Reader<'a>: Sized {\n");
        indent(writer, 1, "fn get_buf(&self) -> &ReadBuf<'a>;\n");
        indent(writer, 0, "}\n\n");

        indent(writer, 0, "pub trait Decoder<'a>: Reader<'a> {\n");
        indent(writer, 1, "fn get_limit(&self) -> usize;\n");
        indent(writer, 1, "fn set_limit(&mut self, limit: usize);\n");
        indent(writer, 0, "}\n\n");
    }

    static void generateSbeSchemaConsts(final Writer writer, final Ir ir) throws IOException
    {
        final String schemaIdType = rustTypeName(ir.headerStructure().schemaIdType());
        final String schemaVersionType = rustTypeName(ir.headerStructure().schemaVersionType());
        final String semanticVersion = ir.semanticVersion() == null ? "" : ir.semanticVersion();

        indent(writer, 0, "pub const SBE_SCHEMA_ID: %s = %d;\n", schemaIdType, ir.id());
        indent(writer, 0, "pub const SBE_SCHEMA_VERSION: %s = %d;\n", schemaVersionType, ir.version());
        indent(writer, 0, "pub const SBE_SEMANTIC_VERSION: &str = \"%s\";\n\n", semanticVersion);
    }

    static void generateSbeErrorEnum(final Writer writer) throws IOException
    {
        indent(writer, 0, "pub type SbeResult<T> = core::result::Result<T, SbeErr>;\n\n");
        indent(writer, 0, "#[derive(Clone, Copy, Debug, PartialEq, Eq, PartialOrd, Ord, Hash)]\n");
        indent(writer, 0, "pub enum SbeErr {\n");
        indent(writer, 1, "ParentNotSet,\n");
        indent(writer, 0, "}\n");

        indent(writer, 0, "impl core::fmt::Display for SbeErr {\n");
        indent(writer, 1, "#[inline]\n");
        indent(writer, 1, "fn fmt(&self, f: &mut core::fmt::Formatter<'_>) -> core::fmt::Result {\n");
        indent(writer, 2, "write!(f, \"{self:?}\")\n");
        indent(writer, 1, "}\n");
        indent(writer, 0, "}\n");

        indent(writer, 0, "impl std::error::Error for SbeErr {}\n\n");
    }

    static void generateEitherEnum(final Writer writer) throws IOException
    {
        indent(writer, 0, "#[derive(Clone, Copy, Debug, PartialEq, Eq, PartialOrd, Ord, Hash)]\n");
        indent(writer, 0, "pub enum Either<L, R> {\n");
        indent(writer, 1, "Left(L),\n");
        indent(writer, 1, "Right(R),\n");
        indent(writer, 0, "}\n\n");
    }

    static void generateReadBuf(final Appendable writer, final ByteOrder byteOrder) throws IOException
    {
        indent(writer, 0, "#[derive(Clone, Copy, Debug, Default)]\n");
        indent(writer, 0, "pub struct %s<%s> {\n", READ_BUF_TYPE, BUF_LIFETIME);
        RustUtil.indent(writer, 1, "data: &%s [u8],\n", BUF_LIFETIME);
        indent(writer, 0, "}\n");

        // impl Reader...
        indent(writer, 0, "impl<%s> Reader<%1$s> for %2$s<%1$s> {\n", BUF_LIFETIME, READ_BUF_TYPE);
        indent(writer, 1, "#[inline]\n");
        indent(writer, 1, "fn get_buf(&self) -> &ReadBuf<%s> {\n", BUF_LIFETIME);
        indent(writer, 2, "self\n");
        indent(writer, 1, "}\n");
        indent(writer, 0, "}\n");

        // impl ReadBuf ...
        indent(writer, 0, "#[allow(dead_code)]\n");
        indent(writer, 0, "impl<%s> %s<%s> {\n", BUF_LIFETIME, READ_BUF_TYPE, BUF_LIFETIME);
        indent(writer, 1, "#[inline]\n");
        indent(writer, 1, "pub fn new(data: &%s [u8]) -> Self {\n", BUF_LIFETIME);
        indent(writer, 2, "Self { data }\n");
        indent(writer, 1, "}\n\n");

        indent(writer, 1, "#[inline]\n");
        indent(writer, 1, "pub(crate) fn get_bytes_at<const N: usize>(slice: &[u8], index: usize) -> [u8; N] {\n");
        indent(writer, 2, "slice[index..index+N].try_into().expect(\"slice with incorrect length\")\n");
        indent(writer, 1, "}\n");

        final LinkedHashSet<String> uniquePrimitiveTypes
            = new LinkedHashSet<>(TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.values());
        final String endianness = byteOrder == LITTLE_ENDIAN ? "le" : "be";

        uniquePrimitiveTypes.remove("u8");
        indent(writer, 0, "\n");
        indent(writer, 1, "#[inline]\n");
        indent(writer, 1, "pub fn get_u8_at(&self, index: usize) -> u8 {\n");
        indent(writer, 2, "self.data[index]\n");
        indent(writer, 1, "}\n");

        for (final String primitiveType : uniquePrimitiveTypes)
        {
            // get_<primitive>_at
            indent(writer, 0, "\n");
            indent(writer, 1, "#[inline]\n");
            indent(writer, 1, "pub fn get_%1$s_at(&self, index: usize) -> %1$s {\n", primitiveType);
            indent(writer, 2, "%s::from_%s_bytes(Self::get_bytes_at(self.data, index))\n", primitiveType, endianness);
            indent(writer, 1, "}\n");
        }

        // get_slice_at
        indent(writer, 0, "\n");
        indent(writer, 1, "#[inline]\n");
        indent(writer, 1, "pub fn get_slice_at(&self, index: usize, len: usize) -> &[u8] {\n");
        indent(writer, 2, "&self.data[index..index+len]\n");
        indent(writer, 1, "}\n\n");

        writer.append("}\n");
    }

    static void generateWriteBuf(final Writer writer, final ByteOrder byteOrder) throws IOException
    {
        indent(writer, 0, "\n");
        indent(writer, 0, "#[derive(Debug, Default)]\n");
        indent(writer, 0, "pub struct %s<%s> {\n", WRITE_BUF_TYPE, BUF_LIFETIME);
        indent(writer, 1, "data: &%s mut [u8],\n", BUF_LIFETIME);
        indent(writer, 0, "}\n");

        indent(writer, 0, "impl<%s> %s<%s> {\n", BUF_LIFETIME, WRITE_BUF_TYPE, BUF_LIFETIME);
        indent(writer, 1, "pub fn new(data: &%s mut [u8]) -> Self {\n", BUF_LIFETIME);
        indent(writer, 2, "Self { data }\n");
        indent(writer, 1, "}\n\n");

        indent(writer, 1, "#[inline]\n");
        indent(writer, 1,
            "pub fn put_bytes_at<const COUNT: usize>(&mut self, index: usize, bytes: &[u8; COUNT]) -> usize {\n");
        indent(writer, 2, "self.data[index..index + COUNT].copy_from_slice(bytes);\n");
        indent(writer, 2, "COUNT\n");
        indent(writer, 1, "}\n\n");

        final LinkedHashSet<String> uniquePrimitiveTypes
            = new LinkedHashSet<>(TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.values());
        final String endianness = byteOrder == LITTLE_ENDIAN ? "le" : "be";

        uniquePrimitiveTypes.remove("u8");
        indent(writer, 1, "#[inline]\n");
        indent(writer, 1, "pub fn put_u8_at(&mut self, index: usize, value: u8) {\n");
        indent(writer, 2, "self.data[index] = value;\n");
        indent(writer, 1, "}\n\n");

        for (final String primitiveType : uniquePrimitiveTypes)
        {
            // put_<primitive>_at
            indent(writer, 1, "#[inline]\n");
            indent(writer, 1, "pub fn put_%1$s_at(&mut self, index: usize, value: %1$s) {\n", primitiveType);
            indent(writer, 2, "self.put_bytes_at(index, &%s::to_%s_bytes(value));\n", primitiveType, endianness);
            indent(writer, 1, "}\n\n");
        }

        // put_slice_at
        indent(writer, 1, "#[inline]\n");
        indent(writer, 1, "pub fn put_slice_at(&mut self, index: usize, src: &[u8]) -> usize {\n");
        indent(writer, 2, "let len = src.len();\n");
        indent(writer, 2, "let dest = self.data.split_at_mut(index).1.split_at_mut(len).0;\n");
        indent(writer, 2, "dest.clone_from_slice(src);\n");
        indent(writer, 2, "len\n");
        indent(writer, 1, "}\n");
        indent(writer, 0, "}\n");

        // impl From<WriteBuf> for &[u8]
        indent(writer, 0, "impl<%s> From<&%1$s mut %2$s<%1$s>> for &%1$s mut [u8] {\n", BUF_LIFETIME, WRITE_BUF_TYPE);
        indent(writer, 1, "#[inline]\n");
        indent(writer, 1, "fn from(buf: &%s mut %2$s<%1$s>) -> &%1$s mut [u8] {\n", BUF_LIFETIME, WRITE_BUF_TYPE);
        indent(writer, 2, "buf.data\n");
        indent(writer, 1, "}\n");
        indent(writer, 0, "}\n\n");
    }
}
