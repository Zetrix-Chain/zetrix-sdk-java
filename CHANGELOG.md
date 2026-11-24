# Changelog — zetrix-sdk-java

## [1.1.2] — 2026-11-24

### Changed
- Depends on `zetrix-encryption` 1.2.1.RELEASE, which fixes private key error messages, SM2 signature length checks and private key loading checks. Upgrade both together.

### Fixed
- **Contract upgrade with only the payload or only the owner.** `ContractUpgradeOperation` with `sPayload` but no owner (or `sOwner` but no payload) failed with a generic `SYSTEM_ERROR` (20000), because both values were always written to the operation and an unset one is `null`. Only the flagged values are now checked and written.
- Contract owner addresses are validated: `ContractCreateOperation.owner` and `ContractUpgradeOperation.owner` (when `sOwner` is set and the owner is not empty) must be valid addresses, otherwise `INVALID_CONTRACT_OWNER_ERROR` (11079). An upgrade with `sPayload` set now also rejects an empty payload (`PAYLOAD_EMPTY_ERROR`), not only `null`.
- `evaluateFee` with `signatureKeys` (Scenario 1) returns the node's error code and description when the test transaction fails (for example `103` for a missing source account), instead of continuing with the fee estimate. It also rejects a negative `ceilLedgerSeq` (`INVALID_CEILLEDGERSEQ_ERROR`), as the `signatureNumber` path already did.
- `evaluateFee` rejects requests that mix inputs from different scenarios with `INVALID_EVALUATE_FEE_INPUT_ERROR` (11080): a `transactionBlob` together with `signatureKeys` or `operations`, or `signatures` without a `transactionBlob`. Previously one input was picked silently and the rest ignored.

### Changed
- Examples: the remaining hard-coded private keys in `DigitalAssetsDemo`, `submitTransactionDemo` and the `TransactionService` test are replaced with `YOUR_PRIVATE_KEY`.

### Added
- `ContractUpgradeAndFeeTest` — contract upgrade operation building and fee estimation input handling, against a local stand-in node.

---

## [1.1.1] — 2026-11-20

### Security
- **HTTPS certificate and hostname checks are now on**. `HttpKit` used a TrustManager that accepted any certificate and a HostnameVerifier that accepted any hostname, so all HTTPS calls to a node — including transaction submission and fee evaluation — could be intercepted and answered by anyone on the network path. HTTPS now uses the JVM's default trust store and hostname verification. A node with an expired, self-signed, untrusted or wrong-host certificate is refused and the call returns a connection error. See "HTTPS and node certificates" in the README for private-CA nodes.

### Changed
- Depends on `zetrix-encryption` **1.2.0.RELEASE** (was 1.1.0.RELEASE). Upgrade both together.
- No public API changes: `HttpKit` and all service method signatures are unchanged.

### Added
- `HttpKitTlsTest` — rejects self-signed and wrong-hostname certificates, accepts a trusted certificate for the right hostname.

---

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
