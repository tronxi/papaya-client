package dev.tronxi.papayatracker.persistence.redis;

import dev.tronxi.papayatracker.models.Peer;
import dev.tronxi.papayatracker.persistence.PeerRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Primary
public class PeerRedisService implements PeerRepository {

    private final PeerRedisRepository peerRedisRepository;

    public PeerRedisService(PeerRedisRepository peerRedisRepository) {
        this.peerRedisRepository = peerRedisRepository;
    }


    @Override
    public void add(Peer peer) {
        peerRedisRepository.save(peer);
    }

    @Override
    public Optional<Peer> find(Peer peer) {
        return peerRedisRepository.findByAddressAndPort(peer.address(), peer.port());
    }

    @Override
    public List<Peer> findAll() {
        Iterable<Peer> peers = peerRedisRepository.findAll();
        return new ArrayList<>(((List<Peer>) peers));
    }

    @Override
    public void remove(Peer peer) {
        peerRedisRepository.delete(peer);
    }
}
