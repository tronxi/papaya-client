package dev.tronxi.papayaclient.persistence.repositories;

import dev.tronxi.papayaclient.persistence.papayastatusfile.PartPeerStatusFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartPeerStatusFileRepository extends JpaRepository<PartPeerStatusFile, Long> {
}
