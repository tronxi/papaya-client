#!/bin/bash

mvn clean install

if [ -d "output" ]; then
  rm -rf output
fi

OS=$(uname)

OS=$(uname)
if [[ "$OS" == *"MINGW"* ]]; then
  ICON="icons/icon.ico"
  echo "Detected Windows. Using icon: $ICON"
elif [ "$OS" == "Darwin" ]; then
  ICON="icons/icon.icns"
  echo "Detected macOS. Using icon: $ICON"
elif [ "$OS" == "Linux" ]; then
  ICON="icons/icon.png"
  echo "Detected Linux. Using icon: $ICON"
fi

VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

echo "OS detected: $OS"
echo "Using icon: $ICON"
echo "Project version: $VERSION"

jpackage \
  --input target \
  --name "Papaya" \
  --main-jar "papaya-client-${VERSION}.jar" \
  --main-class org.springframework.boot.loader.launch.JarLauncher \
  --type app-image \
  --dest output \
  --icon "$ICON" \
  --verbose
