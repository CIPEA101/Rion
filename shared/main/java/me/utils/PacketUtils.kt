package me.utils;

import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer

object PacketUtils : MinecraftInstance() {
    private val packets = ArrayList<Packet<INetHandlerPlayServer>>()

    @JvmField
    var isPacketSend = false

    @JvmStatic
    fun handleSendPacket(packet: Packet<*>): Boolean {
        if (packets.contains(packet)) {
            packets.remove(packet)
            return true
        }
        return false
    }

    @JvmStatic
    fun sendPacketNoEvent(packet: Packet<INetHandlerPlayServer>) {
        if (mc2.connection == null) return

        packets.add(packet)
        isPacketSend = true
        mc2.connection!!.sendPacket(packet)
        isPacketSend = true
//        mc.netHandler.addToSendQueue(packet as IPacket)
    }

    @JvmStatic
    fun getPacketType(packet: Packet<*>): PacketType {
        val className=packet.javaClass.simpleName
        if(className.startsWith("C",ignoreCase = true)){
            return PacketType.CLIENTSIDE
        }else if(className.startsWith("S",ignoreCase = true)){
            return PacketType.SERVERSIDE
        }
        return PacketType.UNKNOWN
    }

    enum class PacketType {
        SERVERSIDE,
        CLIENTSIDE,
        UNKNOWN
    }
}