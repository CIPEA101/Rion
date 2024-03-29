package net.ccbluex.liquidbounce.features.module.modules.tomk

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.TextValue

@ModuleInfo(name = "Title", description = "Title", category = ModuleCategory.MISC)
class Title : Module(){
    private val fakeNameValue = TextValue("SetTitle", "Rion")
    private val CopywritingValue = TextValue("Copywriting", "By RionTeams & NeverTeams")
    private var S = 0
    private var HM = 0
    private var M = 0
    private var H = 0

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        HM += 1
        if (HM ==20){
            S = S + 1
            HM = 0
        }
        if (S ==60){
            M = M +1
            S = 0
        }
        if (M==60){
            H = H+1
            M = 0
        }
        org.lwjgl.opengl.Display.setTitle(fakeNameValue.get()+ " | "+CopywritingValue.get() +" | 您已使用: "  +H  +"  时  "  +M +"  分  "+S+"  秒  ")
    }
}