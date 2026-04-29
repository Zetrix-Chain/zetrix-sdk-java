package org.zetrix.model.response;

import com.alibaba.fastjson.annotation.JSONField;
import org.zetrix.exception.SdkError;
import org.zetrix.model.response.result.HelloResult;

public class HelloResponse extends BaseResponse {
    @JSONField(name = "result")
    private HelloResult result;

    public HelloResult getResult() {
        return result;
    }

    public void setResult(HelloResult result) {
        this.result = result;
    }

    /**
     * @Author riven
     * @Method buildResponse
     * @Params [sdkError, result]
     * @Return void
     * @Date 2018/7/4 15:08
     */
    public void buildResponse(SdkError sdkError, HelloResult result) {
        this.errorCode = sdkError.getCode();
        this.errorDesc = sdkError.getDescription();
        this.result = result;
    }

    /**
     * @Author riven
     * @Method buildResponse
     * @Params [errorCode, errorDesc, result]
     * @Return void
     * @Date 2018/7/4 15:10
     */
    public void buildResponse(int errorCode, String errorDesc, HelloResult result) {
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
        this.result = result;
    }
}
