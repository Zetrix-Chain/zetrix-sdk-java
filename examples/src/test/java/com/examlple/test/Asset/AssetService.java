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
    private SDK sdk = SDK.getInstance("https://test-node.zetrix.com");
    @Test(groups = {"testg"})
    public void getInfo() {
        String address = "ZTX3ag1AT3JHSkTr3XTfnuhwHgtJ88z3SxY8T";
        String code = "102";
        String issuer = "ZTX3ag1AT3JHSkTr3XTfnuhwHgtJ88z3SxY8T";
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
