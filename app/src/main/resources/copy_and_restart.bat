@ copy_and_restart.bat
@ This will copy a downloaded JAR file to a specified location, delete the original, and start the copied JAR.
@ param 1: the full path to the downloaded JAR, eg C:\Users\user\newestshuffleboard123123kjh123123.jar
@ param 2: the full path to the target file to copy to, eg C:\Users\user\WPILib\Shuffleboard.jar

timeout /T 5 /NOBREAK > nul
copy /y %1 %2
del %1
java -jar %2
