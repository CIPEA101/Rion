/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;


import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.Render2DEvent;
import net.ccbluex.liquidbounce.features.module.modules.render.AntiBlind;
import net.ccbluex.liquidbounce.features.module.modules.render.HUD;
import net.ccbluex.liquidbounce.features.module.modules.render.NoScoreboard;
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer;
import net.ccbluex.liquidbounce.utils.ClassUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tomk.Hotbar.Hotbar;

@Mixin(GuiIngame.class)
@SideOnly(Side.CLIENT)
public abstract class MixinGuiInGame extends MixinGui {

    @Shadow
    @Final
    protected static ResourceLocation WIDGETS_TEX_PATH;
    @Shadow
    @Final
    protected Minecraft mc;
    @Shadow public GuiPlayerTabOverlay overlayPlayerList;
    @Shadow
    protected abstract void renderHotbarItem(int xPos, int yPos, float partialTicks, EntityPlayer player, ItemStack stack);

    @Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
    private void renderScoreboard(CallbackInfo callbackInfo) {
        if (LiquidBounce.moduleManager.getModule(HUD.class).getState() || NoScoreboard.INSTANCE.getState())
            callbackInfo.cancel();
    }

    @Overwrite
    protected void renderHotbar(ScaledResolution sr, float partialTicks) {

        final HUD hud = (HUD) LiquidBounce.moduleManager.getModule(HUD.class);
        if (Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityPlayer && !(hud.getHideHotBarValue().get())) {

            boolean canBetterHotbar = hud.getState() && hud.getBetterHotbarValue().get();
            EntityPlayer entityPlayer = (EntityPlayer) this.mc.getRenderViewEntity();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            ItemStack itemstack = entityPlayer.getHeldItemOffhand();
            EnumHandSide enumhandside = entityPlayer.getPrimaryHand().opposite();
            int middleScreen = sr.getScaledWidth() / 2;
            float f = this.zLevel;
            int j = 182;
            int k = 91;
            this.zLevel = -90.0F;
            int itemX = middleScreen - 91 + hud.getHotbarEasePos(entityPlayer.inventory.currentItem * 20);
            if (canBetterHotbar) {
                //GlStateManager.disableTexture2D();
                Hotbar.render(sr, itemX,partialTicks);
                //GlStateManager.enableTexture2D();
            }
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            RenderUtils.drawRect(middleScreen - 91, sr.getScaledHeight() - 2, middleScreen + 91, sr.getScaledHeight() - 22,  Integer.MIN_VALUE);
            RenderUtils.drawRect(middleScreen - 91 + entityPlayer.inventory.currentItem * 20 + 1, sr.getScaledHeight() - 2, middleScreen - 91 + entityPlayer.inventory.currentItem * 20 + 1 + 22, sr.getScaledHeight() - 22, Integer.MAX_VALUE);
            //GuiIngame.drawRect(middleScreen - 91, sr.getScaledHeight() - 24, middleScreen + 90, sr.getScaledHeight(), Integer.MIN_VALUE);
            //GuiIngame.drawRect(middleScreen - 91 - 1 + entityPlayer.inventory.currentItem * 20 + 1, sr.getScaledHeight() - 24, middleScreen - 91 - 1 + entityPlayer.inventory.currentItem * 20 + 22, sr.getScaledHeight() - 22 - 1 + 24, Integer.MAX_VALUE);
            if(!itemstack.isEmpty())
                GuiIngame.drawRect(middleScreen - 91 - 30, sr.getScaledHeight() - 24, middleScreen - 100, sr.getScaledHeight(), Integer.MIN_VALUE);

            this.mc.getTextureManager().bindTexture(WIDGETS_TEX_PATH);

//            if (!itemstack.isEmpty()) {
//                if (enumhandside == EnumHandSide.LEFT) {
//                    this.drawTexturedModalRect(middleScreen - 91 - 29, sr.getScaledHeight() - 23, 24, 22, 29, 24);
//                } else {
//                    this.drawTexturedModalRect(middleScreen + 91, sr.getScaledHeight() - 23, 53, 22, 29, 24);
//                }
//            }
            if (!canBetterHotbar) {
                this.drawTexturedModalRect(middleScreen - 91, sr.getScaledHeight() - 22, 0, 0, 182, 22);
                this.drawTexturedModalRect(itemX - 1, sr.getScaledHeight() - 22 - 1, 0, 22, 24, 22);
            }
            this.zLevel = f;
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderHelper.enableGUIStandardItemLighting();

            for (int l = 0; l < 9; ++l) {
                int i1 = middleScreen - 90 + l * 20 + 2;
                int j1 = sr.getScaledHeight() - 16 - 3;
                this.renderHotbarItem(i1, j1, partialTicks, entityPlayer, entityPlayer.inventory.mainInventory.get(l));
            }

            if (!itemstack.isEmpty()) {
                int l1 = sr.getScaledHeight() - 16 - 3;

                if (enumhandside == EnumHandSide.LEFT) {
                    this.renderHotbarItem(middleScreen - 91 - 26, l1, partialTicks, entityPlayer, itemstack);
                } else {
                    this.renderHotbarItem(middleScreen + 91 + 10, l1, partialTicks, entityPlayer, itemstack);
                }
            }

            if (this.mc.gameSettings.attackIndicator == 2) {
                float f1 = this.mc.player.getCooledAttackStrength(0.0F);

                if (f1 < 1.0F) {
                    int i2 = sr.getScaledHeight() - 20;
                    int j2 = middleScreen + 91 + 6;

                    if (enumhandside == EnumHandSide.RIGHT) {
                        j2 = middleScreen - 91 - 22;
                    }

                    this.mc.getTextureManager().bindTexture(Gui.ICONS);
                    int k1 = (int) (f1 * 19.0F);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    this.drawTexturedModalRect(j2, i2, 0, 94, 18, 18);
                    this.drawTexturedModalRect(j2, i2 + 18 - k1, 18, 112 - k1, 18, k1);
                }
            }

            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
        }
    }


    @Inject(method = "renderHotbar", at = @At("RETURN"))
    private void renderTooltipPost(ScaledResolution sr, float partialTicks, CallbackInfo callbackInfo) {
        if (!ClassUtils.hasClass("net.labymod.api.LabyModAPI")) {
            LiquidBounce.eventManager.callEvent(new Render2DEvent(partialTicks));
            AWTFontRenderer.Companion.garbageCollectionTick();
        }
    }
    @Inject(method = "renderExpBar",at = @At("HEAD"),cancellable = true)
    private void renderExpBar(ScaledResolution p_renderExpBar_1_, int p_renderExpBar_2_, CallbackInfo ci) {
        HUD hud = (HUD) LiquidBounce.moduleManager.getModule(HUD.class);
        if (!hud.getHotbar().get()){
            ci.cancel();
        }
    }
    @Inject(method = { "renderPotionEffects"}, at = @At("HEAD"), cancellable = true)
    protected void renderPotionEffects(ScaledResolution p_renderPotionEffects_1_, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "renderSelectedItem",at = @At("HEAD"),cancellable = true)
    private void renderSelectedItem(ScaledResolution p_renderPlayerStats_1_, CallbackInfo ci) {
        HUD hud = (HUD) LiquidBounce.moduleManager.getModule(HUD.class);
        if (!hud.getHotbar().get()){
            ci.cancel();
        }
    }


    @Inject(method = "renderPlayerStats",at = @At("HEAD"),cancellable = true)
    private void renderPlayerStats(ScaledResolution p_renderPlayerStats_1_, CallbackInfo ci) {
        HUD hud = (HUD) LiquidBounce.moduleManager.getModule(HUD.class);
        if (!hud.getHotbar().get()){
            ci.cancel();
        }
    }

    @Inject(method = "renderPumpkinOverlay", at = @At("HEAD"), cancellable = true)
    private void renderPumpkinOverlay(final CallbackInfo callbackInfo) {
        final AntiBlind antiBlind = (AntiBlind) LiquidBounce.moduleManager.getModule(AntiBlind.class);

        if (antiBlind.getState() && antiBlind.getPumpkinEffect().get())
            callbackInfo.cancel();
    }
}