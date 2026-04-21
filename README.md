# Zetrix Java SDK

Java SDK for the Zetrix blockchain. Supports account management, transaction building, contract operations, and fee estimation for all four key algorithms — **ED25519**, **SM2**, **Dilithium3 (ML-DSA-65 / FIPS 204)**, and **Hybrid (ED25519 + Dilithium3)**.

## Contents

| Directory | Description |
|-----------|-------------|
| [`src/`](src/) | SDK source code and `pom.xml` |
| [`examples/`](examples/) | Standalone Maven example project |

## Requirements

- Java 8 or above (JDK 8–21 tested)
- Maven 3.x

## Installation

### Maven

```xml
<dependency>
  <groupId>org.zetrix.sdk</groupId>
  <artifactId>zetrix-sdk</artifactId>
  <version>1.1.0</version>
</dependency>
```

### JAR

Download `zetrix-sdk-{version}.jar` and add it to your classpath together with the bundled dependency JARs.

## Quick Start

### Initialize the SDK

```java
import org.zetrix.SDK;
import org.zetrix.model.request.SDKInitRequest;

SDKInitRequest initRequest = new SDKInitRequest();
initRequest.setUrl("http://<node-host>:<port>");
SDK sdk = SDK.getInstance();
sdk.init(initRequest);
```

### Create an account

```java
import org.zetrix.crypto.Keypair;
import org.zetrix.encryption.model.KeyType;

// ED25519 (default — backward compatible)
Keypair ed = Keypair.generator();

// SM2
Keypair sm2 = Keypair.generator(KeyType.SM2);

// Dilithium3 (post-quantum)
Keypair dil3 = Keypair.generator(KeyType.DILITHIUM3);

// Hybrid (post-quantum + classical)
Keypair hybrid = Keypair.generator(KeyType.HYBRID);

System.out.println(dil3.getAddress());    // ZTX4...
System.out.println(hybrid.getAddress());  // ZTX5...
```

Via the account service:

```java
import org.zetrix.model.response.AccountCreateResponse;

AccountCreateResponse resp = sdk.getAccountService().create(KeyType.DILITHIUM3);
System.out.println(resp.getResult().getAddress());
System.out.println(resp.getResult().getPrivateKey());
```

### Build, sign, and submit a transaction

```java
TransactionBuildBlobRequest blobReq = new TransactionBuildBlobRequest();
blobReq.setSourceAddress(address);
blobReq.setNonce(nonce + 1);
blobReq.setFeeLimit(1000000L);
blobReq.setGasPrice(10L);
blobReq.addOperation(new GasSendOperation());

String blob = sdk.getTransactionService().buildBlob(blobReq).getResult().getTransactionBlob();

TransactionSignRequest signReq = new TransactionSignRequest();
signReq.setBlob(blob);
signReq.addPrivateKey(privateKey);   // any of the four key types
Signature[] sigs = sdk.getTransactionService().sign(signReq).getResult().getSignatures();

TransactionSubmitRequest submitReq = new TransactionSubmitRequest();
submitReq.setTransactionBlob(blob);
submitReq.setSignatures(sigs);
String hash = sdk.getTransactionService().submit(submitReq).getResult().getHash();
```

### Fee estimation — Scenario 1 (recommended for PQC keys)

Build, estimate, and get a ready-to-submit blob in one call. Supports all key types including Dilithium3 and Hybrid.

```java
TransactionEvaluateFeeRequest feeReq = new TransactionEvaluateFeeRequest();
feeReq.setSourceAddress(address);
feeReq.setNonce(nonce + 1);
feeReq.addOperation(gasSendOp);
feeReq.setGasPrice(10L);
feeReq.setSignatureKeys(new String[]{ privateKey });  // Scenario 1

TransactionEvaluateFeeResponse feeResp = sdk.getTransactionService().evaluateFee(feeReq);
// feeResp.getResult() contains transactionBlob + signatures — submit directly:
TransactionSubmitRequest submitReq = new TransactionSubmitRequest();
submitReq.setTransactionBlob(feeResp.getResult().getTransactionBlob());
submitReq.setSignatures(feeResp.getResult().getSignatures());
sdk.getTransactionService().submit(submitReq);
```

### Fee estimation — Scenario 2 (pre-built blob)

```java
TransactionEvaluateFeeRequest feeReq = new TransactionEvaluateFeeRequest();
feeReq.setTransactionBlob(blobHex);
feeReq.setSignatures(signatures);  // optional
sdk.getTransactionService().evaluateFee(feeReq);
```

## Fee Estimation Scenarios

| Scenario | Input | Description |
|----------|-------|-------------|
| 1 | `signatureKeys` | Two-step: build with `feeLimit=0` → estimate → rebuild with real fee → re-sign. Returns ready-to-submit blob + signatures. |
| 2 / 4 | `transactionBlob` ± `signatures` | Pass pre-built blob directly to `/testTransaction`. |
| 3 | *(none of the above)* | Legacy `transaction_json` path; `signatureNumber` drives the estimate. |

## Address Prefixes

| Key Type | Address Prefix |
|----------|----------------|
| ED25519, SM2 | `ZTX3` |
| Dilithium3 | `ZTX4` |
| Hybrid | `ZTX5` |

## Running the Examples

```bash
cd examples
mvn test -pl . -Dtest=PqcTransactionDemo#testDilithium3Transaction
```

See [`examples/src/main/java/org/zetrix/sdk/example/PqcTransactionDemo.java`](examples/src/main/java/org/zetrix/sdk/example/PqcTransactionDemo.java) for a complete end-to-end example covering activation, GasSend, contract deploy, query, and invoke for all four key types.

## Documentation

Full API reference: https://docs.zetrix.com/en/sdk/java
