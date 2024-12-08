package dev.tronxi.papayaregistryback.persistence.solr.mappers;

import dev.tronxi.papayaregistryback.models.PapayaFileRegistry;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

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
}
