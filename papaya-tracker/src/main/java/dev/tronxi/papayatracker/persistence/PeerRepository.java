package dev.tronxi.papayatracker.persistence;

import dev.tronxi.papayatracker.models.Peer;

import java.util.List;
import java.util.Optional;

public interface PeerRepository {

    void add(Peer peer);
    Optional<Peer> find(Peer peer);
    List<Peer> findAll();
    void remove(Peer peer);
}
