/* -*- mode: java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil -*- */
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
package otf_api;

import java.nio.ByteBuffer;

public class Listener
{
    public Listener resetForDecode(final ByteBuffer dataBuffer)
    {
        return this;
    }

    public Listener subscribe(final OnNext onNext)
    {
        return this;
    }

    public Listener subscribe(final OnNext onNext, final OnError onError)
    {
        return this;
    }

    public Listener subscribe(final OnNext onNext, final OnError onError, final OnCompleted onCompleted)
    {
        return this;
    }

    public static interface OnNext
    {
        public void onNext(final Field field);
        public void onNext(final Group group);
        public void onNext(final Data data);
    }

    public static interface OnError
    {
        public void onError(final Throwable thrown);
    }

    public static interface OnCompleted
    {
        public void onCompleted();
    }
}
