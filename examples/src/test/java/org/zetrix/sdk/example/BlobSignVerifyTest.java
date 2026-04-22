package org.zetrix.sdk.example;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.zetrix.SDK;
import org.zetrix.crypto.Keypair;
import org.zetrix.encryption.key.PublicKey;
import org.zetrix.encryption.model.KeyType;
import org.zetrix.encryption.utils.hex.HexFormat;
import org.zetrix.model.request.SDKInitRequest;
import org.zetrix.model.request.TransactionBuildBlobRequest;
import org.zetrix.model.request.TransactionSignRequest;
import org.zetrix.model.request.operation.GasSendOperation;
import org.zetrix.model.response.TransactionBuildBlobResponse;
import org.zetrix.model.response.TransactionSignResponse;
import org.zetrix.model.response.result.data.Signature;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Verifies that transaction blob signing and message signing produce signatures
 * that are locally verifiable against the corresponding public key, for all
 * four supported key types (ED25519, SM2, Dilithium3, Hybrid).
 *
 * No network calls are made — buildBlob and sign are pure local operations.
 */
@RunWith(Parameterized.class)
public class BlobSignVerifyTest {

    private static final String DEST_ADDRESS = "ZTX3ag1AT3JHSkTr3XTfnuhwHgtJ88z3SxY8T";
    // No network calls made; SDK is initialised only to access buildBlob/sign which are local operations.
    private static final String OFFLINE_TEST_URL = "http://offline.invalid";

    private static SDK sdk;

    private final KeyType keyType;

    public BlobSignVerifyTest(KeyType keyType) {
        this.keyType = keyType;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> types() {
        return Arrays.asList(new Object[][]{
            {KeyType.ED25519},
            {KeyType.SM2},
            {KeyType.DILITHIUM3},
            {KeyType.HYBRID},
        });
    }

    @BeforeClass
    public static void initSDK() {
        sdk = SDK.getInstance(OFFLINE_TEST_URL);
    }

    // ---- helpers ----

    private String buildBlob(String sourceAddress) {
        GasSendOperation op = new GasSendOperation();
        op.setSourceAddress(sourceAddress);
        op.setDestAddress(DEST_ADDRESS);
        op.setAmount(1L);

        TransactionBuildBlobRequest req = new TransactionBuildBlobRequest();
        req.setSourceAddress(sourceAddress);
        req.setNonce(1L);
        req.setFeeLimit(1000000L);
        req.setGasPrice(10L);
        req.addOperation(op);

        TransactionBuildBlobResponse resp = sdk.getTransactionService().buildBlob(req);
        assertEquals("buildBlob must succeed for " + keyType, 0, (int) resp.getErrorCode());
        return resp.getResult().getTransactionBlob();
    }

    private Signature signBlob(String blobHex, String privateKey) {
        TransactionSignRequest req = new TransactionSignRequest();
        req.setBlob(blobHex);
        req.addPrivateKey(privateKey);
        TransactionSignResponse resp = sdk.getTransactionService().sign(req);
        assertEquals("sign must succeed for " + keyType, 0, (int) resp.getErrorCode());
        Signature[] sigs = resp.getResult().getSignatures();
        assertEquals(1, sigs.length);
        return sigs[0];
    }

    // ---- tests ----

    /**
     * Build a transaction blob, sign it, and verify the signature locally.
     * signData (hex) decoded to bytes must verify against blobBytes using the publicKey.
     */
    @Test
    public void blobSignAndVerify() throws Exception {
        Keypair kp = Keypair.generator(keyType);
        String blobHex = buildBlob(kp.getAddress());
        byte[] blobBytes = HexFormat.hexToByte(blobHex);

        Signature sig = signBlob(blobHex, kp.getPrivateKey());

        byte[] signBytes = HexFormat.hexToByte(sig.getSignData());
        assertTrue(
            keyType + ": signature must verify against original blob bytes",
            PublicKey.verify(blobBytes, signBytes, sig.getPublicKey())
        );
    }

    /**
     * A signature produced for one blob must not verify against a tampered blob.
     */
    @Test
    public void tamperedBlobFails() throws Exception {
        Keypair kp = Keypair.generator(keyType);
        String blobHex = buildBlob(kp.getAddress());
        Signature sig = signBlob(blobHex, kp.getPrivateKey());

        byte[] tampered = HexFormat.hexToByte(blobHex);
        tampered[tampered.length - 1] ^= 0xFF;

        byte[] signBytes = HexFormat.hexToByte(sig.getSignData());
        assertFalse(
            keyType + ": signature must not verify against tampered blob",
            PublicKey.verify(tampered, signBytes, sig.getPublicKey())
        );
    }

    /**
     * A signature produced by key A must not verify with key B's public key.
     */
    @Test
    public void wrongPublicKeyFails() throws Exception {
        Keypair kp1 = Keypair.generator(keyType);
        Keypair kp2 = Keypair.generator(keyType);

        String blobHex = buildBlob(kp1.getAddress());
        byte[] blobBytes = HexFormat.hexToByte(blobHex);
        Signature sig = signBlob(blobHex, kp1.getPrivateKey());

        byte[] signBytes = HexFormat.hexToByte(sig.getSignData());
        assertFalse(
            keyType + ": signature from kp1 must not verify with kp2 public key",
            PublicKey.verify(blobBytes, signBytes, kp2.getPublicKey())
        );
    }

    /**
     * Sign a raw message (not a blob) and verify it using the static PrivateKey.sign
     * and PublicKey.verify APIs — mirrors the encryption library's own sign/verify flow.
     */
    @Test
    public void rawMessageSignAndVerify() throws Exception {
        Keypair kp = Keypair.generator(keyType);
        byte[] message = ("zetrix pqc test - " + keyType).getBytes("UTF-8");

        byte[] sig = org.zetrix.encryption.key.PrivateKey.sign(message, kp.getPrivateKey());
        assertNotNull(sig);
        assertTrue(
            keyType + ": raw message signature must verify",
            PublicKey.verify(message, sig, kp.getPublicKey())
        );
    }

    /**
     * A raw message signature must not verify against a different message.
     */
    @Test
    public void rawMessageTamperedFails() throws Exception {
        Keypair kp = Keypair.generator(keyType);
        byte[] message = "original message".getBytes("UTF-8");
        byte[] sig = org.zetrix.encryption.key.PrivateKey.sign(message, kp.getPrivateKey());

        assertFalse(
            keyType + ": signature must not verify against different message",
            PublicKey.verify("tampered message".getBytes("UTF-8"), sig, kp.getPublicKey())
        );
    }
}
