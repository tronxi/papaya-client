package dev.tronxi.papayaclient.persistence.papayafile;

public class PartFile {
    private String fileName;
    private String fileHash;

    public PartFile(String fileName, String fileHash) {
        this.fileName = fileName;
        this.fileHash = fileHash;
    }

    public PartFile() {
    }

    public String getFileHash() {
        return fileHash;
    }

    public PartFile setFileHash(String fileHash) {
        this.fileHash = fileHash;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public PartFile setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    @Override
    public String toString() {
        return "PartFile{" +
                "fileName='" + fileName + '\'' +
                ", fileHash='" + fileHash + '\'' +
                '}';
    }
}
