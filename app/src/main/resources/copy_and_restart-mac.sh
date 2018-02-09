#!/usr/bin/env bash
# copy_and_restart-mac.sh
# This will copy a downloaded JAR file to a specified location, delete the original, and start the copied JAR
# param 1: the full path to the downloaded JAR, eg /tmp/newestshuffleboard123123jkh123.jar
# param 2: the full path to the target file to copy to, eg ~/WPILib/Shuffleboard.jar

sleep 5 # wait 5 seconds to let the shuffleboard application stop before overwriting its JAR
cp $1 $2
rm $1
java -jar $2
