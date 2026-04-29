package org.zetrix.model.response.result.data;

import com.alibaba.fastjson.annotation.JSONField;

public class TransactionCache {
    @JSONField(name = "hash")
    private String hash;
    @JSONField(name = "incoming_time")
    private Long incomingTime;
    @JSONField(name = "signatures")
    private Signature[] signatures;
    private String status;
    @JSONField(name = "transaction")
    private TransactionInfo transaction;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Long getIncomingTime() {
        return incomingTime;
    }

    public void setIncomingTime(Long incomingTime) {
        this.incomingTime = incomingTime;
    }

    public Signature[] getSignatures() {
        return signatures;
    }

    public void setSignatures(Signature[] signatures) {
        this.signatures = signatures;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public TransactionInfo getTransaction() {
        return transaction;
    }

    public void setTransaction(TransactionInfo transaction) {
        this.transaction = transaction;
    }
}
