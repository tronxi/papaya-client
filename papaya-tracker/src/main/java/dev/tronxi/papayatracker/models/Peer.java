package dev.tronxi.papayatracker.models;

import java.util.Objects;

public record Peer(String address, int port, long millis) {
    public static Peer of(PeerDTO peerDTO) {
        return new Peer(peerDTO.address(), peerDTO.port(), System.currentTimeMillis());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Peer peer = (Peer) o;
        return port == peer.port && Objects.equals(address, peer.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, port);
    }
}
