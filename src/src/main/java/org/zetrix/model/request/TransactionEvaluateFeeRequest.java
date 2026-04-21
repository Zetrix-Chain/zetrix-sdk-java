package org.zetrix.model.request;

import org.zetrix.model.request.operation.BaseOperation;
import org.zetrix.model.response.result.data.Signature;

import java.util.Arrays;

/**
 * @Author riven
 * @Date 2018/7/5 15:53
 */
public class TransactionEvaluateFeeRequest {
    private String sourceAddress;
    private Long nonce;
    private BaseOperation[] operations;
    private int signatureNumber = 1;
    private Long ceilLedgerSeq;
    private String metadata;
    // Scenario 2/4: pre-built blob (with optional signatures) for direct fee estimation
    private String transactionBlob;
    private Signature[] signatures;
    // Scenario 1: private keys to sign a built blob for accurate two-step fee estimation
    private String[] signatureKeys;
    private Long gasPrice;

    /**
     * @Author riven
     * @Method getAddress
     * @Params []
     * @Return java.lang.String
     * @Date 2018/7/5 15:54
     */
    public String getSourceAddress() {
        return sourceAddress;
    }

    /**
     * @Author riven
     * @Method setAddress
     * @Params [sourceAddress]
     * @Return void
     * @Date 2018/7/5 15:54
     */
    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    /**
     * @Author riven
     * @Method getNonce
     * @Params []
     * @Return java.lang.Long
     * @Date 2018/7/5 15:54
     */
    public Long getNonce() {
        return nonce;
    }

    /**
     * @Author riven
     * @Method setNonce
     * @Params [nonce]
     * @Return void
     * @Date 2018/7/5 15:54
     */
    public void setNonce(Long nonce) {
        this.nonce = nonce;
    }

    /**
     * @Author riven
     * @Method getOperations
     * @Params []
     * @Return java.lang.String
     * @Date 2018/7/5 15:49
     */
    public BaseOperation[] getOperations() {
        return operations;
    }

    /**
     * @Author riven
     * @Method setOperations
     * @Params [operations]
     * @Return void
     * @Date 2018/7/5 15:49
     */
    public void setOperation(BaseOperation operation) {
        if (null == this.operations) {
            this.operations = new BaseOperation[1];
        } else {
            operations = Arrays.copyOf(operations, 1);
        }
        this.operations[0] = operation;
    }

    /**
     * @Author riven
     * @Method addOperation
     * @Params [operation]
     * @Return void
     * @Date 2018/7/10 11:39
     */
    public void addOperation(BaseOperation operation) {
        if (null == operations) {
            operations = new BaseOperation[1];
        } else {
            operations = Arrays.copyOf(operations, operations.length + 1);
        }
        operations[operations.length - 1] = operation;
    }

    /**
     * @Author riven
     * @Method getSignatureNumber
     * @Params []
     * @Return int
     * @Date 2018/7/5 15:54
     */
    public int getSignatureNumber() {
        return signatureNumber;
    }

    /**
     * @Author riven
     * @Method setSignatureNumber
     * @Params [signatureNumber]
     * @Return void
     * @Date 2018/7/5 15:54
     */
    public void setSignatureNumber(int signatureNumber) {
        this.signatureNumber = signatureNumber;
    }

    /**
     * @Author riven
     * @Method getCeilLedgerSeq
     * @Params []
     * @Return java.lang.String
     * @Date 2018/7/5 19:20
     */
    public Long getCeilLedgerSeq() {
        return ceilLedgerSeq;
    }

    /**
     * @Author riven
     * @Method setCeilLedgerSeq
     * @Params [ceilLedgerSeq]
     * @Return void
     * @Date 2018/7/5 19:20
     */
    public void setCeilLedgerSeq(Long ceilLedgerSeq) {
        this.ceilLedgerSeq = ceilLedgerSeq;
    }

    /**
     * @Author riven
     * @Method getMetadata
     * @Params []
     * @Return java.lang.String
     * @Date 2018/7/5 15:54
     */
    public String getMetadata() {
        return metadata;
    }

    /**
     * @Author riven
     * @Method setMetadata
     * @Params [metadata]
     * @Return void
     * @Date 2018/7/5 15:55
     */
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getTransactionBlob() {
        return transactionBlob;
    }

    public void setTransactionBlob(String transactionBlob) {
        this.transactionBlob = transactionBlob;
    }

    public Signature[] getSignatures() {
        return signatures;
    }

    public void setSignatures(Signature[] signatures) {
        this.signatures = signatures;
    }

    public String[] getSignatureKeys() {
        return signatureKeys;
    }

    public void setSignatureKeys(String[] signatureKeys) {
        this.signatureKeys = signatureKeys;
    }

    public Long getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(Long gasPrice) {
        this.gasPrice = gasPrice;
    }
}
