package org.zetrix.model.response.result;

import com.alibaba.fastjson.annotation.JSONField;
import org.zetrix.model.response.result.data.TransactionCache;

public class TransactionGetCacheResult {
    @JSONField(name = "total_count")
    private Long totalCount;

    @JSONField(name = "transactions")
    private TransactionCache[] transactions;

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public TransactionCache[] getTransactions() {
        return transactions;
    }

    public void setTransactions(TransactionCache[] transactions) {
        this.transactions = transactions;
    }
}
