package dev.tronxi.papayaclient.persistence.repositories;

import dev.tronxi.papayaclient.persistence.papayastatusfile.PartStatusFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartStatusFileRepository extends JpaRepository<PartStatusFile, Long> {
}
