/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */

package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.api.enums.EnumFacingType
import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketEntityAction
import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketUseEntity
import net.ccbluex.liquidbounce.api.minecraft.potion.PotionType
import net.ccbluex.liquidbounce.api.minecraft.util.IMovementInput
import net.ccbluex.liquidbounce.api.minecraft.util.IMovingObjectPosition
import net.ccbluex.liquidbounce.api.minecraft.util.WBlockPos
import net.ccbluex.liquidbounce.api.minecraft.util.WMathHelper.wrapAngleTo180_float
import net.ccbluex.liquidbounce.api.minecraft.util.WVec3
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.render.BlockOverlay
import net.ccbluex.liquidbounce.injection.backend.GlStateManagerImpl
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.block.BlockUtils.canBeClicked
import net.ccbluex.liquidbounce.utils.block.BlockUtils.isReplaceable
import net.ccbluex.liquidbounce.utils.block.PlaceInfo
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.Render
import net.minecraft.item.ItemStack
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.*
import net.minecraft.util.math.MathHelper

import net.minecraft.util.MovementInput





@ModuleInfo(
        name = "HytScaffold",
        description = "Automatically places blocks beneath your feet.",
        category = ModuleCategory.WORLD
)
class HytScaffold : Module() {

    private var keepYaw = 0.0F
    private var jumpTicks = 0;

    private val modeValue = ListValue("Mode", arrayOf("Normal", "Expand"), "Normal")

    // Delay
    private val maxDelayValue: IntegerValue = object : IntegerValue("Max Delay", 0, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val minDelay = minDelayValue.get()
            if (minDelay > newValue) {
                set(minDelay)
            }
        }
    }

    private val minDelayValue: IntegerValue = object : IntegerValue("Min Delay", 0, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val maxDelay = maxDelayValue.get()
            if (maxDelay < newValue) {
                set(maxDelay)
            }
        }
    }

    // Placeable delay
    private val placeDelay = BoolValue("Place Delay", true)
    private val delayOnlyWhenSlashValue = BoolValue("Delay Only When Slash", true)

    // Autoblock
    private val autoBlockValue = ListValue("Auto Block", arrayOf("Off", "Pick", "Switch"), "Switch")

    // Basic stuff
    @JvmField
    val sprintValue = BoolValue("Sprint", false)
    private val swingValue = BoolValue("Swing", true)
    private val searchValue = BoolValue("Search", true)
    private val downValue = BoolValue("Down", true)
    private val placeModeValue = ListValue("Place Timing", arrayOf("Pre", "Post", "Tick"), "Post")

    // Expand
    private val omniDirectionalExpand = BoolValue("Omni Directional Expand", false)
    private val expandLengthValue = IntegerValue("Expand Length", 1, 1, 6)
    private val angleStepValue = IntegerValue("Angle Step", 60, 0, 180)

    // Rotation Options
    private val strafeMode = ListValue("Strafe", arrayOf("Off", "AAC"), "Off")
    private val rotationsValue = BoolValue("Rotations", false)

    private val keepPitch = IntegerValue("Keep Pitch", 87, 0, 90)

    private val rotModeValue = ListValue("Keep Rot Mode", arrayOf("Normal", "AAC", "Backward"), "AAC")
    private val stepValue = BoolValue("Use Angle Step", true)
    private val silentRotationValue = BoolValue("Silent Rotation", true)
    private val keepRotationValue = BoolValue("Keep Rotation", true)
    private val keepLengthValue = IntegerValue("Keep Rotation Length", 0, 0, 20)

    // XZ/Y range
    private val searchMode = ListValue("XYZ Search", arrayOf("Auto", "Center", "Manual"), "Center")

    //Manual Mode
    private val xzRangeValue = FloatValue("XZ Range", 0.8f, 0f, 1f)
    private var yRangeValue = FloatValue("Y Range", 0.8f, 0f, 1f)
    private val minDistValue = FloatValue("Min Dist", 0.0f, 0.0f, 0.2f)

    // Search Accuracy
    private val searchAccuracyValue: IntegerValue = object : IntegerValue("Search Accuracy", 8, 1, 16) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            if (maximum < newValue) {
                set(maximum)
            } else if (minimum > newValue) {
                set(minimum)
            }
        }
    }

    // Turn Speed
    private val maxTurnSpeedValue: FloatValue = object : FloatValue("Max Turn Speed", 180f, 1f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minTurnSpeedValue.get()
            if (v > newValue) set(v)
            if (maximum < newValue) {
                set(maximum)
            } else if (minimum > newValue) {
                set(minimum)
            }
        }
    }
    private val minTurnSpeedValue: FloatValue = object : FloatValue("Min Turn Speed", 180f, 1f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxTurnSpeedValue.get()
            if (v < newValue) set(v)
            if (maximum < newValue) {
                set(maximum)
            } else if (minimum > newValue) {
                set(minimum)
            }
        }
    }

    // Game
    private val timerValue = FloatValue("Timer", 1f, 0.1f, 10f)
    private val jumpChecker = BoolValue("Timer Jump Checker", false)
    private val speedModifierValue = FloatValue("Speed Modifier", 1f, 0f, 2f)
    private val slowValue = BoolValue("Slow", false)
    private val slowSpeed = FloatValue("Slow Speed", 0.6f, 0.2f, 1.0f)

    // Safety
    private val sameYValue = BoolValue("Same Y", false)
    private val safeWalkValue = BoolValue("Safe Walk", true)
    private val airSafeValue = BoolValue("Air Safe", false)

    // Visuals
    private val counterDisplayValue = BoolValue("Counter", true)
    private val markValue = BoolValue("Mark", false)

    // Target block
    private var targetPlace: PlaceInfo? = null

    // Rotation lock
    private var lockRotation: Rotation? = null
    private var lockRotationTimer = TickTimer()

    // Launch position
    private var launchY = 0
    private var facesBlock = false

    // AutoBlock
    private var slot = 0

    // Delay
    private val delayTimer = MSTimer()
    private var delay = 0L

    // Downwards
    private var shouldGoDown = false

    var tempNum = 0

    // Enabling module
    override fun onEnable() {
        this.jumpTicks = 0

        val player = mc.thePlayer ?: return

        launchY = player.posY.roundToInt()
        slot = player.inventory.currentItem

        this.tempNum = player.inventory.currentItem

        facesBlock = false
    }

    // Events
    @EventTarget
    private fun onUpdate(event: UpdateEvent) {
        val player = mc.thePlayer ?: return

        if (this.sameYValue.get()) {
            if (mc.thePlayer!!.onGround
                    || !MovementUtils.isMoving) {
                this.launchY = mc.thePlayer!!.posY.toInt()
            }
        }


        if (mc.gameSettings.keyBindJump.pressed && this.jumpChecker.get())
            mc.timer.timerSpeed = 1.0F
        else
            mc.timer.timerSpeed = timerValue.get()
        shouldGoDown =
                downValue.get() && !sameYValue.get() && mc.gameSettings.isKeyDown(mc.gameSettings.keyBindSneak) && blocksAmount > 1
        if (shouldGoDown) {
            mc.gameSettings.keyBindSneak.pressed = false
        }

        if (slowValue.get() && (mc.thePlayer!!.onGround && !mc.gameSettings.keyBindJump.pressed)) {
            player.motionX = player.motionX * slowSpeed.get()
            player.motionZ = player.motionZ * slowSpeed.get()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null) {
            return
        }
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (strafeMode.get().equals("Off", true)) {
            return
        }

        update()
        val rotation = lockRotation ?: return

        if (rotationsValue.get() && (keepRotationValue.get() || !lockRotationTimer.hasTimePassed(keepLengthValue.get()))) {
            if (targetPlace == null) {
                rotation.yaw = wrapAngleTo180_float((rotation.yaw / 45f).roundToInt() * 45f)
            }
            setRotation(rotation)
            lockRotationTimer.update()

            rotation.applyStrafeToPlayer(event)
            event.cancelEvent()
            return
        }

        val targetRotation = RotationUtils.targetRotation ?: return
        targetRotation.applyStrafeToPlayer(event)
        event.cancelEvent()
    }

    //From old Novoline, LMAO.
    fun getYawBackward(): Float {
        var yaw: Float = wrapAngleTo180_float(mc.thePlayer!!.rotationYaw)
        val input: IMovementInput = mc.thePlayer!!.movementInput
        val strafe = input.moveStrafe
        val forward = input.moveForward
        if (forward != 0f) {
            if (strafe < 0) {
                yaw += if (forward < 0) 135F else 45F
            } else if (strafe > 0) {
                yaw -= if (forward < 0) 135F else 45F
            } else if (strafe == 0f && forward < 0) {
                yaw -= 180f
            }
        } else {
            if (strafe < 0) {
                yaw += 90f
            } else if (strafe > 0) {
                yaw -= 90f
            }
        }
        return wrapAngleTo180_float(yaw - 180)
    }

    @EventTarget
    fun onTick(event: TickEvent) {
        if ((facesBlock || !rotationsValue.get()) && (placeModeValue.get().equals("tick", true))) {
            place()
        }
    }
    @EventTarget
    fun onMotion(event: MotionEvent) {


        if (targetPlace != null) {
            if (targetPlace!!.enumFacing.unwrap().name.equals("north", true)) {
                this.keepYaw = 0.0F
            } else if (targetPlace!!.enumFacing.unwrap().name.equals("south", true)) {
                this.keepYaw = 180.0F
            } else if (targetPlace!!.enumFacing.unwrap().name.equals("west", true)) {
                this.keepYaw = -90.0F
            } else if (targetPlace!!.enumFacing.unwrap().name.equals("east", true)) {
                this.keepYaw = 90.0F
            }
        }

        val eventState = event.eventState
        // Lock Rotation
        if (rotationsValue.get() && (keepRotationValue.get() || !lockRotationTimer.hasTimePassed(keepLengthValue.get())) && lockRotation != null && strafeMode.get()
                        .equals("Off", true)
        ) {
            if (this.stepValue.get())
                this.lockRotation!!.yaw =
                        Math.min(Math.abs(this.lockRotation!!.yaw), angleStepValue.get().toFloat()) * if (this.lockRotation!!.yaw < 0) -1 else 1

            setRotation(lockRotation!!)

            if (eventState == EventState.POST) {
                lockRotationTimer.update()
            }
        }

        if (this.rotModeValue.get().equals("AAC", true)) {
            RotationUtils.setTargetRotation(Rotation(mc.thePlayer!!.rotationYaw - (mc.thePlayer!!.rotationYaw - keepYaw), keepPitch.get().toFloat()))
        }

        // Face block
        if ((facesBlock || !rotationsValue.get()) && (placeModeValue.get().equals(eventState.stateName, true))) {
            place()
        }

        // Update and search for a new block
        if (eventState == EventState.PRE && strafeMode.get().equals("Off", true)) {
            update()
        }

        // Reset placeable delay
        if (targetPlace == null && needToDelay()) {
            delayTimer.reset()
        }
    }

    fun update() {
        val player = mc.thePlayer ?: return

        val holdingItem = player.heldItem != null && classProvider.isItemBlock(player.heldItem!!.item)
        if (if (!autoBlockValue.get()
                                .equals("off", true)
                ) InventoryUtils.findAutoBlockBlock() == -1 && !holdingItem else !holdingItem
        ) {
            return
        }

        findBlock(modeValue.get().equals("expand", true))
    }

    private fun setRotation(rotation: Rotation) {
        val player = mc.thePlayer ?: return

        if (this.sprintValue.get() && this.sprintValue.get())
            return

        if (silentRotationValue.get()) {

            if (this.rotModeValue.get().equals("Backward", true)) {
                RotationUtils.setTargetRotation(Rotation(this.getYawBackward(), keepPitch.get().toFloat()))

            } else RotationUtils.setTargetRotation(rotation, keepLengthValue.get())

        } else {
            player.rotationYaw = rotation.yaw
            player.rotationPitch = rotation.pitch;
        }
    }

    // Search for new target block
    private fun findBlock(expand: Boolean) {
        val player = mc.thePlayer ?: return

        val blockPosition = if (shouldGoDown) {
            (if (player.posY == player.posY.roundToInt() + 0.5) {
                WBlockPos(player.posX, player.posY - 0.6, player.posZ)
            } else {
                WBlockPos(player.posX, player.posY - 0.6, player.posZ).down()
            })
        } else (if (sameYValue.get() && launchY <= player.posY) {
            WBlockPos(player.posX, launchY - 1.0, player.posZ)
        } else (if (player.posY == player.posY.roundToInt() + 0.5) {
            WBlockPos(player)
        } else {
            WBlockPos(player.posX, player.posY, player.posZ).down()
        }))
        if (!expand && (!isReplaceable(blockPosition) || search(blockPosition, !shouldGoDown))) {
            return
        }

        if (expand) {
            val yaw = Math.toRadians(player.rotationYaw.toDouble())
            val x = if (omniDirectionalExpand.get()) -sin(yaw).roundToInt() else player.horizontalFacing.directionVec.x
            val z = if (omniDirectionalExpand.get()) cos(yaw).roundToInt() else player.horizontalFacing.directionVec.z
            for (i in 0 until expandLengthValue.get()) {
                if (search(blockPosition.add(x * i, 0, z * i), false)) {
                    return
                }
            }
        } else if (searchValue.get()) {
            for (x in -1..1) {
                for (z in -1..1) {
                    if (Math.abs(x) != Math.abs(z)) {
                        if (search(blockPosition.add(x, 0, z), !shouldGoDown)) {
                            return
                        }
                    }
                }
            }
        }
    }

    fun place() {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return

        if (targetPlace == null) {
            if (needToDelay()) {
                delayTimer.reset()
            }
            return
        }

        if ((!delayTimer.hasTimePassed(delay) && needToDelay())) {
            return
        }

        var itemStack = player.heldItem
        val oldCurrentItem = player.inventory.currentItem
        if (itemStack == null || !classProvider.isItemBlock(itemStack.item) || classProvider.isBlockBush(itemStack.item!!.asItemBlock().block) || player.heldItem!!.stackSize <= 0) {
            val blockSlot = InventoryUtils.findAutoBlockBlock()

            if (blockSlot == -1) {
                return
            }

            when (autoBlockValue.get().toLowerCase()) {
                "off" -> {
                    return
                }
                "pick" -> {
                    player.inventory.currentItem = blockSlot - 36
                    mc.playerController.updateController()
                }
                "spoof" -> {
                    // if (blockSlot - 36 != slot) {
                    mc.netHandler.addToSendQueue(classProvider.createCPacketHeldItemChange(blockSlot - 36))
                    //  }
                }
                "switch" -> {
                    if (blockSlot - 36 != slot) {
                        player.inventory.currentItem = blockSlot - 36
                        mc.netHandler.addToSendQueue(classProvider.createCPacketHeldItemChange(blockSlot - 36))
                    }
                }
            }
            itemStack = player.inventoryContainer.getSlot(blockSlot - 36).stack
        }
        val oldItemStack = player.unwrap().inventory.mainInventory[player.inventory.currentItem]
        if (mc.playerController.onPlayerRightClick(
                        player, world, itemStack, targetPlace!!.blockPos, targetPlace!!.enumFacing, targetPlace!!.vec3
                )
        ) {
            delayTimer.reset()
            delay = if (!needToDelay()) 0 else TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())

            if (player.onGround) {
                val modifier = speedModifierValue.get()
                player.motionX = player.motionX * modifier
                player.motionZ = player.motionZ * modifier
            }

            if (swingValue.get()) {
                player.swingItem()
            } else {
                mc.netHandler.addToSendQueue(classProvider.createCPacketAnimation())
            }
        }
        if (autoBlockValue.get().equals("Switch", true)) {
            player.inventory.currentItem = oldCurrentItem
            if (slot != player.inventory.currentItem) {
                mc.netHandler.addToSendQueue(classProvider.createCPacketHeldItemChange(player.inventory.currentItem))
            }
        }
        targetPlace = null
    }

    // Disabling module
    override fun onDisable() {
        val player = mc.thePlayer ?: return

        if (!mc.gameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            mc.gameSettings.keyBindSneak.pressed = false
        }
        if (!mc.gameSettings.isKeyDown(mc.gameSettings.keyBindRight)) {
            mc.gameSettings.keyBindRight.pressed = false
        }
        if (!mc.gameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) {
            mc.gameSettings.keyBindLeft.pressed = false
        }

        lockRotation = null
        facesBlock = false
        mc.timer.timerSpeed = 1f
        shouldGoDown = false

        if (this.autoBlockValue.get().equals("Pick")) {
            mc.thePlayer!!.inventory.currentItem = tempNum
        }
        if (slot != player.inventory.currentItem) {
            mc.netHandler.addToSendQueue(classProvider.createCPacketHeldItemChange(player.inventory.currentItem))
        }
    }

    // Entity movement event
    @EventTarget
    fun onMove(event: MoveEvent) {
        val player = mc.thePlayer ?: return

        if (!safeWalkValue.get() || shouldGoDown) {
            return
        }
        if (airSafeValue.get() || player.onGround) {
            event.isSafeWalk = true
        }
    }

    // Scaffold visuals
    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (counterDisplayValue.get()) {
            GL11.glPushMatrix()
            val blockOverlay = LiquidBounce.moduleManager.getModule(BlockOverlay::class.java) as BlockOverlay
            if (blockOverlay.state && blockOverlay.infoValue.get() && blockOverlay.currentBlock != null) {
                GL11.glTranslatef(0f, 15f, 0f)
            }
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            val info = "$blocksAmount"
            val scaledResolution = classProvider.createScaledResolution(mc)

            classProvider.getGlStateManager().resetColor()


            var color = -1
            if (blocksAmount >= 128) {
                color = Color(0,255,0,255).rgb
            } else if (blocksAmount >= 64)  {
                color = Color(255,255,0,255).rgb
            } else {
                color = Color(255,0,0,255).rgb
            }

            if (InventoryUtils.findAutoBlockBlock() - 36 >= 0) {
                var stack = mc.thePlayer!!.inventory.getStackInSlot(InventoryUtils.findAutoBlockBlock() - 36)
                if (stack != null) {
                    mc.renderItem.renderItemAndEffectIntoGUI(stack, scaledResolution.scaledWidth / 2 - 8,
                            scaledResolution.scaledHeight / 2 + 19)
                }
                GlStateManager.enableAlpha()
                GlStateManagerImpl.disableBlend()
                GlStateManager.enableTexture2D()
            }
            val haveNoBlock = InventoryUtils.findAutoBlockBlock() - 36 < 0

            Fonts.font40.drawCenteredString(
                    info,
                    scaledResolution.scaledWidth / 2.toFloat(),
                    scaledResolution.scaledHeight / 2 + (if (haveNoBlock) 21 else 37).toFloat(),
                    color, true
            )

            GL11.glPopMatrix()
        }
    }

    // Visuals
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val player = mc.thePlayer ?: return
        if (!markValue.get()) {
            return
        }

        for (i in 0 until if (modeValue.get().equals("Expand", true)) expandLengthValue.get() + 1 else 2) {
            val yaw = Math.toRadians(player.rotationYaw.toDouble())
            val x = if (omniDirectionalExpand.get()) -sin(yaw).roundToInt() else player.horizontalFacing.directionVec.x
            val z = if (omniDirectionalExpand.get()) cos(yaw).roundToInt() else player.horizontalFacing.directionVec.z
            val blockPos = WBlockPos(
                    player.posX + x * i,
                    //if (sameYValue.get() && !towering && launchY <= player.posY) launchY - 1.0 else player.posY - (if (player.posY == player.posY + 0.5) 0.0 else 1.0) - if (shouldGoDown) 1.0 else 0.0,
                    player.posY - (if (player.posY == player.posY + 0.5) 0.0 else 1.0) - if (shouldGoDown) 1.0 else 0.0,

                    player.posZ + z * i
            )
            val placeInfo = PlaceInfo.get(blockPos)
            if (isReplaceable(blockPos) && placeInfo != null) {
                RenderUtils.drawBlockBox(blockPos, Color(255,255,255,255), false)
                break
            }
        }
    }

    /**
     * Search for placeable block
     *
     * @param blockPosition pos
     * @param raycast visible
     * @return
     */

    private fun search(blockPosition: WBlockPos, raycast: Boolean): Boolean {
        facesBlock = false
        val player = mc.thePlayer ?: return false
        val world = mc.theWorld ?: return false

        if (!isReplaceable(blockPosition)) {
            return false
        }

        // Search Ranges
        val xzRV = xzRangeValue.get().toDouble()
        val xzSSV = calcStepSize(xzRV.toFloat())
        val yRV = yRangeValue.get().toDouble()
        val ySSV = calcStepSize(yRV.toFloat())
        val eyesPos = WVec3(player.posX, player.entityBoundingBox.minY + player.eyeHeight, player.posZ)
        var placeRotation: PlaceRotation? = null
        for (facingType in EnumFacingType.values()) {
            val side = classProvider.getEnumFacing(facingType)
            val neighbor = blockPosition.offset(side)
            if (!canBeClicked(neighbor)) {
                continue
            }
            val dirVec = WVec3(side.directionVec)
            val auto = searchMode.get().equals("Auto", true)
            val center = searchMode.get().equals("Center", true)
            var xSearch = if (auto) 0.1 else 0.5 - xzRV / 2
            while (xSearch <= if (auto) 0.9 else 0.5 + xzRV / 2) {
                var ySearch = if (auto) 0.1 else 0.5 - yRV / 2
                while (ySearch <= if (auto) 0.9 else 0.5 + yRV / 2) {
                    var zSearch = if (auto) 0.1 else 0.5 - xzRV / 2
                    while (zSearch <= if (auto) 0.9 else 0.5 + xzRV / 2) {
                        val posVec = WVec3(blockPosition).addVector(
                                if (center) 0.5 else xSearch, if (center) 0.5 else ySearch, if (center) 0.5 else zSearch
                        )
                        val distanceSqPosVec = eyesPos.squareDistanceTo(posVec)
                        val hitVec = posVec.add(WVec3(dirVec.xCoord * 0.5, dirVec.yCoord * 0.5, dirVec.zCoord * 0.5))
                        if (raycast && (eyesPos.distanceTo(hitVec) > 4.25 || distanceSqPosVec > eyesPos.squareDistanceTo(
                                        posVec.add(dirVec)
                                ) || world.rayTraceBlocks(
                                        eyesPos,
                                        hitVec,
                                        stopOnLiquid = false,
                                        ignoreBlockWithoutBoundingBox = true,
                                        returnLastUncollidableBlock = false
                                ) != null)
                        ) {
                            zSearch += if (auto) 0.1 else xzSSV
                            continue
                        }

                        // Face block
                        val diffX = hitVec.xCoord - eyesPos.xCoord
                        val diffY = hitVec.yCoord - eyesPos.yCoord
                        val diffZ = hitVec.zCoord - eyesPos.zCoord
                        val diffXZ = sqrt(diffX * diffX + diffZ * diffZ)
                        if (!side.isUp() && !side.isDown()) {
                            val diff = abs(if (side.isNorth() || side.isSouth()) diffZ else diffX)
                            if (diff < minDistValue.get()) {
                                zSearch += if (auto) 0.1 else xzSSV
                                continue
                            }
                        }
                        val rotation = Rotation(
                                wrapAngleTo180_float(Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f),
                                wrapAngleTo180_float(-Math.toDegrees(atan2(diffY, diffXZ)).toFloat())
                        )
                        val rotationVector = RotationUtils.getVectorForRotation(rotation)
                        val vector = eyesPos.addVector(
                                rotationVector.xCoord * 4.0, rotationVector.yCoord * 4.0, rotationVector.zCoord * 4.0
                        )

                        val obj = world.rayTraceBlocks(
                                eyesPos,
                                vector,
                                stopOnLiquid = false,
                                ignoreBlockWithoutBoundingBox = false,
                                returnLastUncollidableBlock = true
                        ) ?: continue

                        if (obj.typeOfHit != IMovingObjectPosition.WMovingObjectType.BLOCK || obj.blockPos != neighbor) {
                            zSearch += if (auto) 0.1 else xzSSV
                            continue
                        }
                        if (placeRotation == null || RotationUtils.getRotationDifference(rotation) < RotationUtils.getRotationDifference(
                                        placeRotation.rotation
                                )
                        ) {
                            placeRotation = PlaceRotation(PlaceInfo(neighbor, side.opposite, hitVec), rotation)
                        }

                        zSearch += if (auto) 0.1 else xzSSV
                    }
                    ySearch += if (auto) 0.1 else ySSV
                }
                xSearch += if (auto) 0.1 else xzSSV
            }
        }
        if (placeRotation == null) {
            return false
        }
        if (rotationsValue.get()) {
            if (minTurnSpeedValue.get() < 180) {

                var diffAngle = RotationUtils.getRotationDifference(RotationUtils.serverRotation, placeRotation.rotation)
                if (diffAngle <0) diffAngle = -diffAngle
                if (diffAngle> 180.0) diffAngle = 180.0

                val calculateSpeed = (diffAngle / 180) * maxTurnSpeedValue.get() + (1 - diffAngle / 180) * minTurnSpeedValue.get()

                val limitedRotation = RotationUtils.limitAngleChange(
                        RotationUtils.serverRotation,
                        placeRotation.rotation,
                        calculateSpeed.toFloat()
                )

                if ((10 * wrapAngleTo180_float(limitedRotation.yaw)).roundToInt() == (10 * wrapAngleTo180_float(
                                placeRotation.rotation.yaw
                        )).roundToInt() && (10 * wrapAngleTo180_float(limitedRotation.pitch)).roundToInt() == (10 * wrapAngleTo180_float(
                                placeRotation.rotation.pitch
                        )).roundToInt()
                ) {
                    setRotation(placeRotation.rotation)
                    lockRotation = placeRotation.rotation
                    facesBlock = true
                } else {
                    setRotation(limitedRotation)
                    lockRotation = limitedRotation
                    facesBlock = false
                }
            } else {
                setRotation(placeRotation.rotation)
                lockRotation = placeRotation.rotation
                facesBlock = true
            }
            lockRotationTimer.reset()
        }
        targetPlace = placeRotation.placeInfo
        return true
    }

    private fun needToDelay(): Boolean {
        if (placeDelay.get() && delayOnlyWhenSlashValue.get() && (!MovementUtils.isMoving || MovementUtils.speed == 0F)) {
            return false
        }
        return placeDelay.get() && (!delayOnlyWhenSlashValue.get() || abs(abs(mc.thePlayer!!.motionX) - abs(mc.thePlayer!!.motionZ)) / MovementUtils.speed * 1000.0 <= 800.0)
    }

    private fun calcStepSize(range: Float): Double {
        var accuracy = searchAccuracyValue.get().toDouble()
        accuracy += accuracy % 2 // If it is set to uneven it changes it to even. Fixes a bug
        return if (range / accuracy < 0.01) 0.01 else (range / accuracy)
    }

    @EventTarget
    private fun onJump(event: JumpEvent) {

    }

    // Return hotbar amount
    private val blocksAmount: Int
        get() {
            var amount = 0
            for (i in 36..44) {
                val itemStack = mc.thePlayer!!.inventoryContainer.getSlot(i).stack
                if (itemStack != null && classProvider.isItemBlock(itemStack.item)) {
                    val block = (itemStack.item!!.asItemBlock()).block
                    val heldItem = mc.thePlayer!!.heldItem
                    if (heldItem != null && heldItem == itemStack || !InventoryUtils.BLOCK_BLACKLIST.contains(block) && !classProvider.isBlockBush(
                                    block
                            )
                    ) {
                        amount += itemStack.stackSize
                    }
                }
            }
            return amount
        }
    override val tag: String
        get() = rotModeValue.get().toString()
}