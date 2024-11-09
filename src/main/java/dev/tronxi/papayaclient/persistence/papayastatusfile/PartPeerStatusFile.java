package dev.tronxi.papayaclient.persistence.papayastatusfile;

import dev.tronxi.papayaclient.peer.Peer;
import jakarta.persistence.*;

@Entity
public class PartPeerStatusFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Peer peer;
    private PartPeerStatus partPeerStatus;
    private long latestUpdateTime;

    public PartPeerStatusFile(Peer peer, PartPeerStatus partPeerStatus, long latestUpdateTime) {
        this.peer = peer;
        this.partPeerStatus = partPeerStatus;
        this.latestUpdateTime = latestUpdateTime;
    }

    public PartPeerStatusFile() {

    }

    public Peer getPeer() {
        return peer;
    }

    public PartPeerStatusFile setPeer(Peer peer) {
        this.peer = peer;
        return this;
    }

    public PartPeerStatus getPartPeerStatus() {
        return partPeerStatus;
    }

    public PartPeerStatusFile setPartPeerStatus(PartPeerStatus partPeerStatus) {
        this.partPeerStatus = partPeerStatus;
        return this;
    }

    public long getLatestUpdateTime() {
        return latestUpdateTime;
    }

    public PartPeerStatusFile setLatestUpdateTime(long latestUpdateTime) {
        this.latestUpdateTime = latestUpdateTime;
        return this;
    }

    @Override
    public String toString() {
        return "PartPeerStatusFile{" +
                "peer=" + peer +
                ", partPeerStatus=" + partPeerStatus +
                ", latestUpdateTime=" + latestUpdateTime +
                '}';
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
