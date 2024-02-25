package novoline.ui.InfosUtils


import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager

import net.ccbluex.liquidbounce.utils.render.RenderUtils

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.*
import novoline.font.Fonts
import org.lwjgl.opengl.GL11
import tomk.EaseUtils.easeOutQuart
import tomk.render.RoundedUtil

import java.awt.Color
import java.io.IOException

/**
 *@author xiatian233
 *@Date 2022/12/14
 */

class GuiMainMenu : GuiScreen() {
    var mc: Minecraft = Minecraft.getMinecraft()
    var sr: ScaledResolution? = null
    private var progress = 0f
    private var progress2 = 0f
    private var Alt = false
    private var lastMS = 0L
    private var lastMS2 = 0L
    override fun initGui() {
        sr = ScaledResolution(Minecraft.getMinecraft())
        lastMS = System.currentTimeMillis()
        progress = 0f
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)
        val sr = ScaledResolution(Minecraft.getMinecraft())
        var Color6 =  Color(255,255,255).rgb
        var Color7 =  Color(255,255,255).rgb
        RenderUtils.drawCircle2(sr.scaledWidth.toFloat() - 10F - 15F - 1.5F + Fonts.NovolineIcon.NovolineIcon45.NovolineIcon45.stringWidth("G") / 2,8F + Fonts.NovolineIcon.NovolineIcon45.NovolineIcon45.height / 2 ,13F,Color(24,24,24).rgb)
        if (Alt == true) {
            //  lastMS2 = System.currentTimeMillis()
            // progress2 = 0f
            //Alt小窗
            // progress2 = if (progress2 >= 1f) 1f else (System.currentTimeMillis() - lastMS2).toFloat() / 2000F

            //val trueAnim2 = easeOutQuart(progress2.toDouble())
            // GL11.glPushMatrix()
            //GL11.glTranslated((1 - trueAnim2) * sr!!.scaledWidth.toFloat() - 10F - 15F, (1 - trueAnim2) * 5F, 0.0)
            //GL11.glScaled(trueAnim2,trueAnim2,trueAnim2)

            RoundedUtil.drawRound(
                sr!!.scaledWidth.toFloat() - 10F - 15F - 1.5F - 220 + 13F + Fonts.NovolineIcon.NovolineIcon45.NovolineIcon45.stringWidth(
                    "G"
                ) / 2,
                8F + Fonts.NovolineIcon.NovolineIcon45.NovolineIcon45.height / 2 - 13F,
                220F,
                250F,
                13F,
                Color(24, 24, 24)
            )
            //GL11.glPopMatrix()
        }
        //主界面
        if (RenderUtils.isHovered(sr.scaledWidth.toFloat() - 10F - 15F - 3F,8F,
                Fonts.NovolineIcon.NovolineIcon45.NovolineIcon45.stringWidth("G").toFloat(),
                Fonts.NovolineIcon.NovolineIcon45.NovolineIcon45.height.toFloat(),mouseX, mouseY)){
            Color6= Color(63,186,213).rgb
        }
        RenderUtils.drawCircle2(25F  + Fonts.NovolineIcon.NovolineIcon45.NovolineIcon45.stringWidth("N") / 2,8F + Fonts.NovolineIcon.NovolineIcon45.NovolineIcon45.height / 2 ,13F,Color(24,24,24).rgb)
        if (RenderUtils.isHovered(25F,8F,
                Fonts.NovolineIcon.NovolineIcon45.NovolineIcon45.stringWidth("N").toFloat(),
                Fonts.NovolineIcon.NovolineIcon45.NovolineIcon45.height.toFloat(),mouseX, mouseY)){
            Color7= Color(63,186,213).rgb
            // if (updatey < 140F){
            //     updatey += 10F
            // }
            // RoundedUtil.drawRound(Fonts.NovolineIcon.NovolineIcon45.NovolineIcon45.stringWidth("N").toFloat(),Fonts.NovolineIcon.NovolineIcon45.NovolineIcon45.height.toFloat() + 10,20F,updatey,5F,Color(30,30,30,40))
        }
        //UPDATE
        Fonts.NovolineIcon.NovolineIcon45.NovolineIcon45.drawString("N",25F,7F,Color7)
        //ALT
        Fonts.NovolineIcon.NovolineIcon45.NovolineIcon45.drawString("G",sr.scaledWidth.toFloat() - 10F - 17F,7F,Color6)
        net.ccbluex.liquidbounce.ui.font.Fonts.font35.drawCenteredString(
            "Rion Made By NeverTeams & RionTeams",
            sr.scaledWidth / 2f,
            sr.scaledHeight - 5F - net.ccbluex.liquidbounce.ui.font.Fonts.font35.fontHeight,
            Color(200,200,200).rgb,true
        )
        progress = if (progress >= 1f) 1f else (System.currentTimeMillis() - lastMS).toFloat() / 2000F

        val trueAnim = easeOutQuart(progress.toDouble())

        GL11.glTranslated(0.0, (1 - trueAnim) * -sr!!.scaledHeight, 0.0)
        Fonts.NovolineIcon.NovolineIcon75.NovolineIcon75.drawCenteredString(
            "I",
            sr.scaledWidth / 2f,
            sr.scaledHeight / 2f - 55 - 15 -Fonts.NovolineIcon.NovolineIcon75.NovolineIcon75.height,
            Color(110,183,225).rgb,true
        )

        RoundedUtil.drawRound(sr!!.scaledWidth / 2f - 12 - 45 - 10, sr!!.scaledHeight / 2f - 67,(24 + 90 + 10 + 5).toFloat(),
            (20 * 4 + 7 * 5).toFloat(),6f,Color(24,24,24))
        // RenderUtils.drawImage4(ResourceLocation("liquidwing/background.png"), 0, 0, width, height)
        RoundedUtil.drawRound(
            sr!!.scaledWidth / 2f - 12 - 45 - 5,
            sr!!.scaledHeight / 2f - 60,
            (24 + 95).toFloat(),
            20f,
            7f,
            Color(20,50,80)
        )
        var Color =  Color(39,120,186).rgb
        if (RenderUtils.isHovered(sr!!.scaledWidth / 2f - 12 - 45 - 5,
                sr!!.scaledHeight / 2f - 60,
                (24 + 95).toFloat(),
                20f,mouseX, mouseY)){
            Color = Color(63,186,213).rgb
        }
        net.ccbluex.liquidbounce.ui.font.Fonts.font35.drawCenteredString(
            "SinglePlayer",
            sr.scaledWidth / 2f - 2.5F,
            sr.scaledHeight / 2f - 55,
            Color,true
        )
        RoundedUtil.drawRound(
            sr!!.scaledWidth / 2f - 12 - 45 - 5,
            sr!!.scaledHeight / 2f - 33,
            (24 + 90 + 5).toFloat(),
            20f,
            7f,
            Color(20,50,80)
        )
        var Color2 =  Color(39,120,186).rgb
        if (RenderUtils.isHovered(  sr!!.scaledWidth / 2f - 12 - 45 - 5,
                sr!!.scaledHeight / 2f - 33,
                (24 + 90 + 5).toFloat(),
                20f,mouseX, mouseY)){
            Color2 = Color(63,186,213).rgb
        }
        net.ccbluex.liquidbounce.ui.font.Fonts.font35.drawCenteredString(
            "MultiPlayer",
            sr.scaledWidth / 2f - 2.5F,
            sr.scaledHeight / 2f - 28,
            Color2,true
        )
        RoundedUtil.drawRound(
            sr!!.scaledWidth / 2f - 12 - 45 - 5,
            sr!!.scaledHeight / 2f - 6,
            (24 + 90 + 5 ).toFloat(),
            20f,
            7f,
            Color(20,50,80)
        )
        var Color4 =  Color(39,120,186).rgb
        if (RenderUtils.isHovered(    sr!!.scaledWidth / 2f - 12 - 45 - 5,
                sr!!.scaledHeight / 2f - 6,
                (24 + 90 + 5 ).toFloat(),
                20f,mouseX, mouseY)){
            Color4 = Color(63,186,213).rgb
        }
        net.ccbluex.liquidbounce.ui.font.Fonts.font35.drawCenteredString(
            "Options",
            sr.scaledWidth / 2f - 2.5F,
            sr.scaledHeight / 2f - 1,
            Color4,true
        )
        RoundedUtil.drawRound(
            sr!!.scaledWidth / 2f - 12 - 45 - 5,
            sr!!.scaledHeight / 2f + 21,
            (24 + 90 + 5).toFloat(),
            20f,
            7f,
            Color(20,50,80)
        )
        var Color5 =  Color(39,120,186).rgb
        if (RenderUtils.isHovered(   sr!!.scaledWidth / 2f - 12 - 45 - 5,
                sr!!.scaledHeight / 2f + 21,
                (24 + 90 + 5).toFloat(),
                20f,mouseX, mouseY)){
            Color5= Color(63,186,213).rgb
        }
        net.ccbluex.liquidbounce.ui.font.Fonts.font35.drawCenteredString(
            "Shutdown",
            sr.scaledWidth / 2f - 2.5F,
            sr.scaledHeight / 2f + 26,
            Color5,true
        )
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        progress = if (progress >= 1f) 1f else (System.currentTimeMillis() - lastMS).toFloat() / 2000F

        val trueAnim = easeOutQuart(progress.toDouble())

        GL11.glTranslated(0.0, (1 - trueAnim) * -height, 0.0)
        if (RenderUtils.isHovered(
                sr!!.scaledWidth / 2f - 12 - 45 - 5,
                sr!!.scaledHeight / 2f - 60,
                (24 + 95).toFloat(),
                20f,
                mouseX,
                mouseY
            )
        ) {
            Minecraft.getMinecraft().displayGuiScreen(GuiWorldSelection(this))
        }
        if (RenderUtils.isHovered(
                sr!!.scaledWidth / 2f - 12 - 45 - 5,
                sr!!.scaledHeight / 2f - 33,
                (24 + 90 + 5).toFloat(),
                20f,
                mouseX,
                mouseY
            )
        ) {
            Minecraft.getMinecraft().displayGuiScreen(GuiMultiplayer(this))
        }
        if (RenderUtils.isHovered(sr!!.scaledWidth.toFloat() - 10F - 15F,5F,
                Fonts.NovolineIcon.NovolineIcon45.NovolineIcon45.stringWidth("G").toFloat(),
                Fonts.NovolineIcon.NovolineIcon45.NovolineIcon45.height.toFloat(),mouseX, mouseY)){
            LiquidBounce.wrapper.minecraft.displayGuiScreen(
                LiquidBounce.wrapper.classProvider.wrapGuiScreen(
                    GuiAltManager()
                )
            )
            Alt = !Alt
        }
        if (RenderUtils.isHovered(
                sr!!.scaledWidth / 2f - 12 - 45 - 5,
                sr!!.scaledHeight / 2f - 6,
                (24 + 90 + 5 ).toFloat(),
                20f,
                mouseX,
                mouseY
            )
        ) {
            Minecraft.getMinecraft().displayGuiScreen(GuiOptions(this, Minecraft.getMinecraft().gameSettings))
        }
        if (RenderUtils.isHovered(  sr!!.scaledWidth / 2f - 12 - 45 - 5,
                sr!!.scaledHeight / 2f + 21,
                (24 + 90 + 5).toFloat(),
                20f, mouseX, mouseY)) {
            Minecraft.getMinecraft().shutdown()
        }
    }
}