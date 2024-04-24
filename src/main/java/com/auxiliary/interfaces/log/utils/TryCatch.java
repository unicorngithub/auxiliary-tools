package com.auxiliary.interfaces.log.utils;

public class TryCatch {

    public static void run(TryCatchInterface tryCatchInterface, boolean errorLog) {
        try {
            tryCatchInterface.run();
        } catch (Exception e) {
            if (errorLog) {
                e.printStackTrace();
            }
        }
    }

    public static void run(TryCatchInterface tryCatchInterface) {
        run(tryCatchInterface, false);
    }

    public interface TryCatchInterface {
        void run() throws Exception;
    }
}
