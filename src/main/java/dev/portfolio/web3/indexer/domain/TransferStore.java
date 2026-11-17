package dev.portfolio.web3.indexer.domain;

import java.util.List;

public interface TransferStore {
    long cursor(long chainId);

    int saveTransfers(List<TransferEvent> events);

    void saveCursor(long chainId, long blockNumber);

    List<TransferEvent> findTransfers(long chainId, String address);
}
