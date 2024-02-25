package net.ccbluex.liquidbounce.features.module.modules.tomk


import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntity
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.TextValue
import rion.autoL
import java.util.*

@ModuleInfo(name = "AutoL", description = "AutoL byChip", category = ModuleCategory.TOMK)
class AutoL : Module() {
    val modeValue = ListValue("Mode", arrayOf("Chinese", "English","Rion","Chip", "L","None","Text"), "Chip")
    val lobbyValue = TextValue("Text", "Rion New @2023")
    private val prefix = BoolValue("LiquidbounceName",true)
    private val delay = IntegerValue("Delay",100,0,2000)
    var index = 0
    var R = Random()
    var abuse = arrayOf("Rion New @2023")
    var englishabuse = arrayOf("Rion @2023 Kill You!")
    private var target: IEntity? = null
    var kills = 0
    val msTimer = MSTimer()
    @EventTarget
    fun onAttack(event: AttackEvent) {
        target = event.targetEntity
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (target != null) {
            if (target!!.isDead) {
                if (msTimer.hasTimePassed(delay.get().toLong())) {
                    index ++
                    when (modeValue.get()) {
                        "Chinese" -> {
                            mc.thePlayer!!.sendChatMessage((if (prefix.get()) "[Rion] " else "")+":"+ abuse[R.nextInt(abuse.size)]
                            )
                            kills += 1
                            target = null
                        }
                        "Rion" -> {
                            if (index > autoL.Rion.size) index = 0
                            mc.thePlayer!!.sendChatMessage((if (prefix.get()) "[Rion] " else "") + " " + autoL.Rion[index])
                            kills += 1
                            target = null
                        }
                        "Chip" -> {
                            if (index > autoL.Chip.size) index = 0
                            mc.thePlayer!!.sendChatMessage((if (prefix.get()) "[Rion] " else "") + " " + autoL.Chip[index])
                            kills += 1
                            target = null
                        }
                        "English" -> {
                            kills += 1
                            mc.thePlayer!!.sendChatMessage((if (prefix.get()) "[Rion] " else "") + "  " + englishabuse[R.nextInt(
                                    englishabuse.size
                            )]
                            )
                            target = null
                        }
                        "L" -> {
                            mc.thePlayer!!.sendChatMessage((if (prefix.get()) "[Rion] " else "")+" L " + target!!.name)
                            kills += 1
                            target = null
                        }
                        "None" -> {
                            kills += 1
                            target = null
                        }
                        "Text" -> {
                            mc.thePlayer!!.sendChatMessage(lobbyValue.get()+" [" + target!!.name+"]")
                            kills += 1
                            target = null
                        }
                    }
                    msTimer.reset()
                }
            }
        }
    }
    override val tag: String
        get() = "Kills%$kills"
}