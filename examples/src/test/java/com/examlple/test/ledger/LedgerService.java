package com.examlple.test.ledger;

import com.alibaba.fastjson.JSON;
import org.testng.annotations.Test;
import org.zetrix.SDK;
import org.zetrix.crypto.Keypair;
import org.zetrix.model.request.*;
import org.zetrix.model.response.*;
import org.zetrix.model.response.result.*;

/**
 * @author
 * @description
 * @date
 */
public class LedgerService {
    private SDK sdk = SDK.getInstance("http://192.168.10.100:19343");

    @Test
    public void getNumber() {
        Keypair keypair = Keypair.generator();
        BlockGetNumberResponse response = sdk.getBlockService().getNumber();
        if (0 == response.getErrorCode()) {
            System.out.println(JSON.toJSONString(response.getResult(), true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
    }

    @Test
    public void checkStatus() {
        BlockCheckStatusResponse response = sdk.getBlockService().checkStatus();
        if (0 == response.getErrorCode()) {
            System.out.println(JSON.toJSONString(response.getResult(), true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
    }

    @Test
    public void getTranscation() {
        Long blockNumber = 4210L;// 第617247区块
        BlockGetTransactionsRequest request = new BlockGetTransactionsRequest();
        request.setBlockNumber(blockNumber);

        // 调用getTransactions接口
        BlockGetTransactionsResponse response = sdk.getBlockService().getTransactions(request);
        if (0 == response.getErrorCode()) {
            System.out.println(JSON.toJSONString(response.getResult(), true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
    }

    @Test
    public void getInfo() {
        BlockGetInfoRequest request = new BlockGetInfoRequest();
        request.setBlockNumber(4210L);

// 调用getInfo接口
        BlockGetInfoResponse response = sdk.getBlockService().getInfo(request);
        if (response.getErrorCode() == 0) {
            BlockGetInfoResult result = response.getResult();
            System.out.println(JSON.toJSONString(result, true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
    }

    @Test
    public void getLastInfo(){
        BlockGetLatestInfoResponse response = sdk.getBlockService().getLatestInfo();
        if (response.getErrorCode() == 0) {
            BlockGetLatestInfoResult result = response.getResult();
            System.out.println(JSON.toJSONString(result, true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
    }

    @Test
    public void getValidators(){
        BlockGetValidatorsRequest request = new BlockGetValidatorsRequest();
        request.setBlockNumber(4210L);

// 调用getValidators接口
        BlockGetValidatorsResponse response = sdk.getBlockService().getValidators(request);
        if (response.getErrorCode() == 0) {
            BlockGetValidatorsResult result = response.getResult();
            System.out.println(JSON.toJSONString(result, true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
    }

    @Test
    public void getLastValidators(){
        BlockGetLatestValidatorsResponse response = sdk.getBlockService().getLatestValidators();
        if (response.getErrorCode() == 0) {
            BlockGetLatestValidatorsResult result = response.getResult();
            System.out.println(JSON.toJSONString(result, true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
    }

    @Test
    public void getReward(){
        BlockGetRewardRequest request = new BlockGetRewardRequest();
        request.setBlockNumber(4000L);

        // 调用getReward接口
        BlockGetRewardResponse response = sdk.getBlockService().getReward(request);
        if (response.getErrorCode() == 0) {
            BlockGetRewardResult result = response.getResult();
            System.out.println(JSON.toJSONString(result, true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
    }
    }

    @Test
    public void getLastReward(){
        BlockGetLatestRewardResponse response = sdk.getBlockService().getLatestReward();
        if (response.getErrorCode() == 0) {
            BlockGetLatestRewardResult result = response.getResult();
            System.out.println(JSON.toJSONString(result, true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
    }

    @Test
    public void getFees(){
        BlockGetFeesRequest request = new BlockGetFeesRequest();
        request.setBlockNumber(4201L);

// 调用getFees接口
        BlockGetFeesResponse response = sdk.getBlockService().getFees(request);
        if (response.getErrorCode() == 0) {
            System.out.println(JSON.toJSONString(response.getResult(), true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }
    }

    @Test
    public void getLastFees(){
        BlockGetLatestFeesResponse response = sdk.getBlockService().getLatestFees();
        if (response.getErrorCode() == 0) {
            System.out.println(JSON.toJSONString(response.getResult(), true));
        } else {
            System.out.println("error: " + response.getErrorDesc());
        }

    }

    @Test
    public void getAddress(){
        Keypair keypair = Keypair.generator();
        System.out.println(keypair.getAddress());
        System.out.println(keypair.getPrivateKey());
        System.out.println(keypair.getPublicKey());
    }
}
