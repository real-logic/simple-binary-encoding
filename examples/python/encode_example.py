import io
from baseline.Car import Car
from baseline.BooleanType import BooleanType
from baseline.Model import Model

buffer = bytearray(2048)

# start decoding
car = Car()
car.wrapForEncode(buffer, 0, len(buffer))

car.setSerialNumber(1234)
car.setModelYear(2013)
car.setAvailable(BooleanType.TRUE)
car.setCode(Model.A)

# fixed arrays
for i in range(0, car.someNumbersLength()) :
    car.setSomeNumbers(i, i)


code = [x for x in 'abcdef']
for i in range(0, car.vehicleCodeLength()):
    car.setVehicleCode(i, code[i])

car.extras().setCruiseControl(True)
car.extras().setSportsPack(True)
car.extras().setSunRoof(False)

# composites
car.engine().setCapacity(2000)
car.engine().setNumCylinders(4)
code = [x for x in '123']
for i in range(0, car.engine().manufacturerCodeLength()):
    car.engine().setManufacturerCode(i, code[i])

# groups
fuelFigures = car.fuelFiguresCount(3)
for speed, mpg in [(30,35.9), (55,49.0), (75,40.0)]:
    fuelFigures.next()
    fuelFigures.setSpeed(speed)
    fuelFigures.setMpg(mpg)


performanceFigures = car.performanceFiguresCount(2)

performanceFigures.next()
performanceFigures.setOctaneRating(95)

acceleration = performanceFigures.accelerationCount(3)
for mph, seconds in [(30,4.0), (60,7.5), (100,12.2)]:
    acceleration.next()
    acceleration.setMph(mph)
    acceleration.setSeconds(seconds)

performanceFigures.next()
performanceFigures.setOctaneRating(99)

acceleration = performanceFigures.accelerationCount(3)
for mph, seconds in [(30,3.8), (60,7.1), (100,11.8)]:
    acceleration.next()
    acceleration.setMph(mph)
    acceleration.setSeconds(seconds)

# variable length
car.setMake('Honda')
car.setModel('Civic VTi')

length = car.encodedLength()

f = io.open('car.bin', 'wb')
f.write(buffer[:length])
f.close()
