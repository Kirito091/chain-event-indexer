package dev.portfolio.web3.indexer.infrastructure;

import dev.portfolio.web3.indexer.domain.ChainSource;
import dev.portfolio.web3.indexer.domain.TransferEvent;

import java.math.BigInteger;
import java.util.List;

public final class DemoChainSource implements ChainSource {
    private static final long DEMO_CHAIN_ID = 11_155_111L;
    private static final String TOKEN = "0x00000000000000000000000000000000000000aa";
    private static final String ALICE = "0x00000000000000000000000000000000000000a1";
    private static final String BOB = "0x00000000000000000000000000000000000000b2";

    @Override
    public long latestBlock() {
        return 25;
    }

    @Override
    public List<TransferEvent> transfers(long fromBlock, long toBlock) {
        return List.of(
                new TransferEvent(DEMO_CHAIN_ID, TOKEN, ALICE, BOB, new BigInteger("2500000"),
                        "0x" + "1".repeat(64), 0, 12, "0x" + "a".repeat(64)),
                new TransferEvent(DEMO_CHAIN_ID, TOKEN, BOB, ALICE, new BigInteger("750000"),
                        "0x" + "2".repeat(64), 2, 19, "0x" + "b".repeat(64)))
                .stream()
                .filter(event -> event.blockNumber() >= fromBlock && event.blockNumber() <= toBlock)
                .toList();
    }
}
