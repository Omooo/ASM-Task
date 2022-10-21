package task_7;

public class CatchMethodInvoke {

    public int calc() {
        return 1 / 0;
    }

    // 以下用例都是可以通过的.

//    public int catchCalc() {
//        try {
//            return 1 / 0;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return 0;
//        }
//    }
//
//    public String throwException() {
//        throw new RuntimeException("");
//    }
//
//    public void show() {
//
//    }

}
