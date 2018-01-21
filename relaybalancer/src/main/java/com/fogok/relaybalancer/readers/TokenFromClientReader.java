package com.fogok.relaybalancer.readers;

import com.fogok.dataobjects.datastates.RequestTypeInTokenToServiceTrnsn;
import com.fogok.dataobjects.transactions.BaseReaderFromTransaction;
import com.fogok.dataobjects.transactions.common.TokenToServiceTransaction;
import com.fogok.dataobjects.transactions.utils.TransactionExecutor;
import com.fogok.relaybalancer.connectors.RelayToAuthHandler;
import com.fogok.spaceshipserver.transactions.CheckValidTokenToAuthTransaction;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

public class TokenFromClientReader implements BaseReaderFromTransaction<TokenToServiceTransaction> {

    private RelayToAuthHandler relayToAuthHandler;

    public void setRelayToAuthHandler(RelayToAuthHandler relayToAuthHandler) {
        this.relayToAuthHandler = relayToAuthHandler;
    }

    @Override
    public ChannelFuture read(Channel channel, TokenToServiceTransaction transaction, TransactionExecutor transactionExecutor) {
        relayToAuthHandler.checkValidTokenFromClient(channel, transaction,
                transaction.getRequestTypeInTokenToServiceTrnsn() == RequestTypeInTokenToServiceTrnsn.CHECK_VALID ?
                        CheckValidTokenToAuthTransaction.SENDER_SERVICE :
                        CheckValidTokenToAuthTransaction.SENDER_CLIENT);
        return null;
    }

    @Override
    public boolean isNeedActionAfterRead() {
        return false;
    }

    @Override
    public void actionAfterRead(ChannelFuture channelFuture) {

    }
}
