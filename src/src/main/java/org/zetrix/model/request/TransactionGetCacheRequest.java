package org.zetrix.model.request;

public class TransactionGetCacheRequest {
    private String hash;
    private Integer limit;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
