package org.zetrix.model.response.result.data;

import com.alibaba.fastjson.annotation.JSONField;

public class ContractUpgradeInfo {
    @JSONField(name = "dest_address")
    private String destAddress;
    @JSONField(name = "s_payload")
    private Boolean sPayload;
    private String payload;
    @JSONField(name = "s_owner")
    private Boolean sOwner;
    private String owner;

    public String getDestAddress() {
        return destAddress;
    }

    public void setDestAddress(String destAddress) {
        this.destAddress = destAddress;
    }

    public Boolean getsPayload() {
        return sPayload;
    }

    public void setsPayload(Boolean sPayload) {
        this.sPayload = sPayload;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Boolean getsOwner() {
        return sOwner;
    }

    public void setsOwner(Boolean sOwner) {
        this.sOwner = sOwner;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
