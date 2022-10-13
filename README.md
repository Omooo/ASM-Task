---
ASM-Task 
---

#### 目录

1. 使用 Core&Tree Api 读取 ArrayList
2. 使用 Core&Tree Api 生成 Main 类
3. 使用 Core&Tree Api 输出方法的入参和出参
4. 使用 Core&Tree Api 

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
        return result;
    }
}
```

