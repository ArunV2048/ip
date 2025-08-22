#!/usr/bin/env bash
set e

# create bin directory if it doesn't exist
#if [ ! -d "../bin" ]
#then
#    mkdir ../bin
#fi
[ -d "../bin" ] || mkdir ../bin

# delete output from previous run
#if [ -e "./ACTUAL.TXT" ]
#then
#    rm ACTUAL.TXT
#fi
[ -e "./ACTUAL.TXT" ] && rm ACTUAL.TXT

# compile the code into the bin folder, terminates if error occurred
if ! javac -cp ../src/main/java -Xlint:none -d ../bin ../src/main/java/*.java; then
  echo "********** BUILD FAILURE **********"
  exit 1
fi

# run the program, feed commands from input.txt file and redirect the output to the ACTUAL.TXT
java -classpath ../bin Yuri < input.txt > ACTUAL.TXT

# normalize line endings if dos2unix is available
if command -v dos2unix >/dev/null 2>&1; then
  cp EXPECTED.TXT EXPECTED-UNIX.TXT
  dos2unix ACTUAL.TXT EXPECTED-UNIX.TXT
  diff ACTUAL.TXT EXPECTED-UNIX.TXT
else
  diff ACTUAL.TXT EXPECTED.TXT
fi

echo "Test result: PASSED"