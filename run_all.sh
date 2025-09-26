#!/bin/bash

# Base directory = where this script is located
BASEDIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Clean and compile all code for maven
echo "Cleaning and compiling Maven project..."
mvn clean compile -e
echo "Finished compiling Maven project"

# Use Maven to copy dependencies into lib folder
echo "Copying Maven dependencies."
mvn dependency:copy-dependencies -DoutputDirectory="$BASEDIR/lib"
echo "Finished maven dependencies commands."
read -p "Press enter to continue..."

echo "Maven compilation complete - all classes ready in target/classes"
read -p "Press enter to continue..."

cd "$BASEDIR"

# Run all in separate terminals
gnome-terminal -- bash -c "cd '$BASEDIR' && java -cp target/classes:'$BASEDIR'/lib/* server.edge_server.EdgeServer server1; exec bash" &
gnome-terminal -- bash -c "cd '$BASEDIR' && java -cp target/classes:'$BASEDIR'/lib/* server.edge_server.EdgeServer server2; exec bash" &