package dev.portfolio.web3.indexer.config;

import dev.portfolio.web3.indexer.application.TransferIndexer;
import dev.portfolio.web3.indexer.domain.ChainSource;
import dev.portfolio.web3.indexer.domain.TransferStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IndexerConfiguration {
    @Bean
    TransferIndexer transferIndexer(ChainSource chainSource, TransferStore transferStore,
                                    @Value("${chain.confirmations:3}") long confirmations,
                                    @Value("${indexer.batch-size:100}") long batchSize,
                                    @Value("${chain.id:11155111}") long chainId) {
        return new TransferIndexer(chainSource, transferStore, confirmations, batchSize, chainId);
    }
}
