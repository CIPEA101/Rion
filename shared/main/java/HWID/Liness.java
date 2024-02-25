package HWID;

import javax.swing.*;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
public class Liness {
   public static int complete;
    public static long time1 ;
    public static void getrainbow() {

       try {
           if ((Math.floor(Checker.time2/1000)-Math.floor(time1 /1000)>10)){
               JOptionPane.showMessageDialog(null, "服务器链接超时", "超时", 0);
               System.exit(1);  System.exit(1);  System.exit(1);

           }
           String LLL = WebUtils.get("https://gitcode.net/ChipiJX/myfuck/-/raw/master/WoWFUckYou.txt");
        String HWID = HWIDUtils.getHWID();

        if (!LLL.contains("Ver:2023")) {


        }else{complete++;}
            if (!LLL.contains(HWID)) {
                HWIDUtils.setClipboardString();

            }else{complete++;}
        }catch(NoSuchAlgorithmException | IOException e){
           ;}}}



