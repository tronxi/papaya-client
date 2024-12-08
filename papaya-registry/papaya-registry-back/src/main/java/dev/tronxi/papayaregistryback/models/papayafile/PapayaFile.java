package dev.tronxi.papayaregistryback.models.papayafile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PapayaFile {
    private String fileName;
    private String fileId;
    private List<PartFile> partFiles;

    public PapayaFile(String fileName) {
        this.fileName = fileName;
        this.fileId = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        partFiles = new ArrayList<>();
    }

    public PapayaFile() {
    }

    public String getFileId() {
        return fileId;
    }

    public PapayaFile setFileId(String fileId) {
        this.fileId = fileId;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public PapayaFile setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public List<PartFile> getPartFiles() {
        return partFiles;
    }

    public void addPartFile(PartFile partFile) {
        this.partFiles.add(partFile);
    }

    @Override
    public String toString() {
        return "PapayaFile{" +
                "fileName='" + fileName + '\'' +
                ", fileHash='" + fileId + '\'' +
                ", partFiles=" + partFiles +
                '}';
    }
}
