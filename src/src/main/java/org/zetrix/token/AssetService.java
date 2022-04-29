package org.zetrix.token;

import org.zetrix.model.request.AssetGetInfoRequest;
import org.zetrix.model.response.AssetGetInfoResponse;

/**
 * @Author riven
 * @Date 2018/7/3 17:21
 */
public interface AssetService {
    /**
     * @Author riven
     * @Method getInfo
     * @Params [assetGetRequest]
     * @Return AssetGetInfoResponse
     * @Date 2018/7/5 12:05
     */
    public AssetGetInfoResponse getInfo(AssetGetInfoRequest assetGetRequest);
}
