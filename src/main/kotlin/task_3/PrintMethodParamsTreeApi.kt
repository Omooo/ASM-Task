package task_3

import common.isMethodReturn
import common.loadClass
import common.toFile
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import java.lang.reflect.Modifier

fun main() {
    printMethodParamsByTreeApi()
}

private fun printMethodParamsByTreeApi() {
    val classReader = ClassReader(PrintMethodParams::class.java.canonicalName)
    val classNode = ClassNode(Opcodes.ASM9)
    classReader.accept(classNode, ClassReader.SKIP_DEBUG)
    classNode.name = "sample/PrintMethodParamsTreeClass"
    classNode.methods.filter {
        it.name != "<init>" && !Modifier.isAbstract(it.access) && !Modifier.isNative(it.access)
    }.forEach { methodNode ->
        val methodType = Type.getType(methodNode.desc)
        var slotIndex = if (Modifier.isStatic(methodNode.access)) 0 else 1
        // 给方法入参添加输出
        methodType.argumentTypes.forEach {
            when (it.sort) {
                Type.INT -> {
                    printInt(methodNode, slotIndex)
                }

                Type.getType(String::class.java).sort -> {
                    printString(methodNode, slotIndex)
                }
                // 其他类型大差不差就不写了
            }
            slotIndex += it.size
        }
        // 给方法出参添加输出
        methodNode.instructions.filter {
            it.opcode.isMethodReturn()
        }.forEach {
            when (it.opcode) {
                Opcodes.IRETURN -> {
                    printInt(it, methodNode)
                }

                Opcodes.ARETURN -> {
                    printObject(it, methodNode)
                }
            }
        }
    }
    val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    classNode.accept(classWriter)
    classWriter.toByteArray().apply {
        toFile("files/PrintMethodParamsTreeClass.class")
        loadClass("sample.PrintMethodParamsTreeClass")?.apply {
            val method = getMethod("print", String::class.java, Int::class.java)
            method.invoke(getConstructor().newInstance(), "Omooo", 25)
        }
    }

}

/**
 * 输出出参，类型 [Int]
 *
 * @param nextInsn 下一个节点
 * @param methodNode [MethodNode]
 */
private fun printInt(nextInsn: AbstractInsnNode, methodNode: MethodNode) {
    methodNode.instructions.insertBefore(nextInsn, InsnList().apply {
        add(InsnNode(Opcodes.DUP))
        add(FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"))
        add(InsnNode(Opcodes.SWAP))
        add(
            MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream",
                "println",
                "(I)V",
                false
            )
        )
    })
}

/**
 * 输出出参，引用类型
 *
 * @param nextInsn 下一个节点
 * @param methodNode [MethodNode]
 */
private fun printObject(nextInsn: AbstractInsnNode, methodNode: MethodNode) {
    methodNode.instructions.insertBefore(nextInsn, InsnList().apply {
        add(InsnNode(Opcodes.DUP))
        add(FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"))
        add(InsnNode(Opcodes.SWAP))
        add(
            MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream",
                "println",
                "(Ljava/lang/Object;)V",
                false
            )
        )
    })
}

/**
 * 输出入参，类型 [Int]
 *
 * @param methodNode [MethodNode]
 * @param slotIndex 局部变量表索引
 */
private fun printInt(methodNode: MethodNode, slotIndex: Int) {
    methodNode.instructions.insert(InsnList().apply {
        add(VarInsnNode(Opcodes.ILOAD, slotIndex))
        add(FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"))
        add(InsnNode(Opcodes.SWAP))
        add(
            MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream",
                "println",
                "(I)V",
                false
            )
        )
    })
}

/**
 * 输出出参，类型 [String]
 *
 * @param methodNode [MethodNode]
 * @param slotIndex 局部变量表索引
 */
private fun printString(methodNode: MethodNode, slotIndex: Int) {
    methodNode.instructions.insert(InsnList().apply {
        add(VarInsnNode(Opcodes.ALOAD, slotIndex))
        add(FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"))
        add(InsnNode(Opcodes.SWAP))
        add(
            MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream",
                "println",
                "(${Type.getDescriptor(Object::class.java)})V",
                false
            )
        )
    })
}