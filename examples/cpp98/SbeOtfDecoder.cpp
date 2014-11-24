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
#if defined(WIN32) || defined(_WIN32)
#include <sys/types.h>
#include <sys/stat.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#else
#include <sys/types.h>
#include <sys/uio.h>
#include <unistd.h>
#include <sys/stat.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#endif /* WIN32 */

#include <iostream>
#include <string>

#include "otf_api/Listener.h"
#include "otf_api/IrCollection.h"

using namespace sbe::on_the_fly;
using namespace uk_co_real_logic_sbe_ir_generated;

// class to encapsulate Ir repository as well as Ir callback for dispatch
class IrRepo : public IrCollection, public Ir::Callback
{
public:
    // save a reference to the Listener so we can print out the offset
    IrRepo(Listener &listener) : listener_(listener) {};

    virtual Ir *irForTemplateId(const int templateId, const int schemaVersion)
    {
        std::cout << "Message lookup id=" << templateId << " version=" << schemaVersion << " offset " << listener_.bufferOffset() << std::endl;

        // lookup in IrCollection the IR for the template ID and version
        return (Ir *)IrCollection::message(templateId, schemaVersion);
    };

private:
    Listener &listener_;
};

// class to encapsulate all the callbacks
class CarCallbacks : public OnNext, public OnError, public OnCompleted
{
public:
    // save reference to listener for printing offset
    CarCallbacks(Listener &listener) : listener_(listener) , indent_(0) {};

    // callback for when a field is encountered
    virtual int onNext(const Field &f)
    {
        std::cout << "Field name=\"" << f.fieldName() << "\" id=" << f.schemaId();

        if (f.isComposite())
        {
            std::cout << ", composite name=\"" << f.compositeName() << "\"";
        }
        std::cout << std::endl;

        if (f.isEnum())
        {
            std::cout << " Enum [" << f.validValue() << "]";
            printEncoding(f, 0); // print the encoding. Index is 0.
        }
        else if (f.isSet())
        {
            std::cout << " Set ";
            // print the various names for the bits that are set
            for (std::vector<std::string>::iterator it = ((std::vector<std::string>&)f.choices()).begin(); it != f.choices().end(); ++it)
            {
                std::cout << "[" << *it << "]";
            }

            printEncoding(f, 0); // print the encoding. Index is 0.
        }
        else if (f.isVariableData())
        {
            // index 0 is the length field type, value, etc.
            // index 1 is the actual variable length data

            std::cout << " Variable Data length=" << f.length(1);

            char tmp[256];
            f.getArray(1, tmp, 0, f.length(1));  // copy the data
            std::cout << " value=\"" << std::string(tmp, f.length(1)) << "\"";

            std::cout << " presence=" << presenceStr(f.presence(1));
            // printing out meta attributes
//            std::cout << " epoch=\"" << f.getMetaAttribute(Field::EPOCH, 1) << "\"";
//            std::cout << " timeUnit=\"" << f.getMetaAttribute(Field::TIME_UNIT, 1) << "\"";
//            std::cout << " semanticType=\"" << f.getMetaAttribute(Field::SEMANTIC_TYPE, 1) << "\"";
            std::cout << std::endl;
        }
        else // if not enum, set, or var data, then just normal encodings, but could be composite
        {
            for (int i = 0, size = f.numEncodings(); i < size; i++)
            {
                printEncoding(f, i);
            }
        }

        return 0;
    };

    // callback for when a group is encountered
    virtual int onNext(const Group &g)
    {
        // group started
        if (g.event() == Group::START)
        {
            std::cout << "Group name=\"" << g.name() << "\" id=\"" << g.schemaId() << "\" start (";
            std::cout << g.iteration() << "/" << g.numInGroup() - 1 << "):" << "\n";

            if (g.iteration() == 1)
            {
                indent_++;
            }
        }
        else if (g.event() == Group::END)  // group ended
        {
            std::cout << "Group name=\"" << g.name() << "\" id=\"" << g.schemaId() << "\" end (";
            std::cout << g.iteration() << "/" << g.numInGroup() - 1 << "):" << "\n";

            if (g.iteration() == g.numInGroup() - 1)
            {
                indent_--;
            }
        }
        return 0;
    };

    // callback for when an error is encountered
    virtual int onError(const Error &e)
    {
        std::cout << "Error " << e.message() << " at offset " << listener_.bufferOffset() << "\n";
        return 0;
    };

    // callback for when decoding is completed
    virtual int onCompleted()
    {
        std::cout << "Completed" << "\n";
        return 0;
    };

protected:

    // print out details of an encoding
    void printEncoding(const Field &f, int index)
    {
        std::cout << " name=\"" << f.encodingName(index) << "\" length=" << f.length(index);
        switch (f.primitiveType(index))
        {
            case Ir::CHAR:
                if (f.length(index) == 1)
                {
                    std::cout << " type=CHAR value=\"" << (char)f.getUInt(index) << "\"";
                }
                else
                {
                    char tmp[1024];

                    // copy data to temp array and print it out.
                    f.getArray(index, tmp, 0, f.length(index));
                    std::cout << " type=CHAR value=\"" << std::string(tmp, f.length(index)) << "\"";
                }
                break;
            case Ir::INT8:
                std::cout << " type=INT8 value=\"" << f.getInt(index) << "\"";
                break;
            case Ir::INT16:
                std::cout << " type=INT16 value=\"" << f.getInt(index) << "\"";
                break;
            case Ir::INT32:
                if (f.length() == 1)
                {
                    std::cout << " type=INT32 value=\"" << f.getInt(index) << "\"";
                }
                else
                {
                    char tmp[1024];

                    // copy data to temp array and print it out.
                    f.getArray(index, tmp, 0, f.length(index));
                    std::cout << " type=INT32 value=";
                    for (int i = 0, size = f.length(index); i < size; i++)
                    {
                        std::cout << "{" << *((int32_t *)(tmp + (sizeof(int32_t) * i))) << "}";
                    }
                }
                break;
            case Ir::INT64:
                std::cout << " type=INT64 value=\"" << f.getInt(index) << "\"";
                break;
            case Ir::UINT8:
                std::cout << " type=UINT8 value=\"" << f.getUInt(index) << "\"";
                break;
            case Ir::UINT16:
                std::cout << " type=UINT16 value=\"" << f.getUInt(index) << "\"";
                break;
            case Ir::UINT32:
                std::cout << " type=UINT32 value=\"" << f.getUInt(index) << "\"";
                break;
            case Ir::UINT64:
                std::cout << " type=UINT64 value=\"" << f.getUInt(index) << "\"";
                break;
            case Ir::FLOAT:
                std::cout << " type=FLOAT value=\"" << f.getDouble(index) << "\"";
                break;
            case Ir::DOUBLE:
                std::cout << " type=DOUBLE value=\"" << f.getDouble(index) << "\"";
                break;
            default:
                break;
        }
        std::cout << " presence=" << presenceStr(f.presence(index));
        // printing out meta attributes for encodings
//        std::cout << " epoch=\"" << f.getMetaAttribute(Field::EPOCH, index) << "\"";
//        std::cout << " timeUnit=\"" << f.getMetaAttribute(Field::TIME_UNIT, index) << "\"";
//        std::cout << " semanticType=\"" << f.getMetaAttribute(Field::SEMANTIC_TYPE, index) << "\"";
        std::cout << std::endl;
    }

    // print presence
    const char *presenceStr(Ir::TokenPresence presence)
    {
        switch (presence)
        {
            case Ir::SBE_REQUIRED:
                return "REQUIRED";
                break;

            case Ir::SBE_OPTIONAL:
                return "OPTIONAL";
                break;

            case Ir::SBE_CONSTANT:
                return "CONSTANT";
                break;

            default:
                return "UNKNOWN";
                break;

        }
    }

private:
    Listener &listener_;
    int indent_;
};

// helper function to read in file and save to a buffer
char *readFileIntoBuffer(const char *filename, int *length)
{
    struct stat fileStat;

    if (::stat(filename, &fileStat) != 0)
    {
        return NULL;
    }

    *length = fileStat.st_size;

    std::cout << "Encoded filename " << filename << " length " << *length << std::endl;

    char *buffer = new char[*length];

    FILE *fptr = ::fopen(filename, "r");
    int remaining = *length;

    if (fptr == NULL)
    {
        return NULL;
    }

    int fd = fileno(fptr);
    while (remaining > 0)
    {
        int sz = ::read(fd, buffer + (*length - remaining), (4098 < remaining) ? 4098 : remaining);
        remaining -= sz;
        if (sz < 0)
        {
            break;
        }
    }
    fclose(fptr);

    return buffer;
}

void usage(const char *argv0)
{
    std::cout << argv0 << " irFile messageFile" << "\n";
}

int main(int argc, char * const argv[])
{
    Listener listener;
    IrRepo repo(listener);
    CarCallbacks carCbs(listener);
    char *buffer = NULL;
    int length = 0, ch, justHeader = 0;
#if defined(WIN32)  || defined(_WIN32)
    int optind = 1;

    if (strcmp(argv[optind], "-?") == 0)
    {
        usage(argv[0]);
        exit(-1);
    }
    else if (strcmp(argv[optind], "-h") == 0)
    {
        justHeader++;
        optind++;
    }
#else
    while ((ch = ::getopt(argc, argv, "h")) != -1)
    {
        switch (ch)
        {
            case 'h':
                justHeader++;
                break;

            case '?':
            default:
                usage(argv[0]);
                break;
        }

    }
#endif /* WIN32 */

    // load IR from .sbeir file
    if (repo.loadFromFile(argv[optind]) < 0)
    {
        std::cout << "could not load IR" << std::endl;
        exit(-1);
    }

    // load data from file into a buffer
    if ((buffer = ::readFileIntoBuffer(argv[optind+1], &length)) == NULL)
    {
        std::cout << "could not load encoding" << std::endl;
        exit(-1);
    }

    std::cout << "Decoding..." << std::endl;

    // if all we want is header, then set up IR for header, decode, and print out offset and header fields
    if (justHeader)
    {
        listener.ir(repo.header())
                .resetForDecode(buffer, length)
                .subscribe(&carCbs, &carCbs, &carCbs);

        std::cout << "Message starts at offset " << listener.bufferOffset() << "\n";

        return 0;
    }

    // set up listener and kick off decoding with subscribe
    listener.dispatchMessageByHeader(repo.header(), &repo)
            .resetForDecode(buffer, length)
            .subscribe(&carCbs, &carCbs, &carCbs);

    std::cout << "Message ends at offset " << listener.bufferOffset() << "\n";

    delete[] buffer;

    return 0;
}
