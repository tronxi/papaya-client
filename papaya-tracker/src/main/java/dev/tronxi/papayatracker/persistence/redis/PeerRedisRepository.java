package dev.tronxi.papayatracker.persistence.redis;

import dev.tronxi.papayatracker.models.Peer;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PeerRedisRepository  extends CrudRepository<Peer, String> {
    Optional<Peer> findByAddressAndPort(String address, int port);
}
