package dev.tronxi.papayaclient.persistence.services;

import dev.tronxi.papayaclient.persistence.config.Config;
import dev.tronxi.papayaclient.persistence.repositories.ConfigRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConfigService {

    private final String workspaceName = "workspace";
    private final String trackerName = "tracker";
    private final String useOnlyLocalAddressName = "useOnlyLocalAddress";

    @Value("${build.version}")
    private String version;

    @Value("${papaya.tracker}")
    private String defaultTracker;

    @Value("${papaya.useOnlyLocalAddress}")
    private Boolean defaultUseOnlyLocalAddress;

    private final ConfigRepository configRepository;

    public ConfigService(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public String getVersion() {
        return version;
    }

    public Optional<String> retrieveWorkspace() {
        return configRepository.findById(workspaceName)
                .map(Config::getValue);
    }

    public String retrieveTracker() {
        return configRepository.findById(trackerName)
                .map(Config::getValue)
                .orElse(defaultTracker);
    }

    public Boolean retrieveUseOnlyLocalAddress() {
        return configRepository.findById(useOnlyLocalAddressName)
                .map(config -> Boolean.parseBoolean(config.getValue()))
                .orElse(defaultUseOnlyLocalAddress);
    }

    public void saveWorkspace(String workspace) {
        saveProperty(workspaceName, workspace);
    }

    public void saveTracker(String tracker) {
        saveProperty(trackerName, tracker);
    }

    public void saveUseOnlyLocalAddress(Boolean useOnlyLocalAddress) {
        saveProperty(useOnlyLocalAddressName, useOnlyLocalAddress.toString());
    }

    private void saveProperty(String name, String value) {
        Optional<Config> maybeConfig = configRepository.findById(name);
        Config config;
        if (maybeConfig.isPresent()) {
            config = maybeConfig.get();
            config.setValue(value);
        } else {
            config = new Config();
            config.setName(name);
            config.setValue(value);
        }
        configRepository.save(config);
    }
}
