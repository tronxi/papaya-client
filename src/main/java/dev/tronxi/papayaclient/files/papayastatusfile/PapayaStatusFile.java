package dev.tronxi.papayaclient.files.papayastatusfile;

import java.util.ArrayList;
import java.util.List;

public class PapayaStatusFile {
    private String fileName;
    private String fileHash;
    private List<PartStatusFile> partStatusFiles;

    public PapayaStatusFile(String fileName, String fileHash) {
        this.fileName = fileName;
        this.fileHash = fileHash;
        this.partStatusFiles = new ArrayList<>();
    }

    public PapayaStatusFile() {

    }

    public PapayaStatus getStatus() {
        for(PartStatusFile partStatusFile : partStatusFiles) {
            if(partStatusFile.getStatus() != PapayaStatus.COMPLETE) {
                return PapayaStatus.INCOMPLETE;
            }
        }
        return PapayaStatus.COMPLETE;
    }

    public String getFileHash() {
        return fileHash;
    }

    public PapayaStatusFile setFileHash(String fileHash) {
        this.fileHash = fileHash;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public PapayaStatusFile setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public List<PartStatusFile> getPartStatusFiles() {
        return partStatusFiles;
    }

    public PapayaStatusFile setPartStatusFiles(List<PartStatusFile> partStatusFiles) {
        this.partStatusFiles = partStatusFiles;
        return this;
    }

    public void addPartStatusFile(PartStatusFile partStatusFile) {
        this.partStatusFiles.add(partStatusFile);
    }
}
