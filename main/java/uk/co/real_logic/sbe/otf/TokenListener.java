/*
 * Copyright 2012 Real Logic Ltd.
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
package uk.co.real_logic.sbe.otf;

import uk.co.real_logic.sbe.codec.java.DirectBuffer;
import uk.co.real_logic.sbe.ir.Token;

import java.util.List;

public interface TokenListener
{
    void onBeginMessage(Token token);

    void onEndMessage(Token token);

    void onEncoding(Token fieldToken, DirectBuffer buffer, int bufferIndex, Token typeToken, int actingVersion);

    void onEnum(Token fieldToken,
                DirectBuffer buffer, int bufferIndex,
                List<Token> tokens, int fromIndex, int toIndex,
                int actingVersion,
                TokenListener listener);

    void onBitSet(Token fieldToken,
                  DirectBuffer buffer, int bufferIndex,
                  List<Token> tokens, int fromIndex, int toIndex,
                  int actingVersion,
                  TokenListener listener);

    void onBeginComposite(Token fieldToken, List<Token> tokens, int fromIndex, int toIndex);

    void onEndComposite(Token fieldToken, List<Token> tokens, int fromIndex, int toIndex);

    void onBeginGroup(Token token, int groupIndex, int numInGroup);

    void onEndGroup(Token token, int groupIndex, int numInGroup);
}
