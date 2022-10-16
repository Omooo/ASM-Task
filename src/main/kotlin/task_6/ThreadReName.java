package task_6;

public class ThreadReName {

    public static void main(String[] args) {
        // 不带线程名称
        new Thread(new InternalRunnable()).start();

        // 带线程名称
        Thread thread0 = new Thread(new InternalRunnable(), "thread0");
        System.out.println("thread0: " + thread0.getName());
        thread0.start();

        Thread thread1 = new Thread(new InternalRunnable());
        // 设置线程名字
        thread1.setName("thread1");
        System.out.println("thread1: " + thread1.getName());
        thread1.start();

        // 以下情况反射时会抛 InvocationTargetException 异常，原因是内外部类的类加载器不一致
        // 一般不需要考虑该问题，因为 ThreadReNameCoreClass 是使用自定义的类加载器加载的
//        new Thread(() -> {
//
//        }).start();
    }

}
