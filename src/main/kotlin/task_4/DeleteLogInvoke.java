package task_4;

/**
 * 删除方法里的 sout 语句
 */
public class DeleteLogInvoke {

    public String print(String name, int age) {
        System.out.println(name);
        String result = name + ": " + age;
        System.out.println(result);
        System.out.println("Delete current line.");
        System.out.println("name = " + name + ", age = " + age);
        return result;
    }
}
