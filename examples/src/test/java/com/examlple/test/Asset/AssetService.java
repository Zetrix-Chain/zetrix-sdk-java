package com.examlple.test.Asset;

import org.testng.annotations.Test;
import org.zetrix.SDK;
import org.zetrix.model.request.AssetGetInfoRequest;
import org.zetrix.model.response.AssetGetInfoResponse;

/**
 * @author
 * @description
 * @date
 */
public class AssetService {
    private SDK sdk = SDK.getInstance("http://192.168.10.100:19343");
    @Test(groups = {"testg"})
    public void getInfo() {
        String address = "ZTX3Ta7d4GyAXD41H2kFCTd2eXhDesM83rvC3";
        String code = "102";
        String issuer = "ZTX3Ta7d4GyAXD41H2kFCTd2eXhDesM83rvC3";
        AssetGetInfoRequest request = new AssetGetInfoRequest();
        request.setAddress(address);
        request.setCode(code);
        request.setIssuer(issuer);

        AssetGetInfoResponse response = sdk.getAssetService().getInfo(request);
        System.out.println(response.getResult().getAssets()[0].getAmount());


    }

    @Test
    public void print(){
        System.out.println("test");
    }
}
