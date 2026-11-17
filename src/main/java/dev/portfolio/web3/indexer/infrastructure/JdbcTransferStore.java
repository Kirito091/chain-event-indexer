package dev.portfolio.web3.indexer.infrastructure;

import dev.portfolio.web3.indexer.domain.TransferEvent;
import dev.portfolio.web3.indexer.domain.TransferStore;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public class JdbcTransferStore implements TransferStore {
    private static final RowMapper<TransferEvent> TRANSFER_ROW = (rs, rowNum) -> new TransferEvent(
            rs.getLong("chain_id"), rs.getString("contract_address"), rs.getString("sender"),
            rs.getString("recipient"), new BigInteger(rs.getString("amount_raw")),
            rs.getString("transaction_hash"), rs.getLong("log_index"), rs.getLong("block_number"),
            rs.getString("block_hash"));

    private final JdbcTemplate jdbcTemplate;

    public JdbcTransferStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long cursor(long chainId) {
        Long value = jdbcTemplate.query("SELECT block_number FROM indexer_cursor WHERE chain_id = ?",
                rs -> rs.next() ? rs.getLong(1) : null, chainId);
        return value == null ? 0 : value;
    }

    @Override
    public int saveTransfers(List<TransferEvent> events) {
        int inserted = 0;
        for (TransferEvent event : events) {
            try {
                inserted += jdbcTemplate.update("""
                        INSERT INTO transfer_event (chain_id, contract_address, sender, recipient, amount_raw,
                          transaction_hash, log_index, block_number, block_hash)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """, event.chainId(), event.contractAddress(), event.sender(), event.recipient(),
                        event.amountRaw().toString(), event.transactionHash(), event.logIndex(),
                        event.blockNumber(), event.blockHash());
            } catch (DuplicateKeyException ignored) {
                // The unique (chain_id, transaction_hash, log_index) constraint is the idempotency boundary.
            }
        }
        return inserted;
    }

    @Override
    public void saveCursor(long chainId, long blockNumber) {
        int updated = jdbcTemplate.update("UPDATE indexer_cursor SET block_number = ? WHERE chain_id = ?",
                blockNumber, chainId);
        if (updated == 0) {
            try {
                jdbcTemplate.update("INSERT INTO indexer_cursor (chain_id, block_number) VALUES (?, ?)",
                        chainId, blockNumber);
            } catch (DuplicateKeyException ignored) {
                jdbcTemplate.update("UPDATE indexer_cursor SET block_number = ? WHERE chain_id = ?",
                        blockNumber, chainId);
            }
        }
    }

    @Override
    public List<TransferEvent> findTransfers(long chainId, String address) {
        if (address == null || address.isBlank()) {
            return jdbcTemplate.query("SELECT * FROM transfer_event WHERE chain_id = ? ORDER BY block_number, log_index",
                    TRANSFER_ROW, chainId);
        }
        return jdbcTemplate.query("""
                SELECT * FROM transfer_event
                WHERE chain_id = ? AND (LOWER(sender) = LOWER(?) OR LOWER(recipient) = LOWER(?))
                ORDER BY block_number, log_index
                """, TRANSFER_ROW, chainId, address, address);
    }
}
