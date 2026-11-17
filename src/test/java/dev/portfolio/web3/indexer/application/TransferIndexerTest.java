package dev.portfolio.web3.indexer.application;

import dev.portfolio.web3.indexer.domain.ChainSource;
import dev.portfolio.web3.indexer.domain.TransferEvent;
import dev.portfolio.web3.indexer.domain.TransferStore;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransferIndexerTest {
    private static final String CONTRACT = "0x0000000000000000000000000000000000000001";
    private static final String ACCOUNT = "0x0000000000000000000000000000000000000002";

    @Test
    void scansOnlyConfirmedBlocksAndAdvancesTheCursorAfterSavingEvents() {
        FakeSource source = new FakeSource(15, List.of(transfer(11)));
        FakeStore store = new FakeStore(9);

        TransferIndexer.ScanResult result = new TransferIndexer(source, store, 3, 4, 1).scanOnce();

        assertThat(source.lastFrom).isEqualTo(10);
        assertThat(source.lastTo).isEqualTo(12);
        assertThat(store.cursor).isEqualTo(12);
        assertThat(result.fromBlock()).isEqualTo(10);
        assertThat(result.toBlock()).isEqualTo(12);
        assertThat(result.insertedEvents()).isEqualTo(1);
    }

    @Test
    void doesNotAdvanceWhenThereAreNoFinalizedBlocks() {
        FakeSource source = new FakeSource(11, List.of());
        FakeStore store = new FakeStore(9);

        TransferIndexer.ScanResult result = new TransferIndexer(source, store, 3, 100, 1).scanOnce();

        assertThat(result.scanned()).isFalse();
        assertThat(store.cursor).isEqualTo(9);
        assertThat(source.lastFrom).isEqualTo(-1);
    }

    @Test
    void reportsOnlyNewEventsWhenTheSameLogIsReturnedAgain() {
        FakeSource source = new FakeSource(10, List.of(transfer(10)));
        FakeStore store = new FakeStore(8);
        store.saveTransfers(List.of(transfer(10)));

        TransferIndexer.ScanResult result = new TransferIndexer(source, store, 0, 10, 1).scanOnce();

        assertThat(result.insertedEvents()).isZero();
        assertThat(store.events).hasSize(1);
        assertThat(store.cursor).isEqualTo(10);
    }

    private static TransferEvent transfer(long block) {
        return new TransferEvent(1, CONTRACT, ACCOUNT, CONTRACT, BigInteger.TEN,
                "0xabc", 0, block, "0xblock" + block);
    }

    private static final class FakeSource implements ChainSource {
        private final long head;
        private final List<TransferEvent> events;
        private long lastFrom = -1;
        private long lastTo = -1;

        private FakeSource(long head, List<TransferEvent> events) {
            this.head = head;
            this.events = events;
        }

        @Override
        public long latestBlock() {
            return head;
        }

        @Override
        public List<TransferEvent> transfers(long fromBlock, long toBlock) {
            lastFrom = fromBlock;
            lastTo = toBlock;
            return events;
        }
    }

    private static final class FakeStore implements TransferStore {
        private long cursor;
        private final List<TransferEvent> events = new ArrayList<>();

        private FakeStore(long cursor) {
            this.cursor = cursor;
        }

        @Override
        public long cursor(long chainId) {
            return cursor;
        }

        @Override
        public int saveTransfers(List<TransferEvent> newEvents) {
            int inserted = 0;
            for (TransferEvent event : newEvents) {
                boolean exists = events.stream().anyMatch(saved -> saved.chainId() == event.chainId()
                        && saved.transactionHash().equals(event.transactionHash())
                        && saved.logIndex() == event.logIndex());
                if (!exists) {
                    events.add(event);
                    inserted++;
                }
            }
            return inserted;
        }

        @Override
        public void saveCursor(long chainId, long blockNumber) {
            cursor = blockNumber;
        }

        @Override
        public List<TransferEvent> findTransfers(long chainId, String address) {
            return List.copyOf(events);
        }
    }
}
