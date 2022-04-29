package org.zetrix.sdk.example;

import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;
import org.junit.Assert;
import com.alibaba.fastjson.JSON;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.zetrix.SDK;
import org.zetrix.common.ToBaseUnit;
import org.zetrix.crypto.Keypair;
import org.zetrix.crypto.protobuf.Chain;
import org.zetrix.encryption.key.PrivateKey;
import org.zetrix.encryption.utils.hex.HexFormat;
import org.zetrix.model.request.*;
import org.zetrix.model.request.operation.*;
import org.zetrix.model.response.*;
import org.zetrix.model.response.result.*;
import org.zetrix.model.response.result.data.*;
import org.junit.Test;

/**
 * @author riven
 * @date 2018/7/15 14:32
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DigitalAssetsDemo {
    SDK sdk = SDK.getInstance("http://192.168.4.131:18333");
    String genesisAccount = "ZTX3Xbrrc5Q2cphyB73NymGBBXuAWjCSf2a8Q";
    String genesisAccountPriv = "privBucs1BQLudyDfDQPGSGentAF5BGGcrsATBMaWGv2AuL1J76pW5qt";
    String newAddress = "";
    String newPriv = "";
    String txHash = "";
    Long txBlockNum = 0L;

    @Before
    public void SDKConfigure() {
        SDKConfigure sdkConfigure = new SDKConfigure();
        sdkConfigure.setHttpConnectTimeOut(5000);
        sdkConfigure.setHttpReadTimeOut(5000);
        sdkConfigure.setChainId(0);
        sdkConfigure.setUrl("http://192.168.4.131:18333");
        sdk = SDK.getInstance(sdkConfigure);
    }

    /**
     * Generate an account private key, public key and address
     */
    @Test
    public void Test1() {
        Keypair keypair = Keypair.generator();
        Assert.assertNotNull(keypair);
        newAddress = keypair.getAddress();
        newPriv = keypair.getPrivateKey();
    }

    /**
     * Check whether account address is valid
     */
    @Test
    public void Test10() {
        // Init request
        String address = newAddress;
        AccountCheckValidRequest request = new AccountCheckValidRequest();
        request.setAddress(address);

        // Call checkValid
        AccountCheckValidResponse response = sdk.getAccountService().checkValid(request);
        if (0 == response.getErrorCode()) {
            System.out.println(response.getResult().isValid());
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
        Assert.assertEquals(response.getErrorDesc(), Integer.valueOf(0), response.getErrorCode());
    }

    /**
     * Get account nonce
     */
    @Test
    public void Test11() {
        // Init request
        String accountAddress = genesisAccount;
        AccountGetNonceRequest request = new AccountGetNonceRequest();
        request.setAddress(accountAddress);

        // Call getNonce
        AccountGetNonceResponse response = sdk.getAccountService().getNonce(request);
        if (0 == response.getErrorCode()) {
            System.out.println("账户nonce:" + response.getResult().getNonce());
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
        Assert.assertEquals(response.getErrorDesc(), Integer.valueOf(0), response.getErrorCode());
    }

    /**
     * Activate a new account
     */
    @Test
    public void Test12() {
        // The account private key to activate a new account
        String activatePrivateKey = genesisAccountPriv;
        Long initBalance = ToBaseUnit.ToUGas("1");
        // The fixed write 1000L, the unit is UGas
        Long gasPrice = 1000L;
        // Set up the maximum cost 0.01Gas
        Long feeLimit = ToBaseUnit.ToUGas("0.01");

        // 1. Get the account address to send this transaction
        String activateAddresss = getAddressByPrivateKey(activatePrivateKey);

        // Transaction initiation account's nonce + 1
        Long nonce = getAccountNonce(activateAddresss) + 1;

        Keypair keypair = Keypair.generator();
        System.out.println(JSON.toJSONString(keypair, true));

        // 2. Build activateAccount
        AccountActivateOperation operation = new AccountActivateOperation();
        operation.setSourceAddress(activateAddresss);
        operation.setDestAddress(keypair.getAddress());
        operation.setInitBalance(initBalance);
        operation.setMetadata("activate account");

        String[] signerPrivateKeyArr = {activatePrivateKey};
        // Record txhash for subsequent confirmation of the real result of the transaction.
        // After recommending five blocks, call again through txhash `Get the transaction information
        // from the transaction Hash'(see example: getTxByHash ()) to confirm the final result of the transaction
        String txHash = submitTransaction(signerPrivateKeyArr, activateAddresss, operation, nonce, gasPrice, feeLimit);
        if (txHash != null) {
            System.out.println("hash: " + txHash);
        }
    }

    @Test
    public void Test13() {
        AccountCheckActivatedRequst request = new AccountCheckActivatedRequst();
        request.setAddress(newAddress);

        AccountCheckActivatedResponse response = sdk.getAccountService().checkActivated(request);
        if (response.getErrorCode() == 0) {
            System.out.println("account (" + newAddress + ") is activated");
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
        Assert.assertEquals(response.getErrorDesc(), Integer.valueOf(0), response.getErrorCode());
    }

    /**
     * Get account info
     */
    @Test
    public void Test14() {
        // Init request
        String accountAddress = newAddress;
        AccountGetInfoRequest request = new AccountGetInfoRequest();
        request.setAddress(accountAddress);

        // Call getInfo
        AccountGetInfoResponse response = sdk.getAccountService().getInfo(request);
        if (response.getErrorCode() == 0) {
            AccountGetInfoResult result = response.getResult();
            System.out.println("账户信息: \n" + JSON.toJSONString(result, true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
        Assert.assertEquals(response.getErrorDesc(), Integer.valueOf(0), response.getErrorCode());
    }

    /**
     * Send a transaction of sending gas
     */
    @Test
    public void Test15() {
        // Init variable
        // The account private key to send gas
        String senderPrivateKey = genesisAccountPriv;
        // The account address to receive gas
        String destAddress = newAddress;
        // The amount to be sent
        Long amount = ToBaseUnit.ToUGas("100");
        // The fixed write 1000L, the unit is UGas
        Long gasPrice = 1000L;
        // Set up the maximum cost 0.01Gas
        Long feeLimit = ToBaseUnit.ToUGas("0.01");
        // Transaction initiation account's nonce + 1
        Long nonce = getAccountNonce(genesisAccount) + 1;

        // 1. Get the account address to send this transaction
        String senderAddresss = getAddressByPrivateKey(senderPrivateKey);

        // 2. Build sendGas
        GasSendOperation operation = new GasSendOperation();
        operation.setSourceAddress(senderAddresss);
        operation.setDestAddress(destAddress);
        operation.setAmount(amount);

        String[] signerPrivateKeyArr = {senderPrivateKey};
        // Record txhash for subsequent confirmation of the real result of the transaction.
        // After recommending five blocks, call again through txhash `Get the transaction information
        // from the transaction Hash'(see example: getTxByHash ()) to confirm the final result of the transaction
        String txHash = submitTransaction(signerPrivateKeyArr, senderAddresss, operation, nonce, gasPrice, feeLimit);
        if (txHash != null) {
            System.out.println("hash: " + txHash);
        }
    }

    /**
     * Get account Gas balance
     */
    @Test
    public void Test16() {
        // Init request
        String accountAddress = newAddress;
        AccountGetBalanceRequest request = new AccountGetBalanceRequest();
        request.setAddress(accountAddress);

        // Call getBalance
        AccountGetBalanceResponse response = sdk.getAccountService().getBalance(request);
        if (0 == response.getErrorCode()) {
            AccountGetBalanceResult result = response.getResult();
            System.out.println("Gas余额：" + ToBaseUnit.ToGas(result.getBalance().toString()) + " Gas");
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
        Assert.assertEquals(response.getErrorDesc(), Integer.valueOf(0), response.getErrorCode());
    }

    /**
     * Issue asset
     */
    @Test
    public void Test17() {
        // Init variable
        // The account private key to issue asset
        String issuePrivateKey = genesisAccountPriv;
        // Asset code
        String assetCode = "TST2";
        // Asset amount
        Long assetAmount = 10000000000000L;
        // metadata
        String metadata = "issue TST";
        // The fixed write 1000L, the unit is UGas
        Long gasPrice = 1000L;
        // Set up the maximum cost 50.01Gas
        Long feeLimit = ToBaseUnit.ToUGas("50.01");
        // Transaction initiation account's nonce + 1
        Long nonce = getAccountNonce(genesisAccount) + 1;

        // 1. Get the account address to send this transaction
        String issueAddresss = getAddressByPrivateKey(issuePrivateKey);

        // 2. Build issueAsset
        AssetIssueOperation assetIssueOperation = new AssetIssueOperation();
        assetIssueOperation.setSourceAddress(issueAddresss);
        assetIssueOperation.setCode(assetCode);
        assetIssueOperation.setAmount(assetAmount);
        assetIssueOperation.setMetadata(metadata);


        String[] signerPrivateKeyArr = {issuePrivateKey};
        // Record txhash for subsequent confirmation of the real result of the transaction.
        // After recommending five blocks, call again through txhash `Get the transaction information
        // from the transaction Hash'(see example: getTxByHash ()) to confirm the final result of the transaction
        String txHash = submitTransaction(signerPrivateKeyArr, issueAddresss, assetIssueOperation, nonce, gasPrice, feeLimit);
        if (txHash != null) {
            System.out.println("hash: " + txHash);
        }
    }

    /**
     * Query the specified assets of the specified account
     */
    @Test
    public void Test20() {
        // Init request
        AssetGetInfoRequest request = new AssetGetInfoRequest();
        request.setAddress(genesisAccount);
        request.setIssuer(genesisAccount);
        request.setCode("TST");

        // Call getInfo
        AssetGetInfoResponse response = sdk.getAssetService().getInfo(request);
        if (response.getErrorCode() == 0) {
            AssetGetInfoResult result = response.getResult();
            System.out.println(JSON.toJSONString(result, true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
        Assert.assertEquals(response.getErrorDesc(), Integer.valueOf(0), response.getErrorCode());
    }

    /**
     * Send asset
     */
    @Test
    public void Test21() {
        // Init variable
        // The account private key to start this transaction
        String senderPrivateKey = genesisAccountPriv;
        // The account to receive asset
        String destAddress = "ZTX3Up7doxNiUwUtV1gFatA1G65EjtLDokpUc";
        // Asset code
        String assetCode = "TST";
        // The accout address of issuing asset
        String assetIssuer = genesisAccount;
        // The asset amount to be sent
        Long amount = ToBaseUnit.ToUGas("10");
        // The fixed write 1000L, the unit is UGas
        Long gasPrice = 1000L;
        // Set up the maximum cost 0.01Gas
        Long feeLimit = ToBaseUnit.ToUGas("0.01");
        // Transaction initiation account's nonce + 1
        Long nonce = getAccountNonce(genesisAccount) + 1;

        // 1. Get the account address to send this transaction
        String senderAddresss = getAddressByPrivateKey(senderPrivateKey);

        // 2. Build sendAsset
        AssetSendOperation assetSendOperation = new AssetSendOperation();
        assetSendOperation.setSourceAddress(senderAddresss);
        assetSendOperation.setDestAddress(destAddress);
        assetSendOperation.setCode(assetCode);
        assetSendOperation.setIssuer(assetIssuer);
        assetSendOperation.setAmount(amount);
        assetSendOperation.setMetadata("send token");

        String[] signerPrivateKeyArr = {senderPrivateKey};
        // Record txhash for subsequent confirmation of the real result of the transaction.
        // After recommending five blocks, call again through txhash `Get the transaction information
        // from the transaction Hash'(see example: getTxByHash ()) to confirm the final result of the transaction
        String txHash = submitTransaction(signerPrivateKeyArr, senderAddresss, assetSendOperation, nonce, gasPrice, feeLimit);
        if (txHash != null) {
            System.out.println("hash: " + txHash);
        }
    }

    /**
     * Query all assets under the specified account
     */
    @Test
    public void Test22() {
        // Init request
        AccountGetAssetsRequest request = new AccountGetAssetsRequest();
        request.setAddress(newAddress);

        // Call getAssets
        AccountGetAssetsResponse response = sdk.getAccountService().getAssets(request);
        if (response.getErrorCode() == 0) {
            AccountGetAssetsResult result = response.getResult();
            System.out.println(JSON.toJSONString(result, true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
        Assert.assertEquals(response.getErrorDesc(), Integer.valueOf(0), response.getErrorCode());
    }

    /**
     * Set account metadata
     */
    @Test
    public void Test23() {
        // Init variable
        // The account private key to set metadata
        String accountPrivateKey = genesisAccountPriv;
        // The metadata key
        String key = "test";
        // The metadata value
        String value = "asdfasdfa";
        // The fixed write 1000L, the unit is UGas
        Long gasPrice = 1000L;
        //Set up the maximum cost 0.01Gas
        Long feeLimit = ToBaseUnit.ToUGas("0.01");

        // 1. Get the account address to send this transaction
        String accountAddresss = getAddressByPrivateKey(accountPrivateKey);

        // Transaction initiation account's nonce + 1
        Long nonce = getAccountNonce(accountAddresss) + 1;

        // 2. Build setMetadata
        AccountSetMetadataOperation operation = new AccountSetMetadataOperation();
        operation.setSourceAddress(accountAddresss);
        operation.setKey(key);
        operation.setValue(value);

        String[] signerPrivateKeyArr = {accountPrivateKey};
        // Record txhash for subsequent confirmation of the real result of the transaction.
        // After recommending five blocks, call again through txhash `Get the transaction information
        // from the transaction Hash'(see example: getTxByHash ()) to confirm the final result of the transaction
        String txHash = submitTransaction(signerPrivateKeyArr, accountAddresss, operation, nonce, gasPrice, feeLimit);
        if (txHash != null) {
            System.out.println("hash: " + txHash);
        }
    }

    /**
     * Get account metadata
     */
    @Test
    public void Test24() {
        // Init request
        String accountAddress = genesisAccount;
        AccountGetMetadataRequest request = new AccountGetMetadataRequest();
        request.setAddress(accountAddress);
        request.setKey("test");

        // Call getMetadata
        AccountGetMetadataResponse response = sdk.getAccountService().getMetadata(request);
        if (response.getErrorCode() == 0) {
            AccountGetMetadataResult result = response.getResult();
            System.out.println(JSON.toJSONString(result, true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
        Assert.assertEquals(response.getErrorDesc(), Integer.valueOf(0), response.getErrorCode());
    }

    public Long getAccountNonce(String accountAddress) {
        // Init request
        AccountGetNonceRequest request = new AccountGetNonceRequest();
        request.setAddress(accountAddress);

        // Call getNonce
        AccountGetNonceResponse response = sdk.getAccountService().getNonce(request);
        if (0 == response.getErrorCode()) {
            return response.getResult().getNonce();
        } else {
            System.out.println("error: " + response.getErrorDesc());
            return -1L;
        }
    }

    /**
     * Evaluation of transaction costs
     */
    @Test
    public void Test25() throws Exception {
        // Init variable
        // 发送方私钥
        String senderPrivateKey = genesisAccountPriv;
        // 接收方账户地址
        String destAddress = "ZTX3UMXryvFvuxhZYnpP72K8UMFrXnoMgD4Zt";
        // 发送转出10.9Gas给接收方（目标账户）
        Long amount = ToBaseUnit.ToUGas("10.9");
        // The fixed write 1000L, the unit is UGas
        Long gasPrice = 1000L;
        //Set up the maximum cost 0.01Gas
        Long feeLimit = ToBaseUnit.ToUGas("0.01");
        // Transaction initiation account's nonce + 1;
        Long nonce = getAccountNonce(genesisAccount) + 1;

        // 评估费用
        evaluateFees(senderPrivateKey, destAddress, amount, nonce, gasPrice, feeLimit);
    }

    /**
     * Get transaction information according to the transaction Hash
     */
    @Test
    public void Test26() {
        // Init request
        TransactionGetInfoRequest request = new TransactionGetInfoRequest();
        request.setHash(txHash);

        // Call getInfo
        TransactionGetInfoResponse response = sdk.getTransactionService().getInfo(request);
        if (response.getErrorCode() == 0) {
            System.out.println(JSON.toJSONString(response.getResult(), true));
            txBlockNum = response.getResult().getTransactions()[0].getLedgerSeq();
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
        Assert.assertEquals(response.getErrorDesc(), Integer.valueOf(0), response.getErrorCode());
    }

    /**
     * Check whether the nodes in the connection are block synchronously
     */
    @Test
    public void Test27() {
        BlockCheckStatusResponse response = sdk.getBlockService().checkStatus();
        Assert.assertEquals(response.getErrorDesc(), Integer.valueOf(0), response.getErrorCode());
        Assert.assertTrue(response.getResult().getSynchronous());
    }

    /**
     * Probe user recharge
     * <p>
     * Through the analysis of transactions under the block, to detect the user's charging action
     */
    @Test
    public void Test28() {
        // Init request
        Long blockNumber = txBlockNum;
        BlockGetTransactionsRequest request = new BlockGetTransactionsRequest();
        request.setBlockNumber(blockNumber);

        // Call getTransactions
        BlockGetTransactionsResponse response = sdk.getBlockService().getTransactions(request);
        if (0 == response.getErrorCode()) {
            System.out.println(JSON.toJSONString(response.getResult(), true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
        Assert.assertEquals(response.getErrorDesc(), Integer.valueOf(0), response.getErrorCode());
    }

    /**
     * Get the latest block number
     */
    @Test
    public void Test29() {
        // Call getNumber
        BlockGetNumberResponse response = sdk.getBlockService().getNumber();
        if (0 == response.getErrorCode()) {
            System.out.println(JSON.toJSONString(response.getResult(), true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
        Assert.assertEquals(response.getErrorDesc(), Integer.valueOf(0), response.getErrorCode());
    }

    /**
     * Get the block info according to the specified block number
     */
    @Test
    public void Test3() {
        // Init request
        BlockGetInfoRequest request = new BlockGetInfoRequest();
        request.setBlockNumber(txBlockNum);

        // Call getInfo
        BlockGetInfoResponse response = sdk.getBlockService().getInfo(request);
        if (response.getErrorCode() == 0) {
            BlockGetInfoResult result = response.getResult();
            System.out.println(JSON.toJSONString(result, true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
        Assert.assertEquals(response.getErrorDesc(), Integer.valueOf(0), response.getErrorCode());
    }

    /**
     * Get latest block info
     */
    @Test
    public void Test30() {
        // Call getLatestInfo
        BlockGetLatestInfoResponse response = sdk.getBlockService().getLatestInfo();
        if (response.getErrorCode() == 0) {
            BlockGetLatestInfoResult result = response.getResult();
            System.out.println(JSON.toJSONString(result, true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
        Assert.assertEquals(response.getErrorDesc(), Integer.valueOf(0), response.getErrorCode());
    }

    /**
     * Get the authentication node information of the specified block height
     */
    @Test
    public void Test31() {
        // Init request
        BlockGetValidatorsRequest request = new BlockGetValidatorsRequest();
        request.setBlockNumber(txBlockNum);

        // Call getValidators
        BlockGetValidatorsResponse response = sdk.getBlockService().getValidators(request);
        if (response.getErrorCode() == 0) {
            BlockGetValidatorsResult result = response.getResult();
            System.out.println(JSON.toJSONString(result, true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
        Assert.assertEquals(response.getErrorDesc(), Integer.valueOf(0), response.getErrorCode());
    }

    /**
     * Get the validation node information of the latest block
     */
    @Test
    public void Test32() {
        BlockGetLatestValidatorsResponse response = sdk.getBlockService().getLatestValidators();
        if (response.getErrorCode() == 0) {
            BlockGetLatestValidatorsResult result = response.getResult();
            System.out.println(JSON.toJSONString(result, true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
        Assert.assertEquals(response.getErrorDesc(), Integer.valueOf(0), response.getErrorCode());
    }

    /**
     * Get block award and verification node reward for specifying block height
     */
    @Test
    public void Test33() {
        // Init request
        BlockGetRewardRequest request = new BlockGetRewardRequest();
        request.setBlockNumber(txBlockNum);

        // Call getReward
        BlockGetRewardResponse response = sdk.getBlockService().getReward(request);
        if (response.getErrorCode() == 0) {
            BlockGetRewardResult result = response.getResult();
            System.out.println(JSON.toJSONString(result, true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
        Assert.assertEquals(response.getErrorDesc(), Integer.valueOf(0), response.getErrorCode());
    }

    /**
     * Get the latest block award and verification node Awards
     */
    @Test
    public void Test34() {
        // Call getLatestReward
        BlockGetLatestRewardResponse response = sdk.getBlockService().getLatestReward();
        if (response.getErrorCode() == 0) {
            BlockGetLatestRewardResult result = response.getResult();
            System.out.println(JSON.toJSONString(result, true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
        Assert.assertEquals(response.getErrorDesc(), Integer.valueOf(0), response.getErrorCode());
    }

    /**
     * Get the cost standard for the specified block
     */
    @Test
    public void Test35() {
        // Init request
        BlockGetFeesRequest request = new BlockGetFeesRequest();
        request.setBlockNumber(txBlockNum);

        // Call getFees
        BlockGetFeesResponse response = sdk.getBlockService().getFees(request);
        if (response.getErrorCode() == 0) {
            System.out.println(JSON.toJSONString(response.getResult(), true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
        Assert.assertEquals(response.getErrorDesc(), Integer.valueOf(0), response.getErrorCode());
    }

    /**
     * Get the cost standard for the latest block
     */
    @Test
    public void Test36() {
        // Call getLatestFees
        BlockGetLatestFeesResponse response = sdk.getBlockService().getLatestFees();
        if (response.getErrorCode() == 0) {
            System.out.println(JSON.toJSONString(response.getResult(), true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
        Assert.assertEquals(response.getErrorDesc(), Integer.valueOf(0), response.getErrorCode());
    }

    /**
     * Set account privilege
     */
    @Test
    public void Test37() {
        // Init variable
        // The account private key to set privilege
        String accountPrivateKey = newPriv;
        // The fixed write 1000L, the unit is UGas
        Long gasPrice = 1000L;
        // Set up the maximum cost 0.01Gas
        Long feeLimit = ToBaseUnit.ToUGas("0.01");
        // Transaction initiation account's nonce + 1
        Long nonce = getAccountNonce(newAddress) + 1;

        // 1. Get the account address to send this transaction
        String accountAddresss = getAddressByPrivateKey(accountPrivateKey);

        // 2. Build setPrivilege
        AccountSetPrivilegeOperation operation = new AccountSetPrivilegeOperation();
        operation.setSourceAddress(accountAddresss);
        Signer signer2 = new Signer();
        signer2.setAddress(genesisAccount);
        signer2.setWeight(2L);
        operation.addSigner(signer2);
        operation.setTxThreshold("1");

        String[] signerPrivateKeyArr = {accountPrivateKey};
        // Record txhash for subsequent confirmation of the real result of the transaction.
        // After recommending five blocks, call again through txhash `Get the transaction information
        // from the transaction Hash'(see example: getTxByHash ()) to confirm the final result of the transaction
        String txHash = submitTransaction(signerPrivateKeyArr, accountAddresss, operation, nonce, gasPrice, feeLimit);
        if (txHash != null) {
            System.out.println("hash: " + txHash);
        }
    }

    /**
     * Write logs to the Gas block chain
     */
    @Test
    public void Test38() {
        // Init variable
        // The account private key to create log
        String createPrivateKey = newPriv;
        // Log topic
        String topic = "test";
        // Log content
        String data = "this is not a error";
        // notes
        String metadata = "create log";
        // The fixed write 1000L, the unit is UGas
        Long gasPrice = 1000L;
        // Set up the maximum cost 0.01Gas
        Long feeLimit = ToBaseUnit.ToUGas("0.01");
        // Transaction initiation account's nonce + 1
        Long nonce = getAccountNonce(newAddress) + 1;

        // 1. Get the account address to send this transaction
        String createAddresss = getAddressByPrivateKey(createPrivateKey);

        // Build createLog
        LogCreateOperation operation = new LogCreateOperation();
        operation.setSourceAddress(createAddresss);
        operation.setTopic(topic);
        operation.addData(data);
        operation.setMetadata(metadata);

        String[] signerPrivateKeyArr = {createPrivateKey};
        // Record txhash for subsequent confirmation of the real result of the transaction.
        // After recommending five blocks, call again through txhash `Get the transaction information
        // from the transaction Hash'(see example: getTxByHash ()) to confirm the final result of the transaction
        String txHash = submitTransaction(signerPrivateKeyArr, createAddresss, operation, nonce, gasPrice, feeLimit);
        if (txHash != null) {
            System.out.println("hash: " + txHash);
        }
    }

    /**
     * @param senderPrivateKeys The account private keys to sign transaction
     * @param senderAddresss   The account address to start transaction
     * @param operation        operation
     * @param senderNonce      Transaction initiation account's Nonce
     * @param gasPrice         Gas price
     * @param feeLimit         fee limit
     * @return java.lang.String transaction hash
     * @author riven
     */
    private String submitTransaction(String[] senderPrivateKeys, String senderAddresss, BaseOperation operation, Long senderNonce, Long gasPrice, Long feeLimit) {
        // 3. Build transaction
        TransactionBuildBlobRequest transactionBuildBlobRequest = new TransactionBuildBlobRequest();
        transactionBuildBlobRequest.setSourceAddress(senderAddresss);
        transactionBuildBlobRequest.setNonce(senderNonce);
        transactionBuildBlobRequest.setFeeLimit(feeLimit);
        transactionBuildBlobRequest.setGasPrice(gasPrice);
        transactionBuildBlobRequest.addOperation(operation);
        // transactionBuildBlobRequest.setMetadata("abc");

        // 4. Build transaction BLob
        String transactionBlob;
        TransactionBuildBlobResponse transactionBuildBlobResponse = sdk.getTransactionService().buildBlob(transactionBuildBlobRequest);
        if (transactionBuildBlobResponse.getErrorCode() != 0) {
            System.out.println("error: " + transactionBuildBlobResponse.getErrorDesc());
            Assert.assertEquals(transactionBuildBlobResponse.getErrorDesc(), Integer.valueOf(0), transactionBuildBlobResponse.getErrorCode());
            return null;
        }
        TransactionBuildBlobResult transactionBuildBlobResult = transactionBuildBlobResponse.getResult();
        String txHash = transactionBuildBlobResult.getHash();
        transactionBlob = transactionBuildBlobResult.getTransactionBlob();

        try {
            Chain.Transaction tran = Chain.Transaction.parseFrom(HexFormat.hexToByte(transactionBlob));
            JsonFormat jsonFormat = new JsonFormat();
            System.out.println(jsonFormat.printToString(tran));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        // 5. Sign transaction BLob
        TransactionSignRequest transactionSignRequest = new TransactionSignRequest();
        transactionSignRequest.setBlob(transactionBlob);
        for (int i = 0; i < senderPrivateKeys.length; i++) {
            transactionSignRequest.addPrivateKey(senderPrivateKeys[i]);
        }
        TransactionSignResponse transactionSignResponse = sdk.getTransactionService().sign(transactionSignRequest);
        if (transactionSignResponse.getErrorCode() != 0) {
            System.out.println("error: " + transactionSignResponse.getErrorDesc());
            Assert.assertEquals(transactionSignResponse.getErrorDesc(), Integer.valueOf(0), transactionSignResponse.getErrorCode());
            return null;
        }

        // 6. Broadcast
        TransactionSubmitRequest transactionSubmitRequest = new TransactionSubmitRequest();
        transactionSubmitRequest.setTransactionBlob(transactionBlob);
        transactionSubmitRequest.setSignatures(transactionSignResponse.getResult().getSignatures());
        TransactionSubmitResponse transactionSubmitResponse = sdk.getTransactionService().submit(transactionSubmitRequest);
        if (0 == transactionSubmitResponse.getErrorCode()) {
            System.out.println("Success，hash=" + transactionSubmitResponse.getResult().getHash());
            txHash = transactionSubmitResponse.getResult().getHash();
        } else {
            System.out.println("Failure，hash=" + transactionSubmitResponse.getResult().getHash() + "");
            Assert.assertEquals(transactionSubmitResponse.getErrorDesc(), Integer.valueOf(0), transactionSubmitResponse.getErrorCode());
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return txHash;
    }

    /**
     * @param senderPrivateKey The account private key to start transaction
     * @param destAddress      The account to receive gas
     * @param amount           Gas amount
     * @param nonce            The account nonce to start transaction
     * @param gasPrice         Gas price
     * @param feeLimit         Fee limit
     * @return TransactionFees transaction fees
     * @author riven
     */
    private TransactionFees evaluateFees(String senderPrivateKey, String destAddress, Long amount, Long nonce, Long gasPrice, Long feeLimit) throws Exception {
        // 1. Get the account address to send this transaction
        String senderAddresss = getAddressByPrivateKey(senderPrivateKey);

        // 2. Build sendGas
        GasSendOperation gasSendOperation = new GasSendOperation();
        gasSendOperation.setSourceAddress(senderAddresss);
        gasSendOperation.setDestAddress(destAddress);
        gasSendOperation.setAmount(amount);
        gasSendOperation.setMetadata("616263");

        // 3. Init request
        TransactionEvaluateFeeRequest request = new TransactionEvaluateFeeRequest();
        request.addOperation(gasSendOperation);
        request.setSourceAddress(senderAddresss);
        request.setNonce(nonce);
        request.setSignatureNumber(1);
        request.setMetadata("616263");

        TransactionEvaluateFeeResponse response = sdk.getTransactionService().evaluateFee(request);
        if (response.getErrorCode() == 0) {
            return response.getResult().getTxs()[0].getTransactionEnv().getTransactionFees();
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
        Assert.assertEquals(response.getErrorDesc(), Integer.valueOf(0), response.getErrorCode());
        return null;
    }

    /**
     * @param privatekey private key
     * @return java.lang.String Account address
     * @author riven
     */
    private String getAddressByPrivateKey(String privatekey) {
        String publicKey = PrivateKey.getEncPublicKey(privatekey);
        String address = PrivateKey.getEncAddress(publicKey);
        return address;
    }
}
