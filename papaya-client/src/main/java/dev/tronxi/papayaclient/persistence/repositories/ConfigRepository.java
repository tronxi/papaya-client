package dev.tronxi.papayaclient.persistence.repositories;

import dev.tronxi.papayaclient.persistence.config.Config;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigRepository extends JpaRepository<Config, String> {

}
