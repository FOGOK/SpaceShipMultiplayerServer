package com.fogok.pvpserver;

import com.fogok.dataobjects.utils.libgdxexternals.Pool;
import com.fogok.pvpserver.config.PvpConfig;
import com.fogok.pvpserver.logic.GameRoomManager;
import com.fogok.pvpserver.logic.GameRoomManager.LogicHandler.IOAction;
import com.fogok.spaceshipserver.BaseUdpChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;

import static com.esotericsoftware.minlog.Log.info;

public class PvpHandler extends BaseUdpChannelInboundHandlerAdapter<PvpConfig, DatagramPacket> {

    private DatagramChannel cleanedChannel;

    @Override
    public void init() {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        cleanedChannel = (DatagramChannel) ctx.channel();
        GameRoomManager.instance.initLogicHandler(cleanedChannel, ioActionPool, executorToThreadPool);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket recievedDatagramPacket) throws Exception {
        //read
        byte[] response = new byte[recievedDatagramPacket.content().readableBytes()];
        recievedDatagramPacket.content().readBytes(response);
        GameRoomManager.instance.getLogicHandler().addIoAction(ioActionPool.obtainSync(response, recievedDatagramPacket.sender()));
    }


    private IOActionPool ioActionPool = new IOActionPool();

    /**
     * Thread safe io impl
     */
    public static class IOActionPool extends Pool<IOAction>{
        @Override
        protected IOAction newObject() {
            return new IOAction();
        }


        public synchronized IOAction obtainSync(byte[] response, InetSocketAddress inetSocketAddress) {
            IOAction ioAction = super.obtain();
            ioAction.inetSocketAddress = inetSocketAddress;
            ioAction.receivedBytes = response;
            return ioAction;
        }

        public synchronized void freeSync(IOAction object) {
            super.free(object);
        }

        public void poolStatus(){
            info(String.format("Free objects: %s", getFree()));
        }
    }

}
