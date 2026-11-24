package org.zetrix.blockchain;

import com.sun.net.httpserver.HttpServer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zetrix.SDK;
import org.zetrix.contract.impl.ContractServiceImpl;
import org.zetrix.crypto.protobuf.Chain;
import org.zetrix.encryption.key.PrivateKey;
import org.zetrix.exception.SDKException;
import org.zetrix.exception.SdkError;
import org.zetrix.model.request.TransactionEvaluateFeeRequest;
import org.zetrix.model.request.operation.ContractUpgradeOperation;
import org.zetrix.model.request.operation.GasSendOperation;
import org.zetrix.model.response.TransactionEvaluateFeeResponse;
import org.zetrix.model.response.result.data.Signature;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Contract upgrade operation building and fee estimation input handling, against a local
 * HTTP server standing in for the node. No network access is needed.
 */
public class ContractUpgradeAndFeeTest {

    private static final String FEE_OK =
            "{\"error_code\":0,\"result\":{\"txs\":[{\"transaction_env\":"
                    + "{\"transaction\":{\"fee_limit\":1000000,\"gas_price\":5}}}]}}";

    private static HttpServer server;
    private static volatile String testTransactionReply = FEE_OK;
    private static volatile int testTransactionCalls;

    private static String sourceAddress;
    private static String sourceKey;
    private static String contractAddress;
    private static String otherAddress;

    @BeforeClass
    public static void startNode() throws Exception {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        // Any account looked up is a contract account.
        server.createContext("/getAccountBase", ex -> reply(ex,
                "{\"error_code\":0,\"result\":{\"contract\":{\"payload\":\"\\\"use strict\\\";\"}}}"));
        server.createContext("/testTransaction", ex -> {
            testTransactionCalls++;
            reply(ex, testTransactionReply);
        });
        server.start();
        SDK.getInstance("http://127.0.0.1:" + server.getAddress().getPort());

        PrivateKey source = new PrivateKey();
        sourceAddress = source.getEncAddress();
        sourceKey = source.getEncPrivateKey();
        contractAddress = new PrivateKey().getEncAddress();
        otherAddress = new PrivateKey().getEncAddress();
    }

    @AfterClass
    public static void stopNode() {
        server.stop(0);
    }

    @Before
    public void reset() {
        testTransactionReply = FEE_OK;
        testTransactionCalls = 0;
    }

    private static void reply(com.sun.net.httpserver.HttpExchange ex, String body) throws java.io.IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    // ---- contract upgrade ----

    private static ContractUpgradeOperation upgradeOp() {
        ContractUpgradeOperation op = new ContractUpgradeOperation();
        op.setSourceAddress(sourceAddress);
        op.setContractAddress(contractAddress);
        return op;
    }

    private static void assertUpgradeError(ContractUpgradeOperation op, SdkError expected) {
        try {
            ContractServiceImpl.upgrade(op, sourceAddress);
            fail("expected " + expected);
        } catch (SDKException e) {
            assertEquals(expected.getCode(), e.getErrorCode());
        }
    }

    @Test
    public void upgradePayloadOnlyWithOwnerUnset() throws Exception {
        ContractUpgradeOperation op = upgradeOp();
        op.setsPayload(true);
        op.setPayload("\"use strict\"; function query() { return 'v2'; }");
        // owner left null: only the payload changes

        Chain.OperationUpgradeContract up = ContractServiceImpl.upgrade(op, sourceAddress).getUpgradeContract();
        assertTrue(up.getSPayload());
        assertEquals(op.getPayload(), up.getPayload());
        assertFalse(up.getSOwner());
        assertEquals("", up.getOwner());
    }

    @Test
    public void upgradeOwnerOnlyWithPayloadUnset() throws Exception {
        ContractUpgradeOperation op = upgradeOp();
        op.setsOwner(true);
        op.setOwner(otherAddress);
        // payload left null: only the owner changes

        Chain.OperationUpgradeContract up = ContractServiceImpl.upgrade(op, sourceAddress).getUpgradeContract();
        assertFalse(up.getSPayload());
        assertEquals("", up.getPayload());
        assertTrue(up.getSOwner());
        assertEquals(otherAddress, up.getOwner());
    }

    @Test
    public void upgradeBothPayloadAndOwner() throws Exception {
        ContractUpgradeOperation op = upgradeOp();
        op.setsPayload(true);
        op.setPayload("\"use strict\";");
        op.setsOwner(true);
        op.setOwner(otherAddress);

        Chain.OperationUpgradeContract up = ContractServiceImpl.upgrade(op, sourceAddress).getUpgradeContract();
        assertEquals(contractAddress, up.getDestAddress());
        assertEquals("\"use strict\";", up.getPayload());
        assertEquals(otherAddress, up.getOwner());
    }

    @Test
    public void upgradeIgnoresValuesNotFlagged() throws Exception {
        ContractUpgradeOperation op = upgradeOp();
        op.setsPayload(true);
        op.setPayload("\"use strict\";");
        op.setOwner("not-an-address"); // sOwner is false, so this is not used or checked

        Chain.OperationUpgradeContract up = ContractServiceImpl.upgrade(op, sourceAddress).getUpgradeContract();
        assertFalse(up.getSOwner());
        assertEquals("", up.getOwner());
    }

    @Test
    public void upgradeRejectsInvalidOwnerAddress() {
        ContractUpgradeOperation op = upgradeOp();
        op.setsOwner(true);
        op.setOwner("ZTX3notAValidAddress");
        assertUpgradeError(op, SdkError.INVALID_CONTRACT_OWNER_ERROR);
    }

    @Test
    public void upgradeRejectsMissingFlaggedValues() {
        ContractUpgradeOperation none = upgradeOp();
        assertUpgradeError(none, SdkError.S_PAYLOAD_S_OWNER_NULL_ERROR);

        ContractUpgradeOperation noPayload = upgradeOp();
        noPayload.setsPayload(true);
        assertUpgradeError(noPayload, SdkError.PAYLOAD_EMPTY_ERROR);

        ContractUpgradeOperation emptyPayload = upgradeOp();
        emptyPayload.setsPayload(true);
        emptyPayload.setPayload("");
        assertUpgradeError(emptyPayload, SdkError.PAYLOAD_EMPTY_ERROR);

        ContractUpgradeOperation noOwner = upgradeOp();
        noOwner.setsOwner(true);
        assertUpgradeError(noOwner, SdkError.OWNER_NULL_ERROR);
    }

    // ---- fee estimation ----

    private static TransactionEvaluateFeeRequest feeRequest() {
        GasSendOperation send = new GasSendOperation();
        send.setDestAddress(otherAddress);
        send.setAmount(1L);
        TransactionEvaluateFeeRequest req = new TransactionEvaluateFeeRequest();
        req.setSourceAddress(sourceAddress);
        req.setNonce(1L);
        req.addOperation(send);
        return req;
    }

    private static TransactionEvaluateFeeResponse evaluate(TransactionEvaluateFeeRequest req) {
        return SDK.getSdk().getTransactionService().evaluateFee(req);
    }

    @Test
    public void signatureKeysScenarioReturnsSignedBlob() {
        TransactionEvaluateFeeRequest req = feeRequest();
        req.setSignatureKeys(new String[]{sourceKey});

        TransactionEvaluateFeeResponse resp = evaluate(req);
        assertEquals(Integer.valueOf(0), resp.getErrorCode());
        assertNotNull(resp.getResult().getTransactionBlob());
        assertEquals(1, resp.getResult().getSignatures().length);
        assertEquals(1, testTransactionCalls);
    }

    @Test
    public void signatureKeysScenarioReturnsNodeError() {
        // The node ran the test transaction and reports it would fail, but still includes fees.
        testTransactionReply = "{\"error_code\":151,\"error_desc\":\"contract execution failed\","
                + "\"result\":{\"txs\":[{\"transaction_env\":{\"transaction\":{\"fee_limit\":1000000,\"gas_price\":5}}}]}}";
        TransactionEvaluateFeeRequest req = feeRequest();
        req.setSignatureKeys(new String[]{sourceKey});

        TransactionEvaluateFeeResponse resp = evaluate(req);
        assertEquals(Integer.valueOf(151), resp.getErrorCode());
        assertEquals("contract execution failed", resp.getErrorDesc());
        assertTrue(resp.getResult() == null || resp.getResult().getTransactionBlob() == null);
    }

    @Test
    public void signatureKeysScenarioRejectsNegativeCeilLedgerSeq() {
        TransactionEvaluateFeeRequest req = feeRequest();
        req.setSignatureKeys(new String[]{sourceKey});
        req.setCeilLedgerSeq(-1L);

        TransactionEvaluateFeeResponse resp = evaluate(req);
        assertEquals(SdkError.INVALID_CEILLEDGERSEQ_ERROR.getCode(), resp.getErrorCode());
        assertEquals(0, testTransactionCalls);
    }

    @Test
    public void mixedFeeInputsAreRejected() {
        Signature sig = new Signature();
        sig.setSignData("00");
        sig.setPublicKey("00");

        TransactionEvaluateFeeRequest blobAndKeys = new TransactionEvaluateFeeRequest();
        blobAndKeys.setTransactionBlob("0a0b");
        blobAndKeys.setSignatureKeys(new String[]{sourceKey});

        TransactionEvaluateFeeRequest blobAndOps = feeRequest();
        blobAndOps.setTransactionBlob("0a0b");

        TransactionEvaluateFeeRequest keysAndSignatures = feeRequest();
        keysAndSignatures.setSignatureKeys(new String[]{sourceKey});
        keysAndSignatures.setSignatures(new Signature[]{sig});

        TransactionEvaluateFeeRequest signaturesWithoutBlob = feeRequest();
        signaturesWithoutBlob.setSignatures(new Signature[]{sig});

        for (TransactionEvaluateFeeRequest req : new TransactionEvaluateFeeRequest[]{
                blobAndKeys, blobAndOps, keysAndSignatures, signaturesWithoutBlob}) {
            assertEquals(SdkError.INVALID_EVALUATE_FEE_INPUT_ERROR.getCode(), evaluate(req).getErrorCode());
        }
        assertEquals(0, testTransactionCalls);
    }

    @Test
    public void blobWithSignaturesIsStillAccepted() {
        Signature sig = new Signature();
        sig.setSignData("00");
        sig.setPublicKey("00");
        TransactionEvaluateFeeRequest req = new TransactionEvaluateFeeRequest();
        req.setTransactionBlob("0a0b");
        req.setSignatures(new Signature[]{sig});

        assertEquals(Integer.valueOf(0), evaluate(req).getErrorCode());
        assertEquals(1, testTransactionCalls);
    }
}
