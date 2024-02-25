package HWID;


import javax.swing.*;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class Text {
    static String h;
    public static void hwid() {
        try {
             h = HWIDUtils.getHWID();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        JOptionPane.showInputDialog(null, "您的HWID为（如果已验证请忽略，尚未验证会退出）", h);
    }
}
