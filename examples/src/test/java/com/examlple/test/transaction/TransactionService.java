package com.examlple.test.transaction;

import com.alibaba.fastjson.JSON;
import org.testng.annotations.Test;
import org.zetrix.SDK;
import org.zetrix.common.ToBaseUnit;
import org.zetrix.encryption.utils.hex.HexFormat;
import org.zetrix.model.request.*;
import org.zetrix.model.request.operation.BaseOperation;
import org.zetrix.model.request.operation.GasSendOperation;
import org.zetrix.model.response.*;
import org.zetrix.model.response.result.TransactionEvaluateFeeResult;
import org.zetrix.model.response.result.data.Operation;
import org.zetrix.model.response.result.data.Signature;

/**
 * @author
 * @description
 * @date
 */
public class TransactionService {
    private SDK sdk = SDK.getInstance("http://192.168.10.100:19343");
    @Test(dataProvider = "operationProvider",dataProviderClass = OperationProvider.class)
    public void submit(BaseOperation operation, Long nonce){


        String address = "ZTX3Ta7d4GyAXD41H2kFCTd2eXhDesM83rvC3";

        TransactionBuildBlobRequest buildBlobRequest = new TransactionBuildBlobRequest();
        buildBlobRequest.setSourceAddress(address);
        buildBlobRequest.setFeeLimit(200000000000l);
        buildBlobRequest.setGasPrice(1000L);
        buildBlobRequest.setNonce(nonce);
        buildBlobRequest.setOperation(operation);

        TransactionBuildBlobResponse buildBlobResponse = sdk.getTransactionService().buildBlob(buildBlobRequest);

        TransactionSignRequest  signRequest = new TransactionSignRequest();
        signRequest.setBlob(buildBlobResponse.getResult().getTransactionBlob());
        String[] privateKeys = new String[]{"privBxGeMBaXMfomHDzeJrNb7dc5f7XSc9WaKHNksafaASsgmY5daN4R","privBwYirzSUQ7ZhgLbDpRXC2A75HoRtGAKSF76dZnGGYXUvHhCK4xuz"};
        signRequest.setPrivateKeys(privateKeys);
        TransactionSignResponse signResponse = sdk.getTransactionService().sign(signRequest);

        TransactionSubmitRequest submitRequest = new TransactionSubmitRequest();
        submitRequest.setTransactionBlob(buildBlobResponse.getResult().getTransactionBlob());
        submitRequest.setSignatures(signResponse.getResult().getSignatures());

        TransactionSubmitResponse response = sdk.getTransactionService().submit(submitRequest);
        if (0 == response.getErrorCode()) { // 交易提交成功
            System.out.println(response.getResult().getHash());
        } else{
            System.out.println("error: " + response.getErrorDesc());
        }

    }

    @Test(dataProvider = "hashProvider",dataProviderClass = OperationProvider.class)
    public void getInfo(String hash){
        TransactionGetInfoRequest request = new TransactionGetInfoRequest();
        request.setHash(hash);
        TransactionGetInfoResponse response = sdk.getTransactionService().getInfo(request);
        System.out.println("------------------------------------------------------------------");
        System.out.println(response.getResult().getTransactions()[0].getTxSize());
    }

    @Test
    public void evaluateFee()
    {
        String destAddress = "ZTX3FaM7kHNttENVRxYzMGSX7bLKNRhQnBKFJ";

        String senderAddresss = "ZTX3Ta7d4GyAXD41H2kFCTd2eXhDesM83rvC3";
        Long eaAmount = ToBaseUnit.ToUGas("10.9");
        Long gasPrice = 1000L;
        Long feeLimit = ToBaseUnit.ToUGas("0.01");
        Long nonce = 51L;

// 构建sendGas操作
        GasSendOperation gasSendOperation = new GasSendOperation();
        gasSendOperation.setSourceAddress(senderAddresss);
        gasSendOperation.setDestAddress(destAddress);
        gasSendOperation.setAmount(eaAmount);

// 初始化评估交易请求参数
        TransactionEvaluateFeeRequest request = new TransactionEvaluateFeeRequest();
        request.addOperation(gasSendOperation);
        request.setSourceAddress(senderAddresss);
        request.setNonce(nonce);
        request.setSignatureNumber(1);
        request.setMetadata(HexFormat.byteToHex("evaluate fees".getBytes()));

// 调用evaluateFee接口
        TransactionEvaluateFeeResponse response = sdk.getTransactionService().evaluateFee(request);
        if (response.getErrorCode() == 0) {
            TransactionEvaluateFeeResult result = response.getResult();
            System.out.println(JSON.toJSONString(result, true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
    }
}
