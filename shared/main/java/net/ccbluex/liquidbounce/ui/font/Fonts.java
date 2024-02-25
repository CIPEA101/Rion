/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.font;

import com.google.gson.*;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.api.minecraft.client.gui.IFontRenderer;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.ccbluex.liquidbounce.utils.misc.HttpUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Fonts extends MinecraftInstance {

    @FontDetails(fontName = "Minecraft Font")
    public static final IFontRenderer minecraftFont = mc.getFontRendererObj();
    private static final HashMap<FontInfo, IFontRenderer> CUSTOM_FONT_RENDERERS = new HashMap<>();
    @FontDetails(fontName = "sfbold100", fontSize = 40)
    public static IFontRenderer sfbold100;
    @FontDetails(fontName = "sfbold80", fontSize = 40)
    public static IFontRenderer sfbold80;
    @FontDetails(fontName = "sfbold40", fontSize = 40)
    public static IFontRenderer sfbold40;
    @FontDetails(fontName = "sfbold35", fontSize = 40)
    public static IFontRenderer sfbold35;
    @FontDetails(fontName = "sfbold30", fontSize = 40)
    public static IFontRenderer sfbold30;
    @FontDetails(fontName = "sfbold28", fontSize = 40)
    public static IFontRenderer sfbold28;
    @FontDetails(fontName = "Tenacitybold", fontSize = 18)
    public static IFontRenderer tenacitybold35;
    @FontDetails(fontName = "Tenacitybold", fontSize = 18)
    public static IFontRenderer tenacitybold30;
    @FontDetails(fontName = "Tenacitybold", fontSize = 20)
    public static IFontRenderer tenacitybold40;
    @FontDetails(fontName = "Tenacitybold", fontSize = 21)
    public static IFontRenderer tenacitybold43;
    @FontDetails(fontName = "Tenacitycheck", fontSize = 60)
    public static IFontRenderer tenacitycheck60;
    @FontDetails(fontName = "Roboto Medium", fontSize = 35)
    public static IFontRenderer font35;

    @FontDetails(fontName = "Roboto Medium", fontSize = 40)
    public static IFontRenderer font40;
    @FontDetails(fontName = "Roboto Bold", fontSize = 180)
    public static IFontRenderer fontBold180;
    @FontDetails(fontName = "Ico", fontSize = 20)
    public static IFontRenderer ico1;

    public static void loadFonts() {
        long l = System.currentTimeMillis();

        ClientUtils.getLogger().info("Loading Fonts.");

        downloadFonts();
        sfbold100 = classProvider.wrapFontRenderer(new GameFontRenderer(getsfbold(50)));
        sfbold80 = classProvider.wrapFontRenderer(new GameFontRenderer(getsfbold(40)));
        sfbold40 = classProvider.wrapFontRenderer(new GameFontRenderer(getsfbold(20)));
        sfbold35 = classProvider.wrapFontRenderer(new GameFontRenderer(getsfbold(18)));
        sfbold30 = classProvider.wrapFontRenderer(new GameFontRenderer(getsfbold(16)));
        sfbold28 = classProvider.wrapFontRenderer(new GameFontRenderer(getsfbold(14)));
        tenacitybold35 = classProvider.wrapFontRenderer(new GameFontRenderer(gett(18)));
        tenacitybold30 = classProvider.wrapFontRenderer(new GameFontRenderer(gett(16)));
        tenacitybold40 = classProvider.wrapFontRenderer(new GameFontRenderer(gett(20)));
        tenacitybold43 = classProvider.wrapFontRenderer(new GameFontRenderer(gett(21)));
        tenacitycheck60 = classProvider.wrapFontRenderer(new GameFontRenderer(getf(30)));
        ico1 = classProvider.wrapFontRenderer(new GameFontRenderer(gets(30)));
        font35 = classProvider.wrapFontRenderer(new GameFontRenderer(getFont("Roboto-Medium.ttf", 18)));
        font40 = classProvider.wrapFontRenderer(new GameFontRenderer(getFont("Roboto-Medium.ttf", 20)));
        fontBold180 = classProvider.wrapFontRenderer(new GameFontRenderer(getFont("Roboto-Bold.ttf", 60)));
        net.ccbluex.liquidbounce.feng.FontLoaders.initFonts();
        try {
            CUSTOM_FONT_RENDERERS.clear();

            final File fontsFile = new File(LiquidBounce.fileManager.fontsDir, "fonts.json");

            if (fontsFile.exists()) {
                final JsonElement jsonElement = new JsonParser().parse(new BufferedReader(new FileReader(fontsFile)));

                if (jsonElement instanceof JsonNull)
                    return;

                final JsonArray jsonArray = (JsonArray) jsonElement;

                for (final JsonElement element : jsonArray) {
                    if (element instanceof JsonNull)
                        return;

                    final JsonObject fontObject = (JsonObject) element;

                    Font font = getFont(fontObject.get("fontFile").getAsString(), fontObject.get("fontSize").getAsInt());

                    CUSTOM_FONT_RENDERERS.put(new FontInfo(font), classProvider.wrapFontRenderer(new GameFontRenderer(font)));
                }
            } else {
                fontsFile.createNewFile();

                final PrintWriter printWriter = new PrintWriter(new FileWriter(fontsFile));
                printWriter.println(new GsonBuilder().setPrettyPrinting().create().toJson(new JsonArray()));
                printWriter.close();
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }

        ClientUtils.getLogger().info("Loaded Fonts. (" + (System.currentTimeMillis() - l) + "ms)");
    }

    private static void downloadFonts() {
        try {
            final File outputFile = new File(LiquidBounce.fileManager.fontsDir, "roboto.zip");

            if (!outputFile.exists()) {
                ClientUtils.getLogger().info("Downloading fonts...");
                HttpUtils.download(LiquidBounce.CLIENT_CLOUD + "/fonts/Roboto.zip", outputFile);
                ClientUtils.getLogger().info("Extract fonts...");
                extractZip(outputFile.getPath(), LiquidBounce.fileManager.fontsDir.getPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static Font getsfbold(int size) {
        Font font;
        try {
            InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("liquidbounce/font/sfbold.ttf")).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(0, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("Posterama", 0, size);
        }
        return font;
    }
    private static Font gett(int size) {
        Font font;
        try {
            InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("liquidbounce/font/tenacitybold.ttf")).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(0, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("Posterama", 0, size);
        }
        return font;
    }
    private static Font getf(int size) {
        Font font;
        try {
            InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("liquidbounce/font/tenacitycheck.ttf")).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(0, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("Posterama", 0, size);
        }
        return font;
    }
    private static Font gets(int size) {
        Font font;
        try {
            InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("liquidbounce/font/icon.ttf")).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(0, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("Posterama", 0, size);
        }
        return font;
    }
    private static Font gets2(int size) {
        Font font;
        try {
            InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("liquidbounce/font/icon2.ttf")).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(0, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("Posterama", 0, size);
        }
        return font;
    }
    public static IFontRenderer getFontRenderer(final String name, final int size) {
        for (final Field field : Fonts.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);

                Object o = field.get(null);

                if (o instanceof IFontRenderer) {
                    FontDetails fontDetails = field.getAnnotation(FontDetails.class);

                    if (fontDetails.fontName().equals(name) && fontDetails.fontSize() == size)
                        return (IFontRenderer) o;
                }
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return CUSTOM_FONT_RENDERERS.getOrDefault(new FontInfo(name, size), minecraftFont);
    }

    public static FontInfo getFontDetails(final IFontRenderer fontRenderer) {
        for (final Field field : Fonts.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);

                final Object o = field.get(null);

                if (o.equals(fontRenderer)) {
                    final FontDetails fontDetails = field.getAnnotation(FontDetails.class);

                    return new FontInfo(fontDetails.fontName(), fontDetails.fontSize());
                }
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        for (Map.Entry<FontInfo, IFontRenderer> entry : CUSTOM_FONT_RENDERERS.entrySet()) {
            if (entry.getValue() == fontRenderer)
                return entry.getKey();
        }

        return null;
    }

    public static List<IFontRenderer> getFonts() {
        final List<IFontRenderer> fonts = new ArrayList<>();

        for (final Field fontField : Fonts.class.getDeclaredFields()) {
            try {
                fontField.setAccessible(true);

                final Object fontObj = fontField.get(null);

                if (fontObj instanceof IFontRenderer) fonts.add((IFontRenderer) fontObj);
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        fonts.addAll(Fonts.CUSTOM_FONT_RENDERERS.values());

        return fonts;
    }

    private static Font getFont(final String fontName, final int size) {
        try {
            final InputStream inputStream = new FileInputStream(new File(LiquidBounce.fileManager.fontsDir, fontName));
            Font awtClientFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            awtClientFont = awtClientFont.deriveFont(Font.PLAIN, size);
            inputStream.close();
            return awtClientFont;
        } catch (final Exception e) {
            e.printStackTrace();

            return new Font("default", Font.PLAIN, size);
        }
    }

    private static void extractZip(final String zipFile, final String outputFolder) {
        final byte[] buffer = new byte[1024];

        try {
            final File folder = new File(outputFolder);

            if (!folder.exists()) folder.mkdir();

            final ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile));

            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                File newFile = new File(outputFolder + File.separator + zipEntry.getName());
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fileOutputStream = new FileOutputStream(newFile);

                int i;
                while ((i = zipInputStream.read(buffer)) > 0)
                    fileOutputStream.write(buffer, 0, i);

                fileOutputStream.close();
                zipEntry = zipInputStream.getNextEntry();
            }

            zipInputStream.closeEntry();
            zipInputStream.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static class FontInfo {
        private final String name;
        private final int fontSize;

        public FontInfo(String name, int fontSize) {
            this.name = name;
            this.fontSize = fontSize;
        }

        public FontInfo(Font font) {
            this(font.getName(), font.getSize());
        }

        public String getName() {
            return name;
        }

        public int getFontSize() {
            return fontSize;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FontInfo fontInfo = (FontInfo) o;

            if (fontSize != fontInfo.fontSize) return false;
            return Objects.equals(name, fontInfo.name);
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + fontSize;
            return result;
        }
    }

}