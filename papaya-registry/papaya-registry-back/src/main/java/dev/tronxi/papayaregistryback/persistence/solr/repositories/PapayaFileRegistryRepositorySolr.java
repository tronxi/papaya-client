package dev.tronxi.papayaregistryback.persistence.solr.repositories;

import dev.tronxi.papayaregistryback.models.PapayaFileRegistry;
import dev.tronxi.papayaregistryback.persistence.PapayaFileRegistryRepository;
import dev.tronxi.papayaregistryback.persistence.solr.mappers.PapayaFileRegistryMapper;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PapayaFileRegistryRepositorySolr implements PapayaFileRegistryRepository {

    private final SolrClient solrClient;
    private final PapayaFileRegistryMapper papayaFileRegistryMapper;

    public PapayaFileRegistryRepositorySolr(SolrClient solrClient, PapayaFileRegistryMapper papayaFileRegistryMapper) {
        this.solrClient = solrClient;
        this.papayaFileRegistryMapper = papayaFileRegistryMapper;
    }


    @Override
    public void save(PapayaFileRegistry papayaFileRegistry) {
        SolrInputDocument solrInputDocument = papayaFileRegistryMapper.toSolrInputDocument(papayaFileRegistry);
        try {
            solrClient.add(solrInputDocument);
            solrClient.commit();
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }

    }
}
