package com.examlple.test.account;

import org.testng.annotations.Test;
import org.zetrix.SDK;
import org.zetrix.model.request.*;
import org.zetrix.model.response.*;

/**
 * @author
 * @description
 * @date
 */
public class Accountservice {
    private SDK sdk = SDK.getInstance("http://192.168.10.100:19343");
    @Test(dataProvider = "addressProvide",dataProviderClass =AddressProvider.class )
    public void getNonce(String address)
    {
        AccountGetNonceRequest request = new AccountGetNonceRequest();
        request.setAddress(address);

        AccountGetNonceResponse response = sdk.getAccountService().getNonce(request);
        System.out.println(response.getResult().getNonce());
    }

    @Test(dataProvider = "addressProvide",dataProviderClass =AddressProvider.class )
    public void checkVaild(String address){
        AccountCheckValidRequest request =new AccountCheckValidRequest();
        request.setAddress(address);

        AccountCheckValidResponse response = sdk.getAccountService().checkValid(request);
        System.out.println(response.getResult().isValid());


    }

    @Test(dataProvider = "addressProvide",dataProviderClass =AddressProvider.class )
    public void getInfo(String address) {
        AccountGetInfoRequest request = new AccountGetInfoRequest();
        request.setAddress(address);
        AccountGetInfoResponse response = sdk.getAccountService().getInfo(request);
        System.out.println(response.getResult().getBalance());
        System.out.println(response.getResult().getAddress());
        System.out.println(response.getResult().getNonce());
        System.out.println(response.getResult().getPriv());

    }

    @Test(dataProvider = "addressProvide",dataProviderClass =AddressProvider.class )
    public void getBalance(String address) {
        AccountGetBalanceRequest request = new AccountGetBalanceRequest();
        request.setAddress(address);
        AccountGetBalanceResponse response = sdk.getAccountService().getBalance(request);
        System.out.println(response.getResult().getBalance());
    }

    @Test(dataProvider = "addressProvide",dataProviderClass =AddressProvider.class )
    public void getAsset(String address) {
        AccountGetAssetsRequest request = new AccountGetAssetsRequest();
        request.setAddress(address);
        AccountGetAssetsResponse response = sdk.getAccountService().getAssets(request);
        System.out.println(response.getResult().getAssets()[0].getKey());

    }

    @Test(dataProvider = "addressProvide",dataProviderClass =AddressProvider.class )
    public void getMetadata(String address) {
        AccountGetMetadataRequest request = new AccountGetMetadataRequest();
        request.setAddress(address);
        request.setKey("author");
        AccountGetMetadataResponse response = sdk.getAccountService().getMetadata(request);
        System.out.println(response.getResult().getMetadatas()[0].getValue());
    }
}
