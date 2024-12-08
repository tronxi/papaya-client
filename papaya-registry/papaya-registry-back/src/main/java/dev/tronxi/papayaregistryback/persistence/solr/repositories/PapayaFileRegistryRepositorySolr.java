package dev.tronxi.papayaregistryback.persistence.solr.repositories;

import dev.tronxi.papayaregistryback.models.PapayaFileRegistry;
import dev.tronxi.papayaregistryback.persistence.PapayaFileRegistryRepository;
import dev.tronxi.papayaregistryback.persistence.filesystem.FileSystemRegistryManager;
import dev.tronxi.papayaregistryback.persistence.solr.mappers.PapayaFileRegistryMapper;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class PapayaFileRegistryRepositorySolr implements PapayaFileRegistryRepository {

    private final SolrClient solrClient;
    private final PapayaFileRegistryMapper papayaFileRegistryMapper;
    private final FileSystemRegistryManager fileSystemRegistryManager;

    public PapayaFileRegistryRepositorySolr(SolrClient solrClient, PapayaFileRegistryMapper papayaFileRegistryMapper, FileSystemRegistryManager fileSystemRegistryManager) {
        this.solrClient = solrClient;
        this.papayaFileRegistryMapper = papayaFileRegistryMapper;
        this.fileSystemRegistryManager = fileSystemRegistryManager;
    }


    @Override
    public void save(PapayaFileRegistry papayaFileRegistry) {
        if (findByFileId(papayaFileRegistry.getFileId()).isPresent()) return;

        boolean saved = fileSystemRegistryManager.savePapayaFile(papayaFileRegistry);
        if(!saved) return;

        SolrInputDocument solrInputDocument = papayaFileRegistryMapper.toSolrInputDocument(papayaFileRegistry);
        try {
            solrClient.add(solrInputDocument);
            solrClient.commit();
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }

    }


    public Optional<PapayaFileRegistry> findByFileId(String fileId) {
        try {
            SolrQuery query = new SolrQuery();
            query.setQuery("fileId:" + fileId);
            query.setRows(1);
            QueryResponse response = solrClient.query(query);

            PapayaFileRegistry result = response.getBeans(PapayaFileRegistry.class)
                    .stream()
                    .findFirst()
                    .orElse(null);

            return Optional.ofNullable(result);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
