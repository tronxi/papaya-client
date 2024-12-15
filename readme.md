# Papaya-P2P

Papaya-P2P is a peer-to-peer (P2P) file sharing application written in Java. This project enables users to share and
download files directly between peers.

---

## Features

- **P2P File Sharing**: Direct file transfer between peers.
- **Tracker, Client, and Registry**: Separate modules for coordinating peer connections, managing file transfers, and
  handling shared resources.
- **Decentralized Network**: Allows multiple clients to connect and exchange data in a robust, distributed manner.

---

## Project Structure

- **papaya-client**: Implements the client functionality for connecting to the P2P network, sharing, and downloading
  files.
- **papaya-tracker**: Maintains a list of connected peers with their IP addresses and ports, enabling efficient peer
  discovery.
- **papaya-registry**: Handles the upload and download of `.papaya` files.

---

## Quick Start: Using the Precompiled Release for Papaya-Client

1. **Download the latest release**:
   Go to the [Releases](https://github.com/tronxi/papaya-p2p/releases) page and download the appropriate file for your
   operating system

2. **Run the client**:
   Launch the downloaded file to start the Papaya-P2P client.

3. **Connect to the tracker and registry**:
   The tracker is preconfigured in the client, so no additional setup is needed. Use the following details to connect to
   the online services:
    - **Tracker**: [https://tronxi.ddns.net/tracker](https://tronxi.ddns.net/tracker) (preconfigured in the client)
    - **Registry**: [https://tronxi.ddns.net](https://tronxi.ddns.net)

4. **Start sharing and downloading files**:
   Use the registry to find `.papaya` files, import them into the client to start downloading, or upload your own
   `.papaya` files to share with other peers.

---

## Advanced: Building from Source

If you want to build the project yourself or make custom modifications, follow these instructions for each module:

### papaya-Client

[Papaya-Client Repository](https://github.com/tronxi/papaya-p2p/tree/master/papaya-client)

To build and run the Papaya-Client, navigate to the `papaya-client` directory and choose **one of the following options
**:

1. **Generate a JAR file**:
   Use Maven to build the project and produce a `.jar` file:

   ```bash
   mvn clean install

2. **Create a platform-specific executable**:
   Use the provided [package.sh](https://github.com/tronxi/papaya-p2p/blob/master/papaya-client/package.sh) script to
   generate an executable tailored to your operating system:
   ```bash
   sh package.sh

### papaya-tracker

[Papaya-Tracker Repository](https://github.com/tronxi/papaya-p2p/tree/master/papaya-tracker)

To build and run the Papaya-Tracker, navigate to the `papaya-tracker` directory and follow these steps:

1. **Run with Docker Compose**:
   Use the
   provided [docker-compose.yml](https://github.com/tronxi/papaya-p2p/blob/master/papaya-tracker/docker/docker-compose.yaml)
   file to set up the tracker and its required Redis database. Run the following commands:

   ```bash
   docker-compose up -d

### papaya-registry

[Papaya-Registry Repository](https://github.com/tronxi/papaya-p2p/tree/master/papaya-registry)

To build and run the Papaya-Tracker, navigate to the `papaya-registry` directory and follow these steps:

1. **Run with Docker Compose**:
   Use the
   provided [docker-compose.yml](https://github.com/tronxi/papaya-p2p/blob/master/papaya-registry/docker-compose.yaml)
   file to set up the registry, including its interface, backend, and a Solr instance for indexing files. Run the
   following commands:
   ```bash
   docker-compose up -d

---

## License

This project is licensed under the [Apache License 2.0](https://github.com/tronxi/papaya-p2p/blob/master/LICENSE).