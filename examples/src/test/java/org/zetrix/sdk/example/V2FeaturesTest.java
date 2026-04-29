package org.zetrix.sdk.example;

import org.testng.annotations.Test;
import org.testng.Assert;
import org.zetrix.common.OperationType;
import org.zetrix.exception.SdkError;
import org.zetrix.model.request.TransactionGetCacheRequest;
import org.zetrix.model.request.operation.ContractUpgradeOperation;
import org.zetrix.model.response.result.TransactionGetCacheResult;
import org.zetrix.model.response.result.data.ContractUpgradeInfo;
import org.zetrix.model.response.result.data.TransactionCache;

/**
 * Offline unit tests for v2 additions: CONTRACT_UPGRADE operation,
 * transaction cache request/response classes, and new SdkError codes.
 * No network calls are made.
 */
public class V2FeaturesTest {

    @Test
    public void contractUpgradeEnumValue() {
        Assert.assertEquals(OperationType.CONTRACT_UPGRADE.getNumber(), 17);
    }

    @Test
    public void newSdkErrorCodes() {
        Assert.assertEquals((int) SdkError.S_PAYLOAD_S_OWNER_NULL_ERROR.getCode(), 11077);
        Assert.assertEquals((int) SdkError.OWNER_NULL_ERROR.getCode(), 11078);
    }

    @Test
    public void contractUpgradeOperationType() {
        ContractUpgradeOperation op = new ContractUpgradeOperation();
        Assert.assertEquals(op.getOperationType(), OperationType.CONTRACT_UPGRADE);
    }

    @Test
    public void contractUpgradeOperationFieldsPayloadOnly() {
        ContractUpgradeOperation op = new ContractUpgradeOperation();
        op.setContractAddress("ZTX3someContractAddress");
        op.setPayload("'use strict'; function init(){}");
        op.setsPayload(true);
        op.setsOwner(false);
        op.setOwner("");

        Assert.assertEquals(op.getContractAddress(), "ZTX3someContractAddress");
        Assert.assertEquals(op.getPayload(), "'use strict'; function init(){}");
        Assert.assertTrue(op.getsPayload());
        Assert.assertFalse(op.getsOwner());
        Assert.assertEquals(op.getOwner(), "");
    }

    @Test
    public void contractUpgradeOperationFieldsOwnerOnly() {
        ContractUpgradeOperation op = new ContractUpgradeOperation();
        op.setContractAddress("ZTX3someContractAddress");
        op.setsPayload(false);
        op.setPayload("");
        op.setsOwner(true);
        op.setOwner("ZTX3newOwnerAddress");

        Assert.assertFalse(op.getsPayload());
        Assert.assertTrue(op.getsOwner());
        Assert.assertEquals(op.getOwner(), "ZTX3newOwnerAddress");
    }

    @Test
    public void transactionGetCacheRequestFields() {
        TransactionGetCacheRequest req = new TransactionGetCacheRequest();
        Assert.assertNull(req.getHash());
        Assert.assertNull(req.getLimit());

        req.setHash("abc123def456");
        req.setLimit(5);

        Assert.assertEquals(req.getHash(), "abc123def456");
        Assert.assertEquals(req.getLimit(), Integer.valueOf(5));
    }

    @Test
    public void transactionGetCacheResultInstantiation() {
        Assert.assertNotNull(new TransactionGetCacheResult());
    }

    @Test
    public void contractUpgradeInfoInstantiation() {
        Assert.assertNotNull(new ContractUpgradeInfo());
    }

    @Test
    public void transactionCacheInstantiation() {
        Assert.assertNotNull(new TransactionCache());
    }
}
