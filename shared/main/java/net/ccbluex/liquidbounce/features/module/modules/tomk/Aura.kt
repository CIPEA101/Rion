/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.tomk

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.api.enums.EnumFacingType
import net.ccbluex.liquidbounce.api.enums.WEnumHand
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntity
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntityLivingBase
import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketPlayerDigging
import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketUseEntity
import net.ccbluex.liquidbounce.api.minecraft.potion.PotionType
import net.ccbluex.liquidbounce.api.minecraft.util.WBlockPos
import net.ccbluex.liquidbounce.api.minecraft.util.WVec3
import net.ccbluex.liquidbounce.api.minecraft.world.IWorldSettings
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.Criticals
import net.ccbluex.liquidbounce.features.module.modules.combat.NoFriends
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.features.module.modules.render.FreeCam
import net.ccbluex.liquidbounce.features.module.modules.render.OldHitting
import net.ccbluex.liquidbounce.injection.backend.Backend
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.isAnimal
import net.ccbluex.liquidbounce.utils.extensions.isClientFriend
import net.ccbluex.liquidbounce.utils.extensions.isMob
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.util.EnumParticleTypes
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.Cylinder
import net.ccbluex.liquidbounce.features.module.modules.tomk.Teams
import net.ccbluex.liquidbounce.injection.forge.mixins.client.MixinMinecraft
import tomk.EaseUtils
import tomk.Recorder
import java.awt.Color
import java.awt.Robot
import java.awt.event.InputEvent
import java.util.*
import kotlin.math.*

@ModuleInfo(name = "Aura", description = "Automatically attacks targets around you.",
        category = ModuleCategory.COMBAT)
class Aura : Module() {

    /**
     * OPTIONS
     */

    // CPS - Attack speed
    private val maxCPS: IntegerValue = object : IntegerValue("MaxCPS", 20, 1, 30) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minCPS.get()
            if (i > newValue) set(i)

            attackDelay = TimeUtils.randomClickDelay(minCPS.get(), this.get())
        }
    }

    private val minCPS: IntegerValue = object : IntegerValue("MinCPS", 20, 1, 30) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxCPS.get()
            if (i < newValue) set(i)

            attackDelay = TimeUtils.randomClickDelay(this.get(), maxCPS.get())
        }
    }

    val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 20)
    private val cooldownValue = FloatValue("Cooldown", 0f, 0f, 1f)
    private val switchDelayValue = IntegerValue("SwitchDelay", 1000, 0, 2000)

    // Range
    val rangeValue = FloatValue("Range", 3.5f, 1f, 8f)
    private val throughWallsRangeValue = FloatValue("ThroughWallsRange", 1f, 0f, 8f)
    private val rangeSprintReducementValue = FloatValue("RangeSprintReducement", 0f, 0f, 0.4f)

    // Modes
    private val priorityValue = ListValue("Priority", arrayOf("Health", "Distance", "Direction", "LivingTime"), "Health")
    val targetModeValue = ListValue("TargetMode", arrayOf("Single", "Switch", "Multi"), "Switch")

    // Bypass
    private val swingValue = BoolValue("Swing", true)
    val keepSprintValue = BoolValue("KeepSprint", false)
    val stopSprintAir = BoolValue("StopSprintOnAir", false)

    //RionPit
    val pitjump = BoolValue("PitHelp", false)
    val pitstopsprintair = BoolValue("PitStopSprintAir", false)

    // AutoBlock
    val autoBlockValue = ListValue("AutoBlock", arrayOf("Off", "Packet", "AfterTick", "Right", "AACBy1.12.2" , "Old1.8.9", "Hyt1.12.2"), "Packet")
    private val interactAutoBlockValue = BoolValue("InteractAutoBlock", false)
    private val blockRate = IntegerValue("BlockRate", 100, 1, 100)

    // Raycast
    private val raycastValue = BoolValue("RayCast", true)
    private val raycastIgnoredValue = BoolValue("RayCastIgnored", false)
    private val livingRaycastValue = BoolValue("LivingRayCast", true)

    // Bypass
    private val aacValue = BoolValue("AAC", false)

    // Turn Speed
    private val maxTurnSpeed: FloatValue = object : FloatValue("MaxTurnSpeed", 180f, 0f, 360f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minTurnSpeed.get()
            if (v > newValue) set(v)
        }
    }

    private val minTurnSpeed: FloatValue = object : FloatValue("MinTurnSpeed", 180f, 0f, 360f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxTurnSpeed.get()
            if (v < newValue) set(v)
        }
    }
    private val rotations = ListValue("RotationMode", arrayOf("None", "New", "BackTrack","HytRotation"), "HytRotation")
    private val silentRotationValue = BoolValue("SilentRotation", true)
    private val rotationStrafeValue = ListValue("Strafe", arrayOf("Off", "Strict", "Silent"), "Strict")
    private val randomCenterValue = BoolValue("RandomCenter", true)
    private val outborderValue = BoolValue("Outborder", false)
    private val fovValue = FloatValue("FOV", 180f, 0f, 180f)

    // Predict
    private val predictValue = BoolValue("Predict", true)

    private val maxPredictSize: FloatValue = object : FloatValue("MaxPredictSize", 1f, 0.1f, 5f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minPredictSize.get()
            if (v > newValue) set(v)
        }
    }

    private val minPredictSize: FloatValue = object : FloatValue("MinPredictSize", 0.75f, 0.1f, 5f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxPredictSize.get()
            if (v < newValue) set(v)
        }
    }

    // Bypass
    private val failRateValue = FloatValue("FailRate", 0f, 0f, 100f)
    private val fakeSwingValue = BoolValue("FakeSwing", true)
    private val noInventoryAttackValue = BoolValue("NoInvAttack", false)
    private val noInventoryDelayValue = IntegerValue("NoInvDelay", 200, 0, 500)
    private val limitedMultiTargetsValue = IntegerValue("LimitedMultiTargets", 0, 0, 50)

    // Visuals
    private val markValue = ListValue("Mark", arrayOf("Liquid","FDP","Block","Jello", "Plat", "Red", "Sims", "None"),"FDP")
    private val hiteffect = ListValue("HitEffect", arrayOf("Lightningbolt","Criticals","Blood","Fire","Water","Smoke","Flame","Heart","None"),"Always")
    private val lightingSoundValue = BoolValue("LightingSound", true)
    private val fakeSharpValue = BoolValue("FakeSharp", false)
    private val circleValue = BoolValue("Circle",true)
    private val circleRedValue = IntegerValue("CircleRed", 255, 0, 255)
    private val circleGreenValue = IntegerValue("CircleGreen", 255, 0, 255)
    private val circleBlueValue = IntegerValue("CircleBlue", 255, 0, 255)
    private val circleAlphaValue = IntegerValue("CircleAlpha", 255, 0, 255)
    private val circleThicknessValue = FloatValue("CircleThickness", 2F, 1F, 5F)

    /**
     * MODULE
     */

    // Target
    var target: IEntityLivingBase? = null
    private var currentTarget: IEntityLivingBase? = null
    private var hitable = false
    private val prevTargetEntities = mutableListOf<Int>()

    // Attack delay
    private val attackTimer = MSTimer()
    private val switchTimer = MSTimer()
    private var attackDelay = 0L
    private var clicks = 0

    // Container Delay
    private var containerOpen = -1L

    // Fake block status
    var blockingStatus = false

    /**
     * Enable kill aura module
     */
    override fun onEnable() {
        mc.thePlayer ?: return
        mc.theWorld ?: return

        updateTarget()
    }

    /**
     * Disable kill aura module
     */
    override fun onDisable() {
        target = null
        currentTarget = null
        hitable = false
        prevTargetEntities.clear()
        attackTimer.reset()
        clicks = 0

        stopBlocking()
    }

    /**
     * Motion event
     */
    @EventTarget
    fun onMotion(event: MotionEvent) {
        //stop sprint in air
        if (this.stopSprintAir.get()) {
            if (mc.thePlayer!!.onGround) {
                this.keepSprintValue.set(true)
            } else {
                this.keepSprintValue.set(false)
            }
        }

        //Pit Jump Bypass
        if (this.pitjump.get()) {
            if (mc.thePlayer!!.onGround) {
                if (mc.thePlayer!!.hurtTime > 1)
                    this.silentRotationValue.set(true)
            } else {
                this.silentRotationValue.set(false)
            }
        }
        if (this.pitstopsprintair.get()) {
            if (mc.thePlayer!!.onGround) {
                this.keepSprintValue.set(true)
            } else {
                if (mc.thePlayer!!.hurtTime > 1)
                    this.keepSprintValue.set(false)
            }
        }

        if (event.eventState == EventState.POST) {
            target ?: return
            currentTarget ?: return

            // Update hitable
            updateHitable()

            // AutoBlock
            if (autoBlockValue.get().equals("AACBy1.12.2", true)) mc.gameSettings.keyBindUseItem.pressed = true

            if (autoBlockValue.get().equals("Old1.8.9", true))
                mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerBlockPlacement(WBlockPos(-1, -1, -1), 255, null, 0.0f, 0.0f, 0.0f))

            if (autoBlockValue.get().equals("Hyt1.12.2", true))

                if (autoBlockValue.get().equals("Right", true))
                    Robot().mousePress(InputEvent.BUTTON3_DOWN_MASK)

            if (autoBlockValue.get().equals("AfterTick", true) && canBlock)
                startBlocking(currentTarget!!, hitable)

            return
        }

        if (rotationStrafeValue.get().equals("Off", true))
            update()
    }

    /**
     * Strafe event
     */
    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (rotationStrafeValue.get().equals("Off", true))
            return

        update()

        if (currentTarget != null && RotationUtils.targetRotation != null) {
            when (rotationStrafeValue.get().toLowerCase()) {
                "strict" -> {
                    val (yaw) = RotationUtils.targetRotation ?: return
                    var strafe = event.strafe
                    var forward = event.forward
                    val friction = event.friction

                    var f = strafe * strafe + forward * forward

                    if (f >= 1.0E-4F) {
                        f = sqrt(f)

                        if (f < 1.0F)
                            f = 1.0F

                        f = friction / f
                        strafe *= f
                        forward *= f

                        val yawSin = sin((yaw * Math.PI / 180F).toFloat())
                        val yawCos = cos((yaw * Math.PI / 180F).toFloat())

                        val player = mc.thePlayer!!

                        player.motionX += strafe * yawCos - forward * yawSin
                        player.motionZ += forward * yawCos + strafe * yawSin
                    }
                    event.cancelEvent()
                }
                "silent" -> {
                    update()

                    RotationUtils.targetRotation.applyStrafeToPlayer(event)
                    event.cancelEvent()
                }
            }
        }
    }

    fun update() {
        if (cancelRun || (noInventoryAttackValue.get() && (classProvider.isGuiContainer(mc.currentScreen) ||
                        System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get())))
            return

        // Update target
        updateTarget()

        if (target == null) {
            stopBlocking()
            return
        }

        // Target
        currentTarget = target

        if (!targetModeValue.get().equals("Switch", ignoreCase = true) && isEnemy(currentTarget))
            target = currentTarget
    }

    /**
     * Update event
     */
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (cancelRun) {
            target = null
            currentTarget = null
            hitable = false
            stopBlocking()
            return
        }

        if (noInventoryAttackValue.get() && (classProvider.isGuiContainer(mc.currentScreen) ||
                        System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get())) {
            target = null
            currentTarget = null
            hitable = false
            if (classProvider.isGuiContainer(mc.currentScreen)) containerOpen = System.currentTimeMillis()
            return
        }
        if(target != null && target!!.health<=0.5){
            Recorder.killCounts += 1
        }
        if (target != null && currentTarget != null && (Backend.MINECRAFT_VERSION_MINOR == 8 || mc.thePlayer!!.getCooledAttackStrength(0.0F) >= cooldownValue.get())) {
            while (clicks > 0) {
                runAttack()
                clicks--
            }
        }
    }

    /**
     * Render event
     */
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (circleValue.get()) {
            GL11.glPushMatrix()
            GL11.glTranslated(
                    mc.thePlayer!!.lastTickPosX + (mc.thePlayer!!.posX - mc.thePlayer!!.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX,
                    mc.thePlayer!!.lastTickPosY + (mc.thePlayer!!.posY - mc.thePlayer!!.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY,
                    mc.thePlayer!!.lastTickPosZ + (mc.thePlayer!!.posZ - mc.thePlayer!!.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ
            )
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

            GL11.glLineWidth(circleThicknessValue.get())
            GL11.glColor4f(circleRedValue.get().toFloat() / 255.0F, circleGreenValue.get().toFloat() / 255.0F, circleBlueValue.get().toFloat() / 255.0F, circleAlphaValue.get().toFloat() / 255.0F)
            GL11.glRotatef(90F, 1F, 0F, 0F)
            GL11.glBegin(GL11.GL_LINE_STRIP)

            for (i in 0..360 step 5) { // You can change circle accuracy  (60 - accuracy)
                GL11.glVertex2f(cos(i * Math.PI / 180.0).toFloat() * rangeValue.get(), (sin(i * Math.PI / 180.0).toFloat() * rangeValue.get()))
            }

            GL11.glEnd()

            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)

            GL11.glPopMatrix()
        }

        if (cancelRun) {
            target = null
            currentTarget = null
            hitable = false
            stopBlocking()
            return
        }

        if (noInventoryAttackValue.get() && (classProvider.isGuiContainer(mc.currentScreen) ||
                        System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get())) {
            target = null
            currentTarget = null
            hitable = false
            if (classProvider.isGuiContainer(mc.currentScreen)) containerOpen = System.currentTimeMillis()
            return
        }

        target ?: return

        val ent = classProvider.createEntityLightningBolt(mc.theWorld!!, target!!.posX, target!!.posY, target!!.posZ, false)

        when(hiteffect.get().toLowerCase()){
            "lightningbolt" -> {
                mc.theWorld!!.addEntityToWorld(-1, ent)
                if(lightingSoundValue.get()){
                    mc.soundHandler.playSound("entity.lightning.impact", 1.0f)
                    mc.soundHandler.playSound("entity.lightning.thunder", 1.0f)
                }
            }
            "flame" -> mc.effectRenderer.emitParticleAtEntity(target!!, EnumParticleTypes.FLAME)
            "smoke" -> mc.effectRenderer.emitParticleAtEntity(target!!, EnumParticleTypes.SMOKE_LARGE)
            "heart" -> mc.effectRenderer.emitParticleAtEntity(target!!, EnumParticleTypes.HEART)
            "fire" -> mc.effectRenderer.emitParticleAtEntity(target!!, EnumParticleTypes.LAVA)
            "water" -> mc.effectRenderer.emitParticleAtEntity(target!!, EnumParticleTypes.WATER_DROP)
            "criticals" -> mc.effectRenderer.emitParticleAtEntity(target!!, EnumParticleTypes.CRIT)
            "blood" ->{
                repeat(10) {
                    mc.effectRenderer.spawnEffectParticle(EnumParticleTypes.BLOCK_CRACK.particleID, target!!.posX, target!!.posY + target!!.height / 2, target!!.posZ,
                            target!!.motionX + RandomUtils.nextFloat(-0.5f, 0.5f), target!!.motionY + RandomUtils.nextFloat(-0.5f, 0.5f), target!!.motionZ + RandomUtils.nextFloat(-0.5f, 0.5f), Block.getStateId(Blocks.REDSTONE_BLOCK.defaultState))
                }
            }
        }

        when (markValue.get().toLowerCase()) {
            "liquid" -> {
                RenderUtils.drawPlatform(target!!, if (target!!.hurtTime <= 0) Color(37, 126, 255, 170) else Color(255, 0, 0, 170))
            }
            "plat" -> RenderUtils.drawPlatform(
                    target!!,
                    if (hitable) Color(37, 126, 255, 70) else Color(255, 0, 0, 70)
            )
            "block" -> {
                val bb = target!!.entityBoundingBox
                target!!.entityBoundingBox = bb.expand(0.2, 0.2, 0.2)
                RenderUtils.drawEntityBox(target!!, if (target!!.hurtTime <= 0) Color.GREEN else Color.RED, true)
                target!!.entityBoundingBox = bb
            }
            "red" -> {
                RenderUtils.drawPlatform(target!!, if (target!!.hurtTime <= 0) Color(255, 255, 255, 255) else Color(124, 215, 255, 255))
            }
            "sims" -> {
                val radius = 0.15f
                val side = 4
                GL11.glPushMatrix()
                GL11.glTranslated(
                        target!!.lastTickPosX + (target!!.posX - target!!.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX,
                        (target!!.lastTickPosY + (target!!.posY - target!!.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY) + target!!.height * 1.1,
                        target!!.lastTickPosZ + (target!!.posZ - target!!.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
                )
                GL11.glRotatef(-target!!.width, 0.0f, 1.0f, 0.0f)
                GL11.glRotatef((mc.thePlayer!!.ticksExisted + mc.timer.renderPartialTicks) * 5, 0f, 1f, 0f)
                RenderUtils.glColor(if (target!!.hurtTime <= 0) Color(80, 240, 255) else Color(241, 128, 246))
                RenderUtils.enableSmoothLine(1.5F)
                val c = Cylinder()
                GL11.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f)
                c.draw(0F, radius, 0.3f, side, 1)
                c.drawStyle = 100012
                GL11.glTranslated(0.0, 0.0, 0.3)
                c.draw(radius, 0f, 0.3f, side, 1)
                GL11.glRotatef(90.0f, 0.0f, 0.0f, 1.0f)
                GL11.glTranslated(0.0, 0.0, -0.3)
                c.draw(0F, radius, 0.3f, side, 1)
                GL11.glTranslated(0.0, 0.0, 0.3)
                c.draw(radius, 0F, 0.3f, side, 1)
                RenderUtils.disableSmoothLine()
                GL11.glPopMatrix()
            }
            "fdp" -> {
                val drawTime = (System.currentTimeMillis() % 1500).toInt()
                val drawMode = drawTime > 750
                var drawPercent = drawTime / 750.0
                //true when goes up
                if (!drawMode) {
                    drawPercent = 1 - drawPercent
                } else {
                    drawPercent -= 1
                }
                drawPercent = EaseUtils.easeInOutQuad(drawPercent)
                GL11.glPushMatrix()
                GL11.glDisable(3553)
                GL11.glEnable(2848)
                GL11.glEnable(2881)
                GL11.glEnable(2832)
                GL11.glEnable(3042)
                GL11.glBlendFunc(770, 771)
                GL11.glHint(3154, 4354)
                GL11.glHint(3155, 4354)
                GL11.glHint(3153, 4354)
                GL11.glDisable(2929)
                GL11.glDepthMask(false)

                val bb = target!!.entityBoundingBox
                val radius = (bb.maxX - bb.minX) + 0.3
                val height = bb.maxY - bb.minY
                val x = target!!.lastTickPosX + (target!!.posX - target!!.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX
                val y = (target!!.lastTickPosY + (target!!.posY - target!!.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY) + height * drawPercent
                val z = target!!.lastTickPosZ + (target!!.posZ - target!!.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
                GL11.glLineWidth((radius * 5f).toFloat())
                GL11.glBegin(3)
                for (i in 0..360) {
                    val rainbow = Color(Color.HSBtoRGB((mc.thePlayer!!.ticksExisted / 70.0 + sin(i / 50.0 * 1.75)).toFloat() % 1.0f, 0.7f, 1.0f))
                    GL11.glColor3f(rainbow.red / 255.0f, rainbow.green / 255.0f, rainbow.blue / 255.0f)
                    GL11.glVertex3d(x + radius * cos(i * 6.283185307179586 / 45.0), y, z + radius * sin(i * 6.283185307179586 / 45.0))
                }
                GL11.glEnd()

                GL11.glDepthMask(true)
                GL11.glEnable(2929)
                GL11.glDisable(2848)
                GL11.glDisable(2881)
                GL11.glEnable(2832)
                GL11.glEnable(3553)
                GL11.glPopMatrix()
            }
            "jello" -> {
                val drawTime = (System.currentTimeMillis() % 2000).toInt()
                val drawMode = drawTime > 1000
                var drawPercent = drawTime / 1000.0
                //true when goes up
                if (!drawMode) {
                    drawPercent = 1 - drawPercent
                } else {
                    drawPercent -= 1
                }
                drawPercent = EaseUtils.easeInOutQuad(drawPercent)
                val points = mutableListOf<WVec3>()
                val bb = target!!.entityBoundingBox
                val radius = bb.maxX - bb.minX
                val height = bb.maxY - bb.minY
                val posX = target!!.lastTickPosX + (target!!.posX - target!!.lastTickPosX) * mc.timer.renderPartialTicks
                var posY = target!!.lastTickPosY + (target!!.posY - target!!.lastTickPosY) * mc.timer.renderPartialTicks
                if (drawMode) {
                    posY -= 0.5
                } else {
                    posY += 0.5
                }
                val posZ = target!!.lastTickPosZ + (target!!.posZ - target!!.lastTickPosZ) * mc.timer.renderPartialTicks
                for (i in 0..360 step 7) {
                    points.add(WVec3(posX - sin(i * Math.PI / 180F) * radius, posY + height * drawPercent, posZ + cos(i * Math.PI / 180F) * radius))
                }
                points.add(points[0])
                //draw
                mc.entityRenderer.disableLightmap()
                GL11.glPushMatrix()
                GL11.glDisable(GL11.GL_TEXTURE_2D)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GL11.glEnable(GL11.GL_LINE_SMOOTH)
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                GL11.glBegin(GL11.GL_LINE_STRIP)
                val baseMove = (if (drawPercent > 0.5) {
                    1 - drawPercent
                } else {
                    drawPercent
                }) * 2
                val min = (height / 60) * 20 * (1 - baseMove) * (if (drawMode) {
                    -1
                } else {
                    1
                })
                for (i in 0..20) {
                    var moveFace = (height / 60F) * i * baseMove
                    if (drawMode) {
                        moveFace = -moveFace
                    }
                    val firstPoint = points[0]
                    GL11.glVertex3d(
                            firstPoint.xCoord - mc.renderManager.viewerPosX, firstPoint.yCoord - moveFace - min - mc.renderManager.viewerPosY,
                            firstPoint.zCoord - mc.renderManager.viewerPosZ
                    )
                    GL11.glColor4f(1F, 1F, 1F, 0.7F * (i / 20F))
                    for (vec3 in points) {
                        GL11.glVertex3d(
                                vec3.xCoord - mc.renderManager.viewerPosX, vec3.yCoord - moveFace - min - mc.renderManager.viewerPosY,
                                vec3.zCoord - mc.renderManager.viewerPosZ
                        )
                    }
                    GL11.glColor4f(0F, 0F, 0F, 0F)
                }
                GL11.glEnd()
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GL11.glDisable(GL11.GL_LINE_SMOOTH)
                GL11.glDisable(GL11.GL_BLEND)
                GL11.glEnable(GL11.GL_TEXTURE_2D)
                GL11.glPopMatrix()
            }
        }

        if (currentTarget != null && attackTimer.hasTimePassed(attackDelay) &&
                currentTarget!!.hurtTime <= hurtTimeValue.get()) {
            clicks++
            attackTimer.reset()
            attackDelay = TimeUtils.randomClickDelay(minCPS.get(), maxCPS.get())
        }
    }


    private fun updateRotations(entity: IEntity): Boolean {
        var boundingBox = entity.entityBoundingBox



        if (rotations.get().equals("HytRotation", ignoreCase = true)) {
            if (predictValue.get())
                boundingBox = boundingBox.offset(
                        (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                        (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                        (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
                )
            val (_, rotation) = RotationUtils.lockView(
                    boundingBox,
                    outborderValue.get() && !attackTimer.hasTimePassed(attackDelay / 2),
                    randomCenterValue.get(),
                    predictValue.get(),
                    mc.thePlayer!!.getDistanceToEntityBox(entity) < throughWallsRangeValue.get(),
                    maxRange
            ) ?: return false

            val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation,
                    rotation,
                    (Math.random() * (maxTurnSpeed.get() - minTurnSpeed.get()) + minTurnSpeed.get()).toFloat())

            if (silentRotationValue.get())
                RotationUtils.setTargetRotation(limitedRotation, if (aacValue.get()) 15 else 0)
            else
                limitedRotation.toPlayer(mc.thePlayer!!)

            return true
        }

        if (rotations.get().equals("New", ignoreCase = true)){
            if (maxTurnSpeed.get() <= 0F)
                return true

            if (predictValue.get())
                boundingBox = boundingBox.offset(
                        (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                        (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                        (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
                )

            val (vec, rotation) = RotationUtils.searchCenter(
                    boundingBox,
                    outborderValue.get() && !attackTimer.hasTimePassed(attackDelay / 2),
                    randomCenterValue.get(),
                    predictValue.get(),
                    mc.thePlayer!!.getDistanceToEntityBox(entity) < throughWallsRangeValue.get(),
                    maxRange
            ) ?: return false

            val limitedRotation =  RotationUtils.limitAngleChange(RotationUtils.serverRotation, RotationUtils.getNewRotations(RotationUtils.getCenter(entity.entityBoundingBox),false),(Math.random() * (maxTurnSpeed.get() - minTurnSpeed.get()) + minTurnSpeed.get()).toFloat())

            if (silentRotationValue.get())
                RotationUtils.setTargetRotation(limitedRotation, if (aacValue.get()) 15 else 0)
            else
                limitedRotation.toPlayer(mc.thePlayer!!)

            return true
        }

        if (rotations.get().equals("BackTrack", ignoreCase = true)) {
            if (predictValue.get())
                boundingBox = boundingBox.offset(
                        (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                        (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                        (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
                )


            val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation,
                    RotationUtils.OtherRotation(boundingBox,RotationUtils.getCenter(entity.entityBoundingBox), predictValue.get(),
                            mc.thePlayer!!.getDistanceToEntityBox(entity) < throughWallsRangeValue.get(),maxRange), (Math.random() * (maxTurnSpeed.get() - minTurnSpeed.get()) + minTurnSpeed.get()).toFloat())

            if (silentRotationValue.get()) {
                RotationUtils.setTargetRotation(limitedRotation, if (aacValue.get()) 15 else 0)
            }else {
                limitedRotation.toPlayer(mc.thePlayer!!)
                return true
            }
        }
        return true
    }

    /**
     * Handle entity move
     */
    @EventTarget
    fun onEntityMove(event: EntityMovementEvent) {
        val movedEntity = event.movedEntity

        if (target == null || movedEntity != currentTarget)
            return

        updateHitable()
    }

    /**
     * Attack enemy
     */

    private fun runAttack() {
        target ?: return
        currentTarget ?: return
        val thePlayer = mc.thePlayer ?: return
        val theWorld = mc.theWorld ?: return

        // Settings
        val failRate = failRateValue.get()
        val swing = swingValue.get()
        val multi = targetModeValue.get().equals("Multi", ignoreCase = true)
        val openInventory = aacValue.get() && classProvider.isGuiContainer(mc.currentScreen)
        val failHit = failRate > 0 && Random().nextInt(100) <= failRate


        // Close inventory when open
        if (openInventory)
            mc.netHandler.addToSendQueue(classProvider.createCPacketCloseWindow())

        // Check is not hitable or check failrate

        if (!hitable || failHit) {
            if (swing && (fakeSwingValue.get() || failHit))
                thePlayer.swingItem()
        } else {
            // Attack
            if (!multi) {
                attackEntity(currentTarget!!)
            } else {
                var targets = 0

                for (entity in theWorld.loadedEntityList) {
                    val distance = thePlayer.getDistanceToEntityBox(entity)

                    if (classProvider.isEntityLivingBase(entity) && isEnemy(entity) && distance <= getRange(entity)) {
                        attackEntity(entity.asEntityLivingBase())

                        targets += 1

                        if (limitedMultiTargetsValue.get() != 0 && limitedMultiTargetsValue.get() <= targets)
                            break
                    }
                }
            }

            if(switchTimer.hasTimePassed(switchDelayValue.get().toLong()) || targetModeValue.get() != "Switch") {
                prevTargetEntities.add(if (aacValue.get()) target!!.entityId else currentTarget!!.entityId)
                switchTimer.reset()
            }

            prevTargetEntities.add(if (aacValue.get()) target!!.entityId else currentTarget!!.entityId)

            if (target == currentTarget)
                target = null


        }

        // Open inventory
        if (openInventory)
            mc.netHandler.addToSendQueue(createOpenInventoryPacket())
    }

    /**
     * Update current target
     */
    private fun updateTarget() {
        // Reset fixed target to null
        target = null

        // Settings
        val hurtTime = hurtTimeValue.get()
        val fov = fovValue.get()
        val switchMode = targetModeValue.get().equals("Switch", ignoreCase = true)

        // Find possible targets
        val targets = mutableListOf<IEntityLivingBase>()

        val theWorld = mc.theWorld!!
        val thePlayer = mc.thePlayer!!

        for (entity in theWorld.loadedEntityList) {
            if (!classProvider.isEntityLivingBase(entity) || !isEnemy(entity) || (switchMode && prevTargetEntities.contains(entity.entityId)))
                continue

            val distance = thePlayer.getDistanceToEntityBox(entity)
            val entityFov = RotationUtils.getRotationDifference(entity)

            if (distance <= maxRange && (fov == 180F || entityFov <= fov) && entity.asEntityLivingBase().hurtTime <= hurtTime)
                targets.add(entity.asEntityLivingBase())
        }

        // Sort targets by priority
        when (priorityValue.get().toLowerCase()) {
            "distance" -> targets.sortBy { thePlayer.getDistanceToEntityBox(it) } // Sort by distance
            "health" -> targets.sortBy { it.health } // Sort by health
            "direction" -> targets.sortBy { RotationUtils.getRotationDifference(it) } // Sort by FOV
            "livingtime" -> targets.sortBy { -it.ticksExisted } // Sort by existence
        }

        // Find best target
        for (entity in targets) {
            // Update rotations to current target
            if (!updateRotations(entity)) // when failed then try another target
                continue

            // Set target to current entity
            target = entity
            return
        }

        // Cleanup last targets when no target found and try again
        if (prevTargetEntities.isNotEmpty()) {
            prevTargetEntities.clear()
            updateTarget()
        }
    }

    /**
     * Check if [entity] is selected as enemy with current target options and other modules
     */
    private fun isEnemy(entity: IEntity?): Boolean {
        if (classProvider.isEntityLivingBase(entity) && entity != null && (EntityUtils.targetDead || isAlive(entity.asEntityLivingBase())) && entity != mc.thePlayer) {
            if (!EntityUtils.targetInvisible && entity.invisible)
                return false

            if (EntityUtils.targetPlayer && classProvider.isEntityPlayer(entity)) {
                val player = entity.asEntityPlayer()

                if (player.spectator || AntiBot.isBot(player))
                    return false

                if (player.isClientFriend() && !LiquidBounce.moduleManager[NoFriends::class.java].state)
                    return false

                val teams = LiquidBounce.moduleManager[Teams::class.java] as Teams

                return !teams.state || !teams.isInYourTeam(entity.asEntityLivingBase())
            }

            return EntityUtils.targetMobs && entity.isMob() || EntityUtils.targetAnimals && entity.isAnimal()
        }

        return false
    }

    /**
     * Attack [entity]
     */
    private fun attackEntity(entity: IEntityLivingBase) {
        // Stop blocking
        val thePlayer = mc.thePlayer!!

        if (thePlayer.isBlocking || blockingStatus)
            stopBlocking()

        // Call attack event
        LiquidBounce.eventManager.callEvent(AttackEvent(entity))

        // Attack target
        if (swingValue.get() && Backend.MINECRAFT_VERSION_MINOR == 8)
            thePlayer.swingItem()

        mc.netHandler.addToSendQueue(classProvider.createCPacketUseEntity(entity, ICPacketUseEntity.WAction.ATTACK))

        if (swingValue.get() && Backend.MINECRAFT_VERSION_MINOR != 8)
            thePlayer.swingItem()

        if (keepSprintValue.get()) {
            // Critical Effect
            if (thePlayer.fallDistance > 0F && !thePlayer.onGround && !thePlayer.isOnLadder &&
                    !thePlayer.isInWater && !thePlayer.isPotionActive(classProvider.getPotionEnum(PotionType.BLINDNESS)) && !thePlayer.isRiding)
                thePlayer.onCriticalHit(entity)

            // Enchant Effect
            if (functions.getModifierForCreature(thePlayer.heldItem, entity.creatureAttribute) > 0F)
                thePlayer.onEnchantmentCritical(entity)
        } else {
            if (mc.playerController.currentGameType != IWorldSettings.WGameType.SPECTATOR)
                thePlayer.attackTargetEntityWithCurrentItem(entity)
        }

        // Extra critical effects
        val criticals = LiquidBounce.moduleManager[Criticals::class.java] as Criticals

        for (i in 0..2) {
            // Critical Effect
            if (thePlayer.fallDistance > 0F && !thePlayer.onGround && !thePlayer.isOnLadder && !thePlayer.isInWater && !thePlayer.isPotionActive(classProvider.getPotionEnum(PotionType.BLINDNESS)) && thePlayer.ridingEntity == null || criticals.state && criticals.msTimer.hasTimePassed(criticals.delayValue.get().toLong()) && !thePlayer.isInWater && !thePlayer.isInLava && !thePlayer.isInWeb)
                thePlayer.onCriticalHit(target!!)

            // Enchant Effect
            if (functions.getModifierForCreature(thePlayer.heldItem, target!!.creatureAttribute) > 0.0f || fakeSharpValue.get())
                thePlayer.onEnchantmentCritical(target!!)
        }

        // Start blocking after attack
        if (autoBlockValue.get().equals("Packet", true) && (thePlayer.isBlocking || canBlock))
            startBlocking(entity, interactAutoBlockValue.get())

        @Suppress("ConstantConditionIf")
        if (Backend.MINECRAFT_VERSION_MINOR != 8) {
            thePlayer.resetCooldown()
        }
    }

    /**
     * Update killaura rotations to enemy
     */


    /**
     * Check if enemy is hitable with current rotations
     */
    private fun updateHitable() {
        // Disable hitable check if turn speed is zero
        if (maxTurnSpeed.get() <= 0F) {
            hitable = true
            return
        }

        val reach = min(maxRange.toDouble(), mc.thePlayer!!.getDistanceToEntityBox(target!!)) + 1

        if (raycastValue.get()) {
            val raycastedEntity = RaycastUtils.raycastEntity(reach, object : RaycastUtils.EntityFilter {
                override fun canRaycast(entity: IEntity?): Boolean {
                    return (!livingRaycastValue.get() || (classProvider.isEntityLivingBase(entity) && !classProvider.isEntityArmorStand(entity))) &&
                            (isEnemy(entity) || raycastIgnoredValue.get() || aacValue.get() && mc.theWorld!!.getEntitiesWithinAABBExcludingEntity(entity, entity!!.entityBoundingBox).isNotEmpty())
                }

            })

            if (raycastValue.get() && raycastedEntity != null && classProvider.isEntityLivingBase(raycastedEntity)
                    && (LiquidBounce.moduleManager[NoFriends::class.java].state || !(classProvider.isEntityPlayer(raycastedEntity) && raycastedEntity.asEntityPlayer().isClientFriend())))
                currentTarget = raycastedEntity.asEntityLivingBase()

            hitable = if (maxTurnSpeed.get() > 0F) currentTarget == raycastedEntity else true
        } else
            hitable = RotationUtils.isFaced(currentTarget, reach)
    }

    /**
     * Start blocking
     */
    private fun startBlocking(interactEntity: IEntity, interact: Boolean) {
        if (!(blockRate.get() > 0 && Random().nextInt(100) <= blockRate.get()))
            return

        if (interact) {
            val positionEye = mc.renderViewEntity?.getPositionEyes(1F)

            val expandSize = interactEntity.collisionBorderSize.toDouble()
            val boundingBox = interactEntity.entityBoundingBox.expand(expandSize, expandSize, expandSize)

            val (yaw, pitch) = RotationUtils.targetRotation ?: Rotation(mc.thePlayer!!.rotationYaw, mc.thePlayer!!.rotationPitch)
            val yawCos = cos(-yaw * 0.017453292F - Math.PI.toFloat())
            val yawSin = sin(-yaw * 0.017453292F - Math.PI.toFloat())
            val pitchCos = -cos(-pitch * 0.017453292F)
            val pitchSin = sin(-pitch * 0.017453292F)
            val range = min(maxRange.toDouble(), mc.thePlayer!!.getDistanceToEntityBox(interactEntity)) + 1
            val lookAt = positionEye!!.addVector(yawSin * pitchCos * range, pitchSin * range, yawCos * pitchCos * range)

            val movingObject = boundingBox.calculateIntercept(positionEye, lookAt) ?: return
            val hitVec = movingObject.hitVec

            mc.netHandler.addToSendQueue(classProvider.createCPacketUseEntity(interactEntity, WVec3(
                    hitVec.xCoord - interactEntity.posX,
                    hitVec.yCoord - interactEntity.posY,
                    hitVec.zCoord - interactEntity.posZ)
            ))
            mc.netHandler.addToSendQueue(classProvider.createCPacketUseEntity(interactEntity, ICPacketUseEntity.WAction.INTERACT))
        }
        if (LiquidBounce.moduleManager[OldHitting::class.java].state) {
            mc.netHandler.addToSendQueue(createUseItemPacket(mc.thePlayer!!.inventory.getCurrentItemInHand(), WEnumHand.OFF_HAND))
        } else {
            mc.netHandler.addToSendQueue(createUseItemPacket(mc.thePlayer!!.inventory.getCurrentItemInHand(), WEnumHand.MAIN_HAND))
            mc.netHandler.addToSendQueue(createUseItemPacket(mc.thePlayer!!.inventory.getCurrentItemInHand(), WEnumHand.OFF_HAND))
        }
        blockingStatus = true
    }


    /**
     * Stop blocking
     */
    private fun stopBlocking() {
        if (blockingStatus) {
            mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerDigging(ICPacketPlayerDigging.WAction.RELEASE_USE_ITEM, WBlockPos.ORIGIN, classProvider.getEnumFacing(EnumFacingType.DOWN)))
            blockingStatus = false
        }
    }

    /**
     * Check if run should be cancelled
     */
    private val cancelRun: Boolean
        inline get() = mc.thePlayer!!.spectator || !isAlive(mc.thePlayer!!)
                || LiquidBounce.moduleManager[Blink::class.java].state || LiquidBounce.moduleManager[FreeCam::class.java].state

    /**
     * Check if [entity] is alive
     */
    private fun isAlive(entity: IEntityLivingBase) = entity.entityAlive && entity.health > 0 ||
            aacValue.get() && entity.hurtTime > 5

    /**
     * Check if player is able to block
     */
    private val canBlock: Boolean
        inline get() = mc.thePlayer!!.heldItem != null && classProvider.isItemSword(mc.thePlayer!!.heldItem!!.item)

    /**
     * Range
     */
    private val maxRange: Float
        get() = max(rangeValue.get(), throughWallsRangeValue.get())

    private fun getRange(entity: IEntity) =
            (if (mc.thePlayer!!.getDistanceToEntityBox(entity) >= throughWallsRangeValue.get()) rangeValue.get() else throughWallsRangeValue.get()) - if (mc.thePlayer!!.sprinting) rangeSprintReducementValue.get() else 0F

    /**
     * HUD Tag
     */
    override val tag: String?
        get() = targetModeValue.get()

    val isBlockingChestAura: Boolean
        get() = state && target != null

    companion object {
        lateinit var target: Any
    }
}