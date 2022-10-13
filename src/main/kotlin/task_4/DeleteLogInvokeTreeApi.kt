package task_4

import common.loadClass
import common.toFile
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode

fun main() {
    deleteLogInvokeByTreeApi()
}

private fun deleteLogInvokeByTreeApi() {
    val classReader = ClassReader(DeleteLogInvoke::class.qualifiedName)
    val classNode = ClassNode(Opcodes.ASM9)
    classReader.accept(classNode, ClassReader.SKIP_DEBUG)

    classNode.name = "sample/DeleteLogInvokeTreeClass"
    classNode.methods.forEach { methodNode ->
        // 删除 System.out.println(); 调用
        // TODO: 删除残余的局部变量 out 以及字符串变量
        methodNode.instructions.filterIsInstance<MethodInsnNode>().filter {
            it.name == "println" && it.desc == "(Ljava/lang/String;)V" && it.owner == "java/io/PrintStream"
        }.forEach {
            methodNode.instructions.remove(it)
        }
        // TODO: 不能按照下面方式删 out 变量
//        methodNode.instructions.filterIsInstance<FieldInsnNode>().filter {
//            it.name == "out" && it.desc == "Ljava/io/PrintStream;" && it.owner == "java/lang/System"
//        }.forEach {
//            println("${it.name} ${it.desc} ${it.owner}")
//            methodNode.instructions.remove(it)
//        }
    }

    val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    classNode.accept(classWriter)
    classWriter.toByteArray().apply {
        toFile("files/DeleteLogInvokeTreeClass.class")
        loadClass("sample.DeleteLogInvokeTreeClass")
    }
}
