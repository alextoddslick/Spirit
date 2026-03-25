package me.codexadrian.spirit.network.packet;

import net.minecraft.resources.Identifier;

public interface IPacket<T> {
    Identifier getID();
    IPacketHandler<T> getHandler();
}
