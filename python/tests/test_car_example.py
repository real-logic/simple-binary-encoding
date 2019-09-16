import unittest
from pathlib import Path

from tests.gen.car_example import MessageHeaderEncoder, CarEncoder, BooleanType, Model, EngineEncoder, BoostType
from tests.gen.car_example import MessageHeaderDecoder, CarDecoder, EngineDecoder


class TestCarExample(unittest.TestCase):
    REFERENCE_FILE_ENCODE = Path(__file__).resolve().parent / Path('gen') / 'car_example' / 'car_example_data.sbe'

    VEHICLE_CODE = "abcdef".encode(CarDecoder.vehicleCodeCharacterEncoding())
    MANUFACTURER_CODE = "123".encode(EngineDecoder.manufacturerCodeCharacterEncoding())
    MANUFACTURER = "Honda".encode(CarDecoder.manufacturerCharacterEncoding())
    MODEL = "Civic VTi".encode(CarDecoder.modelCharacterEncoding())
    ACTIVATION_CODE = "abcdef".encode(CarDecoder.activationCodeCharacterEncoding())

    CAR_ENCODER = CarEncoder()
    MESSAGE_HEADER_ENCODER = MessageHeaderEncoder()

    CAR_DECODER = CarDecoder()
    MESSAGE_HEADER_DECODER = MessageHeaderDecoder()

    def test_encoding(self):
        with open(self.REFERENCE_FILE_ENCODE, 'rb') as f:
            buffer = f.read()
            py_buf = bytearray(4096)
            self._encode(py_buf, self.MESSAGE_HEADER_ENCODER, self.CAR_ENCODER)
            # Encoded message should match
            self.assertEqual(buffer, py_buf[:207])
            # The empty part of the buffer should be all 0's
            self.assertEqual(bytes(len(py_buf) - len(buffer)), py_buf[len(buffer):])
            self.assertEqual(207, len(buffer))

    def test_decoding(self):
        self.maxDiff = None
        with open(self.REFERENCE_FILE_ENCODE, 'rb') as f:
            buffer = f.read()
            py_buf = bytearray(4096)
            self._encode(py_buf, self.MESSAGE_HEADER_ENCODER, self.CAR_ENCODER)
            self._decode(py_buf, self.MESSAGE_HEADER_DECODER, self.CAR_DECODER)
            self._decode(buffer, self.MESSAGE_HEADER_DECODER, self.CAR_DECODER)

    def _encode(self, buf: bytearray, header: MessageHeaderEncoder, car: CarEncoder):
        offset = 0

        car.wrapAndApplyHeader(buf, offset, header)
        car.serialNumber(1234)
        car.modelYear(2013)
        car.available(BooleanType.T)
        car.code(Model.A)
        car.set_vehicleCode(self.VEHICLE_CODE)

        car.someNumbers(0,1)
        car.someNumbers(1,2)
        car.someNumbers(2,3)
        car.someNumbers(3,4)

        car.extras() \
            .clear() \
            .cruiseControl(True) \
            .sportsPack(True) \
            .sunRoof(False)

        car.engine() \
            .capacity(2000) \
            .numCylinders(4) \
            .set_manufacturerCode(self.MANUFACTURER_CODE) \
            .efficiency(35) \
            .boosterEnabled(BooleanType.T) \
            .booster() \
            .boostType(BoostType.NITROUS) \
            .horsePower(200)

        ff = car.fuelFiguresCount(3)
        ff.next().speed(30).mpg(35.9).usageDescription("Urban Cycle".encode(CarEncoder.FuelFiguresEncoder
                                                                            .usageDescriptionCharacterEncoding()))
        ff.next().speed(55).mpg(49.0).usageDescription("Combined Cycle".encode(CarEncoder.FuelFiguresEncoder
                                                                               .usageDescriptionCharacterEncoding()))
        ff.next().speed(75).mpg(40.0).usageDescription("Highway Cycle".encode(CarEncoder.FuelFiguresEncoder
                                                                              .usageDescriptionCharacterEncoding()))

        figures = car.performanceFiguresCount(2)
        figures.next().octaneRating(95).accelerationCount(3) \
            .next().mph(30).seconds(4.0) \
            .next().mph(60).seconds(7.5) \
            .next().mph(100).seconds(12.2)

        figures.next().octaneRating(99).accelerationCount(3) \
            .next().mph(30).seconds(3.8) \
            .next().mph(60).seconds(7.1) \
            .next().mph(100).seconds(11.8)

        car.manufacturer(self.MANUFACTURER).model(self.MODEL).activationCode(self.ACTIVATION_CODE)

        return header.encodedLength + car.encodedLength

    def _decode(self, buf: bytes, header: MessageHeaderDecoder, car: CarDecoder):
        offset = 0
        out = ""
        header.wrap(buf, offset)
        template_id = header.templateId()
        if template_id != car.sbeTemplateId:
            raise Exception("Template ID missmatch")
        acting_block_length = header.blockLength()
        acting_block_version = header.version()
        offset += header.encodedLength

        car.wrap(buf, offset, acting_block_length, acting_block_version)
        self.assertEqual(1234, car.serialNumber())
        self.assertEqual(2013, car.modelYear())
        self.assertEqual(BooleanType.T, car.available())
        self.assertEqual(Model.A, car.code())
        self.assertEqual((1, 2, 3, 4), car.getMultiSomeNumbers())
        self.assertEqual(1, car.someNumbers(0))
        self.assertEqual(2, car.someNumbers(1))
        self.assertEqual(3, car.someNumbers(2))
        self.assertEqual(4, car.someNumbers(3))
        index_oob = False
        try:
            car.someNumbers(55)
        except IndexError as e:
            index_oob = True
            self.assertEqual(IndexError, type(e))
        self.assertTrue(index_oob)

        self.assertEqual("abcdef", str(car.getMultiVehicleCode(), car.vehicleCodeCharacterEncoding()))

        extras = car.extras()
        self.assertTrue(extras.cruiseControl())
        self.assertTrue(extras.sportsPack())
        self.assertFalse(extras.sunRoof())

        self.assertEqual("C", car.discountedModel().name)

        engine = car.engine()
        self.assertEqual(2000, engine.capacity())
        self.assertEqual(4, engine.numCylinders())
        self.assertEqual(9000, engine.maxRpm)
        self.assertEqual("123", str(engine.getMultiManufacturerCode(), EngineDecoder.manufacturerCodeCharacterEncoding()))
        self.assertEqual(35, engine.efficiency())
        self.assertEqual(BooleanType.T,  engine.boosterEnabled())
        self.assertEqual(BoostType.NITROUS, engine.booster().boostType())
        self.assertEqual(200, engine.booster().horsePower())

        self.assertEqual("Petrol", str(engine.getFuel(0, len(buf)).tobytes().decode()))

        ff = car.fuelFigures()
        ff.next()
        self.assertEqual(30, ff.speed())
        self.assertAlmostEqual(35.9, ff.mpg(), places=5)
        self.assertEqual("Urban Cycle", ff.usageDescription())
        ff.next()
        self.assertEqual(55, ff.speed())
        self.assertAlmostEqual(49.0, ff.mpg(), places=5)
        self.assertEqual("Combined Cycle", ff.usageDescription())
        ff.next()
        self.assertEqual(75, ff.speed())
        self.assertAlmostEqual(40.0, ff.mpg(), places=5)
        self.assertEqual("Highway Cycle", ff.usageDescription())

        pf = car.performanceFigures()
        pf.next()
        self.assertEqual(95, pf.octaneRating())
        pfa = pf.acceleration()
        pfa.next()
        self.assertEqual(30, pfa.mph())
        self.assertAlmostEqual(4.0, pfa.seconds(), places=5)
        pfa.next()
        self.assertEqual(60, pfa.mph())
        self.assertAlmostEqual(7.5, pfa.seconds(), places=5)
        pfa.next()
        self.assertEqual(100, pfa.mph())
        self.assertAlmostEqual(12.2, pfa.seconds(), places=5)

        pf.next()
        self.assertEqual(99, pf.octaneRating())
        pfa = pf.acceleration()
        pfa.next()
        self.assertEqual(30, pfa.mph())
        self.assertAlmostEqual(3.8, pfa.seconds(), places=5)
        pfa.next()
        self.assertEqual(60, pfa.mph())
        self.assertAlmostEqual(7.1, pfa.seconds(), places=5)
        pfa.next()
        self.assertEqual(100, pfa.mph())
        self.assertAlmostEqual(11.8, pfa.seconds(), places=5)

        self.assertEqual("Honda", car.manufacturer())
        self.assertEqual("Civic VTi", car.model())
        self.assertEqual("abcdef", car.activationCode())
        self.assertEqual(199, car.encodedLength)
        return out
