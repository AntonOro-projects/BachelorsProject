#!/usr/bin/env bash

#instrumented test
./gradlew connectedAndroidTest  --continue

#unittesting
./gradlew test  --continue