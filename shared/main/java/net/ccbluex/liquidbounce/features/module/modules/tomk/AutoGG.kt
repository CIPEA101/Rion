package net.ccbluex.liquidbounce.features.module.modules.tomk

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.network.play.server.SPacketChat
import tomk.Recorder.totalPlayed
import tomk.Recorder.win

@ModuleInfo(name = "AutoGG", category = ModuleCategory.TOMK, description = "Rion")
class AutoGG : Module() {
    var fadeState = FadeState.NO
    private val textValue = TextValue("Text", "GG")
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet.unwrap()

        if (packet is SPacketChat) {
            val text = packet.chatComponent.unformattedText

            if (text.contains("恭喜", true)) {
                mc.thePlayer!!.sendChatMessage(textValue.get())
                win++
                fadeState = FadeState.FRIST


            }
            if (text.contains("游戏开始", true)) {
                totalPlayed++
            }
        }
    }
    override val tag: String
        get() = "HuaYuTing"
}
enum class FadeState { FRIST,IN, STAY, OUT, END,NO }
