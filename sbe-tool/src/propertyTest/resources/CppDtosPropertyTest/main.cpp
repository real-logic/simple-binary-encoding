#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include "sbe_property_test/MessageHeader.h"
#include "sbe_property_test/TestMessage.h"
#include "sbe_property_test/TestMessageDto.h"

using namespace uk::co::real_logic::sbe::properties;

int main(int argc, char* argv[]) {
    if (argc != 2) {
        std::cout << "Usage: " << argv[0] << " $BINARY_FILE" << std::endl;
        return 1;
    }

    std::string binaryFile = argv[1];

    std::cout << "Reading binary file: " << binaryFile << std::endl;
    std::ifstream file(binaryFile, std::ios::binary);
    std::vector<char> inputBytes((std::istreambuf_iterator<char>(file)),
                                 std::istreambuf_iterator<char>());

    char* buffer = inputBytes.data();
    std::size_t bufferLength = inputBytes.size();

    MessageHeader messageHeader(buffer, bufferLength);

    TestMessage decoder;
    decoder.wrapForDecode(
        buffer,
        MessageHeader::encodedLength(),
        messageHeader.blockLength(),
        messageHeader.version(),
        bufferLength);

    std::cout << "Decoding binary into DTO" << std::endl;
    TestMessageDto dto;
    TestMessageDto::decodeWith(decoder, dto);
    std::vector<char> outputBytes(inputBytes.size());
    char* outputBuffer = outputBytes.data();

    TestMessage encoder;
    encoder.wrapAndApplyHeader(outputBuffer, 0, bufferLength);
    TestMessageDto::encodeWith(encoder, dto);

    std::cout << "Writing binary file: output.dat" << std::endl;
    std::ofstream outputFile("output.dat", std::ios::binary);
    outputFile.write(outputBuffer, outputBytes.size());

    std::cout << "Done" << std::endl;

    return 0;
}
