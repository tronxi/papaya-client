package dev.tronxi.papayaclient.persistence.repositories;

import dev.tronxi.papayaclient.persistence.papayastatusfile.PapayaStatusFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PapayaStatusFileRepository extends JpaRepository<PapayaStatusFile, String> {
}
