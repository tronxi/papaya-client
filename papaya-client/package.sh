 jpackage \
    --input target \
    --name "Papaya" \
    --main-jar papaya-client-0.0.1-SNAPSHOT.jar \
    --main-class org.springframework.boot.loader.launch.JarLauncher \
    --type app-image \
    --dest output \
    --verbose