package task_9;

import java.io.*;

public class SerializationCheck implements Serializable {

    public static void main(String[] args) {
        // Caused by: java.io.NotSerializableException: task_9.SerializationCheck$ItemBean1
        try {
            SerializationCheck check = new SerializationCheck();
            check.itemBean1 = new ItemBean1();
            check.itemBean2 = new ItemBean2();
            check.itemBean3 = new ItemBean3();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("output.txt"));
            objectOutputStream.writeObject(check);
            objectOutputStream.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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

