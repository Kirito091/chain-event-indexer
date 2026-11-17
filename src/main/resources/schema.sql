CREATE TABLE IF NOT EXISTS indexer_cursor (
    chain_id BIGINT PRIMARY KEY,
    block_number BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS transfer_event (
    chain_id BIGINT NOT NULL,
    transaction_hash VARCHAR(80) NOT NULL,
    log_index BIGINT NOT NULL,
    contract_address VARCHAR(42) NOT NULL,
    sender VARCHAR(42) NOT NULL,
    recipient VARCHAR(42) NOT NULL,
    amount_raw VARCHAR(78) NOT NULL,
    block_number BIGINT NOT NULL,
    block_hash VARCHAR(80) NOT NULL,
    PRIMARY KEY (chain_id, transaction_hash, log_index)
);
