#!/bin/bash
DIR="$(dirname "${BASH_SOURCE[0]}")"
cd $DIR
(cd .. && ./gradlew build)
(cd sbe-dll && dotnet pack -c release -o ../nuget)
