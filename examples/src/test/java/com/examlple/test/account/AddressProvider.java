package com.examlple.test.account;

import org.testng.annotations.DataProvider;

/**
 * @author
 * @description
 * @date
 */
public class AddressProvider {
    @DataProvider(name = "addressProvide")
    public Object[][] addressProvide() {
        return new Object[][]{
                {"ZTX3Ta7d4GyAXD41H2kFCTd2eXhDesM83rvC3"},
                {"ZTX3FaM7kHNttENVRxYzMGSX7bLKNRhQnBKFJ"},
                {"ZTX3RamGgUv5cED5n2LXCJhi5YJXTXE9rqfiB"}
        };
    }
}
