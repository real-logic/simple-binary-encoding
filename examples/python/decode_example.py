import io
from baseline.Car import Car

length = 129
inp = io.open('car.bin', 'rb')
buffer = inp.read(length)

# start decoding
car = Car()
car.wrapForDecode(buffer, 0, car.sbeBlockLength(), car.sbeSchemaVersion(), length)

# single fixed fields
print('serialNumber: '+str(car.getSerialNumber()))
print('modelYear: '+str(car.getModelYear()))
print('available: '+str(car.getAvailable()))
print('code: '+str(car.getCode()))

# fixed arrays
for i in range(0,car.someNumbersLength()):
    print('someNumber'+str(i)+': '+str(car.getSomeNumbers(i)))

print('vehicleCode: "'+str(car.getVehicleCode())) + '"'

# bitsets
print('cruiseControl: '+str(car.extras().getCruiseControl()))
print('sportsPack: '+str(car.extras().getSportsPack()))
print('sunRoof: '+str(car.extras().getSunRoof()))

# composites
print('capacity: '+str(car.engine().getCapacity()))
print('numCylinders: '+str(car.engine().getNumCylinders()))
print('maxRpm: '+str(car.engine().maxRpm()))

print('manufacturerCode: "'+str(car.engine().getManufacturerCode()))+'"'

# groups
figures = car.fuelFigures()
while figures.hasNext():
    figures.next()
    print('speed: '+str(figures.getSpeed()))
    print('mpg: '+str(figures.getMpg()))

figures = car.performanceFigures()
while figures.hasNext():
    figures.next()
    print('octaneRating: '+str(figures.getOctaneRating()))
    acceleration = figures.acceleration()
    while acceleration.hasNext():
        acceleration.next()
        print('mph: '+str(acceleration.getMph()))
        print('seconds: '+str(acceleration.getSeconds()))

# variable length
make = car.getMake()
print('make: '+str(make))

model = car.getModel()
print('model: '+str(model))
