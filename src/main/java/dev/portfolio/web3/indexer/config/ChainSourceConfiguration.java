package dev.portfolio.web3.indexer.config;

import dev.portfolio.web3.indexer.domain.ChainSource;
import dev.portfolio.web3.indexer.infrastructure.DemoChainSource;
import dev.portfolio.web3.indexer.infrastructure.Web3jChainSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChainSourceConfiguration {
    @Bean
    @ConditionalOnProperty(name = "chain.mode", havingValue = "demo", matchIfMissing = true)
    ChainSource demoChainSource() {
        return new DemoChainSource();
    }

    @Bean
    @ConditionalOnProperty(name = "chain.mode", havingValue = "rpc")
    ChainSource rpcChainSource(@Value("${chain.rpc-url:}") String rpcUrl,
                               @Value("${chain.id}") long chainId,
                               @Value("${chain.contract-address}") String contractAddress) {
        return new Web3jChainSource(rpcUrl, chainId, contractAddress);
    }
}
