# Changelog — zetrix-sdk-java

## [1.1.0] — 2026-04-21

### Added
- **Multi-algorithm account creation** — `Keypair.generator(KeyType)` overload; `AccountServiceImpl.create(KeyType)` overload; both default to ED25519 for full backward compatibility
- **Fee estimation Scenario 1** (`signatureKeys`) — two-step flow: build temp blob with `feeLimit=0`, sign with the supplied private key(s), POST to `/testTransaction` for an accurate fee estimate, rebuild with real fee, re-sign; returns `transactionBlob` + `Signature[]` ready for direct submission; supports all key types including Dilithium3 and Hybrid
- **Fee estimation Scenario 2 / 4** (`transactionBlob`) — pass a pre-built hex blob (with optional pre-computed signatures) directly to `/testTransaction`; no re-signing required
- `TransactionEvaluateFeeRequest.signatureKeys` field (String[]) — encoded private key strings for Scenario 1
- `TransactionEvaluateFeeRequest.transactionBlob` field (String) — hex transaction blob for Scenario 2/4
- `TransactionEvaluateFeeRequest.signatures` field (Signature[]) — optional pre-computed signatures for Scenario 2/4
- `TransactionEvaluateFeeResult.transactionBlob` and `.signatures` — Scenario 1 callers can submit the result directly without re-signing
- `PqcTransactionDemo.java` — complete end-to-end live-node test suite for all four key types: activate account, GasSend, contract deploy, read-only contract query, contract invoke
- Improved fee estimation error messages — actual node error code and description are now surfaced when `/testTransaction` returns a non-zero error code

### Changed
- Fee estimation Scenario 3 (legacy `transaction_json` path) is unchanged — existing callers are fully backward compatible
- `gasPrice` default in evaluateFee Scenario 1 changed from `1000L` to `10L` to match live-node requirements

### Fixed
- Fee estimation error message previously returned the generic string `"Fee estimation failed"` regardless of the actual node response; now appends `[node error <code>: <desc>]` when the node returns a non-zero error code

---

## [1.0.5] — (prior release)

- ED25519 only
