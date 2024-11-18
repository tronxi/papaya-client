package dev.tronxi.papayaclient.persistence.config;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Config {
    @Id
    private String name;

    private String value;

    public Config() {
    }

    public String getName() {
        return name;
    }

    public Config setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public Config setValue(String value) {
        this.value = value;
        return this;
    }
}
