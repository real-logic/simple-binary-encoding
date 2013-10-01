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
package api;

import java.nio.ByteBuffer;

public class ObserverStyleExample
{
    private final ByteBuffer buffer = ByteBuffer.allocateDirect(4096);

    private final Transport transport = new Transport();

    private final Listener listener = new Listener();

    public void simpleDecode()
    {
        buffer.clear();
        transport.receive(buffer);

        /* reset Listener object, find start of serialized IR, and prepare for decoding */
        listener.resetForDecode(buffer)
            /* actually bind the callbacks and start decoding */
            .subscribe(new Listener.OnNext()
            {
                int indentLevel = 0;

                @Override
                public void onNext(final Field field)
                {
                    /* print out the indentLevel, etc. */
                    System.out.println("[" + indentLevel + "] " + field);
                }

                @Override
                public void onNext(final Group group)
                {
                    if (group.isStart())
                    {
                        indentLevel++;
                    }
                    else
                    {
                        indentLevel--;
                    }
                }

                @Override
                public void onNext(final Data data)
                {
                    System.out.println(data);
                }
            });
    }

    public void decodeWithOnError()
    {
        buffer.clear();
        transport.receive(buffer);

        listener.resetForDecode(buffer)
            .subscribe(new Listener.OnNext()
            {
                @Override
                public void onNext(final Field field)
                {
                }

                @Override
                public void onNext(final Group group)
                {
                }

                @Override
                public void onNext(final Data data)
                {
                }
            }, new Listener.OnError()
            {
                @Override
                public void onError(final Throwable thrown)
                {
                }
            });
    }

    public void decodeWithOnErrorAndOnCompleted()
    {
        buffer.clear();
        transport.receive(buffer);

        listener.resetForDecode(buffer)
            .subscribe(new Listener.OnNext()
            {
                @Override
                public void onNext(final Field field)
                {
                }

                @Override
                public void onNext(final Group group)
                {
                }

                @Override
                public void onNext(final Data data)
                {
                }
            }, new Listener.OnError()
            {
                @Override
                public void onError(final Throwable thrown)
                {
                }
            }, new Listener.OnCompleted()
            {
                @Override
                public void onCompleted()
                {
                }
            });
    }
}
