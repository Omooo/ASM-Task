package task_4

import common.loadClass
import common.toFile
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode

fun main() {
    deleteLogInvokeByTreeApi()
}

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
                deleteNodes.add(methodNode.instructions[index])
                deleteNodes.add(methodNode.instructions[index - 1])
                deleteNodes.add(methodNode.instructions[index - 2])
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
