package net.ccbluex.liquidbounce.features.module.modules.tomk



import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.value.BoolValue


@ModuleInfo(name = "CancelC03", category = ModuleCategory.TOMK, description = "Rion Teams")
open class CancelC03 : Module() {
    private val onlyvelocity = BoolValue("onlyvelocity", true)
    var info = false
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if(classProvider.isCPacketPlayer(packet)){
            if (onlyvelocity.get()){
                if (info){
                    event.cancelEvent()
                    ClientUtils.displayChatMessage("取消C03")
                    info = false
                }

            }else{
                event.cancelEvent()
                ClientUtils.displayChatMessage("取消C03")
                info = false
            }



        }

    }
}
