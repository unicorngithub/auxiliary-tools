package com.auxiliary.interfaces.log.utils;

public class LogUtils {

    public static final String LINE_FEED = "\r\n";

    public static StringBuffer log() {
        StringBuffer stringBuffer = new StringBuffer();
        return stringBuffer;
    }

    public static StringBuffer appendLog(String str) {
        StringBuffer stringBuffer = new StringBuffer(str);
        return stringBuffer;
    }

    public static StringBuffer appendLogln(String str) {
        StringBuffer stringBuffer = new StringBuffer(str + LINE_FEED);
        return stringBuffer;
    }

    public static StringBuffer append(StringBuffer stringBuffer, String str) {
        stringBuffer.append(str);
        return stringBuffer;
    }

    public static StringBuffer appendln(StringBuffer stringBuffer, String str) {
        stringBuffer.append(str + LINE_FEED);
        return stringBuffer;
    }

}
