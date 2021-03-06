package com.fogok.relaybalancer.connectors;

import com.fogok.dataobjects.datastates.ConnectionToServiceType;
import com.fogok.dataobjects.transactions.common.BaseTransaction;
import com.fogok.dataobjects.transactions.common.ConnectionInformationTransaction;
import com.fogok.dataobjects.transactions.common.TokenizedTransaction;
import com.fogok.dataobjects.transactions.relaybalancerservice.SSInformationTransaction;
import com.fogok.relaybalancer.config.RelayConfig;
import com.fogok.relaybalancer.readers.ValidResponseFromAuthReader;
import com.fogok.spaceshipserver.transactions.CheckValidTokenFromAuthTransaction;
import com.fogok.spaceshipserver.transactions.CheckValidTokenToAuthTransaction;
import com.fogok.spaceshipserver.transactions.ServiceToServiceDataState;
import com.fogok.spaceshipserver.utlis.BaseTcpHandlerInSvcToSvc;

import io.netty.channel.Channel;

import static com.fogok.dataobjects.transactions.common.ConnectionInformationTransaction.RESPONSE_CODE_ERROR;

public class RelayToAuthHandlerTcp extends BaseTcpHandlerInSvcToSvc<RelayConfig> {

    @Override
    public void init() {
        transactionReader.getTransactionsAndReadersResolver()
                .addToResolve(
                        new ValidResponseFromAuthReader(this),
                        new BaseTransaction(ConnectionToServiceType.SERVICE_TO_SERVICE, ServiceToServiceDataState.CHECK_VALID_TOKEN_FROM_AUTH.ordinal()));
    }

    public void checkValidTokenFromClient(Channel clientChannel, TokenizedTransaction transaction, int validationSender){
        putRequest(clientChannel, transaction, validationSender);
    }

    public void receiveAuthResponseToClient(Channel clientOrServiceChannel, boolean isValid, int validationSender, String token) {
        if (validationSender == CheckValidTokenToAuthTransaction.SENDER_CLIENT)
            transactionReader.getTransactionExecutor().execute(clientOrServiceChannel,
                    isValid ? new SSInformationTransaction(config.getSocialServerServicesIps().get(0))
                            : new ConnectionInformationTransaction(RESPONSE_CODE_ERROR));
        else
            transactionReader.getTransactionExecutor().execute(clientOrServiceChannel,
                    new CheckValidTokenFromAuthTransaction(token, isValid,
                            CheckValidTokenToAuthTransaction.SENDER_SERVICE));
    }
}
