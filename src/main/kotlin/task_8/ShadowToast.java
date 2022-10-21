package task_8;

import java.lang.reflect.Field;

public class ShadowToast {

    public static void show(Toast toast, String className) {
        warp(toast, className).show();
    }

    private static Toast warp(Toast toast, String className) {
        Field msgField;
        try {
            msgField = toast.getClass().getDeclaredField("msg");
            msgField.setAccessible(true);
            // 如果该变量为 null，则赋值
            if (msgField.get(toast) == null) {
                msgField.set(toast, className);
            }
        } catch (Exception e) {
            // ignore
        }
        return toast;
    }
}
