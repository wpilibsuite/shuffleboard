#! /bin/bash

set -e -x

Xvfb :99 & export DISPLAY=:99

./gradlew --version --console=plain
./gradlew spotlessCheck check jacocoTestReport --stacktrace --console=plain -PlogTests

bash <(curl -s https://codecov.io/bash)
