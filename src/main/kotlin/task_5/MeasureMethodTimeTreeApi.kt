package task_5

import common.isMethodReturn
import common.loadClass
import common.toFile
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode

fun main() {
    measureMethodTimeByTreeApi()
}

/**
 * 使用 Tree Api 测量被 @MeasureTime 标记的方法的耗时
 */
private fun measureMethodTimeByTreeApi() {
    val classReader = ClassReader(MeasureMethodTime::class.java.canonicalName)
    val classNode = ClassNode(Opcodes.ASM9)
    classReader.accept(classNode, ClassReader.SKIP_FRAMES)

    classNode.name = "sample/MeasureMethodTimeTreeClass"
    classNode.methods.forEach { methodNode ->
        if (methodNode.invisibleAnnotations?.map { it.desc }
                ?.contains(Type.getDescriptor(MeasureTime::class.java)) == true) {
            val localVariablesSize = methodNode.localVariables.size
            // 在方法的第一个指令之前插入
            val firstInsnNode = methodNode.instructions.first
            methodNode.instructions.insertBefore(firstInsnNode, InsnList().apply {
                add(MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J"))
                add(VarInsnNode(Opcodes.LSTORE, localVariablesSize + 1))
            })

            // 在方法 return 指令之前插入
            methodNode.instructions.filter {
                it.opcode.isMethodReturn()
            }.forEach {
                methodNode.instructions.insertBefore(it, InsnList().apply {
                    add(MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J"))
                    // 注意，Long 是占两个局部变量槽位的，所以这里要较之前 +3，而不是 +2
                    add(VarInsnNode(Opcodes.LSTORE, localVariablesSize + 3))
                    add(FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"))
                    add(VarInsnNode(Opcodes.LLOAD, localVariablesSize + 3))
                    add(VarInsnNode(Opcodes.LLOAD, localVariablesSize + 1))
                    add(InsnNode(Opcodes.LSUB))
                    add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(J)V"))
                })
            }
        }
    }

    val classWrite = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    classNode.accept(classWrite)
    classWrite.toByteArray().apply {
        toFile("files/MeasureMethodTimeTreeClass.class")
        loadClass("sample.MeasureMethodTimeTreeClass")?.apply {
            getMethod("measure").invoke(getConstructor().newInstance())
        }
    }
}
