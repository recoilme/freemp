package ru.recoilme.freeamp;

import android.text.TextUtils;

/**
 * Created with IntelliJ IDEA.
 * User: recoilme
 * Date: 27/11/13
 * Time: 16:59
 * To change this template use File | Settings | File Templates.
 */
public class StringUtils {

    public static String capitalizeFully(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        String [] s = TextUtils.split(str, " ");
        if (s.length == 0) {
            return capitalize(str);
        }
        else {
            StringBuilder stringBuilder = new StringBuilder();
            for (String ss:s){
                stringBuilder.append(capitalize(ss));
                stringBuilder.append(" ");
            }
            return stringBuilder.toString();
        }
    }

    public static String capitalize(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        return new StringBuilder(strLen)
                .append(Character.toTitleCase(str.charAt(0)))
                .append(str.substring(1).toLowerCase())
                .toString();
    }
}
