package org.zetrix.sdk.example;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.zetrix.SDK;
import org.zetrix.common.ToBaseUnit;
import org.zetrix.model.request.*;
import org.zetrix.model.request.operation.*;
import org.zetrix.model.response.*;
import org.zetrix.model.response.result.data.*;

/**
 * @Author riven
 * @Date 2018/10/23 01:24
 */
public class TriggerContractDemo {
    public static SDK sdk;
    public static void main(String argv[]) {
        String url = "http://192.168.10.100:19343";
        sdk = SDK.getInstance(url);

//        long nonce = getAccountNonce();
//        System.out.println("nonce:" + nonce);
//        BaseOperation[] operations = buildOperations();
//        String transactionBlob = seralizeTransaction(nonce, operations);
//        System.out.println("blob: " + transactionBlob);
//        Signature[] signatures = signTransaction(transactionBlob);
//        System.out.println("signData: " + signatures[0].getSignData());
//        System.out.println("publicKey: " + signatures[0].getPublicKey());
//        String hash = submitTransaction(transactionBlob, signatures);
//        System.out.println("hash: " + hash);
//        int status = checkTransactionStatus(hash);
//        System.out.println("status: " + status);

        String result = queryContract();
        System.out.println(result);
    }

    public static long getAccountNonce() {
        long nonce = 0;

        // Init request
        String accountAddress = "ZTX3Ta7d4GyAXD41H2kFCTd2eXhDesM83rvC3";
        AccountGetNonceRequest request = new AccountGetNonceRequest();
        request.setAddress(accountAddress);

        // Call getNonce
        AccountGetNonceResponse response = sdk.getAccountService().getNonce(request);
        if (0 == response.getErrorCode()) {
            nonce = response.getResult().getNonce();
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
        return nonce;
    }

    public static BaseOperation[] buildOperations() {
        // The account address to issue apt1.0 token
        String invokeAddress = "ZTX3Ta7d4GyAXD41H2kFCTd2eXhDesM83rvC3";
        // The contract address
        String contractAddress = "ZTX3dPvBNpB9teUmx5F3efcn2ox1DcRxBPmPu";
        // The destination address
        String destAddress = "ZTX3RYKQ1DV9RHTixFPVnfoWJavez6MgUP1EP";
        // The amount to be transfered
        String transferAmount = "10000";

        // build transfer method input
        JSONObject transferInput = new JSONObject();
        transferInput.put("method", "transfer");
        JSONObject transferParams = new JSONObject();
        transferParams.put("to", destAddress);
        transferParams.put("value", transferAmount);
        transferInput.put("params", transferParams);

        // build send gas operation to transfer CGO
        ContractInvokeByGasOperation transferOperation = new ContractInvokeByGasOperation();
        transferOperation.setSourceAddress(invokeAddress);
        transferOperation.setContractAddress(contractAddress);
        transferOperation.setZtxAmount(0L);
        transferOperation.setInput(transferInput.toJSONString());

        BaseOperation[] operations = { transferOperation };
        return operations;
    }

        public static String seralizeTransaction(Long nonce,  BaseOperation[] operations) {
        String transactionBlob = null;

        // The account address to create contract and issue ctp 1.0 token
        String senderAddresss = "ZTX3Ta7d4GyAXD41H2kFCTd2eXhDesM83rvC3";
        // The gasPrice is fixed at 1000L, the unit is UGas
        Long gasPrice = 1000L;
        // Set up the maximum cost 10.08Gas
        Long feeLimit = ToBaseUnit.ToUGas("10.08");
        // Nonce should add 1
        nonce += 1;

        // Build transaction  Blob
        TransactionBuildBlobRequest transactionBuildBlobRequest = new TransactionBuildBlobRequest();
        transactionBuildBlobRequest.setSourceAddress(senderAddresss);
        transactionBuildBlobRequest.setNonce(nonce);
        transactionBuildBlobRequest.setFeeLimit(feeLimit);
        transactionBuildBlobRequest.setGasPrice(gasPrice);
        for (int i = 0; i < operations.length; i++) {
            transactionBuildBlobRequest.addOperation(operations[i]);
        }
        TransactionBuildBlobResponse transactionBuildBlobResponse = sdk.getTransactionService().buildBlob(transactionBuildBlobRequest);
        if (transactionBuildBlobResponse.getErrorCode() == 0) {
            transactionBlob = transactionBuildBlobResponse. getResult().getTransactionBlob();
        } else {
            System.out.println("error: " + transactionBuildBlobResponse.getErrorDesc());
        }
        return transactionBlob;
    }

    public static Signature[] signTransaction(String transactionBlob) {
        Signature[] signatures = null;
        // The account private key to issue atp1.0 token
        String senderPrivateKey = "privBwYirzSUQ7ZhgLbDpRXC2A75HoRtGAKSF76dZnGGYXUvHhCK4xuz";

        // Sign transaction BLob
        TransactionSignRequest transactionSignRequest = new TransactionSignRequest();
        transactionSignRequest.setBlob(transactionBlob);
        transactionSignRequest.addPrivateKey(senderPrivateKey);
        TransactionSignResponse transactionSignResponse = sdk.getTransactionService().sign(transactionSignRequest);
        if (transactionSignResponse.getErrorCode() == 0) {
            signatures = transactionSignResponse.getResult().getSignatures();
        } else {
            System.out.println("error: " + transactionSignResponse.getErrorDesc());
        }
        return signatures;
    }

    public static String submitTransaction(String transactionBlob, Signature[] signatures) {
        String  hash = null;

        // Submit transaction
        TransactionSubmitRequest transactionSubmitRequest = new TransactionSubmitRequest();
        transactionSubmitRequest.setTransactionBlob(transactionBlob);
        transactionSubmitRequest.setSignatures(signatures);
        TransactionSubmitResponse transactionSubmitResponse = sdk.getTransactionService().submit(transactionSubmitRequest);
        if (0 == transactionSubmitResponse.getErrorCode()) {
            hash = transactionSubmitResponse.getResult().getHash();
        } else {
            System.out.println("error: " + transactionSubmitResponse.getErrorDesc());
        }
        return  hash ;
    }


    public static int checkTransactionStatus(String txHash) {
        // Init request
        TransactionGetInfoRequest request = new TransactionGetInfoRequest();
        request.setHash(txHash);

        // Call getInfo
        TransactionGetInfoResponse response = sdk.getTransactionService().getInfo(request);
        int errorCode = response.getErrorCode();
        if (errorCode == 0){
            TransactionHistory transactionHistory = response.getResult().getTransactions()[0];
            errorCode = transactionHistory.getErrorCode();
        }

        return errorCode;
    }

    public static String queryContract() {
        // Init variable
        // Contract address
        String contractAddress = "ZTX3dPvBNpB9teUmx5F3efcn2ox1DcRxBPmPu";
        // TokenOwner address
        String tokenOwner = "ZTX3Ta7d4GyAXD41H2kFCTd2eXhDesM83rvC3";

        // Init input
        JSONObject input = new JSONObject();
        input.put("method", "balanceOf");
        JSONObject params = new JSONObject();
        params.put("address", tokenOwner);
        input.put("params", params);

        // Init request
        ContractCallRequest request = new ContractCallRequest();
        request.setContractAddress(contractAddress);
        request.setFeeLimit(10000000000L);
        request.setOptType(2);
        request.setInput(input.toJSONString());

        // Call call
        String result = null;
        ContractCallResponse response = sdk.getContractService().call(request);
        if (response.getErrorCode() == 0) {
            result = JSON.toJSONString(response.getResult().getQueryRets().getJSONObject(0));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
        return result;
    }
}
