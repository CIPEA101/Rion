
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import org.json.XMLTokener
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntity
import net.ccbluex.liquidbounce.api.enums.StatType
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.api.minecraft.network.IPacket
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.CPacketPlayer

@ModuleInfo(name = "Criticals", description = "Automatically deals critical hits.", category = ModuleCategory.COMBAT)
class Criticals : Module() {

    val modeValue = ListValue("Mode", arrayOf("GrimAC","LiquidGlow","Packet", "Packet2", "Packet3", "Packet4","Pit","Hyt","HytSpartan","HytTest","SuperPacket", "NoGround", "NoGround2", "Hop", "TPHop", "FakeJump", "FakeJump2", "Jump", "LowJump", "LowJump2", "Visual"), "Packet")
    val pitjump = ListValue("JumpMode",arrayOf("GrimAC","LiquidGlow","Packet", "Packet2", "Packet3", "Packet4","Pit","Hyt","HytSpartan","HytTest","SuperPacket", "NoGround", "NoGround2", "Hop", "TPHop", "FakeJump", "FakeJump2", "Jump", "LowJump", "LowJump2", "Visual"), "Packet")
    val pitjump2 = ListValue("GroundMode",arrayOf("GrimAC","LiquidGlow","Packet", "Packet2", "Packet3", "Packet4","Pit","Hyt","HytSpartan","HytTest","SuperPacket", "NoGround", "NoGround2", "Hop", "TPHop", "FakeJump", "FakeJump2", "Jump", "LowJump", "LowJump2", "Visual"), "Packet")
    val delayValue = IntegerValue("Delay", 0, -500, 500)
    private val lookValue = BoolValue("SendC06", false)
    private val hurtTimeValue = IntegerValue("HurtTime", 10, -10, 20)

    var n = 0
    private var attacks = 0
    private var target = 0

    var targetID = 0

    private fun fakeJump() {
        val thePlayer = mc.thePlayer ?: return

        thePlayer.isAirBorne = true
        thePlayer.triggerAchievement(classProvider.getStatEnum(StatType.JUMP_STAT))
    }

    val msTimer = MSTimer()
    private fun sendCriticalPacket(
            xOffset: Double = 0.0,
            yOffset: Double = 0.0,
            zOffset: Double = 0.0,
            ground: Boolean
    ) {
        val x = mc.thePlayer!!.posX + xOffset
        val y = mc.thePlayer!!.posY + yOffset
        val z = mc.thePlayer!!.posZ + zOffset
        if (lookValue.get()) {
            mc.netHandler.addToSendQueue(
                    classProvider.createCPacketPlayerPosLook(
                            x,
                            y,
                            z,
                            mc.thePlayer!!.rotationYaw,
                            mc.thePlayer!!.rotationPitch,
                            ground
                    )
            )
        } else {
            mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(x, y, z, ground))
        }
    }

    override fun onEnable() {
        val thePlayer = mc.thePlayer ?: return

        if (modeValue.get().equals("NoGround", ignoreCase = true)) {
            thePlayer.jump()
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        //Pit Crit Bypass by Chip test day1
        if (mc.thePlayer!!.onGround) {
            this.modeValue.set(pitjump2.get())
        } else {
            this.modeValue.set(pitjump.get())
        }
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (classProvider.isEntityLivingBase(event.targetEntity)) {
            val thePlayer = mc.thePlayer ?: return
            val entity = event.targetEntity!!.asEntityLivingBase()

            if (!thePlayer.onGround || thePlayer.isOnLadder || thePlayer.isInWeb || thePlayer.isInWater ||
                    thePlayer.isInLava || thePlayer.ridingEntity != null || entity.hurtTime > hurtTimeValue.get() ||
                    LiquidBounce.moduleManager[Fly::class.java]!!.state || !msTimer.hasTimePassed(delayValue.get().toLong()))
                return

            val x = thePlayer.posX
            val y = thePlayer.posY
            val z = thePlayer.posZ

            when (modeValue.get().toLowerCase()) {
                "GrimAC" -> {
                    mc.netHandler.addToSendQueue((classProvider.createCPacketPlayerPosition(x, y + 0.06250000001304, z, true) as IPacket)!!)
                    mc.netHandler.addToSendQueue((classProvider.createCPacketPlayerPosition(x, y + 0.00150000001304, z, false) as IPacket)!!)
                    mc.netHandler.addToSendQueue((classProvider.createCPacketPlayerPosition(x, y + 0.014400000001304, z, false) as IPacket)!!)
                    mc.netHandler.addToSendQueue((classProvider.createCPacketPlayerPosition(x, y + 0.001150000001304, z, false) as IPacket)!!)
                    thePlayer.onCriticalHit((XMLTokener.entity as IEntity)!!)
                }
                "LiquidGlow" -> {
                    mc.netHandler.addToSendQueue((classProvider.createCPacketPlayerPosition(x, y + 0.06250000001304, z, true) as IPacket))
                    mc.netHandler.addToSendQueue((classProvider.createCPacketPlayerPosition(x, y + 0.06150000001304, z, false) as IPacket))
                }
                "lowjump" -> thePlayer.motionY = 0.3425
                "jump" -> thePlayer.motionY = 0.42
                "visual" -> thePlayer.onCriticalHit(entity)
                "lowjump2" -> {
                    if (thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown && !MovementUtils.isMoving) {
                        thePlayer.motionY = 0.20
                    }
                }
                "hop" -> {
                    thePlayer.motionY = 0.1
                    thePlayer.fallDistance = 0.1f
                    thePlayer.onGround = false
                }
                "pit"->{
                    attacks++
                    if (attacks > 5) {
                        sendCriticalPacket(yOffset = 0.0114514, ground = false)
                        sendCriticalPacket(yOffset = 0.0019 ,ground = false)
                        sendCriticalPacket(yOffset = 0.000001 ,ground = false)
                        attacks = 0
                    }
                }
                "hyt" -> {
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(x, y + 0.0000001302232008, z, true))
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(x, y, z, false))
                    thePlayer.sprinting = false
                    thePlayer.onCriticalHit(entity)
                }
                "hyttest" -> {
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(
                            mc.thePlayer!!.posX,
                            mc.thePlayer!!.posY + 0.00001100134977413,
                            mc.thePlayer!!.posZ,
                            false
                    ))
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(
                            mc.thePlayer!!.posX,
                            mc.thePlayer!!.posY + 0.00000000013487744,
                            mc.thePlayer!!.posZ,
                            false
                    ))
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(
                            mc.thePlayer!!.posX,
                            mc.thePlayer!!.posY + 0.00000571003114589,
                            mc.thePlayer!!.posZ,
                            false
                    ))
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(
                            mc.thePlayer!!.posX,
                            mc.thePlayer!!.posY + 0.00000001578887744,
                            mc.thePlayer!!.posZ,
                            false
                    ))
                }

                "tphop" -> {
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(x, y + 0.02, z, false))
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(x, y + 0.01, z, false))
                    thePlayer.setPosition(x, y + 0.01, z)
                }
                "noground2" -> {
                    thePlayer.onCriticalHit(entity)
                }
                "packet" -> {
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(x, y + 0.0625, z, true))
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(x, y, z, false))
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(x, y + 1.1E-5, z, false))
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(x, y, z, false))
                }
                "packet2" -> {
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(x, y + 0.11, z, false))
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(x, y + 0.1100013579, z, false))
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(x, y + 0.0000013579, z, false))
                }
                "packet3" -> {
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(thePlayer.posX, thePlayer.posY + 0.05000000074505806, thePlayer.posZ, false))
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(thePlayer.posX, thePlayer.posY, thePlayer.posZ, false))
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(thePlayer.posX, thePlayer.posY + 0.012511000037193298, thePlayer.posZ, false))
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(thePlayer.posX, thePlayer.posY, thePlayer.posZ, false))
                }
                "packet4" -> {
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(thePlayer.posX, thePlayer.posY + 0.0031311231111, thePlayer.posZ, false))
                }
                "superpacket" -> {
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(thePlayer.posX, thePlayer.posY + 0.1625, thePlayer.posZ, false))
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(thePlayer.posX, thePlayer.posY, thePlayer.posZ, false))
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(thePlayer.posX, thePlayer.posY + 4.0E-6, thePlayer.posZ, false))
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(thePlayer.posX, thePlayer.posY, thePlayer.posZ, false))
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(thePlayer.posX, thePlayer.posY + 1.0E-6, thePlayer.posZ, false))
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(thePlayer.posX, thePlayer.posY, thePlayer.posZ, false))
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayer(false))
                }
                "hytspartan" -> {
                    n = this.attacks;
                    this.attacks = n + 1;
                    if (this.attacks <= 6){

                    }else{
                        mc.netHandler.addToSendQueue(MinecraftInstance.classProvider.createCPacketPlayerPosition(x, y + 0.01, z, false));
                        mc.netHandler.addToSendQueue(MinecraftInstance.classProvider.createCPacketPlayerPosition(x, y + 1.0E-10, z, false));
                        mc.netHandler.addToSendQueue(MinecraftInstance.classProvider.createCPacketPlayerPosition(x, y + 0.114514, z, false));
                        this.attacks = 0;
                    }
                }
                "fakejump" -> {
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(thePlayer.posX, thePlayer.posY + 0.42, thePlayer.posZ, false))
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(thePlayer.posX, thePlayer.posY, thePlayer.posZ, false))
                }
                "fakejump2" -> {
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(thePlayer.posX, thePlayer.posY + 0.42, thePlayer.posZ, false))
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(thePlayer.posX, thePlayer.posY, thePlayer.posZ, false))
                    fakeJump()
                }
            }

            msTimer.reset()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val thePlayer = mc.thePlayer ?: return

        val packet = event.packet

        if (packet is CPacketPlayer && modeValue.get().equals("NoGround", ignoreCase = true))
            packet.onGround = false
        if(packet is CPacketPlayer && modeValue.get().equals("NoGround2", ignoreCase = true) && !thePlayer.onGround && !thePlayer.isCollidedVertically && thePlayer.fallDistance < 2)
            packet.onGround = true

        if (classProvider.isSPacketAnimation(packet) && packet.asSPacketAnimation().animationType == 4 && packet.asSPacketAnimation().entityID == targetID) {
                val name = (LiquidBounce.moduleManager.getModule(KillAura::class.java) as KillAura).target!!.name
                ClientUtils.displayChatMessage("§b[§b§lRionTips]§f触发§c暴击§b(§6玩家:§a$name)")
            }
        }

    override val tag: String?
        get() = "Rion"
}

