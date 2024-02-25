
package net.ccbluex.liquidbounce.features.module.modules.movement

import me.Debug.thePlayerisBlocking
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.api.enums.EnumFacingType
import net.ccbluex.liquidbounce.api.enums.WEnumHand
import net.ccbluex.liquidbounce.api.minecraft.item.IItem
import net.ccbluex.liquidbounce.api.minecraft.item.IItemStack
import net.ccbluex.liquidbounce.api.minecraft.network.play.client.*
//import net.ccbluex.liquidbounce.utils.BlockAnimationUtils.thePlayerisBlocking
import net.ccbluex.liquidbounce.api.minecraft.util.IEnumFacing
import net.ccbluex.liquidbounce.api.minecraft.util.WBlockPos
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.render.OldHitting
import net.ccbluex.liquidbounce.utils.BlockAnimationUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.createUseItemPacket
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.item.ItemSword
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.*
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import tomk.PacketUtils
import java.util.*

@ModuleInfo(name = "NoSlow", category = ModuleCategory.MOVEMENT, description = "HYT")
class NoSlow : Module() {
    fun getValue(): BoolValue? {
        return packet
    }
    private val packet = BoolValue("Packet", false)
    private val msTimer = MSTimer()
    private val modeValue = ListValue("PacketMode", arrayOf("Hyt","GrimAC","Grim","AntiCheat", "Custom", "NCP", "Plt", "AAC", "AAC5","4v4","NewPlt","HytNewFix"), "Plt")
    private val blockForwardMultiplier = FloatValue("BlockForwardMultiplier", 1.0F, 0.2F, 1.0F)
    private val blockStrafeMultiplier = FloatValue("BlockStrafeMultiplier", 1.0F, 0.2F, 1.0F)
    private val consumeForwardMultiplier = FloatValue("ConsumeForwardMultiplier", 1.0F, 0.2F, 1.0F)
    private val consumeStrafeMultiplier = FloatValue("ConsumeStrafeMultiplier", 1.0F, 0.2F, 1.0F)
    private val bowForwardMultiplier = FloatValue("BowForwardMultiplier", 1.0F, 0.2F, 1.0F)
    private val bowStrafeMultiplier = FloatValue("BowStrafeMultiplier", 1.0F, 0.2F, 1.0F)
    private val customOnGround = BoolValue("CustomOnGround", false)
    private val customDelayValue = IntegerValue("CustomDelay", 60, 10, 200)

    // Soulsand
    val soulsandValue = BoolValue("Soulsand", false)

    var pendingFlagApplyPacket = false
    val timer = MSTimer()
    private val Timer = MSTimer()
    private var sendBuf = false
    private var packetBuf = LinkedList<Packet<INetHandlerPlayServer>>()
    private var nextTemp = false
    private var waitC03 = false
    private var lastBlockingStat = false

    fun fuckKotline(value: Int): Boolean{
        return value == 1
    }

    private fun OnPre(event : MotionEvent): Boolean {
        return event.eventState == EventState.PRE
    }

    private fun OnPost(event : MotionEvent): Boolean {
        return event.eventState == EventState.POST
    }

    private val isBlocking: Boolean
        get() = (mc.thePlayer!!.isUsingItem || (LiquidBounce.moduleManager[KillAura::class.java] as KillAura).blockingStatus) && mc.thePlayer!!.heldItem != null && mc.thePlayer!!.heldItem!!.item is ItemSword

    override fun onDisable() {
        Timer.reset()
        msTimer.reset()
        pendingFlagApplyPacket = false
        sendBuf = false
        packetBuf.clear()
        nextTemp = false
        waitC03 = false
    }

    private fun sendPacket(
        event: MotionEvent,
        sendC07: Boolean,
        sendC08: Boolean,
        delay: Boolean,
        delayValue: Long,
        onGround: Boolean
    ) {
        val digging = classProvider.createCPacketPlayerDigging(ICPacketPlayerDigging.WAction.RELEASE_USE_ITEM, WBlockPos(-1, -1, -1), classProvider.getEnumFacing(EnumFacingType.DOWN))
        val blockMain = createUseItemPacket(mc.thePlayer!!.inventory.getCurrentItemInHand(), WEnumHand.MAIN_HAND)
        val blockOFF = createUseItemPacket(mc.thePlayer!!.inventory.getCurrentItemInHand(), WEnumHand.OFF_HAND)
        if (onGround && !mc.thePlayer!!.onGround) {
            return
        }
        if (sendC07 && event.eventState == EventState.PRE) {
            if (delay && msTimer.hasTimePassed(delayValue)) {
                mc.netHandler.addToSendQueue(digging)
            } else if (!delay) {
                mc.netHandler.addToSendQueue(digging)
            }
        }
        if (sendC08 && event.eventState == EventState.POST) {
            if (delay && msTimer.hasTimePassed(delayValue)) {
                if (LiquidBounce.moduleManager[OldHitting::class.java].state) {
                    mc.netHandler.addToSendQueue(blockOFF)
                } else {
                    mc.netHandler.addToSendQueue(blockMain)
                    mc.netHandler.addToSendQueue(blockOFF)
                }
                msTimer.reset()
            } else if (!delay) {
                if (LiquidBounce.moduleManager[OldHitting::class.java].state) {
                    mc.netHandler.addToSendQueue(blockOFF)
                } else {
                    mc.netHandler.addToSendQueue(blockMain)
                    mc.netHandler.addToSendQueue(blockOFF)
                }
            }
        }
    }

    val killAura = LiquidBounce.moduleManager[KillAura::class.java] as KillAura

    fun isBlock(): Boolean {
        return thePlayerisBlocking || killAura.blockingStatus
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (!MovementUtils.isMoving) {
            return
        }
        when (modeValue.get().toLowerCase()) {
            "anticheat" -> {
                this.sendPacket(event, true, false, false, 0, false)
                if (mc.thePlayer!!.ticksExisted % 2 == 0) {
                    this.sendPacket(event, false, true, false, 0, false)
                }
            }
            "hyt" -> {
                if (event.eventState == EventState.PRE) {
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerDigging(ICPacketPlayerDigging.WAction.RELEASE_USE_ITEM,
                        WBlockPos.ORIGIN, classProvider.getEnumFacing(EnumFacingType.DOWN)))
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerBlockPlacement(mc.thePlayer!!.inventory.getCurrentItemInHand() as IItemStack))
                }
            }

            "hytnewfix" -> {
                if (event.eventState == EventState.PRE && mc.thePlayer!!.itemInUse != null && mc.thePlayer!!.itemInUse!!.item != null) {
                    if (!classProvider.isItemSword(mc.thePlayer!!.inventory.getCurrentItemInHand()))
                        if (mc.thePlayer!!.isUsingItem && mc.thePlayer!!.itemInUseCount >= 1) {
                            mc2.connection!!.sendPacket(CPacketHeldItemChange((mc2.player.inventory.currentItem + 1) % 9))
                            mc2.connection!!.sendPacket(CPacketHeldItemChange(mc2.player.inventory.currentItem))
                        }
                }
                if (event.eventState == EventState.PRE && mc.thePlayer!!.itemInUse != null && mc.thePlayer!!.itemInUse!!.item != null){
                    if (classProvider.isItemSword(mc.thePlayer!!.inventory.getCurrentItemInHand()))
                        if (mc.thePlayer!!.isUsingItem || mc.thePlayer!!.isBlocking || isBlock()) {
                            mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerBlockPlacement(mc.thePlayer!!.inventory.getCurrentItemInHand() as IItemStack))
                            mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerDigging(ICPacketPlayerDigging.WAction.RELEASE_USE_ITEM, WBlockPos.ORIGIN, classProvider.getEnumFacing(EnumFacingType.DOWN)))
                        }
                }
            }
            "Newplt" -> {
                if (event.eventState == EventState.PRE){
                    mc2.connection!!.sendPacket(CPacketPlayerTryUseItemOnBlock(BlockPos(-1, -1, -1),
                        EnumFacing.UP, EnumHand.MAIN_HAND,0f,0f,0f))
                }
            }

            "4v4" -> {
                if (event.eventState == EventState.PRE && mc.thePlayer!!.itemInUse != null && mc.thePlayer!!.itemInUse!!.item != null) {
                    if (!classProvider.isItemSword(mc.thePlayer!!.inventory.getCurrentItemInHand()))
                        if (mc.thePlayer!!.isUsingItem && mc.thePlayer!!.itemInUseCount >= 1) {
                            mc2.connection!!.sendPacket(CPacketHeldItemChange((mc2.player.inventory.currentItem + 1) % 9))
                            mc2.connection!!.sendPacket(CPacketHeldItemChange(mc2.player.inventory.currentItem))
                        }
                }
                if (event.eventState == EventState.PRE && mc.thePlayer!!.itemInUse != null && mc.thePlayer!!.itemInUse!!.item != null){
                    if (classProvider.isItemSword(mc.thePlayer!!.inventory.getCurrentItemInHand()))
                        if (mc.thePlayer!!.isUsingItem || mc.thePlayer!!.isBlocking || isBlock()) {
                            mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerBlockPlacement(mc.thePlayer!!.inventory.getCurrentItemInHand() as IItemStack))
                        }else if(mc.thePlayer!!.isUsingItem || mc.thePlayer!!.isBlocking || isBlock()){
                            mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerBlockPlacement(mc.thePlayer!!.inventory.getCurrentItemInHand() as IItemStack))
                            mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerDigging(ICPacketPlayerDigging.WAction.RELEASE_USE_ITEM, WBlockPos.ORIGIN, classProvider.getEnumFacing(EnumFacingType.DOWN)))
                        }
                }
            }

            "aac" -> {
                if (mc.thePlayer!!.ticksExisted % 3 == 0) {
                    sendPacket(event, true, false, false, 0, false)
                } else {
                    sendPacket(event, false, true, false, 0, false)
                }
            }

            "aac5" -> {
                if (mc.thePlayer!!.isUsingItem || mc.thePlayer!!.isBlocking || isBlock()) {
                    mc.netHandler.addToSendQueue(createUseItemPacket(mc.thePlayer!!.inventory.getCurrentItemInHand(), WEnumHand.MAIN_HAND))
                    mc.netHandler.addToSendQueue(createUseItemPacket(mc.thePlayer!!.inventory.getCurrentItemInHand(), WEnumHand.OFF_HAND))
                }
            }

            "custom" -> {
                sendPacket(event, true, true, true, customDelayValue.get().toLong(), customOnGround.get())
            }

            "ncp" -> {
                sendPacket(event, true, true, false, 0, false)
            }
            "grim"-> {
                if ((event.eventState == EventState.PRE && mc.thePlayer!!.itemInUse != null && mc.thePlayer!!.itemInUse!!.item != null) && !mc.thePlayer!!.isBlocking && classProvider.isItemFood(mc.thePlayer!!.heldItem!!.item) || classProvider.isItemPotion(mc.thePlayer!!.heldItem!!.item)) {
                    if (mc.thePlayer!!.isUsingItem && mc.thePlayer!!.itemInUseCount >= 1) {
                        mc2.connection!!.sendPacket(CPacketHeldItemChange((mc2.player.inventory.currentItem + 1) % 9))
                        mc2.connection!!.sendPacket(CPacketHeldItemChange(mc2.player.inventory.currentItem))
                    }
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if(modeValue.equals("Matrix") || modeValue.equals("Vulcan") || modeValue.equals("Grim") && nextTemp) {
            if((packet is CPacketPlayerDigging || classProvider.isCPacketPlayerBlockPlacement(packet)) && isBlocking) {
                event.cancelEvent()
            }
            event.cancelEvent()
        }else if (packet is CPacketPlayer || packet is CPacketAnimation || packet is CPacketEntityAction || packet is CPacketUseEntity || packet is CPacketPlayerDigging || packet is ICPacketPlayerBlockPlacement) {
            if (modeValue.equals("Vulcan") && waitC03 && packet is ICPacketPlayer) {
                waitC03 = false
                return
            }
            packetBuf.add(packet as Packet<INetHandlerPlayServer>)
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if((modeValue.equals("Matrix") || modeValue.equals("Vulcan") || modeValue.equals("GrimAC")) && (lastBlockingStat || isBlocking)) {
            if(msTimer.hasTimePassed(230) && nextTemp) {
                nextTemp = false
                if(modeValue.equals("GrimAC")) {
                    PacketUtils.sendPacketNoEvent(CPacketHeldItemChange((mc2.player.inventory.currentItem + 1) % 9))
                    PacketUtils.sendPacketNoEvent(CPacketHeldItemChange(mc2.player.inventory.currentItem))
                } else {
                    PacketUtils.sendPacketNoEvent(CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos(-1, -1, -1), EnumFacing.DOWN))
                }
                classProvider.createCPacketPlayerDigging(ICPacketPlayerDigging.WAction.RELEASE_USE_ITEM, WBlockPos(-1, -1, -1), EnumFacing.DOWN as IEnumFacing)
                if(packetBuf.isNotEmpty()) {
                    var canAttack = false
                    for(packet in packetBuf) {
                        if(packet is CPacketPlayer) {
                            canAttack = true
                        }
                        if(!((packet is ICPacketUseEntity || packet is ICPacketAnimation) && !canAttack)) {
                            PacketUtils.sendPacketNoEvent(packet)
                        }
                    }
                    packetBuf.clear()
                }
            }
            if(!nextTemp) {
                lastBlockingStat = isBlocking
                if (!isBlocking) {
                    return
                }
                classProvider.createCPacketPlayerBlockPlacement(WBlockPos(-1, -1, -1), 255, mc.thePlayer!!.inventory.currentItem as IItemStack, 0f, 0f, 0f)
                nextTemp = true
                waitC03 = modeValue.equals("Vulcan")
                msTimer.reset()
            }
        }
    }

    @EventTarget
    fun onSlowDown(event: SlowDownEvent) {
        val heldItem = mc.thePlayer!!.heldItem?.item

        event.forward = getMultiplier(heldItem, true)
        event.strafe = getMultiplier(heldItem, false)
    }

    private fun getMultiplier(item: IItem?, isForward: Boolean): Float {
        return when {
            classProvider.isItemFood(item) || classProvider.isItemPotion(item) || classProvider.isItemBucketMilk(item) -> {
                if (isForward) this.consumeForwardMultiplier.get() else this.consumeStrafeMultiplier.get()
            }
            classProvider.isItemSword(item) -> {
                if (isForward) this.blockForwardMultiplier.get() else this.blockStrafeMultiplier.get()
            }
            classProvider.isItemBow(item) -> {
                if (isForward) this.bowForwardMultiplier.get() else this.bowStrafeMultiplier.get()
            }
            else -> 0.2F
        }
    }
    override val tag: String?
        get() = modeValue.get()
}