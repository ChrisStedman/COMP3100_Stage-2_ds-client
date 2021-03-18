#!/bin/bash

echo "Compiling Client..."
javac Client.java
javac Job.java
echo "Compilation complete."
echo "Exectuting Client..."
java Client
