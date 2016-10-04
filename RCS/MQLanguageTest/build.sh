#!/bin/bash

NDK_PATH="/home/cody/Programs/android-ndk-r9b"

cd src 
javac com/cyjh/input/InputEventStub.java
javah -classpath . com.cyjh.input.InputEventStub

cd ..

$NDK_PATH/ndk-build
