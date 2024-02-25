package net.ccbluex.liquidbounce.features.module.modules.tomk;

import net.ccbluex.liquidbounce.api.minecraft.network.IPacket;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.MoveEvent;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;

@ModuleInfo(name = "NoC03", description = "No C03", category = ModuleCategory.TOMK)
public class NoC03 extends Module {
    @EventTarget
    public void onPacket(PacketEvent event){
        IPacket packet = event.getPacket();
        if(classProvider.isCPacketPlayer(packet)){
            event.cancelEvent();
        }
    }
    @EventTarget
    public void onMove(MoveEvent event){
        event.zero();
    }
}
