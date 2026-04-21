package org.zetrix.sdk.example;

import com.alibaba.fastjson.JSON;
import org.zetrix.SDK;
import org.zetrix.common.ToBaseUnit;
import org.zetrix.crypto.Keypair;
import org.zetrix.encryption.model.KeyType;
import org.zetrix.model.request.*;
import org.zetrix.model.request.operation.AccountActivateOperation;
import org.zetrix.model.request.operation.GasSendOperation;
import org.zetrix.model.response.*;
import org.zetrix.model.response.result.*;
import org.zetrix.model.response.result.data.Signature;
import org.junit.Test;

/**
 * Demonstrates all three active evaluateFee() scenarios.
 *
 * Scenario 1 (signatureKeys): SDK builds blob, signs, estimates, rebuilds with real
 *   fees, re-signs, and returns a ready-to-submit blob+signatures.
 * Scenario 2/4 (transactionBlob + signatures): caller provides a pre-built blob
 *   for direct fee estimation; useful for offline signing or PQC key types.
 * Scenario 3 (legacy): existing transaction_json path driven by signatureNumber.
 */
public class EvaluateFeeDemo {

    SDK sdk = SDK.getInstance("https://test-node.zetrix.com");

    private static final String SPONSOR_ADDRESS     = "ZTX3ag1AT3JHSkTr3XTfnuhwHgtJ88z3SxY8T";
    private static final String SPONSOR_PRIVATE_KEY = "YOUR_PRIVATE_KEY";

    // -----------------------------------------------------------------------
    // Scenario 1: signatureKeys — two-step auto-fee evaluation
    // -----------------------------------------------------------------------
    @Test
    public void scenario1_signatureKeys() {
        Keypair dest = Keypair.generator(KeyType.SM2);
        System.out.println("New SM2 address: " + dest.getAddress());

        long nonce = getSponsorNonce();

        AccountActivateOperation op = new AccountActivateOperation();
        op.setSourceAddress(SPONSOR_ADDRESS);
        op.setDestAddress(dest.getAddress());
        op.setInitBalance(ToBaseUnit.ToUGas("0.02"));

        TransactionEvaluateFeeRequest request = new TransactionEvaluateFeeRequest();
        request.setSourceAddress(SPONSOR_ADDRESS);
        request.setNonce(nonce + 1);
        request.addOperation(op);
        request.setGasPrice(5L);
        request.setSignatureKeys(new String[]{SPONSOR_PRIVATE_KEY});

        TransactionEvaluateFeeResponse response = sdk.getTransactionService().evaluateFee(request);
        if (response.getErrorCode() != 0) {
            System.out.println("Error: " + response.getErrorDesc());
            return;
        }

        TransactionEvaluateFeeResult result = response.getResult();
        System.out.println("feeLimit: " + result.getTxs()[0].getTransactionEnv().getTransactionFees().getFeeLimit());
        System.out.println("gasPrice: " + result.getTxs()[0].getTransactionEnv().getTransactionFees().getGasPrice());

        TransactionSubmitRequest submitRequest = new TransactionSubmitRequest();
        submitRequest.setTransactionBlob(result.getTransactionBlob());
        submitRequest.setSignatures(result.getSignatures());
        TransactionSubmitResponse submitResponse = sdk.getTransactionService().submit(submitRequest);
        System.out.println("Submit result: " + JSON.toJSONString(submitResponse));
    }

    // -----------------------------------------------------------------------
    // Scenario 2/4: pre-built blob + signatures — direct passthrough
    // -----------------------------------------------------------------------
    @Test
    public void scenario2_prebuiltBlob() {
        long nonce = getSponsorNonce();

        // Step 1: build a GasSend blob manually
        GasSendOperation sendOp = new GasSendOperation();
        sendOp.setSourceAddress(SPONSOR_ADDRESS);
        sendOp.setDestAddress(SPONSOR_ADDRESS); // send to self for demo
        sendOp.setAmount(1000L);

        TransactionBuildBlobRequest blobReq = new TransactionBuildBlobRequest();
        blobReq.setSourceAddress(SPONSOR_ADDRESS);
        blobReq.setNonce(nonce + 1);
        blobReq.setFeeLimit(ToBaseUnit.ToUGas("0.01"));
        blobReq.setGasPrice(1000L);
        blobReq.addOperation(sendOp);

        TransactionBuildBlobResponse blobResp = sdk.getTransactionService().buildBlob(blobReq);
        if (blobResp.getErrorCode() != 0) {
            System.out.println("Build blob error: " + blobResp.getErrorDesc());
            return;
        }
        String blob = blobResp.getResult().getTransactionBlob();

        // Step 2: sign the blob
        TransactionSignRequest signReq = new TransactionSignRequest();
        signReq.setBlob(blob);
        signReq.addPrivateKey(SPONSOR_PRIVATE_KEY);
        TransactionSignResponse signResp = sdk.getTransactionService().sign(signReq);
        if (signResp.getErrorCode() != 0) {
            System.out.println("Sign error: " + signResp.getErrorDesc());
            return;
        }
        Signature[] signatures = signResp.getResult().getSignatures();

        // Step 3: pass pre-built blob + signatures to evaluateFee
        TransactionEvaluateFeeRequest feeReq = new TransactionEvaluateFeeRequest();
        feeReq.setTransactionBlob(blob);
        feeReq.setSignatures(signatures);

        TransactionEvaluateFeeResponse feeResp = sdk.getTransactionService().evaluateFee(feeReq);
        System.out.println("Scenario 2 fee response: " + JSON.toJSONString(feeResp));
    }

    // -----------------------------------------------------------------------
    // Scenario 3: legacy transaction_json path (backward-compatible)
    // -----------------------------------------------------------------------
    @Test
    public void scenario3_legacy() {
        long nonce = getSponsorNonce();
        Keypair dest = Keypair.generator(KeyType.ED25519);

        AccountActivateOperation op = new AccountActivateOperation();
        op.setSourceAddress(SPONSOR_ADDRESS);
        op.setDestAddress(dest.getAddress());
        op.setInitBalance(ToBaseUnit.ToUGas("0.02"));

        TransactionEvaluateFeeRequest request = new TransactionEvaluateFeeRequest();
        request.setSourceAddress(SPONSOR_ADDRESS);
        request.setNonce(nonce + 1);
        request.addOperation(op);
        request.setSignatureNumber(1);

        TransactionEvaluateFeeResponse response = sdk.getTransactionService().evaluateFee(request);
        System.out.println("Scenario 3 fee response: " + JSON.toJSONString(response));
    }

    // -----------------------------------------------------------------------
    private long getSponsorNonce() {
        AccountGetNonceRequest req = new AccountGetNonceRequest();
        req.setAddress(SPONSOR_ADDRESS);
        AccountGetNonceResponse resp = sdk.getAccountService().getNonce(req);
        if (resp.getErrorCode() != 0) {
            System.out.println("getNonce error: " + resp.getErrorDesc());
            return 0;
        }
        return resp.getResult().getNonce() != null ? resp.getResult().getNonce() : 0;
    }
}
