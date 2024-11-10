package dev.tronxi.papayatracker.models;

public record PeerDTO(String address, int port) {
    public static PeerDTO of(Peer peer) {
        return new PeerDTO(peer.address(), peer.port());
    }
}
