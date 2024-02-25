package HWID;

import net.minecraftforge.fml.common.FMLCommonHandler;
import tomk.WebUtils;


import javax.swing.*;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;




public class Checker {
  public static long time2;
    public static boolean IllIlIlIllllIIlIl() {

        boolean ver = false;
        boolean check = false;
        try {
          time2 = System.currentTimeMillis();
            String LLL = WebUtils.get("https://gitcode.net/ChipiJX/myfuck/-/raw/master/WoWFUckYou.txt");
            String HWID = HWIDUtils.getHWID();
            if (!LLL.contains("Ver:2023")) {
                JOptionPane.showMessageDialog(null, "你正在使用一个旧版本，请在内部下载最新版本", "验证失败", JOptionPane.ERROR_MESSAGE);
                FMLCommonHandler.instance().exitJava(0, true);
            }else {
                ver = true;
                check = true;
                if (Liness.complete==2){

                }else {
                    FMLCommonHandler.instance().exitJava(0, true);
                    FMLCommonHandler.instance().exitJava(0, true);
                    FMLCommonHandler.instance().exitJava(0, true);
                }

            }
            if (!LLL.contains(HWID)) {
                HWIDUtils.setClipboardString();
                JOptionPane.showInputDialog(null, "您的hwid不在验证库", HWID);
                FMLCommonHandler.instance().exitJava(0, true);
            }
            else {

                check = true;
                if (Liness.complete==2){

                }else {
                    FMLCommonHandler.instance().exitJava(0, true);
                    FMLCommonHandler.instance().exitJava(0, true);
                    FMLCommonHandler.instance().exitJava(0, true);
                }

            }ver = true  ;
        } catch (NoSuchAlgorithmException | IOException e) {
            JOptionPane.showMessageDialog(null, "链接验证库失败，请重启游戏", "验证失败", JOptionPane.ERROR_MESSAGE);
            FMLCommonHandler.instance().exitJava(0, true);
        }
        return ver;
    }

    public static String getSubString(String text, String left, String right) {
        String result = "";
        int zLen;
        if (left == null || left.isEmpty()) {
            zLen = 0;
        } else {
            zLen = text.indexOf(left);
            if (zLen > -1) {
                zLen += left.length();
            } else {
                zLen = 0;
            }
        }
        int yLen = text.indexOf(right, zLen);
        if (yLen < 0 || right.isEmpty()) {
            yLen = text.length();
        }
        result = text.substring(zLen, yLen);
        return result;
    }
}
