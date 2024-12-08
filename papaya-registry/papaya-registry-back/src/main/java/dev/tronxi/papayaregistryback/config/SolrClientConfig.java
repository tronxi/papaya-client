package dev.tronxi.papayaregistryback.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolrClientConfig {

    @Value("${registry.solr.host}")
    private String host;

    @Value("${registry.solr.core}")
    private String core;

    @Bean
    public SolrClient createSolrClient() {
        return new HttpSolrClient.Builder(host + core).build();
    }
}
