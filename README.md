---
ASM-Task 
---

#### 目录

1. 使用 Core&Tree Api 读取 ArrayList
2. 使用 Core&Tree Api 生成 Main 类
3. 使用 Core&Tree Api 输出方法的入参和出参
4. 使用 Core&Tree Api 删除方法里面的日志输出语句
4. 使用 Core&Tree Api 输出特定方法耗时
4. 使用 Core&Tree Api 统一线程重命名
4. 使用 Core&Tree Api 给特定方法加上 try-catch 块
4. 使用 Core&Tree Api 替换方法调用，为系统类或第三方库代码兜底
4. 使用 Core&Tree Api 进行序列化检查
4. 使用 Core&Tree Api 检查是否有调用不存在的方法

#### 任务一：读取 ArrayList 类

掌握最基础的 class 表现形式。

#### 任务二：生成 Main 类

使用 ASM Api 生成以下 Main 类：

```java
public class Main {

    private static final String TAG = "Main";

    public static void main(String[] args) {
        System.out.println("Hello World.");
    }
}
```

#### 任务三：输出方法的入参和出参

修改前的 Java 文件：

```java
public class PrintMethodParams {
    public String print(String name, int age) {
        return name + ": " + age;
    }
}
```

目标生成的类文件，且需要验证反射该 class 文件的正确性：

```java
public class PrintMethodParamsCoreClass {
    public PrintMethodParamsCoreClass() {
    }

    public String print(String var1, int var2) {
        System.out.println(var1);
        System.out.println(var2);
        String var10000 = var1 + ": " + var2;
        System.out.println(var10000);
        return var10000;
    }
}
```

#### 任务四：删除方法里面的日志输出语句

修改前的 Java 文件：

```java
public class DeleteLogInvoke {

    public String print(String name, int age) {
        System.out.println(name);
        String result = name + ": " + age;
        System.out.println(result);
        System.out.println("Delete current line.");
        System.out.println("name = " + name + ", age = " + age);
        System.out.printf("name: %s%n", name);
        System.out.println(String.format("age: %d", age));
        return result;
    }
}
```

目标生成的类文件，且需要验证反射该 class 文件的正确性：

```java
public class DeleteLogInvokeCoreClass {
    public DeleteLogInvokeCoreClass() {
    }

    public String print(String var1, int var2) {
        String var3 = var1 + ": " + var2;
        return var3;
    }
}
```

#### 任务五：输出特定方法的方法耗时

修改前的 Java 文件：

```java
public class MeasureMethodTime {

    @MeasureTime
    public void measure() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

目标生成的类文件，且需要验证反射该 class 的正确性：

```java
public class MeasureMethodTimeCoreClass {
    public MeasureMethodTimeCoreClass() {
    }

    @MeasureTime
    public void measure() {
        long var1 = System.currentTimeMillis();

        try {
            Thread.sleep(2000L);
        } catch (InterruptedException var8) {
            RuntimeException var10000 = new RuntimeException(var8);
            long var4 = System.currentTimeMillis();
            System.out.println(var4 - var1);
            throw var10000;
        }

        long var6 = System.currentTimeMillis();
        System.out.println(var6 - var1);
    }
}
```

#### 任务六：线程重命名

修改前的 Java 文件：

```java
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
    }

}
```

目标生成的类文件，且需要验证反射该 class 的正确性：

```java
public class ThreadReNameCoreClass {
    public ThreadReNameCoreClass() {
    }

    public static void main(String[] var0) {
        (new ShadowThread(new InternalRunnable(), "sample/ThreadReNameCoreClass#main-Thread-0")).start();
        ShadowThread var1 = new ShadowThread(new InternalRunnable(), "thread0", "sample/ThreadReNameCoreClass#main-Thread-1");
        System.out.println("thread0: " + var1.getName());
        var1.start();
        ShadowThread var2 = new ShadowThread(new InternalRunnable(), "sample/ThreadReNameCoreClass#main-Thread-2");
        var2.setName(ShadowThread.makeThreadName("thread1", "sample/ThreadReNameCoreClass#main-Thread-3"));
        System.out.println("thread1: " + var2.getName());
        var2.start();
    }
}
```

#### 任务七：给特定方法加上 try-catch 块

修改前的 Java 文件：

```java
public class CatchMethodInvoke {

    public int calc() {
        return 1 / 0;
    }
}
```

目标生成的类文件，且需要验证反射该 class 的正确性：

```java
public class CatchMethodInvokeCoreClass {
    public CatchMethodInvokeCoreClass() {
    }

    public int calc() {
        try {
            return 1 / 0;
        } catch (Exception var2) {
            var2.printStackTrace();
            return 0;
        }
    }
}
```

#### 任务八：使用 Core&Tree Api 替换方法调用，为系统类或第三方库代码兜底

这个任务的思路来源于：[Booster 修复系统 Bug - Android 7.1 Toast 崩溃](https://booster.johnsonlee.io/zh/guide/bugfixing/toast-crash-on-android-25.html#%E6%A0%B9%E6%9C%AC%E5%8E%9F%E5%9B%A0)

其实延续上个任务的思路，为特定方法添加 try-catch 也能解决，但是 try-catch 过于粗暴，替换方法调用的方式灵活性更好些（其实在任务六里也有用到该思路）。

修改前的 Java 文件：

```java
public class ReplaceMethodInvoke {
    public static void main(String[] args) {
        // throw NPE
        new Toast().show();
    }
}
```

目标生成的类文件，且需要验证反射该 class 的正确性：

```java
public class ReplaceMethodInvokeCoreClass {
    public ReplaceMethodInvokeCoreClass() {
    }

    public static void main(String[] var0) {
        ShadowToast.show(new Toast(), "sample/ReplaceMethodInvokeCoreClass");
    }
}
```

#### 任务九：序列化检查

这个任务的思路来源于：[ByteX - 序列化检查](https://github.com/bytedance/ByteX/blob/master/serialization-check-plugin/README-zh.md)

对于 Java 的序列化，有以下几条规则需要遵守（也就是 IDEA Inspections 里的几条规则）：

1. 实现了 Serializable 的类未提供 serialVersionUID 字段
2. 实现了 Serializable 的类包含非 transient、static 的字段，这些字段并未实现 Serializable 接口
3. 未实现 Serializable 接口的类，包含 transient、serialVersionUID 字段
4. 实现了 Serializable 的非静态内部类，它的外层类并未实现 Serializable 接口

针对以下代码：

```java
public class SerializationCheck implements Serializable {

    private ItemBean1 itemBean1;
    private ItemBean2 itemBean2;
    private transient ItemBean3 itemBean3;
    private String name;
    private int age;

    static class ItemBean1 {
    }

    static class ItemBean2 implements Serializable {
    }

    static class ItemBean3 {
    }
}
```

检查输出：

```java
Attention: Non-serializable field 'itemBean1' in a Serializable class [sample/SerializationCheckCoreClass]
Attention: This [sample/SerializationCheckCoreClass] class is serializable, but does not define a 'serialVersionUID' field.
```

#### 任务十：检查是否有调用不存在的方法

这个任务的思路来源于：[ByteX - 非法引用检查](https://github.com/bytedance/ByteX/blob/master/refer-check-plugin/README-zh.md)

不过示例里写的相对简单很多，怎么模拟引用找不到但编译不会报错的情况呢？我们可以借助于依赖方式：

```groovy
    compileOnly(project(":library1"))
    runtimeOnly(fileTree("libs") { include("*.jar") })
```

针对以下代码：

```java
fun main() {
    // throw IllegalAccessError:
    // class task_11.ReferCheckKt tried to access private method LibraryClass.testPrivateMethod()V
    LibraryClass().testPrivateMethod()
    // throw NoSuchMethodError: LibraryClass.testModifyParamsMethod()V
    LibraryClass().testModifyParamsMethod()
    // NoSuchMethodError: LibraryClass.testModifyReturnTypeMethod()V
    LibraryClass().testModifyReturnTypeMethod()
}
```

检查输出：

```
在 sample/ReferCheckTreeClass$testPrivateMethod 方法里监测到非法调用: 
    IllegalAccessError: 试图调用 private final LibraryClass.testPrivateMethod()V 方法.
   
在 sample/ReferCheckTreeClass$testModifyParamsMethod 方法里监测到非法调用: 
    NoSuchMethodError: 找不到 LibraryClass.testModifyParamsMethod()V 方法.
    
在 sample/ReferCheckTreeClass$testModifyReturnTypeMethod 方法里监测到非法调用: 
    NoSuchMethodError: 找不到 LibraryClass.testModifyReturnTypeMethod()V 方法.
```

#### 任务十一：
