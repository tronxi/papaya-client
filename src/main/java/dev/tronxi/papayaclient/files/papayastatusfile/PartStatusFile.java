package dev.tronxi.papayaclient.files.papayastatusfile;

public class PartStatusFile {
    private String fileName;
    private String fileHash;
    private PapayaStatus status;

    public PartStatusFile(String fileName, String fileHash, PapayaStatus status) {
        this.fileName = fileName;
        this.fileHash = fileHash;
        this.status = status;
    }

    public PartStatusFile() {

    }

    public String getFileHash() {
        return fileHash;
    }

    public PartStatusFile setFileHash(String fileHash) {
        this.fileHash = fileHash;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public PartStatusFile setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public PapayaStatus getStatus() {
        return status;
    }

    public PartStatusFile setStatus(PapayaStatus status) {
        this.status = status;
        return this;
    }
}
