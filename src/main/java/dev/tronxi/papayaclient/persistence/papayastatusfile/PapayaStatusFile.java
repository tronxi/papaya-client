package dev.tronxi.papayaclient.persistence.papayastatusfile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class PapayaStatusFile {

    @Id
    private String fileId;

    private String fileName;

    @OneToMany
    private List<PartStatusFile> partStatusFiles;

    public PapayaStatusFile(String fileName, String fileId) {
        this.fileName = fileName;
        this.fileId = fileId;
        this.partStatusFiles = new ArrayList<>();
    }

    public PapayaStatusFile() {

    }

    public PapayaStatus getStatus() {
        for (PartStatusFile partStatusFile : partStatusFiles) {
            if (partStatusFile.getStatus() != PapayaStatus.COMPLETE) {
                return PapayaStatus.INCOMPLETE;
            }
        }
        return PapayaStatus.COMPLETE;
    }

    public String getFileId() {
        return fileId;
    }

    public PapayaStatusFile setFileId(String fileId) {
        this.fileId = fileId;
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

    @Override
    public String toString() {
        return "PapayaStatusFile{" +
                "fileName='" + fileName + '\'' +
                ", fileId='" + fileId + '\'' +
                ", partStatusFiles=" + partStatusFiles +
                '}';
    }
}
