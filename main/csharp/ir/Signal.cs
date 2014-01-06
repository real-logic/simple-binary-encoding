/*
 * Copyright 2013 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
namespace Adaptive.SimpleBinaryEncoding.ir
{

    /// <summary>
    /// Signal the <seealso cref="Token"/> role within a stream of tokens. These signals begin/end various entities
    /// such as fields, composites, messages, repeating groups, enumerations, bitsets, etc.
    /// </summary>
    public enum Signal
    {
        /// <summary>
        /// Denotes the beginning of a message 
        /// </summary>
        BeginMessage,

        /// <summary>
        /// Denotes the end of a message 
        /// </summary>
        EndMessage,

        /// <summary>
        /// Denotes the beginning of a composite
        /// </summary>
        BeginComposite,

        /// <summary>
        /// Denotes the end of a composite 
        /// </summary>
        EndComposite,

        /// <summary>
        /// Denotes the beginning of a field 
        /// </summary>
        BeginField,

        /// <summary>
        /// Denotes the end of a field 
        /// </summary>
        EndField,

        /// <summary>
        /// Denotes the beginning of a repeating group 
        /// </summary>
        BeginGroup,

        /// <summary>
        /// Denotes the end of a repeating group 
        /// </summary>
        EndGroup,

        /// <summary>
        /// Denotes the beginning of an enumeration 
        /// </summary>
        BeginEnum,

        /// <summary>
        /// Denotes a value of an enumeration 
        /// </summary>
        ValidValue,

        /// <summary>
        /// Denotes the end of an enumeration 
        /// </summary>
        EndEnum,

        /// <summary>
        /// Denotes the beginning of a bitset 
        /// </summary>
        BeginSet,

        /// <summary>
        /// Denotes a bit value (choice) of a bitset 
        /// </summary>
        Choice,

        /// <summary>
        /// Denotes the end of a bitset 
        /// </summary>
        EndSet,

        /// <summary>
        /// Denotes the beginning of a variable data block 
        /// </summary>
        BeginVarData,

        /// <summary>
        /// Denotes the end of a variable data block 
        /// </summary>
        EndVarData,

        /// <summary>
        /// Denotes the <seealso cref="Token"/> is an encoding 
        /// </summary>
        Encoding
    }
}