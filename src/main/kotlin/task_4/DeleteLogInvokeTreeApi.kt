package task_4

import common.loadClass
import common.toFile
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodInsnNode

fun main() {
    deleteLogInvokeByTreeApi()
}


/**
 * public class task_4.DeleteLogInvoke {
public task_4.DeleteLogInvoke();
Code:
0: aload_0
1: invokespecial #1                  // Method java/lang/Object."<init>":()V
4: return

public java.lang.String print(java.lang.String, int);
Code:
0: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
3: aload_1
4: invokevirtual #3                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
7: aload_1
8: iload_2
9: invokedynamic #4,  0              // InvokeDynamic #0:makeConcatWithConstants:(Ljava/lang/String;I)Ljava/lang/String;
14: astore_3
15: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
18: aload_3
19: invokevirtual #3                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
22: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
25: ldc           #5                  // String Delete current line.
27: invokevirtual #3                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
30: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
33: aload_1
34: iload_2
35: invokedynamic #6,  0              // InvokeDynamic #1:makeConcatWithConstants:(Ljava/lang/String;I)Ljava/lang/String;
40: invokevirtual #3                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
43: aload_3
44: areturn
}

 */
private fun deleteLogInvokeByTreeApi() {
    val classReader = ClassReader(DeleteLogInvoke::class.qualifiedName)
    val classNode = ClassNode(Opcodes.ASM9)
    classReader.accept(classNode, ClassReader.SKIP_DEBUG)

    //    remove
    //    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
    //    ALOAD 1
    //    INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/Object;)V
    classNode.name = "sample/DeleteLogInvokeTreeClass"
    classNode.methods.forEach { methodNode ->
        val deleteNodes = mutableListOf<AbstractInsnNode>()
        methodNode.instructions.forEachIndexed { index, node ->
            if (node is MethodInsnNode && node.opcode == Opcodes.INVOKEVIRTUAL && node.name == "println" && node.desc == "(Ljava/lang/String;)V" && node.owner == "java/io/PrintStream") {
                // 往前找到 getStatic
                var i = index - 1
                while (!methodNode.instructions[i].let {
                        it is FieldInsnNode && it.opcode == Opcodes.GETSTATIC && it.name == "out" && it.desc == "Ljava/io/PrintStream;" && it.owner == "java/lang/System"
                    }) {
                    i--
                }
                (i..index).forEach {
                    deleteNodes.add(methodNode.instructions[it])
                }
            }
        }

        deleteNodes.forEach {
            methodNode.instructions.remove(it)
        }

    }

    val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    classNode.accept(classWriter)
    classWriter.toByteArray().apply {
        toFile("files/DeleteLogInvokeTreeClass.class")
        loadClass("sample.DeleteLogInvokeTreeClass")
    }
}
