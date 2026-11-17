package dev.portfolio.web3.indexer.domain;

import java.math.BigInteger;
import java.util.Locale;
import java.util.Objects;

public record TransferEvent(
        long chainId,
        String contractAddress,
        String sender,
        String recipient,
        BigInteger amountRaw,
        String transactionHash,
        long logIndex,
        long blockNumber,
        String blockHash) {

    public TransferEvent {
        if (chainId <= 0 || logIndex < 0 || blockNumber < 0) {
            throw new IllegalArgumentException("chainId must be positive and block/log positions cannot be negative");
        }
        if (amountRaw == null || amountRaw.signum() < 0) {
            throw new IllegalArgumentException("amountRaw must be a non-negative integer");
        }
        contractAddress = normalize(contractAddress);
        sender = normalize(sender);
        recipient = normalize(recipient);
        transactionHash = Objects.requireNonNull(transactionHash, "transactionHash").toLowerCase(Locale.ROOT);
        blockHash = Objects.requireNonNull(blockHash, "blockHash").toLowerCase(Locale.ROOT);
    }

    private static String normalize(String value) {
        return Objects.requireNonNull(value, "address").toLowerCase(Locale.ROOT);
    }
}
