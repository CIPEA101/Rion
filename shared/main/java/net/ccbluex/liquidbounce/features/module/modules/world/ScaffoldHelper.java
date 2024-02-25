package net.ccbluex.liquidbounce.features.module.modules.world;

import net.ccbluex.liquidbounce.features.module.*;
import org.jetbrains.annotations.*;
import net.ccbluex.liquidbounce.*;
import kotlin.*;
import net.ccbluex.liquidbounce.features.module.modules.world.*;
import net.ccbluex.liquidbounce.utils.*;
import kotlin.jvm.internal.*;
import net.ccbluex.liquidbounce.api.minecraft.client.entity.*;
import net.ccbluex.liquidbounce.event.*;

@ModuleInfo(name = "ScaffoldHelper", description = "ScaffoldHelper", category = ModuleCategory.WORLD)
@Metadata(mv = { 1, 1, 16 }, bv = { 1, 0, 3 }, k = 1, d1 = { "\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005?\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0016J\u0012\u0010\u0005\u001a\u00020\u00042\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007H\u0007��\u0006\b" }, d2 = { "Lnet/ccbluex/liquidbounce/features/module/modules/hyt/ScaffoldHelper;", "Lnet/ccbluex/liquidbounce/features/module/Module;", "()V", "onDisable", "", "onUpdate", "event", "Lnet/ccbluex/liquidbounce/event/UpdateEvent;", "Forever" })
public final class ScaffoldHelper extends Module
{
    @Override
    public void onDisable() {
    }
    
    @EventTarget
    public final void onUpdate(@Nullable final UpdateEvent event) {
        final Module module = LiquidBounce.INSTANCE.getModuleManager().getModule(Scaffold.class);
        if (module == null) {
            throw new TypeCastException("null cannot be cast to non-null type net.ccbluex.liquidbounce.features.module.modules.world.Scaffold1");
        }
        final Scaffold scaffoldmodule = (Scaffold)module;
        final Module module2 = LiquidBounce.INSTANCE.getModuleManager().getModule(Timer.class);
        if (module2 == null) {
            throw new TypeCastException("null cannot be cast to non-null type net.ccbluex.liquidbounce.features.module.modules.world.Timer");
        }
        final Timer timermodule = (Timer)module2;
        final IEntityPlayerSP thePlayer = MinecraftInstance.mc.getThePlayer();
        if (thePlayer == null) {
            Intrinsics.throwNpe();
        }
        if (!thePlayer.isSneaking()) {
            final IEntityPlayerSP thePlayer2 = MinecraftInstance.mc.getThePlayer();
            if (thePlayer2 == null) {
                Intrinsics.throwNpe();
            }
            if (thePlayer2.getOnGround()) {
                scaffoldmodule.setState(false);
                timermodule.setState(false);
            }
            else {
                scaffoldmodule.setState(true);
                timermodule.setState(true);
            }
        }
    }
}
