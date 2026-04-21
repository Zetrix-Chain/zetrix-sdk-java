package org.zetrix.sdk.example;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.zetrix.SDK;
import org.zetrix.common.ToBaseUnit;
import org.zetrix.crypto.Keypair;
import org.zetrix.encryption.model.KeyType;
import org.zetrix.model.request.*;
import org.zetrix.model.request.operation.*;
import org.zetrix.model.response.*;
import org.zetrix.model.response.result.*;
import org.zetrix.model.response.result.data.Signature;

/**
 * End-to-end live node tests for all four key types (backward compat, ED25519,
 * SM2, Dilithium3, Hybrid).
 *
 * Group A — Basic transaction flow: activate account → GasSend from new account.
 * Group B — Contract flow: activate → deploy contract → query → invoke.
 *
 * All tests use the live node at test-node.zetrix.com. Run each @Test method
 * individually from IntelliJ (right-click → Run).
 */
public class PqcTransactionDemo {

    private static final String NODE_URL        = "https://test-node.zetrix.com";
    private static final String SPONSOR_ADDRESS = "ZTX3ag1AT3JHSkTr3XTfnuhwHgtJ88z3SxY8T";
    private static final String SPONSOR_KEY     = "YOUR_PRIVATE_KEY";
    private static final long   INIT_BALANCE    = ToBaseUnit.ToUGas("0.1");

    // Minimal JS contract — query returns its input unchanged
    private static final String CONTRACT_PAYLOAD =
        "'use strict';" +
        "function init(bar){}" +
        "function main(input_str){let input=JSON.parse(input_str);if(input.do_foo){}}" +
        "function query(input_str){return input_str;}";

    SDK sdk = SDK.getInstance(NODE_URL);

    // =========================================================================
    // Backward compatibility — no-arg APIs must still produce ED25519 keys
    // =========================================================================
    @Test
    public void testBackwardCompatibility() {
        System.out.println("=== Backward Compatibility ===");

        Keypair kp = Keypair.generator();
        System.out.println("Keypair.generator() address: " + kp.getAddress());
        if (!kp.getAddress().startsWith("ZTX3")) {
            System.out.println("FAIL: expected ZTX3 prefix, got " + kp.getAddress());
            return;
        }

        AccountCreateResponse createResp = sdk.getAccountService().create();
        if (createResp.getErrorCode() != 0) {
            System.out.println("FAIL: create() error: " + createResp.getErrorDesc());
            return;
        }
        String addr = createResp.getResult().getAddress();
        System.out.println("AccountService.create() address: " + addr);
        if (!addr.startsWith("ZTX3")) {
            System.out.println("FAIL: expected ZTX3 prefix, got " + addr);
            return;
        }

        String hash = activateAndSend(kp.getAddress(), kp.getPrivateKey());
        System.out.println("Backward compat tx hash: " + hash);
        System.out.println("PASS");
    }

    // =========================================================================
    // Group A — Basic Transaction Tests
    // =========================================================================
    @Test
    public void testED25519Transaction() {
        runBasicTransactionTest(KeyType.ED25519);
    }

    @Test
    public void testSM2Transaction() {
        runBasicTransactionTest(KeyType.SM2);
    }

    @Test
    public void testDilithium3Transaction() {
        runBasicTransactionTest(KeyType.DILITHIUM3);
    }

    @Test
    public void testHybridTransaction() {
        runBasicTransactionTest(KeyType.HYBRID);
    }

    // =========================================================================
    // Group B — Contract Tests
    // =========================================================================
    @Test
    public void testED25519ContractOperations() {
        runContractTest(KeyType.ED25519);
    }

    @Test
    public void testSM2ContractOperations() {
        runContractTest(KeyType.SM2);
    }

    @Test
    public void testDilithium3ContractOperations() {
        runContractTest(KeyType.DILITHIUM3);
    }

    @Test
    public void testHybridContractOperations() {
        runContractTest(KeyType.HYBRID);
    }

    // =========================================================================
    // Shared flows
    // =========================================================================

    private void runBasicTransactionTest(KeyType keyType) {
        System.out.println("=== Basic Transaction: " + keyType + " ===");
        Keypair kp = Keypair.generator(keyType);
        System.out.println("Address:    " + kp.getAddress());
        System.out.println("PublicKey:  " + kp.getPublicKey());
        String hash = activateAndSend(kp.getAddress(), kp.getPrivateKey());
        System.out.println("Final tx hash: " + hash);
    }

    private void runContractTest(KeyType keyType) {
        System.out.println("=== Contract Test: " + keyType + " ===");
        Keypair kp = Keypair.generator(keyType);
        System.out.println("Address: " + kp.getAddress());

        // Step 1: activate the new account
        String activateHash = activate(kp.getAddress());
        if (activateHash == null) return;
        System.out.println("Activation hash: " + activateHash);

        // Step 2: deploy contract from PQC account
        String createHash = deployContract(kp.getAddress(), kp.getPrivateKey());
        if (createHash == null) return;
        System.out.println("Contract deploy hash: " + createHash);

        // Step 3: resolve deployed contract address
        String contractAddress = resolveContractAddress(createHash);
        if (contractAddress == null) return;
        System.out.println("Contract address: " + contractAddress);

        // Step 4: read-only query (no signing)
        queryContract(contractAddress);

        // Step 5: invoke the contract from the PQC account
        String invokeHash = invokeContract(kp.getAddress(), kp.getPrivateKey(), contractAddress);
        System.out.println("Contract invoke hash: " + invokeHash);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Activate a new address then send a small gas amount back to sponsor. */
    private String activateAndSend(String newAddress, String newPrivateKey) {
        String activateHash = activate(newAddress);
        if (activateHash == null) return null;
        System.out.println("Activation hash: " + activateHash);

        long nonce = getNonce(newAddress);
        GasSendOperation sendOp = new GasSendOperation();
        sendOp.setSourceAddress(newAddress);
        sendOp.setDestAddress(SPONSOR_ADDRESS);
        sendOp.setAmount(1000L);

        TransactionEvaluateFeeRequest feeReq = new TransactionEvaluateFeeRequest();
        feeReq.setSourceAddress(newAddress);
        feeReq.setNonce(nonce + 1);
        feeReq.addOperation(sendOp);
        feeReq.setGasPrice(5L);
        feeReq.setSignatureKeys(new String[]{newPrivateKey});

        TransactionEvaluateFeeResponse feeResp = sdk.getTransactionService().evaluateFee(feeReq);
        if (feeResp.getErrorCode() != 0) {
            System.out.println("Fee eval error: " + feeResp.getErrorDesc());
            return null;
        }
        TransactionEvaluateFeeResult feeResult = feeResp.getResult();
        System.out.println("feeLimit: " + feeResult.getTxs()[0].getTransactionEnv().getTransactionFees().getFeeLimit());

        TransactionSubmitRequest submitReq = new TransactionSubmitRequest();
        submitReq.setTransactionBlob(feeResult.getTransactionBlob());
        submitReq.setSignatures(feeResult.getSignatures());
        TransactionSubmitResponse submitResp = sdk.getTransactionService().submit(submitReq);
        if (submitResp.getErrorCode() != 0) {
            System.out.println("GasSend submit error: " + submitResp.getErrorDesc());
            return null;
        }
        return submitResp.getResult().getHash();
    }

    /** Activate (create on chain) a new address funded by the sponsor. */
    private String activate(String destAddress) {
        long sponsorNonce = getNonce(SPONSOR_ADDRESS);
        AccountActivateOperation activateOp = new AccountActivateOperation();
        activateOp.setSourceAddress(SPONSOR_ADDRESS);
        activateOp.setDestAddress(destAddress);
        activateOp.setInitBalance(INIT_BALANCE);

        TransactionBuildBlobRequest blobReq = new TransactionBuildBlobRequest();
        blobReq.setSourceAddress(SPONSOR_ADDRESS);
        blobReq.setNonce(sponsorNonce + 1);
        blobReq.setFeeLimit(ToBaseUnit.ToUGas("0.01"));
        blobReq.setGasPrice(5L);
        blobReq.addOperation(activateOp);

        TransactionBuildBlobResponse blobResp = sdk.getTransactionService().buildBlob(blobReq);
        if (blobResp.getErrorCode() != 0) {
            System.out.println("Build blob error: " + blobResp.getErrorDesc());
            return null;
        }
        String blob = blobResp.getResult().getTransactionBlob();

        TransactionSignRequest signReq = new TransactionSignRequest();
        signReq.setBlob(blob);
        signReq.addPrivateKey(SPONSOR_KEY);
        TransactionSignResponse signResp = sdk.getTransactionService().sign(signReq);
        if (signResp.getErrorCode() != 0) {
            System.out.println("Sign error: " + signResp.getErrorDesc());
            return null;
        }

        TransactionSubmitRequest submitReq = new TransactionSubmitRequest();
        submitReq.setTransactionBlob(blob);
        submitReq.setSignatures(signResp.getResult().getSignatures());
        TransactionSubmitResponse submitResp = sdk.getTransactionService().submit(submitReq);
        if (submitResp.getErrorCode() != 0) {
            System.out.println("Activate submit error: " + submitResp.getErrorDesc());
            return null;
        }
        return submitResp.getResult().getHash();
    }

    /** Deploy the simple test contract from the given account. */
    private String deployContract(String fromAddress, String fromPrivateKey) {
        long nonce = getNonce(fromAddress);
        ContractCreateOperation createOp = new ContractCreateOperation();
        createOp.setSourceAddress(fromAddress);
        createOp.setInitBalance(ToBaseUnit.ToUGas("0.01"));
        createOp.setPayload(CONTRACT_PAYLOAD);
        createOp.setMetadata("pqc contract test");

        TransactionEvaluateFeeRequest feeReq = new TransactionEvaluateFeeRequest();
        feeReq.setSourceAddress(fromAddress);
        feeReq.setNonce(nonce + 1);
        feeReq.addOperation(createOp);
        feeReq.setGasPrice(5L);
        feeReq.setSignatureKeys(new String[]{fromPrivateKey});

        TransactionEvaluateFeeResponse feeResp = sdk.getTransactionService().evaluateFee(feeReq);
        if (feeResp.getErrorCode() != 0) {
            System.out.println("Contract deploy fee eval error: " + feeResp.getErrorDesc());
            return null;
        }

        TransactionSubmitRequest submitReq = new TransactionSubmitRequest();
        submitReq.setTransactionBlob(feeResp.getResult().getTransactionBlob());
        submitReq.setSignatures(feeResp.getResult().getSignatures());
        TransactionSubmitResponse submitResp = sdk.getTransactionService().submit(submitReq);
        if (submitResp.getErrorCode() != 0) {
            System.out.println("Contract deploy submit error: " + submitResp.getErrorDesc());
            return null;
        }
        return submitResp.getResult().getHash();
    }

    /** Resolve the deployed contract address from the creation tx hash. */
    private String resolveContractAddress(String txHash) {
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        ContractGetAddressRequest req = new ContractGetAddressRequest();
        req.setHash(txHash);
        ContractGetAddressResponse resp = sdk.getContractService().getAddress(req);
        if (resp.getErrorCode() != 0) {
            System.out.println("getContractAddress error: " + resp.getErrorDesc());
            return null;
        }
        if (resp.getResult().getContractAddressInfos() == null
                || resp.getResult().getContractAddressInfos().isEmpty()) {
            System.out.println("No contract address found for tx: " + txHash);
            return null;
        }
        return resp.getResult().getContractAddressInfos().get(0).getContractAddress();
    }

    /** Read-only contract query — no signing needed. */
    private void queryContract(String contractAddress) {
        ContractCallRequest req = new ContractCallRequest();
        req.setContractAddress(contractAddress);
        req.setOptType(2);
        req.setInput("{\"method\":\"query\",\"params\":{\"test\":\"pqc\"}}");
        ContractCallResponse resp = sdk.getContractService().call(req);
        System.out.println("Contract query: errorCode=" + resp.getErrorCode()
                + (resp.getErrorCode() == 0
                    ? ", result=" + JSON.toJSONString(resp.getResult())
                    : ", msg=" + resp.getErrorDesc()));
    }

    /** Invoke the contract from a PQC account using evaluateFee Scenario 1. */
    private String invokeContract(String fromAddress, String fromPrivateKey, String contractAddress) {
        long nonce = getNonce(fromAddress);
        ContractInvokeByGasOperation invokeOp = new ContractInvokeByGasOperation();
        invokeOp.setSourceAddress(fromAddress);
        invokeOp.setContractAddress(contractAddress);
        invokeOp.setZtxAmount(0L);
        invokeOp.setInput("{\"method\":\"main\",\"params\":{\"do_foo\":false}}");

        TransactionEvaluateFeeRequest feeReq = new TransactionEvaluateFeeRequest();
        feeReq.setSourceAddress(fromAddress);
        feeReq.setNonce(nonce + 1);
        feeReq.addOperation(invokeOp);
        feeReq.setGasPrice(5L);
        feeReq.setSignatureKeys(new String[]{fromPrivateKey});

        TransactionEvaluateFeeResponse feeResp = sdk.getTransactionService().evaluateFee(feeReq);
        if (feeResp.getErrorCode() != 0) {
            System.out.println("Contract invoke fee eval error: " + feeResp.getErrorDesc());
            return null;
        }

        TransactionSubmitRequest submitReq = new TransactionSubmitRequest();
        submitReq.setTransactionBlob(feeResp.getResult().getTransactionBlob());
        submitReq.setSignatures(feeResp.getResult().getSignatures());
        TransactionSubmitResponse submitResp = sdk.getTransactionService().submit(submitReq);
        if (submitResp.getErrorCode() != 0) {
            System.out.println("Contract invoke submit error: " + submitResp.getErrorDesc());
            return null;
        }
        return submitResp.getResult().getHash();
    }

    private long getNonce(String address) {
        AccountGetNonceRequest req = new AccountGetNonceRequest();
        req.setAddress(address);
        AccountGetNonceResponse resp = sdk.getAccountService().getNonce(req);
        if (resp.getErrorCode() != 0) {
            System.out.println("getNonce error for " + address + ": " + resp.getErrorDesc());
            return 0;
        }
        return resp.getResult().getNonce() != null ? resp.getResult().getNonce() : 0;
    }
}
