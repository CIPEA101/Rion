/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketUseEntity
import net.ccbluex.liquidbounce.api.minecraft.util.WBlockPos
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.tomk.CancelC03
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.util.math.MathHelper
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@ModuleInfo(name = "Velocity",description="L" ,category = ModuleCategory.COMBAT)
class RionVelocity : Module() {

    /**
     * OPTIONS
     */
    private val horizontalValue = FloatValue("Horizontal", 0F, -2F, 2F)
    private val verticalValue = FloatValue("Vertical", 0F, -2F, 2F)
    private val velocityTickValue = IntegerValue("VelocityTick", 1, 0, 10)
    private val modeValue = ListValue("Mode", arrayOf(
            "Cancel", "Simple", "Tick",
            "AACPush", "AACZero", "AAC4Reduce", "AAC5Reduce",
            "Redesky1", "Redesky2",
            "AAC5.2.0", "AAC5.2.0Combat",
            "MatrixReduce", "MatrixSimple", "MatrixReverse", "MatrixSpoof", "MatrixGround",
            "Reverse", "SmoothReverse",
            "Jump",
            "Phase", "PacketPhase", "Glitch", "Spoof","GrimTest","grim-packet","grim-motion",
            "Legit","Fire","HytPacket"), "Simple")

    // Reverse
    private val reverseStrengthValue = FloatValue("ReverseStrength", 1F, 0.1F, 1F)
    private val reverse2StrengthValue = FloatValue("SmoothReverseStrength", 0.05F, 0.02F, 0.1F)

    // AAC Push
    private val aacPushXZReducerValue = FloatValue("AACPushXZReducer", 2F, 1F, 3F)
    private val aacPushYReducerValue = BoolValue("AACPushYReducer", true)
    // phase
    private val phaseHeightValue = FloatValue("PhaseHeight", 0.5F, 0F, 1F)
    private val phaseOnlyGround = BoolValue("PhaseOnlyGround", true)
    private val hytGround = BoolValue("HytOnlyGround", true)
    // legit
    private val legitStrafeValue = BoolValue("LegitStrafe", false )
    private val legitFaceValue = BoolValue("LegitFace", true)

    private val rspAlwaysValue = BoolValue("RedeskyAlwaysReduce", true)
    private val rspDengerValue = BoolValue("RedeskyOnlyDanger", false)

    private val onlyGroundValue = BoolValue("OnlyGround", false)
    private val onlyCombatValue = BoolValue("OnlyCombat",false)
    private val noFireValue = BoolValue("noFire", false)

    private val overrideDirectionValue = ListValue("OverrideDirection", arrayOf("None", "Hard", "Offset"), "None")
    private val overrideDirectionYawValue = FloatValue("OverrideDirectionYaw", 0F, -180F, 180F)

    /**
     * VALUES
     */
    private var velocityTimer = MSTimer()
    private var velocityCalcTimer = MSTimer()
    private var velocityInput = false
    private var velocityTick = 0

    // SmoothReverse
    private var reverseHurt = false

    // AACPush
    private var jump = false

    // Legit
    private var pos: WBlockPos? = null

    private var redeCount = 24

    private var templateX = 0
    private var templateY = 0
    private var templateZ = 0

    private var isMatrixOnGround = false

    override val tag: String
        get() = modeValue.get()

    override fun onDisable() {
        mc.thePlayer!!?.speedInAir = 0.02F
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if(velocityInput) {
            velocityTick++
        }else velocityTick = 0

        if (redeCount <24) redeCount++
        if (mc.thePlayer!!.isInWater || mc.thePlayer!!.isInLava || mc.thePlayer!!.isInWeb) {
            return
        }

        if ((onlyGroundValue.get() && !mc.thePlayer!!.onGround) ) {
            return
        }
        if(onlyCombatValue.get() && !LiquidBounce.combatManager.inCombat) return
        // if(onlyHitVelocityValue.get() && mc.thePlayer!!.motionY<0.05) return；
        if (noFireValue.get() && mc.thePlayer!!.burning) return

        when (modeValue.get().toLowerCase()) {
            "tick" -> {
                if(velocityTick > velocityTickValue.get()) {
                    if(mc.thePlayer!!.motionY > 0) mc.thePlayer!!.motionY = 0.0
                    mc.thePlayer!!.motionX = 0.0
                    mc.thePlayer!!.motionZ = 0.0
                    mc.thePlayer!!.jumpMovementFactor = -0.00001f
                    velocityInput = false
                }
                if(mc.thePlayer!!.onGround && velocityTick > 1) {
                    velocityInput = false
                }
            }
            "Fire" -> {
                if (mc.thePlayer!!.hurtTime > 0) {
                    mc.thePlayer!!.motionX += -1.0E-7
                    mc.thePlayer!!.motionY += -1.0E-7
                    mc.thePlayer!!.motionZ += -1.0E-7
                    mc.thePlayer!!.isAirBorne = true
                }
            }
            "jump" -> if (mc.thePlayer!!.hurtTime > 0 && mc.thePlayer!!.onGround) {
                mc.thePlayer!!.motionY = 0.42
            }

            "reverse" -> {
                if (!velocityInput) {
                    return
                }

                if (!mc.thePlayer!!.onGround) {
                    MovementUtils.strafe(MovementUtils.speed * reverseStrengthValue.get())
                } else if (velocityTimer.hasTimePassed(80L)) {
                    velocityInput = false
                }
            }
            "hytpacket" ->{
                if(hytGround.get()){
                    if (mc.thePlayer!!.hurtTime > 0 && !mc.thePlayer!!.isDead && mc.thePlayer!!.hurtTime <=5 && mc.thePlayer!!.onGround) {
                        mc.thePlayer!!.motionX *= 0.5
                        mc.thePlayer!!.motionZ *= 0.5
                        mc.thePlayer!!.motionY /= 1.781145F
                    }
                }else{
                    if (mc.thePlayer!!.hurtTime > 0 && !mc.thePlayer!!.isDead && mc.thePlayer!!.hurtTime <=5 ) {
                        mc.thePlayer!!.motionX *= 0.5
                        mc.thePlayer!!.motionZ *= 0.5
                        mc.thePlayer!!.motionY /= 1.781145F
                    }
                }

            }
            "smoothreverse" -> {
                if (!velocityInput) {
                    mc.thePlayer!!.speedInAir = 0.02F
                    return
                }

                if (mc.thePlayer!!.hurtTime > 0) {
                    reverseHurt = true
                }

                if (!mc.thePlayer!!.onGround) {
                    if (reverseHurt) {
                        mc.thePlayer!!.speedInAir = reverse2StrengthValue.get()
                    }
                } else if (velocityTimer.hasTimePassed(80L)) {
                    velocityInput = false
                    reverseHurt = false
                }
            }

            "aac4reduce" -> {
                if (mc.thePlayer!!.hurtTime> 0 && !mc.thePlayer!!.onGround && velocityInput && velocityTimer.hasTimePassed(80L)) {
                    mc.thePlayer!!.motionX *= 0.62
                    mc.thePlayer!!.motionZ *= 0.62
                }
                if (velocityInput && (mc.thePlayer!!.hurtTime <4 || mc.thePlayer!!.onGround) && velocityTimer.hasTimePassed(120L)) {
                    velocityInput = false
                }
            }

            "aac5reduce" -> {
                if (mc.thePlayer!!.hurtTime> 1 && velocityInput) {
                    mc.thePlayer!!.motionX *= 0.81
                    mc.thePlayer!!.motionZ *= 0.81
                }
                if (velocityInput && (mc.thePlayer!!.hurtTime <5 || mc.thePlayer!!.onGround) && velocityTimer.hasTimePassed(120L)) {
                    velocityInput = false
                }
            }

            "aac5.2.0combat" -> {
                if (mc.thePlayer!!.hurtTime> 0 && velocityInput) {
                    velocityInput = false
                    mc.thePlayer!!.motionX = 0.0
                    mc.thePlayer!!.motionZ = 0.0
                    mc.thePlayer!!.motionY = 0.0
                    mc.thePlayer!!.jumpMovementFactor = -0.002f
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(mc.thePlayer!!.posX, 1.7976931348623157E+308, mc.thePlayer!!.posZ, true))
                }
                if (velocityTimer.hasTimePassed(80L) && velocityInput) {
                    velocityInput = false
                    mc.thePlayer!!.motionX = templateX / 8000.0
                    mc.thePlayer!!.motionZ = templateZ / 8000.0
                    mc.thePlayer!!.motionY = templateY / 8000.0
                    mc.thePlayer!!.jumpMovementFactor = -0.002f
                }
            }

            "aacpush" -> {
                if (jump) {
                    if (mc.thePlayer!!.onGround) {
                        jump = false
                    }
                } else {
                    // Strafe
                    if (mc.thePlayer!!.hurtTime > 0 && mc.thePlayer!!.motionX != 0.0 && mc.thePlayer!!.motionZ != 0.0) {
                        mc.thePlayer!!.onGround = true
                    }

                    // Reduce Y
                    if (mc.thePlayer!!.hurtResistantTime > 0 && aacPushYReducerValue.get() &&
                            !LiquidBounce.moduleManager[Speed::class.java]!!.state) {
                        mc.thePlayer!!.motionY -= 0.014999993
                    }
                }

                // Reduce XZ
                if (mc.thePlayer!!.hurtResistantTime >= 19) {
                    val reduce = aacPushXZReducerValue.get()

                    mc.thePlayer!!.motionX /= reduce
                    mc.thePlayer!!.motionZ /= reduce
                }
            }
            "matrixreduce" -> {
                if (mc.thePlayer!!.hurtTime > 0) {
                    if (mc.thePlayer!!.onGround) {
                        if (mc.thePlayer!!.hurtTime <= 6) {
                            mc.thePlayer!!.motionX *= 0.70
                            mc.thePlayer!!.motionZ *= 0.70
                        }
                        if (mc.thePlayer!!.hurtTime <= 5) {
                            mc.thePlayer!!.motionX *= 0.80
                            mc.thePlayer!!.motionZ *= 0.80
                        }
                    } else if (mc.thePlayer!!.hurtTime <= 10) {
                        mc.thePlayer!!.motionX *= 0.60
                        mc.thePlayer!!.motionZ *= 0.60
                    }
                }
            }
            "grim-motion" -> {
                if (mc.thePlayer!!.hurtTime > 0) {
                    mc2.player.addVelocity(-1.0E-7,-1.0E-7,-1.0E-7)
                    mc2.player.addVelocity(-1.0E-7,-1.0E-7,-1.0E-7)
                    mc2.player.addVelocity(-1.0E-7,-1.0E-7,-1.0E-7)
                }
            }
            "matrixground" -> {
                isMatrixOnGround = mc.thePlayer!!.onGround && !mc.gameSettings.keyBindJump.isKeyDown
                if (isMatrixOnGround) mc.thePlayer!!.onGround = false
            }

            "glitch" -> {
                mc.thePlayer!!.noClip = velocityInput

                if (mc.thePlayer!!.hurtTime == 7) {
                    mc.thePlayer!!.motionY = 0.4
                }

                velocityInput = false
            }

            "aaczero" -> {
                if (mc.thePlayer!!.hurtTime > 0) {
                    if (!velocityInput || mc.thePlayer!!.onGround || mc.thePlayer!!.fallDistance > 2F)
                        return

                    mc.thePlayer!!.motionY -= 1.0
                    mc.thePlayer!!.isAirBorne = true
                    mc.thePlayer!!.onGround = true
                } else {
                    velocityInput = false
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if ((onlyGroundValue.get() && !mc.thePlayer!!.onGround)) {
            return
        }
        if(onlyCombatValue.get() && !LiquidBounce.combatManager.inCombat) return

        val packet = event.packet

        if (classProvider.isSPacketEntityVelocity(packet)) {

            val packetEntityVelocity = packet.asSPacketEntityVelocity()
            if ((mc.theWorld?.getEntityByID(packetEntityVelocity.entityID) ?: return) != mc.thePlayer!!) {
                return
            }
            // if(onlyHitVelocityValue.get() && packetEntityVelocity.motionY<400.0) return
            if (noFireValue.get() && mc.thePlayer!!.burning) return
            velocityTimer.reset()
            velocityTick = 0

            if(!overrideDirectionValue.equals("None")) {
                val yaw = Math.toRadians(if(overrideDirectionValue.get() == "Hard") {
                    overrideDirectionYawValue.get()
                } else {
                    mc.thePlayer!!.rotationYaw + overrideDirectionYawValue.get() + 90
                }.toDouble())
                val dist = sqrt((packetEntityVelocity.motionX * packetEntityVelocity.motionX + packetEntityVelocity.motionZ * packetEntityVelocity.motionZ).toDouble())
                val x = cos(yaw) * dist
                val z = sin(yaw) * dist
                packetEntityVelocity.motionX = x.toInt()
                packetEntityVelocity.motionZ = z.toInt()
            }

            when (modeValue.get().toLowerCase()) {
                "tick" -> {
                    velocityInput = true
                    val horizontal = horizontalValue.get()
                    val vertical = verticalValue.get()

                    if (horizontal == 0F && vertical == 0F) {
                        event.cancelEvent()
                    }

                    packetEntityVelocity.motionX = (packetEntityVelocity.motionX * horizontal).toInt()
                    packetEntityVelocity.motionY = (packetEntityVelocity.motionY * vertical).toInt()
                    packetEntityVelocity.motionZ = (packetEntityVelocity.motionZ * horizontal).toInt()
                }
                "simple" -> {
                    //velocityInput = true
                    val horizontal = horizontalValue.get()
                    val vertical = verticalValue.get()

                    if (horizontal == 0F && vertical == 0F) {
                        event.cancelEvent()
                    }

                    packetEntityVelocity.motionX = (packetEntityVelocity.motionX * horizontal).toInt()
                    packetEntityVelocity.motionY = (packetEntityVelocity.motionY * vertical).toInt()
                    packetEntityVelocity.motionZ = (packetEntityVelocity.motionZ * horizontal).toInt()
                }
                "grimtest" -> {

                    var cancelC03 = LiquidBounce.moduleManager.getModule(CancelC03::class.java) as CancelC03
                    cancelC03.info = true
                    //velocityInput = true
                    packetEntityVelocity.motionX = (packetEntityVelocity.motionX * 0).toInt()
                    packetEntityVelocity.motionY = (packetEntityVelocity.motionY * 0).toInt()
                    packetEntityVelocity.motionZ = (packetEntityVelocity.motionZ * 0).toInt()
                }
                "cancel" -> {
                    event.cancelEvent()
                }
                "grim-packet" ->{
                    mc.thePlayer!!.motionX += -0.01001400514
                    mc.thePlayer!!.motionZ += 0.0001011101
                    mc.thePlayer!!.motionY += -0.0010101010
                }
                "matrixsimple" -> {
                    packetEntityVelocity.motionX = (packetEntityVelocity.motionX * 0.36).toInt()
                    packetEntityVelocity.motionZ = (packetEntityVelocity.motionZ * 0.36).toInt()
                    if (mc.thePlayer!!.onGround) {
                        packetEntityVelocity.motionX = (packetEntityVelocity.motionX * 0.9).toInt()
                        packetEntityVelocity.motionZ = (packetEntityVelocity.motionZ * 0.9).toInt()
                    }
                }

                "matrixground" -> {
                    packetEntityVelocity.motionX = (packetEntityVelocity.motionX * 0.36).toInt()
                    packetEntityVelocity.motionZ = (packetEntityVelocity.motionZ * 0.36).toInt()
                    if (isMatrixOnGround) {
                        packetEntityVelocity.motionY = (-628.7).toInt()
                        packetEntityVelocity.motionX = (packetEntityVelocity.motionX * 0.6).toInt()
                        packetEntityVelocity.motionZ = (packetEntityVelocity.motionZ * 0.6).toInt()
                    }
                }

                "matrixreverse" -> {
                    packetEntityVelocity.motionX = (packetEntityVelocity.motionX * -0.3).toInt()
                    packetEntityVelocity.motionZ = (packetEntityVelocity.motionZ * -0.3).toInt()
                }

                "matrixspoof" -> {
                    event.cancelEvent()
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(mc.thePlayer!!.posX + packetEntityVelocity.motionX / - 24000.0, mc.thePlayer!!.posY + packetEntityVelocity.motionY / -24000.0, mc.thePlayer!!.posZ + packetEntityVelocity.motionZ / 8000.0, false))
                }

                "aac4reduce" -> {
                    velocityInput = true
                    packetEntityVelocity.motionX = (packetEntityVelocity.motionX * 0.6).toInt()
                    packetEntityVelocity.motionZ = (packetEntityVelocity.motionZ * 0.6).toInt()
                }

                "aac5.2.0" -> {
                    event.cancelEvent()
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(mc.thePlayer!!.posX, 1.7976931348623157E+308, mc.thePlayer!!.posZ, true))
                }

                "aac5reduce", "reverse", "smoothreverse", "aaczero" -> velocityInput = true

                "phase" -> {
                    if (!mc.thePlayer!!.onGround && phaseOnlyGround.get()) {
                        return
                    }

                    velocityInput = true
                    mc.thePlayer!!.setPositionAndUpdate(mc.thePlayer!!.posX, mc.thePlayer!!.posY - phaseHeightValue.get(), mc.thePlayer!!.posZ)
                    event.cancelEvent()
                    packetEntityVelocity.motionX = 0
                    packetEntityVelocity.motionY = 0
                    packetEntityVelocity.motionZ = 0
                }

                "aac5.2.0combat" -> {
                    event.cancelEvent()
                    velocityInput = true
                    templateX = packetEntityVelocity.motionX
                    templateZ = packetEntityVelocity.motionZ
                    templateY = packetEntityVelocity.motionY
                }

                "spoof" -> {
                    event.cancelEvent()
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(mc.thePlayer!!.posX + packetEntityVelocity.motionX / 8000.0, mc.thePlayer!!.posY + packetEntityVelocity.motionY / 8000.0, mc.thePlayer!!.posZ + packetEntityVelocity.motionZ / 8000.0, false))
                }

                "packetphase" -> {
                    if (!mc.thePlayer!!.onGround && phaseOnlyGround.get()) {
                        return
                    }

//                    chat("MOTX=${packetEntityVelocity.motionX}, MOTZ=${packetEntityVelocity.motionZ}")
                    if (packetEntityVelocity.motionX <500 && packetEntityVelocity.motionY <500) {
                        return
                    }

                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(mc.thePlayer!!.posX, mc.thePlayer!!.posY - phaseHeightValue.get(), mc.thePlayer!!.posZ, false))
                    event.cancelEvent()
                    packetEntityVelocity.motionX = 0
                    packetEntityVelocity.motionY = 0
                    packetEntityVelocity.motionZ = 0
                }

                "glitch" -> {
                    if (!mc.thePlayer!!.onGround) {
                        return
                    }

                    velocityInput = true
                    event.cancelEvent()
                }

                "legit" -> {
                    pos = WBlockPos(mc.thePlayer!!.posX, mc.thePlayer!!.posY, mc.thePlayer!!.posZ)
                }

                "redesky2" -> {
                    if (packetEntityVelocity.motionX == 0 && packetEntityVelocity.motionZ == 0) { // ignore horizonal velocity
                        return
                    }

                    val target = (LiquidBounce.moduleManager[KillAura::class.java] as KillAura).target
                    mc.thePlayer!!.motionX = 0.0
                    mc.thePlayer!!.motionZ = 0.0
                    packetEntityVelocity.motionX = 0
                    packetEntityVelocity.motionZ = 0
                    for (i in 0..redeCount) {
                        mc.thePlayer!!.sendQueue.addToSendQueue(classProvider.createCPacketUseEntity(target!!, ICPacketUseEntity.WAction.ATTACK))
                        mc.thePlayer!!.sendQueue.addToSendQueue(classProvider.createCPacketAnimation())
                    }
                    if (redeCount> 12) redeCount -= 5
                }

                "redesky1" -> {
                    if (packetEntityVelocity.motionX == 0 && packetEntityVelocity.motionZ == 0) { // ignore horizonal velocity
                        return
                    }

                    if (rspDengerValue.get()) {
                        val pos = FallingPlayer(mc.thePlayer!!.posX, mc.thePlayer!!.posY, mc.thePlayer!!.posZ, packetEntityVelocity.motionX / 8000.0, packetEntityVelocity.motionY / 8000.0, packetEntityVelocity.motionZ / 8000.0, 0f, 0f, 0f).findCollision(60)
                        if (pos != null && pos.pos.y> (mc.thePlayer!!.posY - 7)) {
                            return
                        }
                    }

                    val target = (LiquidBounce.moduleManager[KillAura::class.java] as KillAura).target
                    if (rspAlwaysValue.get()) {
                        mc.thePlayer!!.motionX = 0.0
                        mc.thePlayer!!.motionZ = 0.0
                        // mc.thePlayer!!.motionY=(packetEntityVelocity.motionY/8000f)*1.0
                        packetEntityVelocity.motionX = 0
                        packetEntityVelocity.motionZ = 0
                        // event.cancelEvent() better stuff
                    }

                    if (velocityCalcTimer.hasTimePassed(500)) {
                        if (!rspAlwaysValue.get()) {
                            mc.thePlayer!!.motionX = 0.0
                            mc.thePlayer!!.motionZ = 0.0
                            // mc.thePlayer!!.motionY=(packetEntityVelocity.motionY/8000f)*1.0
                            packetEntityVelocity.motionX = 0
                            packetEntityVelocity.motionZ = 0
                        }
                        val count = if (!velocityCalcTimer.hasTimePassed(800)) {
                            8
                        } else if (!velocityCalcTimer.hasTimePassed(1200)) {
                            12
                        } else {
                            25
                        }
                        for (i in 0..count) {
                            mc.thePlayer!!.sendQueue.addToSendQueue(classProvider.createCPacketUseEntity(target!!, ICPacketUseEntity.WAction.ATTACK))
                            mc.thePlayer!!.sendQueue.addToSendQueue(classProvider.createCPacketAnimation())
                        }
                        velocityCalcTimer.reset()
                    } else {
                        packetEntityVelocity.motionX = (packetEntityVelocity.motionX * 0.6).toInt()
                        packetEntityVelocity.motionZ = (packetEntityVelocity.motionZ * 0.6).toInt()
                        for (i in 0..4) {
                            mc.thePlayer!!.sendQueue.addToSendQueue(classProvider.createCPacketUseEntity(target!!, ICPacketUseEntity.WAction.ATTACK))
                            mc.thePlayer!!.sendQueue.addToSendQueue(classProvider.createCPacketAnimation())
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if ((onlyGroundValue.get() && !mc.thePlayer!!.onGround)) {
            return
        }

        when (modeValue.get().toLowerCase()) {
            "legit" -> {
                if (pos == null || mc.thePlayer!!.hurtTime <= 0) {
                    return
                }

                val rot = RotationUtils.getRotations(pos!!.x.toDouble(), pos!!.y.toDouble(), pos!!.z.toDouble())
                if (legitFaceValue.get()) {
                    RotationUtils.setTargetRotation(rot)
                }
                val yaw = rot.yaw
                if (legitStrafeValue.get()) {
                    val speed = MovementUtils.speed
                    val yaw1 = Math.toRadians(yaw.toDouble())
                    mc.thePlayer!!.motionX = -sin(yaw1) * speed
                    mc.thePlayer!!.motionZ = cos(yaw1) * speed
                } else {
                    var strafe = event.strafe
                    var forward = event.forward
                    val friction = event.friction

                    var f = strafe * strafe + forward * forward

                    if (f >= 1.0E-4F) {
                        f = MathHelper.sqrt(f)

                        if (f < 1.0F) {
                            f = 1.0F
                        }

                        f = friction / f
                        strafe *= f
                        forward *= f

                        val yawSin = MathHelper.sin((yaw * Math.PI / 180F).toFloat())
                        val yawCos = MathHelper.cos((yaw * Math.PI / 180F).toFloat())

                        mc.thePlayer!!.motionX += strafe * yawCos - forward * yawSin
                        mc.thePlayer!!.motionZ += forward * yawCos + strafe * yawSin
                    }
                }
            }
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (mc.thePlayer!!.isInWater || mc.thePlayer!!.isInLava || mc.thePlayer!!.isInWeb || (onlyGroundValue.get() && !mc.thePlayer!!.onGround)) {
            return
        }

        if ((onlyGroundValue.get() && !mc.thePlayer!!.onGround)) {
            return
        }

        when (modeValue.get().toLowerCase()) {
            "aacpush" -> {
                jump = true

                if (!mc.thePlayer!!.isCollidedVertically) {
                    event.cancelEvent()
                }
            }
            "aaczero" -> if (mc.thePlayer!!.hurtTime > 0) {
                event.cancelEvent()
            }
        }
    }
}
