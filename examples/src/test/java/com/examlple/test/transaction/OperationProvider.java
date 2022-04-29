package com.examlple.test.transaction;

import org.testng.annotations.DataProvider;
import org.zetrix.model.request.operation.*;
import org.zetrix.model.response.result.data.Signer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @description
 * @date
 */
public class OperationProvider {

    @DataProvider(name = "operationProvider")
    public Object[][] operationProvide(){
        String sourceAddress = "ZTX3Ta7d4GyAXD41H2kFCTd2eXhDesM83rvC3";
        String destAddress = "ZTX3FaM7kHNttENVRxYzMGSX7bLKNRhQnBKFJ";
        String contractAddres = "ZTX3RamGgUv5cED5n2LXCJhi5YJXTXE9rqfiB";


        AccountActivateOperation accountActivateOperation = new AccountActivateOperation();
        accountActivateOperation.setDestAddress(destAddress);
        accountActivateOperation.setSourceAddress(sourceAddress);
        accountActivateOperation.setInitBalance(10000000L);
        accountActivateOperation.setMetadata("active");

        AccountSetMetadataOperation accountSetMetadataOperation = new AccountSetMetadataOperation();
        accountSetMetadataOperation.setSourceAddress(sourceAddress);
        accountSetMetadataOperation.setKey("author");
        accountSetMetadataOperation.setValue("dzw");
        accountSetMetadataOperation.setVersion(1L);
        accountSetMetadataOperation.setDeleteFlag(false);

        AssetIssueOperation assetIssueOperation = new AssetIssueOperation();
        assetIssueOperation.setSourceAddress(sourceAddress);
        assetIssueOperation.setCode("102");
        assetIssueOperation.setAmount(10000000L);

        AssetSendOperation assetSendOperation = new AssetSendOperation();
        assetSendOperation.setSourceAddress(sourceAddress);
        assetSendOperation.setDestAddress(destAddress);
        assetSendOperation.setAmount(100000L);
        assetSendOperation.setCode("101");
        assetSendOperation.setIssuer(sourceAddress);

        GasSendOperation gasSendOperation = new GasSendOperation();
        gasSendOperation.setAmount(100000000L);
        gasSendOperation.setSourceAddress(sourceAddress);
        gasSendOperation.setDestAddress(destAddress);

        ContractCreateOperation contractCreateOperation = new ContractCreateOperation();
        contractCreateOperation.setInitBalance(10000000L);
        contractCreateOperation.setSourceAddress(sourceAddress);
        contractCreateOperation.setType(0);
        contractCreateOperation.setPayload("\"use strict\";function init(input){return;}function main(input){let para = JSON.parse(input);let x = {'hello' : 'world'};}function query(input){return input;}");
        contractCreateOperation.setInitInput("{}");

        LogCreateOperation logCreateOperation = new LogCreateOperation();
        logCreateOperation.setSourceAddress(sourceAddress);
        logCreateOperation.setTopic("logtest");
        List<String> logList = new ArrayList<>();
        logList.add("error");
        logCreateOperation.setDatas(logList);

        ContractInvokeByAssetOperation contractInvokeByAssetOperation = new ContractInvokeByAssetOperation();
        contractInvokeByAssetOperation.setContractAddress(contractAddres);
        contractInvokeByAssetOperation.setSourceAddress(sourceAddress);
        contractInvokeByAssetOperation.setCode("102");
        contractInvokeByAssetOperation.setIssuer(sourceAddress);
        contractInvokeByAssetOperation.setInput("{}");
        contractInvokeByAssetOperation.setAssetAmount(1000000L);

        ContractInvokeByGasOperation contractInvokeByGasOperation = new ContractInvokeByGasOperation();
        contractInvokeByGasOperation.setContractAddress(contractAddres);
        contractInvokeByGasOperation.setInput("{}");
        contractInvokeByGasOperation.setSourceAddress(sourceAddress);
        contractInvokeByGasOperation.setZtxAmount(1000000L);

        AccountSetPrivilegeOperation accountSetPrivilegeOperation = new AccountSetPrivilegeOperation();
        accountSetPrivilegeOperation.setSourceAddress(destAddress);
        accountSetPrivilegeOperation.setMasterWeight("2");
        accountSetPrivilegeOperation.setTxThreshold("3");
        Signer signer = new Signer();
        signer.setAddress(sourceAddress);
        signer.setWeight(1L);
        accountSetPrivilegeOperation.addSigner(signer);




        return new Object[][]{
//                {accountActivateOperation,48L},
//                {assetIssueOperation,52L},
//                {assetSendOperation,53L},
//                {contractCreateOperation,54L},
//                {logCreateOperation,55L},
//                {contractInvokeByAssetOperation,56L},
//                {contractInvokeByGasOperation,57L},
                {accountSetPrivilegeOperation,58L}
        };
    }

    @DataProvider(name = "hashProvider")
    public Object[][] hashProvider(){
        return new Object[][]{
                {"c2859edb32fac92a047f6e55d1dd1cd93dceb8d3eb1da1ad9ee7fd66112ef906"}
        };
    }
}
