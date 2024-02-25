/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce

import com.google.gson.JsonObject
import HWID.WebUtils
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.api.Wrapper
import net.ccbluex.liquidbounce.api.minecraft.util.IResourceLocation
import net.ccbluex.liquidbounce.cape.CapeAPI.registerCapeService
import net.ccbluex.liquidbounce.event.ClientShutdownEvent
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.special.AntiForge
import net.ccbluex.liquidbounce.features.special.BungeeCordSpoof
import net.ccbluex.liquidbounce.features.special.DonatorCape
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.injection.backend.Backend
import net.ccbluex.liquidbounce.script.ScriptManager
import net.ccbluex.liquidbounce.script.remapper.Remapper.loadSrg
import net.ccbluex.liquidbounce.tabs.BlocksTab
import net.ccbluex.liquidbounce.tabs.ExploitsTab
import net.ccbluex.liquidbounce.tabs.HeadsTab
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.client.hud.HUD.Companion.createDefault
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.ClassUtils.hasForge
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.misc.HttpUtils
import net.minecraftforge.fml.common.FMLCommonHandler
import tomk.AnimationHandler
import tomk.CombatManager
import HWID.Checker
import HWID.Liness
import HWID.Text
import com.sun.jna.Native
import com.sun.jna.Pointer
import java.awt.AWTException
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.HashMap
import javax.swing.JOptionPane
import kotlin.system.exitProcess

object LiquidBounce {

    // Client information


    const val CLIENT_NAME = "Rion"
    const val CLIENT_VERSION = 1
    const val CLIENT_CREATOR = "114514"
    const val MINECRAFT_VERSION = Backend.MINECRAFT_VERSION
    const val CLIENT_CLOUD = "https://cloud.liquidbounce.net/LiquidBounce"
    var isStarting = false

    // Managers
    lateinit var combatManager: CombatManager
    lateinit var moduleManager: ModuleManager
    lateinit var commandManager: CommandManager
    lateinit var eventManager: EventManager
    lateinit var fileManager: FileManager
    lateinit var scriptManager: ScriptManager

    // HUD & ClickGUI
    lateinit var hud: HUD
    lateinit var animationHandler: AnimationHandler
    lateinit var clickGui: ClickGui
    var playTimeStart: Long = 0

    // Update information
    var latestVersion = 0

    // Menu Background
    var background: IResourceLocation? = null

    lateinit var wrapper: Wrapper

    // Discord RPC

    @Throws(AWTException::class)
    private fun displayTray(Title: String, Text: String, type: TrayIcon.MessageType) {
        val tray = SystemTray.getSystemTray()
        val image = Toolkit.getDefaultToolkit().createImage("icon.png")
        val trayIcon = TrayIcon(image, "Tray Demo")
        trayIcon.isImageAutoSize = true
        trayIcon.toolTip = "System tray icon demo"
        tray.add(trayIcon)
        trayIcon.displayMessage(Title, Text, type)
    }

    /**
     * Execute if client will be started
     */
    fun startClient() {

        var QQNumber = "dawdaw"

        //Text.hwid()
        //Liness.time1 = System.currentTimeMillis()
        //Liness.getrainbow()

        //if (!Checker.IllIlIlIllllIIlIl()) {
            //FMLCommonHandler.instance().exitJava(0, false)
        //}

        isStarting = true

        /**
         * 取两个文本之间的文本值
         *
         * @param text  源文本 比如：欲取全文本为 12345
         * @param left  文本前面
         * @param right 后面文本
         * @return 返回 String
         */
        fun getSubString(text: String, left: String?, right: String?): String? {
            var result = ""
            var zLen: Int
            if (left == null || left.isEmpty()) {
                zLen = 0
            } else {
                zLen = text.indexOf(left)
                if (zLen > -1) {
                    zLen += left.length
                } else {
                    zLen = 0
                }
            }
            var yLen = text.indexOf(right!!, zLen)
            if (yLen < 0 || right == null || right.isEmpty()) {
                yLen = text.length
            }
            result = text.substring(zLen, yLen)
            return result
        }

        /****
         * 过滤有效qq窗体信息
         * @param windowText
         * @return 是否为qq窗体信息
         */
        fun _filterQQInfo(windowText: String): Boolean {
            return if (windowText.startsWith("qqexchangewnd_shortcut_prefix_")) true else false
        }

        /******
         * 获取当前登录qq的信息
         * @return map集合
         */
        fun getLoginQQList(): Map<String, String>? {
            val QQNumber1 = arrayOfNulls<String>(1)
            val map: MutableMap<String, String> = HashMap(5)
            val user32 = WebUtils.User32.INSTANCE
            user32.EnumWindows({ hWnd: Pointer, userData: Pointer? ->
                val windowText = ByteArray(512)
                user32.GetWindowTextA(hWnd, windowText, 512)
                val wText = Native.toString(windowText)
                if (_filterQQInfo(wText)) {
                    map[hWnd.toString()] = wText.substring(wText.indexOf("qqexchangewnd_shortcut_prefix_") + "qqexchangewnd_shortcut_prefix_".length)
                }
                QQNumber1[0] = getSubString(map.toString(), "=", "}")
                QQNumber = QQNumber1[0].toString()
                true
            }, null)
            return map
        }

        getLoginQQList()



        ClientUtils.getLogger().info("Starting $CLIENT_NAME b$CLIENT_VERSION, by $CLIENT_CREATOR")
        isStarting = true
        ClientUtils.getLogger().info("Starting $CLIENT_NAME b$CLIENT_VERSION, by $CLIENT_CREATOR")

        // Create file manager
        fileManager = FileManager()

        // Crate event manager
        eventManager = EventManager()

        // Register listeners
        eventManager.registerListener(RotationUtils())
        eventManager.registerListener(AntiForge())
        eventManager.registerListener(BungeeCordSpoof())
        eventManager.registerListener(DonatorCape())
        eventManager.registerListener(InventoryUtils())

        // Create command manager
        commandManager = CommandManager()

        // Load client fonts
        Fonts.loadFonts()

        // Setup module manager and register modules
        moduleManager = ModuleManager()
        moduleManager.registerModules()
        animationHandler = AnimationHandler()
        // Remapper
        try {
            loadSrg()

            // ScriptManager
            scriptManager = ScriptManager()
            scriptManager.loadScripts()
            scriptManager.enableScripts()
        } catch (throwable: Throwable) {
            ClientUtils.getLogger().error("Failed to load scripts.", throwable)
        }

        // Register commands
        commandManager.registerCommands()

        // Load configs
        fileManager.loadConfigs(fileManager.modulesConfig, fileManager.valuesConfig, fileManager.accountsConfig,
            fileManager.friendsConfig, fileManager.xrayConfig, fileManager.shortcutsConfig)

        // ClickGUI
        clickGui = ClickGui()
        fileManager.loadConfig(fileManager.clickGuiConfig)

        // Tabs (Only for Forge!)
        if (hasForge()) {
            BlocksTab()
            ExploitsTab()
            HeadsTab()
        }

        // Register capes service
        try {
            registerCapeService()
        } catch (throwable: Throwable) {
            ClientUtils.getLogger().error("Failed to register cape service", throwable)
        }

        // Set HUD
        hud = createDefault()
        fileManager.loadConfig(fileManager.hudConfig)

        // Disable optifine fastrender
        ClientUtils.disableFastRender()

        try {
            // Read versions json from cloud
            val jsonObj = JsonParser()
                .parse(HttpUtils.get("$CLIENT_CLOUD/versions.json"))

            // Check json is valid object and has current minecraft version
            if (jsonObj is JsonObject && jsonObj.has(MINECRAFT_VERSION)) {
                // Get official latest client version
                latestVersion = jsonObj[MINECRAFT_VERSION].asInt
            }
        } catch (exception: Throwable) { // Print throwable to console
            ClientUtils.getLogger().error("Failed to check for updates.", exception)
        }

        // Load generators
        GuiAltManager.loadGenerators()
            /*try {
            if (wight("https://gitcode.net/ChipiJX/myfuck/-/raw/master/WoWFUckYou.txt").contains(QQNumber)) {
                displayTray("Rion", "Rion 1.12", TrayIcon.MessageType.WARNING)
            } else {
                displayTray("Rion", CLIENT_NAME, TrayIcon.MessageType.ERROR)
                JOptionPane.showMessageDialog(null, "验证失败，请联系管理员，并重新启动客户端", "Rion", JOptionPane.ERROR_MESSAGE)
                exitProcess(0)
            }
        } catch (e: IOException) {
            JOptionPane.showMessageDialog(null, "错误，请重新启动客户端", "Rion", JOptionPane.ERROR_MESSAGE)
            exitProcess(0)
        }*/

        // Set is starting status
        isStarting = false
    }

    /**
     * Execute if client will be stopped
     */
    fun stopClient() {
        // Call client shutdown
        eventManager.callEvent(ClientShutdownEvent())

        // Save all available configs
        fileManager.saveAllConfigs()
    }
    fun wight(wight: String?): String {
        val con = URL(wight).openConnection() as HttpURLConnection
        con.requestMethod = "GET"
        con.setRequestProperty("User-Agent", "Mozilla/5.0")
        val `in` = BufferedReader(InputStreamReader(con.inputStream))
        var inputLine: String?
        val response = StringBuilder()
        while (`in`.readLine().also { inputLine = it } != null) {
            response.append(inputLine)
            response.append("\n")
        }
        `in`.close()
        return response.toString()
    }
}