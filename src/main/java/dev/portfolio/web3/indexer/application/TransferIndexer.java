package dev.portfolio.web3.indexer.application;

import dev.portfolio.web3.indexer.domain.ChainSource;
import dev.portfolio.web3.indexer.domain.TransferEvent;
import dev.portfolio.web3.indexer.domain.TransferStore;

import java.util.List;
import java.util.Objects;

public final class TransferIndexer {
    private final ChainSource chainSource;
    private final TransferStore transferStore;
    private final long confirmations;
    private final long batchSize;
    private final long chainId;

    public TransferIndexer(ChainSource chainSource, TransferStore transferStore,
                           long confirmations, long batchSize, long chainId) {
        this.chainSource = Objects.requireNonNull(chainSource, "chainSource");
        this.transferStore = Objects.requireNonNull(transferStore, "transferStore");
        if (confirmations < 0 || batchSize < 1 || chainId < 1) {
            throw new IllegalArgumentException("confirmations must be >= 0, batchSize and chainId must be positive");
        }
        this.confirmations = confirmations;
        this.batchSize = batchSize;
        this.chainId = chainId;
    }

    public ScanResult scanOnce() {
        long head = chainSource.latestBlock();
        long safeHeight = Math.max(0, head - confirmations);
        long from = transferStore.cursor(chainId) + 1;
        if (from > safeHeight) {
            return new ScanResult(false, from, safeHeight, 0, head, safeHeight);
        }

        long to = Math.min(safeHeight, from + batchSize - 1);
        List<TransferEvent> events = chainSource.transfers(from, to);
        for (TransferEvent event : events) {
            if (event.chainId() != chainId || event.blockNumber() < from || event.blockNumber() > to) {
                throw new IllegalStateException("chain source returned an event outside the requested range");
            }
        }

        int inserted = transferStore.saveTransfers(events);
        transferStore.saveCursor(chainId, to);
        return new ScanResult(true, from, to, inserted, head, safeHeight);
    }

    public record ScanResult(boolean scanned, long fromBlock, long toBlock,
                             int insertedEvents, long latestBlock, long safeBlock) {
    }
}
