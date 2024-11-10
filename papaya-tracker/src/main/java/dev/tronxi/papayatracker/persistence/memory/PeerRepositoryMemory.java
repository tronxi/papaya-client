package dev.tronxi.papayatracker.persistence.memory;

import dev.tronxi.papayatracker.models.Peer;
import dev.tronxi.papayatracker.persistence.PeerRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PeerRepositoryMemory implements PeerRepository {

    private final List<Peer> peers = new ArrayList<>();


    @Override
    public void add(Peer peer) {
        peers.add(peer);
    }

    @Override
    public Optional<Peer> find(Peer peer) {
        return peers.stream().filter(p -> p.equals(peer))
                .findFirst();
    }

    @Override
    public List<Peer> findAll() {
        return peers;
    }

    @Override
    public void remove(Peer peer) {
        peers.remove(peer);
    }
}
