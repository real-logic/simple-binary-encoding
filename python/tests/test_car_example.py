import unittest
from pathlib import Path

from tests.gen.car_example import MessageHeader, Car, BooleanType, Model, Engine, BoostType


class TestCarExample(unittest.TestCase):
    REFERENCE_FILE_ENCODE = Path(__file__).resolve().parent / Path('gen') / 'car_example' / 'car_example_data.sbe'

    VEHICLE_CODE = "abcdef".encode(Car.VEHICLE_CODE_CHAR_ENCODING)
    MANUFACTURER_CODE = "123".encode(Engine.MANUFACTURER_CODE_CHAR_ENCODING)
    MANUFACTURER = "Honda".encode(Car.MANUFACTURER_CHAR_ENCODING)
    MODEL = "Civic VTi".encode(Car.MODEL_CHAR_ENCODING)
    ACTIVATION_CODE = "abcdef".encode(Car.ACTIVATION_CODE_CHAR_ENCODING)

    CAR = Car()
    MESSAGE_HEADER = MessageHeader()

    def test_encoding(self):
        with open(self.REFERENCE_FILE_ENCODE, 'rb') as f:
            buffer = f.read()
            py_buf = bytearray(4096)
            self._encode(py_buf, self.MESSAGE_HEADER, self.CAR)
            # Encoded message should match
            self.assertEqual(buffer, py_buf[:211])
            # The empty part of the buffer should be all 0's
            self.assertEqual(bytes(len(py_buf) - len(buffer)), py_buf[len(buffer):])
            self.assertEqual(211, len(buffer))

    def test_decoding(self):
        self.maxDiff = None
        with open(self.REFERENCE_FILE_ENCODE, 'rb') as f:
            buffer = f.read()
            py_buf = bytearray(4096)
            self._encode(py_buf, self.MESSAGE_HEADER, self.CAR)
            self._decode(py_buf, self.MESSAGE_HEADER, self.CAR)
            self._decode(buffer, self.MESSAGE_HEADER, self.CAR)

    def _encode(self, buf: bytes, header: MessageHeader, car: Car):
        offset = 0
        header.wrap(buf, offset)
        header.set_block_length(Car.BLOCK_LENGTH)
        header.set_schema_id(Car.SCHEMA_ID)
        header.set_template_id(Car.SCHEMA_ID)
        header.set_version(Car.SCHEMA_VERSION)

        car.wrap_encode(buf, offset + MessageHeader.SIZE)
        car.set_serial_number(1234)
        car.set_model_year(2013)
        car.set_available(BooleanType.T)
        car.set_code(Model.A)
        car.set_vehicle_code(self.VEHICLE_CODE)

        for i in range(Car.SOME_NUMBERS_LENGTH):
            car.put_some_numbers(i, i)

        car.set_extras() \
            .clear() \
            .set_cruise_control(True) \
            .set_sports_pack(True) \
            .set_sun_roof(False)

        car.set_engine() \
            .set_capacity(2000) \
            .set_num_cylinders(4) \
            .set_manufacturer_code(self.MANUFACTURER_CODE) \
            .set_efficiency(35) \
            .set_booster_enabled(BooleanType.T) \
            .set_booster() \
            .set_boost_type(BoostType.NITROUS) \
            .set_horse_power(200)

        ff = car.set_fuel_figures_count(3)
        ff.next().set_speed(30).set_mpg(35.9).set_usage_description("Urban Cycle".encode(Car.FuelFiguresGroup.USAGE_DESCRIPTION_CHAR_ENCODING))
        ff.next().set_speed(55).set_mpg(49.0).set_usage_description("Combined Cycle".encode(Car.FuelFiguresGroup.USAGE_DESCRIPTION_CHAR_ENCODING))
        ff.next().set_speed(75).set_mpg(40.0).set_usage_description("Highway Cycle".encode(Car.FuelFiguresGroup.USAGE_DESCRIPTION_CHAR_ENCODING))

        figures = car.set_performance_figures_count(2)
        figures.next().set_octane_rating(95).set_acceleration_count(3) \
            .next().set_mph(30).set_seconds(4.0) \
            .next().set_mph(60).set_seconds(7.5) \
            .next().set_mph(100).set_seconds(12.2)

        figures.next().set_octane_rating(99).set_acceleration_count(3) \
            .next().set_mph(30).set_seconds(3.8) \
            .next().set_mph(60).set_seconds(7.1) \
            .next().set_mph(100).set_seconds(11.8)

        car.set_manufacturer(self.MANUFACTURER).set_model(self.MODEL).set_activation_code(self.ACTIVATION_CODE)

        return MessageHeader.SIZE + car.size()

    def _decode(self, buf: bytes, header: MessageHeader, car: Car):
        offset = 0
        out = ""
        header.wrap(buf, offset)
        template_id = header.get_template_id()
        if template_id != Car.TEMPLATE_ID:
            raise Exception("Template ID missmatch")
        acting_block_length = header.get_block_length()
        acting_block_version = header.get_version()
        offset += header.SIZE

        car.wrap_decode(buf, offset, acting_block_length, acting_block_version)
        self.assertEqual(1234, car.get_serial_number())
        self.assertEqual(2013, car.get_model_year())
        self.assertEqual(BooleanType.T, car.get_available())
        self.assertEqual(Model.A, car.get_code())
        self.assertEqual((0, 1, 2, 3, 4), car.get_multi_some_numbers())
        self.assertEqual(0, car.get_some_numbers(0))
        self.assertEqual(1, car.get_some_numbers(1))
        self.assertEqual(2, car.get_some_numbers(2))
        self.assertEqual(3, car.get_some_numbers(3))
        self.assertEqual(4, car.get_some_numbers(4))
        index_oob = False
        try:
            car.get_some_numbers(55)
        except IndexError as e:
            index_oob = True
            self.assertEqual(IndexError, type(e))
        self.assertTrue(index_oob)

        self.assertEqual("abcdef", str(car.get_multi_vehicle_code(), car.VEHICLE_CODE_CHAR_ENCODING))

        extras = car.get_extras()
        self.assertTrue(extras.has_cruise_control())
        self.assertTrue(extras.has_sports_pack())
        self.assertFalse(extras.has_sun_roof())

        self.assertEqual("C", car.get_discounted_model().name)

        engine = car.get_engine()
        self.assertEqual(2000, engine.get_capacity())
        self.assertEqual(4, engine.get_num_cylinders())
        self.assertEqual(9000, engine.get_max_rpm)
        self.assertEqual("123", str(engine.get_multi_manufacturer_code(), Engine.MANUFACTURER_CODE_CHAR_ENCODING))
        self.assertEqual(35, engine.get_efficiency())
        self.assertEqual(BooleanType.T,  engine.get_booster_enabled())
        self.assertEqual(BoostType.NITROUS, engine.get_booster().get_boost_type())
        self.assertEqual(200, engine.get_booster().get_horse_power())

        self.assertEqual("Petrol", str(engine.get_fuel(0, len(buf)).tobytes().decode()))

        ff = car.get_fuel_figures()
        ff.next()
        self.assertEqual(30, ff.get_speed())
        self.assertAlmostEqual(35.9, ff.get_mpg(), places=5)
        self.assertEqual("Urban Cycle", ff.get_usage_description().tobytes().decode(Car.FuelFiguresGroup.USAGE_DESCRIPTION_CHAR_ENCODING))
        ff.next()
        self.assertEqual(55, ff.get_speed())
        self.assertAlmostEqual(49.0, ff.get_mpg(), places=5)
        self.assertEqual("Combined Cycle", ff.get_usage_description().tobytes().decode(Car.FuelFiguresGroup.USAGE_DESCRIPTION_CHAR_ENCODING))
        ff.next()
        self.assertEqual(75, ff.get_speed())
        self.assertAlmostEqual(40.0, ff.get_mpg(), places=5)
        self.assertEqual("Highway Cycle", ff.get_usage_description().tobytes().decode(Car.FuelFiguresGroup.USAGE_DESCRIPTION_CHAR_ENCODING))

        pf = car.get_performance_figures()
        pf.next()
        self.assertEqual(95, pf.get_octane_rating())
        pfa = pf.get_acceleration()
        pfa.next()
        self.assertEqual(30, pfa.get_mph())
        self.assertAlmostEqual(4.0, pfa.get_seconds(), places=5)
        pfa.next()
        self.assertEqual(60, pfa.get_mph())
        self.assertAlmostEqual(7.5, pfa.get_seconds(), places=5)
        pfa.next()
        self.assertEqual(100, pfa.get_mph())
        self.assertAlmostEqual(12.2, pfa.get_seconds(), places=5)

        pf.next()
        self.assertEqual(99, pf.get_octane_rating())
        pfa = pf.get_acceleration()
        pfa.next()
        self.assertEqual(30, pfa.get_mph())
        self.assertAlmostEqual(3.8, pfa.get_seconds(), places=5)
        pfa.next()
        self.assertEqual(60, pfa.get_mph())
        self.assertAlmostEqual(7.1, pfa.get_seconds(), places=5)
        pfa.next()
        self.assertEqual(100, pfa.get_mph())
        self.assertAlmostEqual(11.8, pfa.get_seconds(), places=5)

        self.assertEqual("Honda", car.get_manufacturer().tobytes().decode(Car.MANUFACTURER_CHAR_ENCODING))
        self.assertEqual("Civic VTi", car.get_model().tobytes().decode(Car.MODEL_CHAR_ENCODING))
        self.assertEqual("abcdef", car.get_activation_code().tobytes().decode(Car.ACTIVATION_CODE_CHAR_ENCODING))
        self.assertEqual(203, car.size())
        return out
