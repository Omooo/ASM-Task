package task_7;

public class CatchMethodInvoke {

    public int calc() {
        return 1 / 0;
    }

    // TODO: 2022/10/21 过不了该测试用例
//    public int catchCalc() {
//        try {
//            return  1 / 0;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return 0;
//        }
//    }

//    public String throwException() {
//        throw new RuntimeException("");
//    }
//
//    public void show() {
//
//    }

}
