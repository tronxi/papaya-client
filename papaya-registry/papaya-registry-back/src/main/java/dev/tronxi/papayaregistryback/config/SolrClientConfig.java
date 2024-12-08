package dev.tronxi.papayaregistryback.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolrClientConfig {
    private static final String SOLR_URL = "http://localhost:8983/solr";

    @Bean
    public SolrClient createSolrClient() {
        return new HttpSolrClient.Builder(SOLR_URL + "/gettingstarted").build();
    }
}
