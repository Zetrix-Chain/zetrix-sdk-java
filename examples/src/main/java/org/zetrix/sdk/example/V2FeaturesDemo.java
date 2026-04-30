package org.zetrix.sdk.example;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.zetrix.SDK;
import org.zetrix.common.ToBaseUnit;
import org.zetrix.model.request.*;
import org.zetrix.model.request.operation.*;
import org.zetrix.model.response.*;

/**
 * Live-node integration demos for the three features merged from v2.0.0-release:
 *   1. hello()           — node connectivity check
 *   2. getCache()        — transaction cache query
 *   3. Contract upgrade  — deploy a contract then upgrade its payload
 *
 * Requires env vars:
 *   ZETRIX_SPONSOR_ADDRESS — funded account address
 *   ZETRIX_SPONSOR_KEY     — sponsor private key
 *   ZETRIX_NODE_URL        — node URL (defaults to test-node.zetrix.com)
 *
 * Run each @Test method individually from IntelliJ (right-click → Run).
 */
public class V2FeaturesDemo {

    private static final String NODE_URL =
            System.getenv("ZETRIX_NODE_URL") != null
                    ? System.getenv("ZETRIX_NODE_URL")
                    : "https://test-node.zetrix.com";
    private static final String SPONSOR_ADDRESS = System.getenv("ZETRIX_SPONSOR_ADDRESS");
    private static final String SPONSOR_KEY     = System.getenv("ZETRIX_SPONSOR_KEY");

    private static final String CONTRACT_V1 =
            "'use strict';\n" +
            "function init(bar) { return; }\n" +
            "function main(input) { return; }\n" +
            "function query(input) { return 'v1'; }";

    private static final String CONTRACT_V2 =
            "'use strict';\n" +
            "function init(bar) { return; }\n" +
            "function main(input) { return; }\n" +
            "function query(input) { return 'v2'; }";

    SDK sdk = SDK.getInstance(NODE_URL);

    private void requireEnvVars() {
        if (SPONSOR_ADDRESS == null || SPONSOR_KEY == null) {
            throw new IllegalStateException(
                    "Set ZETRIX_SPONSOR_ADDRESS and ZETRIX_SPONSOR_KEY to run live demos");
        }
    }

    // =========================================================================
    // 1. hello() — node connectivity
    // =========================================================================

    @Test
    public void testHello() {
        System.out.println("=== hello() ===");
        HelloResponse resp = sdk.hello();
        System.out.println("errorCode : " + resp.getErrorCode());
        System.out.println("errorDesc : " + resp.getErrorDesc());
        if (resp.getErrorCode() == 0) {
            System.out.println("result    : " + JSON.toJSONString(resp.getResult()));
            System.out.println("PASS");
        } else {
            System.out.println("FAIL — node unreachable or returned error");
        }
    }

    // =========================================================================
    // 2. getCache() — transaction cache queries
    // =========================================================================

    @Test
    public void testGetCacheNoFilter() {
        System.out.println("=== getCache() — no filter (recent cached txs) ===");
        TransactionGetCacheRequest req = new TransactionGetCacheRequest();
        TransactionGetCacheResponse resp = sdk.getTransactionService().getCache(req);
        System.out.println("errorCode : " + resp.getErrorCode());
        System.out.println("errorDesc : " + resp.getErrorDesc());
        if (resp.getErrorCode() == 0) {
            System.out.println("result    : " + JSON.toJSONString(resp.getResult()));
            System.out.println("PASS");
        } else {
            System.out.println("INFO — cache may be empty on a quiet node (errorCode=" + resp.getErrorCode() + ")");
        }
    }

    @Test
    public void testGetCacheWithLimit() {
        System.out.println("=== getCache() — limit=5 ===");
        TransactionGetCacheRequest req = new TransactionGetCacheRequest();
        req.setLimit(5);
        TransactionGetCacheResponse resp = sdk.getTransactionService().getCache(req);
        System.out.println("errorCode : " + resp.getErrorCode());
        System.out.println("result    : " + JSON.toJSONString(resp.getResult()));
    }

    // =========================================================================
    // 3. Contract upgrade — deploy v1, upgrade to v2, verify via query
    // =========================================================================

    @Test
    public void testContractUpgrade() {
        requireEnvVars();
        System.out.println("=== Contract Upgrade ===");

        // Step 1: deploy v1
        String deployHash = deployContract(SPONSOR_ADDRESS, SPONSOR_KEY, CONTRACT_V1);
        if (deployHash == null) return;
        System.out.println("Deploy hash: " + deployHash);
        if (!waitForTx(deployHash)) {
            System.out.println("FAIL — deploy tx not confirmed");
            return;
        }

        // Step 2: resolve contract address
        String contractAddress = resolveContractAddress(deployHash);
        if (contractAddress == null) return;
        System.out.println("Contract address: " + contractAddress);

        // Step 3: query v1
        System.out.println("Query before upgrade: " + queryContract(contractAddress));

        // Step 4: upgrade to v2
        String upgradeHash = upgradeContract(SPONSOR_ADDRESS, SPONSOR_KEY, contractAddress, CONTRACT_V2);
        if (upgradeHash == null) return;
        System.out.println("Upgrade hash: " + upgradeHash);
        if (!waitForTx(upgradeHash)) {
            System.out.println("FAIL — upgrade tx not confirmed");
            return;
        }

        // Step 5: query v2
        System.out.println("Query after upgrade: " + queryContract(contractAddress));
        System.out.println("PASS");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private String deployContract(String fromAddress, String fromKey, String payload) {
        long nonce = getNonce(fromAddress);
        ContractCreateOperation createOp = new ContractCreateOperation();
        createOp.setSourceAddress(fromAddress);
        createOp.setInitBalance(ToBaseUnit.ToUGas("0.01"));
        createOp.setPayload(payload);
        createOp.setOwner(fromAddress);

        TransactionEvaluateFeeRequest feeReq = new TransactionEvaluateFeeRequest();
        feeReq.setSourceAddress(fromAddress);
        feeReq.setNonce(nonce + 1);
        feeReq.addOperation(createOp);
        feeReq.setGasPrice(10L);
        feeReq.setSignatureKeys(new String[]{fromKey});

        TransactionEvaluateFeeResponse feeResp = sdk.getTransactionService().evaluateFee(feeReq);
        if (feeResp.getErrorCode() != 0) {
            System.out.println("Deploy fee eval error: " + feeResp.getErrorDesc());
            return null;
        }

        TransactionSubmitRequest submitReq = new TransactionSubmitRequest();
        submitReq.setTransactionBlob(feeResp.getResult().getTransactionBlob());
        submitReq.setSignatures(feeResp.getResult().getSignatures());
        TransactionSubmitResponse submitResp = sdk.getTransactionService().submit(submitReq);
        if (submitResp.getErrorCode() != 0) {
            System.out.println("Deploy submit error: " + submitResp.getErrorDesc());
            return null;
        }
        return submitResp.getResult().getHash();
    }

    private String upgradeContract(String ownerAddress, String ownerKey,
                                   String contractAddress, String newPayload) {
        long nonce = getNonce(ownerAddress);
        ContractUpgradeOperation upgradeOp = new ContractUpgradeOperation();
        upgradeOp.setSourceAddress(ownerAddress);
        upgradeOp.setContractAddress(contractAddress);
        upgradeOp.setsPayload(true);
        upgradeOp.setPayload(newPayload);
        upgradeOp.setsOwner(false);
        upgradeOp.setOwner("");  // not changing owner

        TransactionEvaluateFeeRequest feeReq = new TransactionEvaluateFeeRequest();
        feeReq.setSourceAddress(ownerAddress);
        feeReq.setNonce(nonce + 1);
        feeReq.addOperation(upgradeOp);
        feeReq.setGasPrice(10L);
        feeReq.setSignatureKeys(new String[]{ownerKey});

        TransactionEvaluateFeeResponse feeResp = sdk.getTransactionService().evaluateFee(feeReq);
        if (feeResp.getErrorCode() != 0) {
            System.out.println("Upgrade fee eval error: " + feeResp.getErrorDesc());
            return null;
        }

        TransactionSubmitRequest submitReq = new TransactionSubmitRequest();
        submitReq.setTransactionBlob(feeResp.getResult().getTransactionBlob());
        submitReq.setSignatures(feeResp.getResult().getSignatures());
        TransactionSubmitResponse submitResp = sdk.getTransactionService().submit(submitReq);
        if (submitResp.getErrorCode() != 0) {
            System.out.println("Upgrade submit error: " + submitResp.getErrorDesc());
            return null;
        }
        return submitResp.getResult().getHash();
    }

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
            System.out.println("No contract address for tx: " + txHash);
            return null;
        }
        return resp.getResult().getContractAddressInfos().get(0).getContractAddress();
    }

    private String queryContract(String contractAddress) {
        ContractCallRequest req = new ContractCallRequest();
        req.setContractAddress(contractAddress);
        req.setFeeLimit(1000000000L);
        req.setOptType(2);
        req.setInput("{\"method\":\"query\",\"params\":{}}");
        ContractCallResponse resp = sdk.getContractService().call(req);
        if (resp.getErrorCode() != 0) return "error: " + resp.getErrorDesc();
        return JSON.toJSONString(resp.getResult());
    }

    private boolean waitForTx(String hash) {
        TransactionGetInfoRequest req = new TransactionGetInfoRequest();
        req.setHash(hash);
        for (int i = 0; i < 15; i++) {
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            TransactionGetInfoResponse resp = sdk.getTransactionService().getInfo(req);
            Long count = resp.getErrorCode() == 0 && resp.getResult() != null
                    ? resp.getResult().getTotalCount() : null;
            if (count != null && count > 0) {
                System.out.println("Tx confirmed: " + hash);
                return true;
            }
        }
        System.out.println("Tx not confirmed after 30s: " + hash);
        return false;
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
