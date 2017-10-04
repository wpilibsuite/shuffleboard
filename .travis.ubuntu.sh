#! /bin/bash

set -e -x

Xvfb :99 & export DISPLAY=:99

./gradlew --version --console=plain
./gradlew spotlessCheck check jacocoJunit5TestReport --stacktrace --console=plain -PlogTests -Pheadless

bash <(curl -s https://codecov.io/bash)
