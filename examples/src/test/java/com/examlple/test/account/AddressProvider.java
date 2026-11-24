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
                {"ZTX3ag1AT3JHSkTr3XTfnuhwHgtJ88z3SxY8T"},
                {"ZTX3FaM7kHNttENVRxYzMGSX7bLKNRhQnBKFJ"},
                {"ZTX3RamGgUv5cED5n2LXCJhi5YJXTXE9rqfiB"}
        };
    }
}
