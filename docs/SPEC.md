# Chain Event Indexer — Scope

## Goal

Demonstrate a Java/Spring Boot backend that indexes ERC-20 `Transfer` logs from an EVM-compatible chain, safely resumes after restart, and does not persist duplicate logs.

## User-visible behavior

- `GET /api/v1/indexer/status` reports the chain id, latest observed block, confirmation-safe height, and saved cursor.
- `POST /api/v1/indexer/run-once` scans at most one configured block batch up to `head - confirmations`.
- `GET /api/v1/transfers` returns indexed transfers, optionally filtered by an address.
- The default `demo` source is deterministic and requires no RPC endpoint or wallet.
- The optional `rpc` source reads logs only; it cannot sign or submit transactions.

## Invariants

- A log is uniquely identified by `(chainId, transactionHash, logIndex)`.
- The cursor advances only after the scan's events are saved.
- Blocks newer than the confirmation-safe height are not indexed.
- Invalid EVM addresses are rejected at the API boundary.
- No private keys, seed phrases, or real funds are required.

## Acceptance checks

- A scan never passes the confirmation-safe height and respects the batch size.
- Re-running the same range does not duplicate transfer records.
- A scan with no finalized blocks leaves the cursor unchanged.
- The demo application starts without external services and exposes health/status endpoints.
- Maven tests pass on Java 21.
