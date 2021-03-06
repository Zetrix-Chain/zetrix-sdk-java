package org.zetrix.model.request.operation;

import org.zetrix.common.OperationType;

/**
 * @Author riven
 * @Date 2018/7/9 17:20
 */
public class ContractInvokeByGasOperation extends BaseOperation {
    private String contractAddress;
    private Long ztxAmount;
    private String input;

    public ContractInvokeByGasOperation() {
        operationType = OperationType.CONTRACT_INVOKE_BY_GAS;
    }

    /**
     * @Author riven
     * @Method getOperationType
     * @Params []
     * @Return OperationType
     * @Date 2018/7/9 17:21
     */
    @Override
    public OperationType getOperationType() {
        return operationType;
    }

    /**
     * @Author riven
     * @Method getContractAddress
     * @Params []
     * @Return java.lang.String
     * @Date 2018/7/9 17:21
     */
    public String getContractAddress() {
        return contractAddress;
    }

    /**
     * @Author riven
     * @Method setContractAddress
     * @Params [contractAddress]
     * @Return void
     * @Date 2018/7/9 17:21
     */
    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    /**
     * @Author riven
     * @Method getTokenAmount
     * @Params []
     * @Return java.lang.Long
     * @Date 2018/7/9 17:21
     */
    public Long getZtxAmount() {
        return ztxAmount;
    }

    /**
     * @Author riven
     * @Method setTokenAmount
     * @Params [ztxAmount]
     * @Return void
     * @Date 2018/7/9 17:21
     */
    public void setZtxAmount(Long ztxAmount) {
        this.ztxAmount = ztxAmount;
    }

    /**
     * @Author riven
     * @Method getInput
     * @Params []
     * @Return java.lang.String
     * @Date 2018/7/9 17:21
     */
    public String getInput() {
        return input;
    }

    /**
     * @Author riven
     * @Method setInput
     * @Params [input]
     * @Return void
     * @Date 2018/7/9 17:29
     */
    public void setInput(String input) {
        this.input = input;
    }
}
