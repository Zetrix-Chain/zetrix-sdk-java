package org.zetrix.model.request.operation;

import org.zetrix.common.OperationType;

public class ContractUpgradeOperation extends BaseOperation {
    private String contractAddress;
    private boolean sPayload;
    private String payload;
    private boolean sOwner;
    private String owner;

    public ContractUpgradeOperation() {
        operationType = OperationType.CONTRACT_UPGRADE;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public boolean getsPayload() {
        return sPayload;
    }

    public void setsPayload(boolean sPayload) {
        this.sPayload = sPayload;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public boolean getsOwner() {
        return sOwner;
    }

    public void setsOwner(boolean sOwner) {
        this.sOwner = sOwner;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
