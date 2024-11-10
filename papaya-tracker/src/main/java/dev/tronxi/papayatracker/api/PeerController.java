package dev.tronxi.papayatracker.api;

import dev.tronxi.papayatracker.models.Peer;
import dev.tronxi.papayatracker.models.PeerDTO;
import dev.tronxi.papayatracker.usecases.AddPeerUseCase;
import dev.tronxi.papayatracker.usecases.RemovePeerUseCase;
import dev.tronxi.papayatracker.usecases.RetrievePeersUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("peer")
public class PeerController {

    private final AddPeerUseCase addPeerUseCase;
    private final RetrievePeersUseCase retrievePeersUseCase;
    private final RemovePeerUseCase removePeerUseCase;

    public PeerController(AddPeerUseCase addPeerUseCase, RetrievePeersUseCase retrievePeersUseCase, RemovePeerUseCase removePeerUseCase) {
        this.addPeerUseCase = addPeerUseCase;
        this.retrievePeersUseCase = retrievePeersUseCase;
        this.removePeerUseCase = removePeerUseCase;
    }

    @PostMapping
    ResponseEntity<Void> addPeer(@RequestBody PeerDTO peerDTO) {
        addPeerUseCase.add(Peer.of(peerDTO));
        return ResponseEntity.ok().build();
    }

    @GetMapping
    ResponseEntity<List<PeerDTO>> getAllPeers() {
        List<PeerDTO> peerDTOS = retrievePeersUseCase.retrievePeers()
                .stream().map(PeerDTO::of)
                .toList();
        return ResponseEntity.ok(peerDTOS);
    }

    @DeleteMapping
    ResponseEntity<Void> removePeer(@RequestBody PeerDTO peerDTO) {
        removePeerUseCase.remove(Peer.of(peerDTO));
        return ResponseEntity.ok().build();
    }
}
