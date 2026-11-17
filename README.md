# Chain Event Indexer

A small Java/Spring Boot service that indexes ERC-20 `Transfer` events from an EVM-compatible RPC. It is designed as a portfolio project around backend concerns that matter in wallet and exchange systems: confirmation safety, bounded scans, durable cursors, and idempotent writes.

## What it demonstrates

- A chain adapter boundary with a deterministic local demo source and an optional read-only web3j RPC source.
- Confirmation-aware scanning: the indexer stops at `head - confirmations` and scans no more than one configured batch at a time.
- Restart-safe progress stored in JDBC, with `(chain_id, transaction_hash, log_index)` as the event idempotency key.
- REST endpoints for operational status, one-shot scans, and address-filtered transfer history.
- No signing, private-key handling, seed phrases, or real-fund operations.

## Run locally

Requirements: Java 21 and Maven 3.6.3+.

```bash
mvn test
mvn spring-boot:run
```

The default profile uses a local file-backed H2 database under `data/` and a deterministic demo source; it does not need a node URL or wallet. The database file is ignored by Git. Tests override it with an isolated in-memory database. Try:

```bash
curl http://localhost:8080/api/v1/indexer/status
curl -X POST http://localhost:8080/api/v1/indexer/run-once
curl http://localhost:8080/api/v1/transfers
```

## Optional read-only RPC mode

Set `CHAIN_MODE=rpc`, `CHAIN_ID`, `RPC_URL`, and `TOKEN_CONTRACT` before starting the service. Use a public test network and a token contract you control. The RPC adapter checks `eth_chainId` before each scan and fails closed if the endpoint is on a different chain. It only reads the head and logs; it has no transaction-signing code.

## API

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/indexer/status` | Head, safe height, chain id, saved cursor |
| `POST` | `/api/v1/indexer/run-once` | Index one bounded block range |
| `GET` | `/api/v1/transfers?address=0x...` | List all transfers or filter by sender/recipient |

The concise behavioral specification is in [`docs/SPEC.md`](docs/SPEC.md).
