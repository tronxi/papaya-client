package dev.tronxi.papayaregistryback.persistence.solr.mappers;

import dev.tronxi.papayaregistryback.models.PapayaFileRegistry;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Optional;

@Component
public class PapayaFileRegistryMapper {

    public SolrInputDocument toSolrInputDocument(PapayaFileRegistry registry) {
        SolrInputDocument document = new SolrInputDocument();
        document.addField("fileId", registry.getFileId());
        document.addField("fileName", registry.getFileName());
        document.addField("path", registry.getPath().toString());
        document.addField("description", registry.getDescription());
        document.addField("downloads", registry.getDownloads());
        return document;
    }

    public Optional<PapayaFileRegistry> fromSolrDocumentList(SolrDocumentList solrDocumentList) {
        if (solrDocumentList.isEmpty()) {
            return Optional.empty();
        }
        return solrDocumentList.stream().findFirst()
                .map(this::fromSolrDocument);
    }

    private PapayaFileRegistry fromSolrDocument(SolrDocument solrDocument) {
        PapayaFileRegistry registry = new PapayaFileRegistry();
        registry.setFileId((String) solrDocument.getFirstValue("fileId"));
        registry.setFileName((String) solrDocument.getFirstValue("fileName"));
        registry.setPath(Path.of((String) solrDocument.getFirstValue("path")));
        registry.setDescription((String) solrDocument.getFirstValue("description"));
        registry.setDownloads((Long) solrDocument.getFirstValue("downloads"));
        return registry;
    }
}
