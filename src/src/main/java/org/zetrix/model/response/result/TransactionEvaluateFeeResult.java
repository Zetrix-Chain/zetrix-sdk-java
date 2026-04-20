package org.zetrix.model.response.result;

import com.alibaba.fastjson.annotation.JSONField;
import org.zetrix.model.response.result.data.Signature;
import org.zetrix.model.response.result.data.TestTx;

/**
 * @Author riven
 * @Date 2018/7/5 15:55
 */
public class TransactionEvaluateFeeResult {
    @JSONField(name = "txs")
    private TestTx[] txs;

    // Populated for Scenario 1 (signatureKeys path): ready-to-submit blob and signatures
    private String transactionBlob;
    private Signature[] signatures;

    public TestTx[] getTxs() {
        return txs;
    }

    public void setTxs(TestTx[] txs) {
        this.txs = txs;
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
}
