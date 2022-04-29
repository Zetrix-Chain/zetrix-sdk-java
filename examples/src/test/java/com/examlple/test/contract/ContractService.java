package com.examlple.test.contract;

import org.testng.annotations.Test;
import org.zetrix.SDK;
import org.zetrix.model.request.ContractCallRequest;
import org.zetrix.model.request.ContractCheckValidRequest;
import org.zetrix.model.request.ContractGetAddressRequest;
import org.zetrix.model.request.ContractGetInfoRequest;
import org.zetrix.model.response.ContractCallResponse;
import org.zetrix.model.response.ContractCheckValidResponse;
import org.zetrix.model.response.ContractGetAddressResponse;
import org.zetrix.model.response.ContractGetInfoResponse;

/**
 * @author
 * @description
 * @date
 */
public class ContractService {
    private SDK sdk = SDK.getInstance("http://192.168.10.100:19343");
    @Test
    public void getaddres(){
        String hash = "4c588af9f05e1e8a6d4d7e5786c9f06e782568abc9961f855e5189b99edfeaf9";
        ContractGetAddressRequest request =new ContractGetAddressRequest();
        request.setHash(hash);

        ContractGetAddressResponse response = sdk.getContractService().getAddress(request);
        System.out.println(response.getResult().getContractAddressInfos().get(0).getContractAddress());
    }

    @Test
    public void checkValid(){
        String contractAddress = "ZTX3RamGgUv5cED5n2LXCJhi5YJXTXE9rqfiB";
        ContractCheckValidRequest request = new ContractCheckValidRequest();
        request.setContractAddress(contractAddress);

        ContractCheckValidResponse response = sdk.getContractService().checkValid(request);

        System.out.println(response.getResult().getValid());
    }

    @Test
    public void getInfo(){
        String contractAddress = "ZTX3RamGgUv5cED5n2LXCJhi5YJXTXE9rqfiB";
        ContractGetInfoRequest request = new ContractGetInfoRequest();
        request.setContractAddress(contractAddress);

        ContractGetInfoResponse response = sdk.getContractService().getInfo(request);

        System.out.println(response.getResult().getContract().getPayload());


    }

    @Test
    public void call(){
        ContractCallRequest request = new ContractCallRequest();
        String contractAddress = "ZTX3RamGgUv5cED5n2LXCJhi5YJXTXE9rqfiB";
        request.setContractAddress(contractAddress);
        request.setOptType(2);
        request.setInput("{\"input\":\"dzw\"}");
        request.setFeeLimit(200000000000l);
        request.setGasPrice(1000L);


        ContractCallResponse response = sdk.getContractService().call(request);
        System.out.println(response.getResult().getQueryRets().get(0));

    }
}
