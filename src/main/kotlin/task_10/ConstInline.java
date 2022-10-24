package task_10;

import java.util.HashMap;
import java.util.List;

public class ConstInline {
    private static final String TAG = "Demo";

    private static final String LIST_NAME;

    static {
        String simpleName = List.class.getSimpleName();
        LIST_NAME = simpleName;
    }

    public static void main(String[] args) {
        System.out.println(TAG);
        System.out.println(LIST_NAME);
    }
}
