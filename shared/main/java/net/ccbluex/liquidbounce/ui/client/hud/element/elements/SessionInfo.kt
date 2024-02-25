@file:Suppress("SENSELESS_COMPARISON")

package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.tomk.AutoL
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.font.Fonts


import net.ccbluex.liquidbounce.utils.render.RenderUtils

import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import tomk.Recorder
import tomk.ShadowUtils

import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*

/**
 * CustomHUD Armor element
 *
 * Shows a horizontal display of current armor
 */

@ElementInfo(name = "GameInfo")
class SessionInfo(x: Double = 10.0, y: Double = 10.0, scale: Float = 1F) : Element(x, y, scale) {
    private val radiusValue = FloatValue("Radius", 4.25f, 0f, 10f)
    private val shadowValue = FloatValue("shadow-Value", 10F, 0f, 20f)

    private val shadowColorMode = ListValue("Shadow-Color", arrayOf("Background", "Custom"), "Background")
    private val shadowColorRedValue = IntegerValue("Shadow-Red", 0, 0, 255)
    private val shadowColorGreenValue = IntegerValue("Shadow-Green", 111, 0, 255)
    private val shadowColorBlueValue = IntegerValue("Shadow-Blue", 255, 0, 255)

    private val redValue = IntegerValue("Line-R", 255, 0, 255)
    private val greenValue = IntegerValue("Line-G", 255, 0, 255)
    private val blueValue = IntegerValue("Line-B", 255, 0, 255)
    private val colorRedValue2 = IntegerValue("Line-R2", 0, 0, 255)
    private val colorGreenValue2 = IntegerValue("Line-G2", 111, 0, 255)
    private val colorBlueValue2 = IntegerValue("Line-B2", 255, 0, 255)
    /**
     * Draw element
     */
    override fun drawElement(): Border {
        val x2 = 145.0 *1.15
        val y2 = (Fonts.tenacitybold35.fontHeight * 5 + 18)*1.15
        val durationInMillis: Long = System.currentTimeMillis() - LiquidBounce.playTimeStart
        val second = durationInMillis / 1000 % 60
        val minute = durationInMillis / (1000 * 60) % 60
        val hour = durationInMillis / (1000 * 60 * 60) % 24
        val time: String
        time = String.format("%02dh %02dm %02ds", hour, minute, second)
            val DATE_FORMAT = SimpleDateFormat("HH:mm:ss")



        RenderUtils.drawRoundedRect(-2F,0F,x2.toFloat(),y2.toFloat(),radiusValue.get(),Color(80, 80, 80,40).rgb)
        RenderUtils.drawGradientSideways(
            2.44,
            Fonts.tenacitybold35.fontHeight + 2.5 + 0.0,
            x2 + -2.44f,
            Fonts.tenacitybold35.fontHeight + 2.5 + 1.16f,Color(redValue.get(),greenValue.get(),blueValue.get()).rgb, Color(colorRedValue2.get(),colorGreenValue2.get(),colorBlueValue2.get()).rgb)
        GL11.glTranslated(-renderX, -renderY, 0.0)
        GL11.glScalef( 1F,  1F,  1F)
        GL11.glPushMatrix()
        ShadowUtils.shadow(shadowValue.get(),{
            GL11.glPushMatrix()
            GL11.glTranslated(renderX, renderY, 0.0)
            GL11.glScalef(scale, scale, scale)
            RenderUtils.originalRoundedRect(-2F,0F,x2.toFloat(),y2.toFloat(),radiusValue.get(),
                if (shadowColorMode.get().equals("background", true))
                    Color(80, 80, 80,40).rgb
                else
                    Color(shadowColorRedValue.get(), shadowColorGreenValue.get(), shadowColorBlueValue.get()).rgb)
            GL11.glPopMatrix()
        },{
            GL11.glPushMatrix()
            GL11.glTranslated(renderX, renderY, 0.0)
            GL11.glScalef(scale, scale, scale)
            GlStateManager.enableBlend()
            GlStateManager.disableTexture2D()
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
            RenderUtils.fastRoundedRect(-2F,0F,x2.toFloat(),y2.toFloat(),radiusValue.get())
            GlStateManager.enableTexture2D()
            GlStateManager.disableBlend()
            GL11.glPopMatrix()
        })
        GL11.glPopMatrix()
        GL11.glScalef(scale, scale, scale)
        GL11.glTranslated(renderX, renderY, 0.0)

        val autoL = LiquidBounce.moduleManager.getModule(AutoL::class.java) as AutoL
        Fonts.tenacitybold35.drawCenteredString("Session info", (x2/2).toFloat(),2.5F,Color.WHITE.rgb)
        Fonts.tenacitybold35.drawStringWithShadow("Play Time: ",2,
            (Fonts.tenacitybold35.fontHeight * 1.15 +8f).toInt(),Color.WHITE.rgb)
        Fonts.tenacitybold35.drawStringWithShadow("${DATE_FORMAT.format(Date(System.currentTimeMillis() - Recorder.startTime - 8000L * 3600L))}",
            (x2.toFloat()-Fonts.tenacitybold35.getStringWidth("${
                DATE_FORMAT.format(Date(System.currentTimeMillis() - Recorder.startTime - 8000L * 3600L))
            }\"")).toInt(), (Fonts.tenacitybold35.fontHeight * 1.15 +8f).toInt(),Color.WHITE.rgb)
          Fonts.tenacitybold35.drawStringWithShadow("Players Killed: ", 2,
            (  Fonts.tenacitybold35.fontHeight * 2 * 1.15 + 8f + 4f).toInt(), Color.WHITE.rgb)
        Fonts.tenacitybold35.drawStringWithShadow(autoL.kills.toString(),
            (x2.toFloat()-Fonts.tenacitybold35.getStringWidth((Recorder.killCounts).toString())-3f).toInt(), (  Fonts.tenacitybold35.fontHeight * 2 * 1.15 +8f+ 4f).toInt(), Color.WHITE.rgb)
          Fonts.tenacitybold35.drawStringWithShadow("Win: " , 2,
            (  Fonts.tenacitybold35.fontHeight * 3 * 1.15  + 8f + 4f*2 ).toInt(), Color.WHITE.rgb)
        Fonts.tenacitybold35.drawStringWithShadow(Recorder.totalPlayed.toString(),
            (x2.toFloat()-Fonts.tenacitybold35.getStringWidth(Recorder.totalPlayed.toString())-3f).toInt(),   (  Fonts.tenacitybold35.fontHeight * 3 * 1.15 +8f+ 4f*2).toInt(), Color.WHITE.rgb)
          Fonts.tenacitybold35.drawStringWithShadow("Total: " , 2,
            (  Fonts.tenacitybold35.fontHeight * 4 * 1.15 + 8f + 4f*3).toInt(), Color.WHITE.rgb)
        Fonts.tenacitybold35.drawStringWithShadow(Recorder.totalPlayed2.toString(),
            (x2.toFloat()-Fonts.tenacitybold35.getStringWidth(Recorder.totalPlayed2.toString())-3f).toInt(), (  Fonts.tenacitybold35.fontHeight * 4 * 1.15 + 8f+ 4f*3).toInt(), Color.WHITE.rgb)


    return Border(-2F, 0F, x2.toFloat(), y2.toFloat())
    }

}
