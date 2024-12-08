package dev.tronxi.papayaregistryback.models;


import dev.tronxi.papayaregistryback.models.papayafile.PapayaFile;
import org.apache.solr.client.solrj.beans.Field;

import java.nio.file.Path;

public class PapayaFileRegistry {

    @Field("fileId")
    private String fileId;

    @Field("fileName")
    private String fileName;

    @Field("path")
    private Path path;

    @Field("description")
    private String description;

    @Field("downloads")
    private Long downloads;
    private PapayaFile papayaFile;

    public PapayaFileRegistry(String fileId, String fileName, Path path, String description, Long downloads, PapayaFile papayaFile) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.path = path;
        this.description = description;
        this.downloads = downloads;
        this.papayaFile = papayaFile;
    }

    public PapayaFileRegistry() {
    }


    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getDownloads() {
        return downloads;
    }

    public void setDownloads(Long downloads) {
        this.downloads = downloads;
    }

    public PapayaFile getPapayaFile() {
        return papayaFile;
    }

    public void setPapayaFile(PapayaFile papayaFile) {
        this.papayaFile = papayaFile;
    }

    @Override
    public String toString() {
        return "PapayaFileRegistry{" +
                "fileId='" + fileId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", path=" + path +
                ", description='" + description + '\'' +
                ", downloads=" + downloads +
                ", papayaFile=" + papayaFile +
                '}';
    }
}
