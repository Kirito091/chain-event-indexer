package dev.portfolio.web3.indexer;

import dev.portfolio.web3.indexer.application.TransferIndexer;
import dev.portfolio.web3.indexer.domain.TransferStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class IndexerApplicationSmokeTest {
    @Autowired
    private TransferIndexer indexer;

    @Autowired
    private TransferStore transferStore;

    @Test
    void demoApplicationIndexesAndPersistsItsSampleTransfers() {
        TransferIndexer.ScanResult first = indexer.scanOnce();
        TransferIndexer.ScanResult second = indexer.scanOnce();

        assertThat(first.insertedEvents()).isEqualTo(2);
        assertThat(second.scanned()).isFalse();
        assertThat(transferStore.findTransfers(11_155_111L, null)).hasSize(2);
    }
}
