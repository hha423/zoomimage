#!/bin/bash

# Exit immediately if a command exits with a non-zero status.
set -e

cd ../../

./gradlew jsBrowserTest

echo "✅  JS tests are passed successfully."