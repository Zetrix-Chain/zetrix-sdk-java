package org.zetrix.contract;

import org.zetrix.model.request.ContractCallRequest;
import org.zetrix.model.request.ContractCheckValidRequest;
import org.zetrix.model.request.ContractGetAddressRequest;
import org.zetrix.model.request.ContractGetInfoRequest;
import org.zetrix.model.response.ContractCallResponse;
import org.zetrix.model.response.ContractCheckValidResponse;
import org.zetrix.model.response.ContractGetAddressResponse;
import org.zetrix.model.response.ContractGetInfoResponse;

/**
 * @Author riven
 * @Date 2018/7/5 13:20
 */
public interface ContractService {
    /**
     * @Author riven
     * @Method getInfo
     * @Params [contractGetRequest]
     * @Return ContractGetInfoResponse
     * @Date 2018/7/5 14:16
     */
    public ContractGetInfoResponse getInfo(ContractGetInfoRequest contractGetRequest);

    /**
     * @Author riven
     * @Method checkValid
     * @Params [contractCheckValidRequest]
     * @Return ContractCheckValidResponse
     * @Date 2018/7/5 15:37
     */
    public ContractCheckValidResponse checkValid(ContractCheckValidRequest contractCheckValidRequest);

    /**
     * @Author riven
     * @Method call
     * @Params [contractCallRequest]
     * @Return ContractCallResponse
     * @Date 2018/7/11 18:50
     */
    public ContractCallResponse call(ContractCallRequest contractCallRequest);

    /**
     * @Author riven
     * @Method getAddress
     * @Params [contractGetAddressRequest]
     * @Return ContractGetAddressResponse
     * @Date 2018/8/1 15:01
     */
    public ContractGetAddressResponse getAddress(ContractGetAddressRequest contractGetAddressRequest);
}
