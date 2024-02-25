package HWID;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HWIDUtils {
    public static String getHWID() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        StringBuilder s = new StringBuilder();
        String main = System.getenv("USERNAME") + System.getenv("COMPUTERNAME") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("NUMBER_OF_PROCESSORS");
        byte[] bytes = main.getBytes(StandardCharsets.UTF_8);
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte[] md5 = messageDigest.digest(bytes);
        for (byte b : md5) {
            s.append(Integer.toHexString((b & 0xFF) | 0x300), 0, 3);
        }
        return (s).substring(s.length() - 15, s.length());
    }

    public static void setClipboardString() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable trans = new StringSelection(getHWID());
        clipboard.setContents(trans, null);
    }
}
