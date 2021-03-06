package org.zetrix;

import org.zetrix.account.AccountService;
import org.zetrix.account.impl.AccountServiceImpl;
import org.zetrix.blockchain.BlockService;
import org.zetrix.blockchain.TransactionService;
import org.zetrix.blockchain.impl.BlockServiceImpl;
import org.zetrix.blockchain.impl.TransactionServiceImpl;
import org.zetrix.common.Tools;
import org.zetrix.contract.ContractService;
import org.zetrix.contract.impl.ContractServiceImpl;
import org.zetrix.crypto.http.HttpKit;
import org.zetrix.exception.SDKException;
import org.zetrix.exception.SdkError;
import org.zetrix.model.request.SDKConfigure;
import org.zetrix.token.AssetService;
import org.zetrix.token.impl.AssetServiceImpl;

/**
 * @Author riven
 * @Date 2018/7/4 12:23
 */
public class SDK {
    private static SDK sdk = null;
    private String url;
    private long chainId = 0;

    /**
     * @Author riven
     * @Method Structure
     * @Params [url]
     * @Date 2018/7/15 14:50
     */
    public SDK() {
    }

    /**
     * @Author riven
     * @Method getInstance
     * @Params [url]
     * @Return SDK
     * @Date 2018/7/15 14:51
     */
    public synchronized static SDK getInstance(String url) throws SDKException {
        if (sdk == null) {
            sdk = new SDK();
        }
        sdk.init(url);
        return sdk;
    }

    /**
     * @Author riven
     * @Method getInstance
     * @Params [url]
     * @Return SDK
     * @Date 2018/7/15 14:51
     */
    public synchronized static SDK getInstance(SDKConfigure sdkConfigure) throws SDKException {
        if (sdk == null) {
            sdk = new SDK();
        }
        sdk.init(sdkConfigure);
        return sdk;
    }

    /**
     * @Author riven
     * @Method getAccountService
     * @Params []
     * @Return AccountService
     * @Date 2018/7/15 14:50
     */
    public AccountService getAccountService() {
        return new AccountServiceImpl();
    }

    /**
     * @Author riven
     * @Method getAssetService
     * @Params []
     * @Return AssetService
     * @Date 2018/7/15 14:50
     */
    public AssetService getAssetService() {
        return new AssetServiceImpl();
    }

    /**
     * @Author riven
     * @Method getTransactionService
     * @Params []
     * @Return TransactionService
     * @Date 2018/7/15 14:50
     */
    public TransactionService getTransactionService() {
        return new TransactionServiceImpl();
    }

    /**
     * @Author riven
     * @Method getBlockService
     * @Params []
     * @Return BlockService
     * @Date 2018/7/15 14:50
     */
    public BlockService getBlockService() {
        return new BlockServiceImpl();
    }

    /**
     * @Author riven
     * @Method getContractService
     * @Params []
     * @Return ContractService
     * @Date 2018/7/15 14:50
     */
    public ContractService getContractService() {
        return new ContractServiceImpl();
    }

    /**
     * @Author riven
     * @Method getSdk
     * @Return SDK
     * @Date 2018/7/15 14:51
     */
    public static SDK getSdk() {
        return sdk;
    }

    /**
     * @Author riven
     * @Method getUrl
     * @Params []
     * @Return java.lang.String
     * @Date 2018/11/27 11:12
     */
    public String getUrl() {
        return url;
    }

    /**
     * @Author riven
     * @Method getChainId
     * @Params []
     * @Return long
     * @Date 2018/11/27 11:12
     */
    public long getChainId() {
        return chainId;
    }

    /**
     * @Author riven
     * @Method init
     * @Params [url]
     * @Return void
     * @Date 2018/7/15 14:50
     */
    private void init(SDKConfigure sdkConfigure) throws SDKException {
        if (Tools.isEmpty(sdkConfigure.getUrl())) {
            throw new SDKException(SdkError.URL_EMPTY_ERROR);
        }
        sdk.url = sdkConfigure.getUrl();
        int httpConnectTimeOut = sdkConfigure.getHttpConnectTimeOut();
        if (httpConnectTimeOut > 0) {
            HttpKit.connectTimeOut = httpConnectTimeOut;
        }
        int readTimeOut = sdkConfigure.getHttpReadTimeOut();
        if (readTimeOut > 0) {
            HttpKit.readTimeOut = readTimeOut;
        }
        long chainId = sdkConfigure.getChainId();
        if (chainId > 0) {
            sdk.chainId = chainId;
        }
    }

    /**
     * @Author riven
     * @Method init
     * @Params [url]
     * @Return void
     * @Date 2018/7/15 14:50
     */
    private void init(String url) throws SDKException {
        if (Tools.isEmpty(url)) {
            throw new SDKException(SdkError.URL_EMPTY_ERROR);
        }
        sdk.url = url;
    }
}
