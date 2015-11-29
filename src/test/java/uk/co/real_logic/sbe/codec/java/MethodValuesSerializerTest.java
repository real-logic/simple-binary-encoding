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
package uk.co.real_logic.sbe.codec.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MethodValuesSerializerTest
{
    enum E1
    {
        A, B
    }

    static List<Class<?>> order = new ArrayList<>();

    @GroupOrder({X.Y.class, X.Z.class})
    static class X
    {
        class Z
        {
            public boolean publicBoolean()
            {
                order.add(Z.class);
                return true;
            }
        }

        class Y
        {
            public boolean publicBoolean()
            {
                order.add(Y.class);
                return true;
            }
        }

        class S
        {
            public boolean publicBoolean()
            {
                return true;
            }
        }

        public Z publicZ()
        {
            return new Z();
        }

        public Y publicY()
        {
            return new Y();
        }

        public List<S> publicListS()
        {
            return Arrays.asList(new S());
        }

        public E1 publicEnum()
        {
            return E1.A;
        }

        public int[] publicIntArr()
        {
            return new int[]{1};
        }

        public String publicString()
        {
            return "hello";
        }

        public int publicInt()
        {
            return 1;
        }

        public X publicSelfIgnored()
        {
            return X.this;
        }

        public static int staticPublicIntIgnored()
        {
            return 23;
        }
    }

    @Test
    public void test() throws InvocationTargetException, IllegalAccessException
    {
        final MethodValuesSerializer serializer = new MethodValuesSerializer(
            new MethodSelector(MethodSelector.objectAndIteratorMethods()));

        final JsonObject expected = new JsonObject();
        expected.add("publicInt", new JsonPrimitive(1));
        expected.add("publicString", new JsonPrimitive("hello"));
        expected.add("publicEnum", new JsonPrimitive("A"));

        final JsonArray arr = new JsonArray();
        arr.add(new JsonPrimitive(1));
        expected.add("publicIntArr", arr);

        final JsonArray ss = new JsonArray();
        final JsonObject s = new JsonObject();
        s.add("publicBoolean", new JsonPrimitive(true));
        ss.add(s);
        expected.add("publicListS", ss);

        final JsonObject z = new JsonObject();
        z.add("publicBoolean", new JsonPrimitive(true));
        expected.add("publicZ", z);

        final JsonObject y = new JsonObject();
        y.add("publicBoolean", new JsonPrimitive(true));
        expected.add("publicY", y);
        final JsonElement got = serializer.serialize(new X());

        Assert.assertEquals(expected, got);
        Assert.assertEquals(Arrays.asList(X.Y.class, X.Z.class), order);
    }
}
