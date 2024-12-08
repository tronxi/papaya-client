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
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        if (!saved) return;

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
            return papayaFileRegistryMapper.fromSolrDocumentList(response.getResults());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Path> findPathByFileIdForDownload(String fileId) {
        Optional<PapayaFileRegistry> maybePapayaFileRegistry = findByFileId(fileId);
        if (maybePapayaFileRegistry.isPresent()) {
            PapayaFileRegistry papayaFileRegistry = maybePapayaFileRegistry.get();
            updateDownloads(papayaFileRegistry);
            return Optional.of(fileSystemRegistryManager.getAbsolutePath(papayaFileRegistry));
        }
        return Optional.empty();
    }

    private void updateDownloads(PapayaFileRegistry papayaFileRegistry) {
        try {
            SolrInputDocument document = new SolrInputDocument();
            document.addField("id", papayaFileRegistry.getId());
            Map<String, Object> fieldModifier = new HashMap<>();
            fieldModifier.put("set", papayaFileRegistry.getDownloads() + 1);
            document.addField("downloads", fieldModifier);
            solrClient.add(document);
            solrClient.commit();
        } catch (SolrServerException | IOException ignored) {
        }
    }

    @Override
    public List<PapayaFileRegistry> retrieveTopDownloads(int pageNumber, int pageSize) {
        try {
            SolrQuery query = new SolrQuery();
            query.setQuery("*:*");
            query.setSort("downloads", SolrQuery.ORDER.desc);
            query.setRows(1);
            int start = (pageNumber - 1) * pageSize;
            query.setStart(start);
            query.setRows(pageSize);
            QueryResponse response = solrClient.query(query);
            return papayaFileRegistryMapper.listFromSolrDocumentList(response.getResults());
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public List<PapayaFileRegistry> retrieveWithQuery(String query) {
        try {
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(String.format("fileId:*%s* OR description:*%s* OR fileName:*%s*", query, query, query));
            solrQuery.setSort("downloads", SolrQuery.ORDER.desc);
            solrQuery.setRows(20);
            QueryResponse response = solrClient.query(solrQuery);
            return papayaFileRegistryMapper.listFromSolrDocumentList(response.getResults());
        } catch (Exception e) {
            return List.of();
        }
    }
}
