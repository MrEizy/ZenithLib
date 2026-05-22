package net.zic.zenithlib.network;

import io.netty.buffer.ByteBuf;

@FunctionalInterface
public interface Decoder<T>{
    T decode(ByteBuf buf);
}
