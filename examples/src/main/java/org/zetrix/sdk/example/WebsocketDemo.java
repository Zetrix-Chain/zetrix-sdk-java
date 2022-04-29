package org.zetrix.sdk.example;

import org.zetrix.common.Tools;
import org.zetrix.crypto.protobuf.Chain;
import org.zetrix.crypto.protobuf.Overlay;
import org.zetrix.crypto.websocket.BlockChainAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsocketDemo {
    private Logger logger_;
    BlockChainAdapter chain_message_one_;

    public static void main(String[] argv) {
        WebsocketDemo chain_test = new WebsocketDemo();
        chain_test.Initialize(argv);
        System.out.println("*****************start chain_message successfully******************");
        try {
            Thread.sleep(100000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void Initialize(String[] argv) {
        String nodeIP_ = "192.168.10.100";
        String websocketPort_ = "7153";
        logger_ = LoggerFactory.getLogger(BlockChainAdapter.class);
        chain_message_one_ = new BlockChainAdapter("ws://" + nodeIP_ + ":" + websocketPort_);
        chain_message_one_.AddChainResponseMethod(Overlay.ChainMessageType.CHAIN_HELLO_VALUE, this::OnChainHello);
        chain_message_one_.AddChainMethod(Overlay.ChainMessageType.CHAIN_TX_STATUS_VALUE, this::OnChainTxStatus);
        chain_message_one_.AddChainMethod(Overlay.ChainMessageType.CHAIN_TX_ENV_STORE_VALUE, this::OnChainTxEnv);

        Overlay.ChainHello.Builder chain_hello = Overlay.ChainHello.newBuilder();
        chain_hello.setTimestamp(System.currentTimeMillis());
        if (!chain_message_one_.Send(Overlay.ChainMessageType.CHAIN_HELLO.getNumber(), chain_hello.build().toByteArray())) {
            logger_.error("send hello failed");
        }
    }
    private void OnChainHello(byte[] msg, int length) {
        try {
            Overlay.ChainStatus chain_status = Overlay.ChainStatus.parseFrom(msg);
            if (!Tools.isNULL(chain_status)) {
                System.out.println("hello响应：" + chain_status);
                //logger_.info("hello响应：" + JSON.toJSONString(chain_status));
            }

            // 消息订阅
            Overlay.ChainSubscribeTx.Builder chainSubscribeTx = Overlay.ChainSubscribeTx.newBuilder();
            chainSubscribeTx.addAddress("ZTX3Ta7d4GyAXD41H2kFCTd2eXhDesM83rvC3");
            if(!chain_message_one_.Send(Overlay.ChainMessageType.CHAIN_SUBSCRIBE_TX_VALUE, chainSubscribeTx.build().toByteArray())) {
                logger_.error("send subscribe_tx failed");
            }
        } catch (Exception e) {
            logger_.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private void OnChainTxStatus(byte[] msg, int length) {
        try {
            Overlay.ChainTxStatus chain_tx_status = Overlay.ChainTxStatus.parseFrom(msg);
            if (!Tools.isNULL(chain_tx_status)) {
                System.out.println("ChainTxStatus响应消息： " + chain_tx_status);
                //logger_.info("ChainTxStatus响应消息： " + JSON.toJSONString(chain_tx_status));
            }

        } catch (Exception e) {
            logger_.error(e.getMessage(), e);
        }
    }

    private void OnChainTxEnv(byte[] msg, int length) {
        try {
            Chain.TransactionEnvStore transactionEnvStore = Chain.TransactionEnvStore.parseFrom(msg);
            if (!Tools.isNULL(transactionEnvStore)) {
                System.out.println("ChainTxEnv响应消息： " + transactionEnvStore);
                //logger_.info("ChainTxEnv响应消息： " + JSON.toJSONString(transactionEnvStore));
            }
        } catch (Exception e) {
            logger_.error(e.getMessage(), e);
        }
    }
}
