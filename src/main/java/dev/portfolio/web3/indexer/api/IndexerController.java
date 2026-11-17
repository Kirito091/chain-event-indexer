package dev.portfolio.web3.indexer.api;

import dev.portfolio.web3.indexer.application.TransferIndexer;
import dev.portfolio.web3.indexer.domain.ChainSource;
import dev.portfolio.web3.indexer.domain.TransferEvent;
import dev.portfolio.web3.indexer.domain.TransferStore;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1")
public class IndexerController {
    private static final String EVM_ADDRESS = "^0x[a-fA-F0-9]{40}$";

    private final TransferIndexer indexer;
    private final ChainSource chainSource;
    private final TransferStore transferStore;
    private final long chainId;
    private final long confirmations;

    public IndexerController(TransferIndexer indexer, ChainSource chainSource, TransferStore transferStore,
                             @Value("${chain.id:11155111}") long chainId,
                             @Value("${chain.confirmations:3}") long confirmations) {
        this.indexer = indexer;
        this.chainSource = chainSource;
        this.transferStore = transferStore;
        this.chainId = chainId;
        this.confirmations = confirmations;
    }

    @GetMapping("/indexer/status")
    public StatusResponse status() {
        long latest = chainSource.latestBlock();
        return new StatusResponse(chainId, latest, Math.max(0, latest - confirmations),
                transferStore.cursor(chainId));
    }

    @PostMapping("/indexer/run-once")
    public TransferIndexer.ScanResult runOnce() {
        return indexer.scanOnce();
    }

    @GetMapping("/transfers")
    public List<TransferEvent> transfers(@RequestParam(required = false)
                                         @Pattern(regexp = EVM_ADDRESS, message = "address must be a 20-byte EVM address")
                                         String address) {
        return transferStore.findTransfers(chainId, address);
    }

    public record StatusResponse(long chainId, long latestBlock, long confirmationSafeBlock, long savedCursor) {
    }
}
