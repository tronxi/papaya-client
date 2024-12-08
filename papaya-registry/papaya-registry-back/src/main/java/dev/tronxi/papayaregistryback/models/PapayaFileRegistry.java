package dev.tronxi.papayaregistryback.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import dev.tronxi.papayaregistryback.models.papayafile.PapayaFile;
import org.apache.solr.client.solrj.beans.Field;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PapayaFileRegistry {

    @Field("id")
    @JsonIgnore
    private String id;

    @Field("fileId")
    private String fileId;

    @Field("fileName")
    private String fileName;

    @Field("path")
    private String path;

    @Field("description")
    private String description;

    @Field("downloads")
    private Long downloads;
    private PapayaFile papayaFile;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
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
