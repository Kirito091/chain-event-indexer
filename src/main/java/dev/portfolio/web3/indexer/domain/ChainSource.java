package dev.portfolio.web3.indexer.domain;

import java.util.List;

public interface ChainSource {
    long latestBlock();

    List<TransferEvent> transfers(long fromBlock, long toBlock);
}
