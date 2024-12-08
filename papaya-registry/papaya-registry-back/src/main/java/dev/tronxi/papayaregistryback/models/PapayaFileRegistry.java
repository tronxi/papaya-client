package dev.tronxi.papayaregistryback.models;


import dev.tronxi.papayaregistryback.models.papayafile.PapayaFile;

import java.nio.file.Path;

public class PapayaFileRegistry {

    private String fileId;
    private String fileName;
    private Path path;
    private String description;
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
}
