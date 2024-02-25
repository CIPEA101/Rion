package net.ccbluex.liquidbounce.features.module.modules.tomk;

import kotlin.Metadata;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.api.IClassProvider;
import net.ccbluex.liquidbounce.api.minecraft.client.block.IBlock;
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntity;
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntityPlayerSP;
import net.ccbluex.liquidbounce.api.minecraft.client.multiplayer.IWorldClient;
import net.ccbluex.liquidbounce.api.minecraft.client.network.IINetHandlerPlayClient;
import net.ccbluex.liquidbounce.api.minecraft.network.IPacket;
import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketEntityAction.WAction;
import net.ccbluex.liquidbounce.api.minecraft.network.play.server.ISPacketEntityVelocity;
import net.ccbluex.liquidbounce.api.minecraft.potion.PotionType;
import net.ccbluex.liquidbounce.event.BlockBBEvent;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.JumpEvent;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed;
import net.ccbluex.liquidbounce.injection.backend.PacketImpl;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.timer.MSTimer;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ModuleInfo(
        name = "Velocity2",
        description = "Rion Teams",
        category = ModuleCategory.COMBAT
)
@Metadata(
        mv = {1, 1, 16},
        bv = {1, 0, 3},
        k = 1,
        d1 = {"\u0000l\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010.\u001a\u00020/2\u0006\u00100\u001a\u000201H\u0007J\b\u00102\u001a\u00020/H\u0016J\u0010\u00103\u001a\u00020/2\u0006\u00100\u001a\u000204H\u0007J\u0010\u00105\u001a\u00020/2\u0006\u00100\u001a\u000206H\u0007J\u0010\u00107\u001a\u00020/2\u0006\u00100\u001a\u000208H\u0007R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\t\u0010\n\"\u0004\b\u000b\u0010\fR\u000e\u0010\r\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001b\u001a\u00020\u001cX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001d\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001e\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001f\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010 \u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010!\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\"\u001a\u00020#8VX\u0096\u0004\u00a2\u0006\u0006\u001a\u0004\b$\u0010%R\u000e\u0010&\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010'\u001a\u00020(X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010)\u001a\u00020*X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010+\u001a\u00020,X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010-\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00069"},
        d2 = {"Lnet/ccbluex/liquidbounce/features/module/modules/combat/Velocity;", "Lnet/ccbluex/liquidbounce/features/module/Module;", "()V", "aacPushXZReducerValue", "Lnet/ccbluex/liquidbounce/value/FloatValue;", "aacPushYReducerValue", "Lnet/ccbluex/liquidbounce/value/BoolValue;", "block", "Lnet/ccbluex/liquidbounce/api/minecraft/client/block/IBlock;", "getBlock", "()Lnet/ccbluex/liquidbounce/api/minecraft/client/block/IBlock;", "setBlock", "(Lnet/ccbluex/liquidbounce/api/minecraft/client/block/IBlock;)V", "canCancelJump", "", "canCleanJump", "customC06FakeLag", "customX", "customY", "customYStart", "customZ", "horizontalValue", "huayutingjumpflag", "hytGround", "hytpacketaset", "hytpacketbset", "jump", "modeValue", "Lnet/ccbluex/liquidbounce/value/ListValue;", "newaac4XZReducerValue", "noFireValue", "reverse2StrengthValue", "reverseHurt", "reverseStrengthValue", "tag", "", "getTag", "()Ljava/lang/String;", "velocityInput", "velocityTick", "", "velocityTickValue", "Lnet/ccbluex/liquidbounce/value/IntegerValue;", "velocityTimer", "Lnet/ccbluex/liquidbounce/utils/timer/MSTimer;", "verticalValue", "onBlockBB", "", "event", "Lnet/ccbluex/liquidbounce/event/BlockBBEvent;", "onDisable", "onJump", "Lnet/ccbluex/liquidbounce/event/JumpEvent;", "onPacket", "Lnet/ccbluex/liquidbounce/event/PacketEvent;", "onUpdate", "Lnet/ccbluex/liquidbounce/event/UpdateEvent;", "LiquidSense"}
)
public final class Velocity2 extends Module {
    private final FloatValue horizontalValue = new FloatValue("Horizontal", 0.0F, 0.0F, 1.0F);
    private final FloatValue verticalValue = new FloatValue("Vertical", 0.0F, 0.0F, 1.0F);
    private final ListValue modeValue = new ListValue("Mode", new String[]{"HytBest", "HuaYuTingJump", "Custom", "AAC4", "Simple", "SimpleFix", "AAC", "AACPush", "AACZero", "Reverse", "SmoothReverse", "Jump", "AAC5Reduce", "HytPacketA", "Glitch", "HytCancel", "HytTick", "Vanilla", "HytTest", "HytNewTest", "HytPacket", "NewAAC4", "Hyt", "FeiLe", "HytMotion", "NewHytMotion", "HytPacketB", "HytMotionB", "HytPacketFix", "victorFix", "Victor", "hytnew"}, "Vanilla");
    private final FloatValue newaac4XZReducerValue = new FloatValue("NewAAC4XZReducer", 0.45F, 0.0F, 1.0F);
    private final IntegerValue velocityTickValue = new IntegerValue("VelocityTick", 1, 0, 10);
    private final FloatValue reverseStrengthValue = new FloatValue("ReverseStrength", 1.0F, 0.1F, 1.0F);
    private final FloatValue reverse2StrengthValue = new FloatValue("SmoothReverseStrength", 0.05F, 0.02F, 0.1F);
    private final FloatValue hytpacketaset = new FloatValue("HytPacketASet", 0.35F, 0.1F, 1.0F);
    private final FloatValue hytpacketbset = new FloatValue("HytPacketBSet", 0.5F, 1.0F, 1.0F);
    private final FloatValue aacPushXZReducerValue = new FloatValue("AACPushXZReducer", 2.0F, 1.0F, 3.0F);
    private final BoolValue aacPushYReducerValue = new BoolValue("AACPushYReducer", true);
    @Nullable
    private IBlock block;
    private final BoolValue noFireValue = new BoolValue("noFire", false);
    private final BoolValue hytGround = new BoolValue("HytOnlyGround", true);
    private final FloatValue customX = new FloatValue("CustomX", 0.0F, 0.0F, 1.0F);
    private final BoolValue customYStart = new BoolValue("CanCustomY", false);
    private final FloatValue customY = new FloatValue("CustomY", 1.0F, 1.0F, 2.0F);
    private final FloatValue customZ = new FloatValue("CustomZ", 0.0F, 0.0F, 1.0F);
    private final BoolValue customC06FakeLag = new BoolValue("CustomC06FakeLag", false);
    private boolean huayutingjumpflag;
    private MSTimer velocityTimer = new MSTimer();
    private boolean velocityInput;
    private boolean canCleanJump;
    private int velocityTick;
    private boolean reverseHurt;
    private boolean jump;
    private boolean canCancelJump;

    @Nullable
    public final IBlock getBlock() {
        return this.block;
    }

    public final void setBlock(@Nullable IBlock var1) {
        this.block = var1;
    }

    @NotNull
    public String getTag() {
        return (String)this.modeValue.get();
    }

    public void onDisable() {
        IEntityPlayerSP var10000 = MinecraftInstance.mc.getThePlayer();
        if (var10000 != null) {
            var10000.setSpeedInAir(0.02F);
        }

    }

    @EventTarget
    public final void onUpdate(@NotNull UpdateEvent event) {
        Intrinsics.checkParameterIsNotNull(event, "event");
        IEntityPlayerSP var10000 = MinecraftInstance.mc.getThePlayer();
        if (var10000 != null) {
            IEntityPlayerSP thePlayer = var10000;
            if (!thePlayer.isInWater() && !thePlayer.isInLava() && !thePlayer.isInWeb()) {
                if ((Boolean)this.noFireValue.get()) {
                    var10000 = MinecraftInstance.mc.getThePlayer();
                    if (var10000 == null) {
                        Intrinsics.throwNpe();
                    }

                    if (var10000.isBurning()) {
                        return;
                    }
                }

                String var3 = (String)this.modeValue.get();
                boolean var4 = false;
                if (var3 == null) {
                    throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
                } else {
                    String var11 = var3.toLowerCase();
                    Intrinsics.checkExpressionValueIsNotNull(var11, "(this as java.lang.String).toLowerCase()");
                    var3 = var11;
                    float reduce;
                    IClassProvider var12;
                    IINetHandlerPlayClient var13;
                    IEntityPlayerSP var10002;
                    switch(var3.hashCode()) {
                        case -1970553484:
                            if (var3.equals("smoothreverse")) {
                                if (!this.velocityInput) {
                                    thePlayer.setSpeedInAir(0.02F);
                                    return;
                                }

                                if (thePlayer.getHurtTime() > 0) {
                                    this.reverseHurt = true;
                                }

                                if (!thePlayer.getOnGround()) {
                                    if (this.reverseHurt) {
                                        thePlayer.setSpeedInAir(((Number)this.reverse2StrengthValue.get()).floatValue());
                                    }
                                } else if (this.velocityTimer.hasTimePassed(80L)) {
                                    this.velocityInput = false;
                                    this.reverseHurt = false;
                                }
                            }
                            break;
                        case -1810282708:
                            if (var3.equals("huayutingjump")) {
                                var10000 = MinecraftInstance.mc.getThePlayer();
                                if (var10000 == null) {
                                    Intrinsics.throwNpe();
                                }

                                if (var10000.getHurtTime() > 0 && this.huayutingjumpflag) {
                                    var10000 = MinecraftInstance.mc.getThePlayer();
                                    if (var10000 == null) {
                                        Intrinsics.throwNpe();
                                    }

                                    if (var10000.getOnGround()) {
                                        var10000 = MinecraftInstance.mc.getThePlayer();
                                        if (var10000 == null) {
                                            Intrinsics.throwNpe();
                                        }

                                        if (var10000.getHurtTime() <= 6) {
                                            var10000 = MinecraftInstance.mc.getThePlayer();
                                            if (var10000 == null) {
                                                Intrinsics.throwNpe();
                                            }

                                            var10000.setMotionX(var10000.getMotionX() * 0.600151164D);
                                            var10000 = MinecraftInstance.mc.getThePlayer();
                                            if (var10000 == null) {
                                                Intrinsics.throwNpe();
                                            }

                                            var10000.setMotionZ(var10000.getMotionZ() * 0.600151164D);
                                        }

                                        var10000 = MinecraftInstance.mc.getThePlayer();
                                        if (var10000 == null) {
                                            Intrinsics.throwNpe();
                                        }

                                        if (var10000.getHurtTime() <= 4) {
                                            var10000 = MinecraftInstance.mc.getThePlayer();
                                            if (var10000 == null) {
                                                Intrinsics.throwNpe();
                                            }

                                            var10000.setMotionX(var10000.getMotionX() * 0.700151164D);
                                            var10000 = MinecraftInstance.mc.getThePlayer();
                                            if (var10000 == null) {
                                                Intrinsics.throwNpe();
                                            }

                                            var10000.setMotionZ(var10000.getMotionZ() * 0.700151164D);
                                        }
                                    } else {
                                        var10000 = MinecraftInstance.mc.getThePlayer();
                                        if (var10000 == null) {
                                            Intrinsics.throwNpe();
                                        }

                                        if (var10000.getHurtTime() <= 9) {
                                            var10000 = MinecraftInstance.mc.getThePlayer();
                                            if (var10000 == null) {
                                                Intrinsics.throwNpe();
                                            }

                                            var10000.setMotionX(var10000.getMotionX() * 0.6001421204D);
                                            var10000 = MinecraftInstance.mc.getThePlayer();
                                            if (var10000 == null) {
                                                Intrinsics.throwNpe();
                                            }

                                            var10000.setMotionZ(var10000.getMotionZ() * 0.6001421204D);
                                        }
                                    }

                                    var13 = MinecraftInstance.mc.getNetHandler();
                                    var12 = MinecraftInstance.classProvider;
                                    var10002 = MinecraftInstance.mc.getThePlayer();
                                    if (var10002 == null) {
                                        Intrinsics.throwNpe();
                                    }

                                    var13.addToSendQueue((IPacket)var12.createCPacketEntityAction((IEntity)var10002, WAction.START_SNEAKING));
                                    this.huayutingjumpflag = false;
                                }
                            }
                            break;
                        case -1513652168:
                            if (var3.equals("aac5reduce")) {
                                if (thePlayer.getHurtTime() > 1 && this.velocityInput) {
                                    thePlayer.setMotionX(thePlayer.getMotionX() * 0.81D);
                                    thePlayer.setMotionZ(thePlayer.getMotionZ() * 0.81D);
                                }

                                if (this.velocityInput && (thePlayer.getHurtTime() < 5 || thePlayer.getOnGround()) && this.velocityTimer.hasTimePassed(120L)) {
                                    this.velocityInput = false;
                                }
                            }
                            break;
                        case -1466691239:
                            if (var3.equals("newhytmotion") && thePlayer.getHurtTime() > 0 && !thePlayer.isDead() && !thePlayer.getOnGround()) {
                                if (!thePlayer.isPotionActive(MinecraftInstance.classProvider.getPotionEnum(PotionType.MOVE_SPEED))) {
                                    thePlayer.setMotionX(thePlayer.getMotionX() * 0.47188D);
                                    thePlayer.setMotionZ(thePlayer.getMotionZ() * 0.47188D);
                                    if (thePlayer.getMotionY() == 0.42D || thePlayer.getMotionY() > 0.42D) {
                                        thePlayer.setMotionY(thePlayer.getMotionY() * 0.4D);
                                    }
                                } else {
                                    thePlayer.setMotionX(thePlayer.getMotionX() * 0.65025D);
                                    thePlayer.setMotionZ(thePlayer.getMotionZ() * 0.65025D);
                                    if (thePlayer.getMotionY() == 0.42D || thePlayer.getMotionY() > 0.42D) {
                                        thePlayer.setMotionY(thePlayer.getMotionY() * 0.4D);
                                    }
                                }
                            }
                            break;
                        case -1371801463:
                            if (var3.equals("hytmotionb") && thePlayer.getHurtTime() > 0 && !thePlayer.isDead() && !thePlayer.getOnGround() && !thePlayer.isPotionActive(MinecraftInstance.classProvider.getPotionEnum(PotionType.MOVE_SPEED))) {
                                thePlayer.setMotionX(thePlayer.getMotionX() * (double)0.451145F);
                                thePlayer.setMotionZ(thePlayer.getMotionZ() * (double)0.451145F);
                            }
                            break;
                        case -1349088399:
                            if (var3.equals("custom") && thePlayer.getHurtTime() > 0 && !thePlayer.isDead()) {
                                var10000 = MinecraftInstance.mc.getThePlayer();
                                if (var10000 == null) {
                                    Intrinsics.throwNpe();
                                }

                                if (!var10000.isPotionActive(MinecraftInstance.classProvider.getPotionEnum(PotionType.MOVE_SPEED))) {
                                    var10000 = MinecraftInstance.mc.getThePlayer();
                                    if (var10000 == null) {
                                        Intrinsics.throwNpe();
                                    }

                                    if (!var10000.isInWater()) {
                                        thePlayer.setMotionX(thePlayer.getMotionX() * ((Number)this.customX.get()).doubleValue());
                                        thePlayer.setMotionZ(thePlayer.getMotionZ() * ((Number)this.customZ.get()).doubleValue());
                                        if ((Boolean)this.customYStart.get()) {
                                            thePlayer.setMotionY(thePlayer.getMotionY() / ((Number)this.customY.get()).doubleValue());
                                        }

                                        if ((Boolean)this.customC06FakeLag.get()) {
                                            MinecraftInstance.mc.getNetHandler().addToSendQueue((IPacket)MinecraftInstance.classProvider.createCPacketPlayerPosLook(thePlayer.getPosX(), thePlayer.getPosY(), thePlayer.getPosZ(), thePlayer.getRotationYaw(), thePlayer.getRotationPitch(), thePlayer.getOnGround()));
                                        }
                                    }
                                }
                            }
                            break;
                        case -1243181771:
                            if (var3.equals("glitch")) {
                                thePlayer.setNoClip(this.velocityInput);
                                if (thePlayer.getHurtTime() == 7) {
                                    thePlayer.setMotionY(0.4D);
                                }

                                this.velocityInput = false;
                            }
                            break;
                        case -1234547235:
                            if (var3.equals("aacpush")) {
                                if (this.jump) {
                                    if (thePlayer.getOnGround()) {
                                        this.jump = false;
                                    }
                                } else {
                                    if (thePlayer.getHurtTime() > 0 && thePlayer.getMotionX() != 0.0D && thePlayer.getMotionZ() != 0.0D) {
                                        thePlayer.setOnGround(true);
                                    }

                                    if (thePlayer.getHurtResistantTime() > 0 && (Boolean)this.aacPushYReducerValue.get()) {
                                        Module var14 = LiquidBounce.INSTANCE.getModuleManager().get(Speed.class);
                                        if (var14 == null) {
                                            Intrinsics.throwNpe();
                                        }

                                        if (!var14.getState()) {
                                            thePlayer.setMotionY(thePlayer.getMotionY() - 0.014999993D);
                                        }
                                    }
                                }

                                if (thePlayer.getHurtResistantTime() >= 19) {
                                    reduce = ((Number)this.aacPushXZReducerValue.get()).floatValue();
                                    thePlayer.setMotionX(thePlayer.getMotionX() / (double)reduce);
                                    thePlayer.setMotionZ(thePlayer.getMotionZ() / (double)reduce);
                                }
                            }
                            break;
                        case -1234264725:
                            if (var3.equals("aaczero")) {
                                if (thePlayer.getHurtTime() > 0) {
                                    if (!this.velocityInput || thePlayer.getOnGround() || thePlayer.getFallDistance() > 2.0F) {
                                        return;
                                    }

                                    thePlayer.setMotionY(thePlayer.getMotionY() - 1.0D);
                                    thePlayer.setAirBorne(true);
                                    thePlayer.setOnGround(true);
                                } else {
                                    this.velocityInput = false;
                                }
                            }
                            break;
                        case -1202224835:
                            if (var3.equals("hytnew")) {
                                var10000 = MinecraftInstance.mc.getThePlayer();
                                if (var10000 == null) {
                                    Intrinsics.throwNpe();
                                }

                                if (var10000.getHurtTime() > 0 && this.velocityInput) {
                                    var10000 = MinecraftInstance.mc.getThePlayer();
                                    if (var10000 == null) {
                                        Intrinsics.throwNpe();
                                    }

                                    double var10001;
                                    if (var10000.getOnGround()) {
                                        var10000 = MinecraftInstance.mc.getThePlayer();
                                        if (var10000 == null) {
                                            Intrinsics.throwNpe();
                                        }

                                        var10001 = var10000.getMotionX();
                                        var10002 = MinecraftInstance.mc.getThePlayer();
                                        if (var10002 == null) {
                                            Intrinsics.throwNpe();
                                        }

                                        var10000.setMotionX(var10001 * var10002.getMotionX() * 0.56D * Math.random());
                                        var10000 = MinecraftInstance.mc.getThePlayer();
                                        if (var10000 == null) {
                                            Intrinsics.throwNpe();
                                        }

                                        var10001 = var10000.getMotionY();
                                        var10002 = MinecraftInstance.mc.getThePlayer();
                                        if (var10002 == null) {
                                            Intrinsics.throwNpe();
                                        }

                                        var10000.setMotionY(var10001 * var10002.getMotionX() * 0.77D * Math.random());
                                        var10000 = MinecraftInstance.mc.getThePlayer();
                                        if (var10000 == null) {
                                            Intrinsics.throwNpe();
                                        }

                                        var10001 = var10000.getMotionZ();
                                        var10002 = MinecraftInstance.mc.getThePlayer();
                                        if (var10002 == null) {
                                            Intrinsics.throwNpe();
                                        }

                                        var10000.setMotionZ(var10001 * var10002.getMotionX() * 0.56D * Math.random());
                                        var10000 = MinecraftInstance.mc.getThePlayer();
                                        if (var10000 == null) {
                                            Intrinsics.throwNpe();
                                        }

                                        var10000.setOnGround(false);
                                    } else {
                                        var10000 = MinecraftInstance.mc.getThePlayer();
                                        if (var10000 == null) {
                                            Intrinsics.throwNpe();
                                        }

                                        var10001 = var10000.getMotionX();
                                        var10002 = MinecraftInstance.mc.getThePlayer();
                                        if (var10002 == null) {
                                            Intrinsics.throwNpe();
                                        }

                                        var10000.setMotionX(var10001 * var10002.getMotionX() * 0.77D * Math.random());
                                        var10000 = MinecraftInstance.mc.getThePlayer();
                                        if (var10000 == null) {
                                            Intrinsics.throwNpe();
                                        }

                                        var10000.setOnGround(true);
                                        var10000 = MinecraftInstance.mc.getThePlayer();
                                        if (var10000 == null) {
                                            Intrinsics.throwNpe();
                                        }

                                        var10001 = var10000.getMotionZ();
                                        var10002 = MinecraftInstance.mc.getThePlayer();
                                        if (var10002 == null) {
                                            Intrinsics.throwNpe();
                                        }

                                        var10000.setMotionZ(var10001 * var10002.getMotionZ() * 0.77D * Math.random());
                                    }

                                    var13 = MinecraftInstance.mc.getNetHandler();
                                    var12 = MinecraftInstance.classProvider;
                                    var10002 = MinecraftInstance.mc.getThePlayer();
                                    if (var10002 == null) {
                                        Intrinsics.throwNpe();
                                    }

                                    var13.addToSendQueue((IPacket)var12.createCPacketEntityAction((IEntity)var10002, WAction.START_SNEAKING));
                                    this.velocityInput = false;
                                }
                            }
                            break;
                        case -714948573:
                            if (var3.equals("VulgarSense") && thePlayer.getHurtTime() > 0) {
                                thePlayer.setMotionX(thePlayer.getMotionX() + -1.0E-7D);
                                thePlayer.setMotionY(thePlayer.getMotionY() + -1.0E-7D);
                                thePlayer.setMotionZ(thePlayer.getMotionZ() + -1.0E-7D);
                                thePlayer.setAirBorne(true);
                            }
                            break;
                        case 96323:
                            if (var3.equals("aac") && this.velocityInput && this.velocityTimer.hasTimePassed(80L)) {
                                thePlayer.setMotionX(thePlayer.getMotionX() * ((Number)this.horizontalValue.get()).doubleValue());
                                thePlayer.setMotionZ(thePlayer.getMotionZ() * ((Number)this.horizontalValue.get()).doubleValue());
                                this.velocityInput = false;
                            }
                            break;
                        case 103811:
                            if (var3.equals("hyt") && thePlayer.getHurtTime() > 0 && !thePlayer.getOnGround()) {
                                thePlayer.setMotionX(thePlayer.getMotionX() * (double)RenderUtils.drawJelloShadow());
                                thePlayer.setMotionZ(thePlayer.getMotionZ() * (double)RenderUtils.drawImage4());
                            }
                            break;
                        case 2986065:
                            if (var3.equals("aac4")) {
                                if (!thePlayer.getOnGround()) {
                                    if (this.velocityInput) {
                                        thePlayer.setSpeedInAir(0.02F);
                                        thePlayer.setMotionX(thePlayer.getMotionX() * 0.6D);
                                        thePlayer.setMotionZ(thePlayer.getMotionZ() * 0.6D);
                                    }
                                } else if (this.velocityTimer.hasTimePassed(80L)) {
                                    this.velocityInput = false;
                                    thePlayer.setSpeedInAir(0.02F);
                                }
                            }
                            break;
                        case 3273774:
                            if (var3.equals("jump") && thePlayer.getHurtTime() > 0 && thePlayer.getOnGround()) {
                                thePlayer.setMotionY(0.42D);
                                reduce = thePlayer.getRotationYaw() * 0.017453292F;
                                double var7 = thePlayer.getMotionX();
                                boolean var5 = false;
                                float var9 = (float)Math.sin((double)reduce);
                                thePlayer.setMotionX(var7 - (double)var9 * 0.2D);
                                var7 = thePlayer.getMotionZ();
                                var5 = false;
                                var9 = (float)Math.cos((double)reduce);
                                thePlayer.setMotionZ(var7 + (double)var9 * 0.2D);
                            }
                            break;
                        case 3387523:
                            if (var3.equals("noxz")) {
                                if ((Boolean)this.hytGround.get()) {
                                    if (thePlayer.getHurtTime() > 0 && !thePlayer.isDead() && thePlayer.getHurtTime() <= 5 && thePlayer.getOnGround()) {
                                        thePlayer.setMotionX(thePlayer.getMotionX() * 0.4D);
                                        thePlayer.setMotionZ(thePlayer.getMotionZ() * 0.4D);
                                        thePlayer.setMotionY(thePlayer.getMotionY() * 0.01D);
                                        thePlayer.setMotionY(thePlayer.getMotionY() / (double)1.4F);
                                    }
                                } else if (thePlayer.getHurtTime() > 0 && !thePlayer.isDead() && thePlayer.getHurtTime() <= 5) {
                                    thePlayer.setMotionX(thePlayer.getMotionX() * 0.4D);
                                    thePlayer.setMotionZ(thePlayer.getMotionZ() * 0.4D);
                                    thePlayer.setMotionY(thePlayer.getMotionY() * 0.04D);
                                    thePlayer.setMotionY(thePlayer.getMotionY() / (double)1.4F);
                                }
                            }
                            break;
                        case 97312387:
                            if (var3.equals("feile") && thePlayer.getOnGround()) {
                                this.canCleanJump = true;
                                thePlayer.setMotionY(1.5D);
                                thePlayer.setMotionZ(1.2D);
                                thePlayer.setMotionX(1.5D);
                                if (thePlayer.getOnGround() && this.velocityTick > 2) {
                                    this.velocityInput = false;
                                }
                            }
                            break;
                        case 232843001:
                            if (var3.equals("hytmotion")) {
                                if ((Boolean)this.hytGround.get()) {
                                    if (thePlayer.getHurtTime() > 0 && !thePlayer.isDead() && thePlayer.getHurtTime() <= 5 && thePlayer.getOnGround()) {
                                        thePlayer.setMotionX(thePlayer.getMotionX() * 0.4D);
                                        thePlayer.setMotionZ(thePlayer.getMotionZ() * 0.4D);
                                        thePlayer.setMotionY(thePlayer.getMotionY() * (double)0.381145F);
                                        thePlayer.setMotionY(thePlayer.getMotionY() / (double)1.781145F);
                                    }
                                } else if (thePlayer.getHurtTime() > 0 && !thePlayer.isDead() && thePlayer.getHurtTime() <= 5) {
                                    thePlayer.setMotionX(thePlayer.getMotionX() * 0.4D);
                                    thePlayer.setMotionZ(thePlayer.getMotionZ() * 0.4D);
                                    thePlayer.setMotionY(thePlayer.getMotionY() * (double)0.381145F);
                                    thePlayer.setMotionY(thePlayer.getMotionY() / (double)1.781145F);
                                }
                            }
                            break;
                        case 305296331:
                            if (var3.equals("hytpacket")) {
                                if ((Boolean)this.hytGround.get()) {
                                    if (thePlayer.getHurtTime() > 0 && !thePlayer.isDead() && thePlayer.getHurtTime() <= 5 && thePlayer.getOnGround()) {
                                        thePlayer.setMotionX(thePlayer.getMotionX() * 0.5D);
                                        thePlayer.setMotionZ(thePlayer.getMotionZ() * 0.5D);
                                        thePlayer.setMotionY(thePlayer.getMotionY() / (double)1.781145F);
                                    }
                                } else if (thePlayer.getHurtTime() > 0 && !thePlayer.isDead() && thePlayer.getHurtTime() <= 5) {
                                    thePlayer.setMotionX(thePlayer.getMotionX() * 0.5D);
                                    thePlayer.setMotionZ(thePlayer.getMotionZ() * 0.5D);
                                    thePlayer.setMotionY(thePlayer.getMotionY() / (double)1.781145F);
                                }
                            }
                            break;
                        case 1099846370:
                            if (var3.equals("reverse")) {
                                if (!this.velocityInput) {
                                    return;
                                }

                                if (!thePlayer.getOnGround()) {
                                    MovementUtils.strafe(MovementUtils.INSTANCE.getSpeed() * ((Number)this.reverseStrengthValue.get()).floatValue());
                                } else if (this.velocityTimer.hasTimePassed(80L)) {
                                    this.velocityInput = false;
                                }
                            }
                            break;
                        case 1385378279:
                            if (var3.equals("hytbest") && thePlayer.getHurtTime() > 0 && !thePlayer.getOnGround()) {
                                thePlayer.setMotionX(thePlayer.getMotionX() / (double)1);
                                thePlayer.setMotionZ(thePlayer.getMotionZ() / (double)1);
                            }
                            break;
                        case 1385917856:
                            if (var3.equals("hyttick")) {
                                if (this.velocityTick > ((Number)this.velocityTickValue.get()).intValue()) {
                                    if (thePlayer.getMotionY() > (double)0) {
                                        thePlayer.setMotionY(0.0D);
                                    }

                                    thePlayer.setMotionX(0.0D);
                                    thePlayer.setMotionZ(0.0D);
                                    thePlayer.setJumpMovementFactor(-1.0E-5F);
                                    this.velocityInput = false;
                                }

                                if (thePlayer.getOnGround() && this.velocityTick > 1) {
                                    this.velocityInput = false;
                                }
                            }
                            break;
                        case 1845586417:
                            if (var3.equals("newaac4") && thePlayer.getHurtTime() > 0 && !thePlayer.getOnGround()) {
                                reduce = ((Number)this.newaac4XZReducerValue.get()).floatValue();
                                thePlayer.setMotionX(thePlayer.getMotionX() * (double)reduce);
                                thePlayer.setMotionZ(thePlayer.getMotionZ() * (double)reduce);
                            }
                    }

                }
            }
        }
    }

    @EventTarget
    public final void onBlockBB(@NotNull BlockBBEvent event) {
        Intrinsics.checkParameterIsNotNull(event, "event");
        this.block = event.getBlock();
    }

    @EventTarget
    public final void onPacket(@NotNull PacketEvent event) {
        Intrinsics.checkParameterIsNotNull(event, "event");
        IEntityPlayerSP var10000 = MinecraftInstance.mc.getThePlayer();
        if (var10000 != null) {
            IEntityPlayerSP thePlayer = var10000;
            IPacket packet = event.getPacket();
            if (MinecraftInstance.classProvider.isSPacketEntityVelocity(packet)) {
                ISPacketEntityVelocity packetEntityVelocity = packet.asSPacketEntityVelocity();
                if ((Boolean)this.noFireValue.get()) {
                    var10000 = MinecraftInstance.mc.getThePlayer();
                    if (var10000 == null) {
                        Intrinsics.throwNpe();
                    }

                    if (var10000.isBurning()) {
                        return;
                    }
                }

                IWorldClient var14 = MinecraftInstance.mc.getTheWorld();
                if (var14 != null) {
                    IEntity var15 = var14.getEntityByID(packetEntityVelocity.getEntityID());
                    if (var15 != null) {
                        if (Intrinsics.areEqual(var15, thePlayer) ^ true) {
                            return;
                        }

                        this.velocityTimer.reset();
                        String var5 = (String)this.modeValue.get();
                        boolean var6 = false;
                        if (var5 == null) {
                            throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
                        }

                        String var16 = var5.toLowerCase();
                        Intrinsics.checkExpressionValueIsNotNull(var16, "(this as java.lang.String).toLowerCase()");
                        var5 = var16;
                        float vertical;
                        float horizontal;
                        boolean $i$f$unwrap;
                        switch(var5.hashCode()) {
                            case -1970553484:
                                if (!var5.equals("smoothreverse")) {
                                    return;
                                }
                                break;
                            case -1810282708:
                                if (var5.equals("huayutingjump")) {
                                    $i$f$unwrap = false;
                                    if (packet == null) {
                                        throw new TypeCastException("null cannot be cast to non-null type net.ccbluex.liquidbounce.injection.backend.PacketImpl<*>");
                                    }

                                    if (((PacketImpl)packet).getWrapped() instanceof SPacketEntityVelocity) {
                                        this.huayutingjumpflag = true;
                                        var10000 = MinecraftInstance.mc.getThePlayer();
                                        if (var10000 == null) {
                                            Intrinsics.throwNpe();
                                        }

                                        if (var10000.getHurtTime() != 0) {
                                            event.cancelEvent();
                                            packet.asSPacketEntityVelocity().setMotionX(0);
                                            packet.asSPacketEntityVelocity().setMotionY(0);
                                            packet.asSPacketEntityVelocity().setMotionZ(0);
                                        }

                                        return;
                                    }
                                }

                                return;
                            case -1657634710:
                                if (var5.equals("hytpacketfix")) {
                                    if (thePlayer.getHurtTime() > 0 && !thePlayer.isDead()) {
                                        var10000 = MinecraftInstance.mc.getThePlayer();
                                        if (var10000 == null) {
                                            Intrinsics.throwNpe();
                                        }

                                        if (!var10000.isPotionActive(MinecraftInstance.classProvider.getPotionEnum(PotionType.MOVE_SPEED))) {
                                            var10000 = MinecraftInstance.mc.getThePlayer();
                                            if (var10000 == null) {
                                                Intrinsics.throwNpe();
                                            }

                                            if (!var10000.isInWater()) {
                                                thePlayer.setMotionX(thePlayer.getMotionX() * 0.4D);
                                                thePlayer.setMotionZ(thePlayer.getMotionZ() * 0.4D);
                                                thePlayer.setMotionY(thePlayer.getMotionY() / (double)1.45F);
                                            }
                                        }
                                    }

                                    if (thePlayer.getHurtTime() < 1) {
                                        packetEntityVelocity.setMotionY(0);
                                    }

                                    if (thePlayer.getHurtTime() < 5) {
                                        packetEntityVelocity.setMotionX(0);
                                        packetEntityVelocity.setMotionZ(0);
                                        return;
                                    }
                                }

                                return;
                            case -1513652168:
                                if (!var5.equals("aac5reduce")) {
                                    return;
                                }
                                break;
                            case -1243181771:
                                if (var5.equals("glitch")) {
                                    if (!thePlayer.getOnGround()) {
                                        return;
                                    }

                                    this.velocityInput = true;
                                    event.cancelEvent();
                                }

                                return;
                            case -1234264725:
                                if (!var5.equals("aaczero")) {
                                    return;
                                }
                                break;
                            case -902286926:
                                if (var5.equals("simple")) {
                                    horizontal = ((Number)this.horizontalValue.get()).floatValue();
                                    vertical = ((Number)this.verticalValue.get()).floatValue();
                                    if (horizontal == 0.0F && vertical == 0.0F) {
                                        event.cancelEvent();
                                    }

                                    packetEntityVelocity.setMotionX((int)((float)packetEntityVelocity.getMotionX() * horizontal));
                                    packetEntityVelocity.setMotionY((int)((float)packetEntityVelocity.getMotionY() * vertical));
                                    packetEntityVelocity.setMotionZ((int)((float)packetEntityVelocity.getMotionZ() * horizontal));
                                }

                                return;
                            case -767500465:
                                if (var5.equals("hytnewtest") && thePlayer.getOnGround()) {
                                    this.velocityInput = true;
                                    horizontal = thePlayer.getRotationYaw() * 0.017453292F;
                                    packetEntityVelocity.setMotionX((int)((double)packetEntityVelocity.getMotionX() * 0.75D));
                                    packetEntityVelocity.setMotionZ((int)((double)packetEntityVelocity.getMotionZ() * 0.75D));
                                    double var9 = thePlayer.getMotionX();
                                    $i$f$unwrap = false;
                                    float var11 = (float)Math.sin((double)horizontal);
                                    thePlayer.setMotionX(var9 - (double)var11 * 0.2D);
                                    var9 = thePlayer.getMotionZ();
                                    $i$f$unwrap = false;
                                    var11 = (float)Math.cos((double)horizontal);
                                    thePlayer.setMotionZ(var9 + (double)var11 * 0.2D);
                                }

                                return;
                            case -66562627:
                                if (var5.equals("hytcancel")) {
                                    event.cancelEvent();
                                }

                                return;
                            case 96323:
                                if (!var5.equals("aac")) {
                                    return;
                                }
                                break;
                            case 2986065:
                                if (!var5.equals("aac4")) {
                                    return;
                                }
                                break;
                            case 233102203:
                                if (var5.equals("vanilla")) {
                                    event.cancelEvent();
                                }

                                return;
                            case 874251766:
                                if (var5.equals("hytpacketa")) {
                                    packetEntityVelocity.setMotionX((int)((double)((float)packetEntityVelocity.getMotionX() * ((Number)this.hytpacketaset.get()).floatValue()) / 1.5D));
                                    packetEntityVelocity.setMotionY((int)0.7D);
                                    packetEntityVelocity.setMotionZ((int)((double)((float)packetEntityVelocity.getMotionZ() * ((Number)this.hytpacketaset.get()).floatValue()) / 1.5D));
                                    event.cancelEvent();
                                }

                                return;
                            case 874251767:
                                if (var5.equals("hytpacketb")) {
                                    packetEntityVelocity.setMotionX((int)((double)((float)packetEntityVelocity.getMotionX() * ((Number)this.hytpacketbset.get()).floatValue()) / 2.5D));
                                    packetEntityVelocity.setMotionY((int)((double)((float)packetEntityVelocity.getMotionY() * ((Number)this.hytpacketbset.get()).floatValue()) / 2.5D));
                                    packetEntityVelocity.setMotionZ((int)((double)((float)packetEntityVelocity.getMotionZ() * ((Number)this.hytpacketbset.get()).floatValue()) / 2.5D));
                                }

                                return;
                            case 1099846370:
                                if (!var5.equals("reverse")) {
                                    return;
                                }
                                break;
                            case 1385914517:
                                if (var5.equals("hyttest") && thePlayer.getOnGround()) {
                                    this.canCancelJump = false;
                                    packetEntityVelocity.setMotionX((int)0.985114D);
                                    packetEntityVelocity.setMotionY((int)0.885113D);
                                    packetEntityVelocity.setMotionZ((int)0.785112D);
                                    thePlayer.setMotionX(thePlayer.getMotionX() / 1.75D);
                                    thePlayer.setMotionZ(thePlayer.getMotionZ() / 1.75D);
                                }

                                return;
                            case 1385917856:
                                if (var5.equals("hyttick")) {
                                    this.velocityInput = true;
                                    horizontal = 0.0F;
                                    vertical = 0.0F;
                                    event.cancelEvent();
                                }

                                return;
                            default:
                                return;
                        }

                        this.velocityInput = true;
                        return;
                    }
                }

            }
        }
    }

    @EventTarget
    public final void onJump(@NotNull JumpEvent event) {
        Intrinsics.checkParameterIsNotNull(event, "event");
        IEntityPlayerSP thePlayer = MinecraftInstance.mc.getThePlayer();
        if (thePlayer != null && !thePlayer.isInWater() && !thePlayer.isInLava() && !thePlayer.isInWeb()) {
            String var3 = (String)this.modeValue.get();
            boolean var4 = false;
            if (var3 == null) {
                throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
            } else {
                String var10000 = var3.toLowerCase();
                Intrinsics.checkExpressionValueIsNotNull(var10000, "(this as java.lang.String).toLowerCase()");
                var3 = var10000;
                switch(var3.hashCode()) {
                    case -1234547235:
                        if (var3.equals("aacpush")) {
                            this.jump = true;
                            if (!thePlayer.isCollidedVertically()) {
                                event.cancelEvent();
                            }
                        }
                        break;
                    case -1234264725:
                        if (var3.equals("aaczero") && thePlayer.getHurtTime() > 0) {
                            event.cancelEvent();
                        }
                        break;
                    case 2986065:
                        if (var3.equals("aac4") && thePlayer.getHurtTime() > 0) {
                            event.cancelEvent();
                        }
                }

            }
        }
    }
}
