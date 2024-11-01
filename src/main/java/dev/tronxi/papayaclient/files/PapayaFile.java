package dev.tronxi.papayaclient.files;

import java.util.ArrayList;
import java.util.List;

public class PapayaFile {
    private String fileName;
    private String fileHash;
    private List<PartFile> partFiles;

    public PapayaFile(String fileName, String fileHash) {
        this.fileName = fileName;
        this.fileHash = fileHash;
        partFiles = new ArrayList<>();
    }

    public PapayaFile() {
    }

    public String getFileHash() {
        return fileHash;
    }

    public PapayaFile setFileHash(String fileHash) {
        this.fileHash = fileHash;
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
                ", fileHash='" + fileHash + '\'' +
                ", partFiles=" + partFiles +
                '}';
    }
}
