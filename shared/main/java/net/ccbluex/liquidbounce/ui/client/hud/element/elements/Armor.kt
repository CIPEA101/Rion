package net.ccbluex.liquidbounce.ui.client.hud.element.elements;

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import tomk.Hotbar.BlurUtils.blurArea
import org.lwjgl.opengl.GL11
import java.awt.Color

@ElementInfo(name = "Armor")
class Armor(x: Double = -8.0, y: Double = 57.0, scale: Float = 1F):Element() {
    private val radiusValue = FloatValue("Radius", 4.25f, 0f, 10f)
    private val bgalphaValue = IntegerValue("Bg-Alpha", 150, 60, 255)
    private val blurValue = BoolValue("Blur-Value", true)
    private val blurStrength = FloatValue("BlurStrength-Value", 10f, 0f, 40f)
    override fun drawElement(): Border? {
        val width = 70F
        var height = 0F
        val renderItem = mc.renderItem
        var y = 1

        for (index in 0..3) {
            if(mc.thePlayer!!.inventory.armorInventory[index] != null)
                height += 25
        }
        if (blurValue.get()) {
            GL11.glTranslated(-renderX, -renderY, 0.0)
            GL11.glPushMatrix()
            blurArea(renderX.toFloat(), renderY.toFloat(), width, 100F,blurStrength.get())
            GL11.glPopMatrix()
            GL11.glTranslated(renderX, renderY, 0.0)
        }
        RenderUtils.drawShadow(0F,0F,width,0+height)
        RenderUtils.drawRect(0F,0F,width,0+height,Color(0,0,0,bgalphaValue.get()).rgb)
        for (index in 3 downTo 0) {
            val stack = mc.thePlayer!!.inventory.armorInventory[index] ?: continue
            val armorValue = (((stack.maxDamage - stack.itemDamage).toFloat() / stack.maxDamage) * 100f).toDouble()
            val armorValue2 = (((stack.maxDamage - stack.itemDamage).toFloat() / stack.maxDamage))
            Fonts.sfbold28.drawString(Math.round(armorValue).toString() + "%",29,y+8,Color.WHITE.rgb)
            renderItem.renderItemIntoGUI(stack, 5, y + 5)
            RenderUtils.drawRect(22.5.toInt(),y + 10,23.5.toInt(),y + 20,Color(132,125,125,200).rgb)
            RenderUtils.originalRoundedRect(30F, (Fonts.sfbold28.fontHeight + y + 5).toFloat(),
                    30F + armorValue2  * 30F,(Fonts.sfbold28.fontHeight + y + 5 + 3.5).toFloat(),
                    1F,if (armorValue2 > 0.5) Color(10,111,82).rgb else Color(222,191,15).rgb
            )

            y += 25

        }
        return Border(0F,0F,width,100F)
    }
}

