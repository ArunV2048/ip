#!/usr/bin/env bash
# Text UI test harness for Yuri (package: yuri)
# Run from repo root:  bash text-ui-test/runtest.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
SRC_DIR="$ROOT_DIR/src/main/java"
BIN_DIR="$ROOT_DIR/bin"

INPUT="$SCRIPT_DIR/input.txt"
EXPECTED="$SCRIPT_DIR/EXPECTED.TXT"
ACTUAL="$SCRIPT_DIR/ACTUAL.TXT"

# 1) Clean bin
rm -rf "$BIN_DIR"
mkdir -p "$BIN_DIR"

# 2) Compile all Java sources
find "$SRC_DIR" -name "*.java" > "$SCRIPT_DIR/sources.list"
echo "********** COMPILING **********"
javac -encoding UTF-8 -Xlint:unchecked -d "$BIN_DIR" @"$SCRIPT_DIR/sources.list"

# 2.5) Reset save file so tests start clean
SAVE_FILE="$ROOT_DIR/data/duke.txt"
rm -f "$SAVE_FILE"
mkdir -p "$(dirname "$SAVE_FILE")"
: > "$SAVE_FILE"

# 3) Run the program
echo "********** RUNNING **********"
java -classpath "$BIN_DIR" yuri.Yuri < "$INPUT" > "$ACTUAL"


# 4) Compare ACTUAL vs EXPECTED
echo "********** DIFF **********"
if diff -u "$EXPECTED" "$ACTUAL"; then
  echo "********** TEST RESULT: PASSED **********"
  exit 0
else
  echo "********** TEST RESULT: FAILED **********"
  exit 1
fi
