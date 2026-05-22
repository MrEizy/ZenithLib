package net.zic.zenithlib.network;

import io.netty.buffer.ByteBuf;

@FunctionalInterface
public interface Encoder<T>{
    void encode(T val, ByteBuf buf);
}
