package org.zetrix.sdk.example;

import com.alibaba.fastjson.JSON;
import org.zetrix.SDK;
import org.zetrix.common.ToBaseUnit;
import org.zetrix.crypto.Keypair;
import org.zetrix.encryption.model.KeyType;
import org.zetrix.model.request.TransactionEvaluateFeeRequest;
import org.zetrix.model.request.TransactionSubmitRequest;
import org.zetrix.model.request.operation.AccountActivateOperation;
import org.zetrix.model.response.TransactionEvaluateFeeResponse;
import org.zetrix.model.response.TransactionSubmitResponse;
import org.zetrix.model.response.result.TransactionEvaluateFeeResult;
import org.zetrix.model.response.result.data.Signature;
import org.junit.Test;

/**
 * Demonstrates all three active evaluateFee() scenarios.
 *
 * Scenario 1 (signatureKeys): SDK builds blob, signs, estimates, rebuilds with real
 *   fees, re-signs, and returns a ready-to-submit blob+signatures.
 * Scenario 2/4 (transactionBlob ± signatures): caller provides a pre-built blob
 *   for direct fee estimation; useful for PQC key types where signing is done
 *   offline or by a separate key management system.
 * Scenario 3 (legacy): existing transaction_json path driven by signatureNumber.
 */
public class EvaluateFeeDemo {

    SDK sdk = SDK.getInstance("http://192.168.10.100:19343");

    // Sponsor account that funds the demo activations
    private static final String SPONSOR_ADDRESS     = "ZTX3Ta7d4GyAXD41H2kFCTd2eXhDesM83rvC3";
    private static final String SPONSOR_PRIVATE_KEY = "privBwYirzSUQ7ZhgLbDpRXC2A75HoRtGAKSF76dZnGGYXUvHhCK4xuz";
    private static final Long   SPONSOR_NONCE       = 10L;

    // -----------------------------------------------------------------------
    // Scenario 1: signatureKeys — two-step auto-fee evaluation
    // -----------------------------------------------------------------------
    @Test
    public void scenario1_signatureKeys() {
        // Generate a new SM2 destination account
        Keypair dest = Keypair.generator(KeyType.SM2);
        System.out.println("New SM2 address: " + dest.getAddress());

        AccountActivateOperation op = new AccountActivateOperation();
        op.setSourceAddress(SPONSOR_ADDRESS);
        op.setDestAddress(dest.getAddress());
        op.setInitBalance(ToBaseUnit.ToUGas("0.02"));

        TransactionEvaluateFeeRequest request = new TransactionEvaluateFeeRequest();
        request.setSourceAddress(SPONSOR_ADDRESS);
        request.setNonce(SPONSOR_NONCE);
        request.addOperation(op);
        // Provide private keys — SDK will sign, estimate, rebuild, and re-sign
        request.setSignatureKeys(new String[]{SPONSOR_PRIVATE_KEY});

        TransactionEvaluateFeeResponse response = sdk.getTransactionService().evaluateFee(request);
        if (response.getErrorCode() != 0) {
            System.out.println("Error: " + response.getErrorDesc());
            return;
        }

        TransactionEvaluateFeeResult result = response.getResult();
        System.out.println("feeLimit:  " + result.getTxs()[0].getTransactionEnv().getTransactionFees().getFeeLimit());
        System.out.println("gasPrice:  " + result.getTxs()[0].getTransactionEnv().getTransactionFees().getGasPrice());
        System.out.println("blob:      " + result.getTransactionBlob());

        // Submit directly — no re-signing needed
        TransactionSubmitRequest submitRequest = new TransactionSubmitRequest();
        submitRequest.setTransactionBlob(result.getTransactionBlob());
        submitRequest.setSignatures(result.getSignatures());
        TransactionSubmitResponse submitResponse = sdk.getTransactionService().submit(submitRequest);
        System.out.println("Submit result: " + JSON.toJSONString(submitResponse));
    }

    // -----------------------------------------------------------------------
    // Scenario 2/4: transactionBlob provided — direct passthrough
    // -----------------------------------------------------------------------
    @Test
    public void scenario2_prebuiltBlob() {
        // Caller already has a blob (e.g. built offline for a Dilithium3 account)
        String prebuiltBlob = "YOUR_HEX_BLOB_HERE";
        Signature sig = new Signature();
        sig.setSignData("YOUR_SIGN_DATA_HEX");
        sig.setPublicKey("YOUR_PUBLIC_KEY_HEX");

        TransactionEvaluateFeeRequest request = new TransactionEvaluateFeeRequest();
        request.setTransactionBlob(prebuiltBlob);
        request.setSignatures(new Signature[]{sig});

        TransactionEvaluateFeeResponse response = sdk.getTransactionService().evaluateFee(request);
        System.out.println("Fee response: " + JSON.toJSONString(response));
    }

    // -----------------------------------------------------------------------
    // Scenario 3: legacy transaction_json path (backward-compatible)
    // -----------------------------------------------------------------------
    @Test
    public void scenario3_legacy() {
        Keypair dest = Keypair.generator(KeyType.ED25519);

        AccountActivateOperation op = new AccountActivateOperation();
        op.setSourceAddress(SPONSOR_ADDRESS);
        op.setDestAddress(dest.getAddress());
        op.setInitBalance(ToBaseUnit.ToUGas("0.02"));

        TransactionEvaluateFeeRequest request = new TransactionEvaluateFeeRequest();
        request.setSourceAddress(SPONSOR_ADDRESS);
        request.setNonce(SPONSOR_NONCE);
        request.addOperation(op);
        request.setSignatureNumber(1);  // drives the legacy estimate

        TransactionEvaluateFeeResponse response = sdk.getTransactionService().evaluateFee(request);
        System.out.println("Fee response: " + JSON.toJSONString(response));
    }
}
