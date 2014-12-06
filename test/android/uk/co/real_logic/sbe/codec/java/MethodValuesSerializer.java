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
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

public class MethodValuesSerializer
{
    private final MethodSelector methodSelector;

    public MethodValuesSerializer(final MethodSelector methodSelector)
    {
        this.methodSelector = methodSelector;
    }

    public JsonElement serialize(final Object object)
        throws InvocationTargetException, IllegalAccessException
    {
        return serialize(object, true);
    }

    JsonElement serialize(final Object object, final boolean visitIterable)
        throws InvocationTargetException, IllegalAccessException
    {
        if (object == null)
        {
            return JsonNull.INSTANCE;
        }

        final Class<?> clazz = object.getClass();
        if (Number.class.isAssignableFrom(clazz))
        {
            return new JsonPrimitive((Number)object);
        }
        else if (clazz == String.class)
        {
            return new JsonPrimitive((String)object);
        }
        else if (clazz == Boolean.class)
        {
            return new JsonPrimitive((Boolean)object);
        }
        else if (object instanceof Enum)
        {
            return new JsonPrimitive(((Enum<?>)object).name());
        }
        else if (clazz.isArray())
        {
            final JsonArray result = new JsonArray();
            final int len = Array.getLength(object);
            for (int i = 0; i < len; ++i)
            {
                result.add(serialize(Array.get(object, i)));
            }

            return result;
        }
        else if (visitIterable && Iterable.class.isAssignableFrom(clazz))
        {
            final Iterator<?> iter = ((Iterable<?>)object).iterator();
            final JsonArray result = new JsonArray();
            while (iter.hasNext())
            {
                result.add(serialize(iter.next(), false));
            }

            return result;
        }
        else if (Map.class.isAssignableFrom(clazz))
        {
            final JsonObject result = new JsonObject();
            for (final Map.Entry<?, ?> entry : ((Map<?, ?>)object).entrySet())
            {
                result.add(entry.getKey().toString(), serialize(entry.getValue()));
            }

            return result;
        }
        else
        {
            return serializeObject(object, clazz);
        }
    }

    private JsonElement serializeObject(final Object object, final Class<?> clazz)
        throws InvocationTargetException, IllegalAccessException
    {
        final JsonObject jsonObject = new JsonObject();
        for (final Method method : methodSelector.select(clazz))
        {
            final Object result = method.invoke(object);
            if (result == object)
            {
                continue;
            }

            jsonObject.add(method.getName(), serialize(result));
        }

        return jsonObject;
    }
}
