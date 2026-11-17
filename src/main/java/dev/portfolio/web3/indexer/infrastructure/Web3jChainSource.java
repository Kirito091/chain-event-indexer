package dev.portfolio.web3.indexer.infrastructure;

import dev.portfolio.web3.indexer.domain.ChainSource;
import dev.portfolio.web3.indexer.domain.TransferEvent;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthChainId;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public final class Web3jChainSource implements ChainSource {
    private static final String TRANSFER_TOPIC = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef";
    private final Web3j web3j;
    private final long chainId;
    private final String contractAddress;

    public Web3jChainSource(String rpcUrl, long chainId, String contractAddress) {
        if (rpcUrl == null || rpcUrl.isBlank()) {
            throw new IllegalArgumentException("chain.rpc-url is required when chain.mode=rpc");
        }
        this.web3j = Web3j.build(new HttpService(rpcUrl));
        this.chainId = chainId;
        this.contractAddress = contractAddress;
    }

    @Override
    public long latestBlock() {
        try {
            verifyConfiguredChain();
            EthBlockNumber response = web3j.ethBlockNumber().send();
            if (response.hasError()) {
                throw new IllegalStateException("RPC error while reading block number: " + response.getError().getMessage());
            }
            return response.getBlockNumber().longValueExact();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to read latest block from configured RPC", exception);
        }
    }

    @Override
    public List<TransferEvent> transfers(long fromBlock, long toBlock) {
        EthFilter filter = new EthFilter(new DefaultBlockParameterNumber(BigInteger.valueOf(fromBlock)),
                new DefaultBlockParameterNumber(BigInteger.valueOf(toBlock)), contractAddress);
        filter.addSingleTopic(TRANSFER_TOPIC);
        try {
            verifyConfiguredChain();
            EthLog response = web3j.ethGetLogs(filter).send();
            if (response.hasError()) {
                throw new IllegalStateException("RPC error while reading transfer logs: " + response.getError().getMessage());
            }
            List<TransferEvent> events = new ArrayList<>();
            for (EthLog.LogResult<?> item : response.getLogs()) {
                Log log = (Log) item.get();
                List<String> topics = log.getTopics();
                if (topics.size() < 3 || log.getData() == null || log.getData().length() < 3) {
                    continue;
                }
                String sender = "0x" + topics.get(1).substring(topics.get(1).length() - 40);
                String recipient = "0x" + topics.get(2).substring(topics.get(2).length() - 40);
                BigInteger amount = new BigInteger(log.getData().substring(2), 16);
                events.add(new TransferEvent(chainId, log.getAddress(), sender, recipient, amount,
                        log.getTransactionHash(), log.getLogIndex().longValueExact(),
                        log.getBlockNumber().longValueExact(), log.getBlockHash()));
            }
            return List.copyOf(events);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to read transfer logs from configured RPC", exception);
        }
    }

    private void verifyConfiguredChain() throws Exception {
        EthChainId response = web3j.ethChainId().send();
        if (response.hasError()) {
            throw new IllegalStateException("RPC error while reading chain id: " + response.getError().getMessage());
        }
        long actualChainId = response.getChainId().longValueExact();
        if (actualChainId != chainId) {
            throw new IllegalStateException("configured chain id " + chainId
                    + " does not match RPC chain id " + actualChainId);
        }
    }
}
