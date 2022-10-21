package task_7

import common.isEmpty
import common.isMethodReturn
import common.loadClass
import common.toFile
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

fun main() {
    catchMethodInvokeByTreeApi()
}

fun catchMethodInvokeByTreeApi() {
    val classReader = ClassReader(CatchMethodInvoke::class.java.canonicalName)
    val classNode = ClassNode(Opcodes.ASM9)
    classReader.accept(classNode, ClassReader.SKIP_DEBUG)
    classNode.name = "sample/CatchMethodInvokeTreeClass"

    classNode.methods.filter {
        it.name != "<init>" && !it.isEmpty()
    }.forEach { methodNode ->
        val fromLabel = LabelNode()
        val toLabel = LabelNode()
        val targetLabel = LabelNode()
        methodNode.tryCatchBlocks.add(TryCatchBlockNode(fromLabel, toLabel, targetLabel, "java/lang/Exception"))

        methodNode.instructions.filter {
            it.opcode.isMethodReturn()
        }.forEach {
            val varIndex = methodNode.maxLocals + 1
            methodNode.instructions.insert(fromLabel)
            methodNode.instructions.insertBefore(it, toLabel)

            methodNode.instructions.insert(it, InsnNode(Opcodes.IRETURN))
            methodNode.instructions.insert(it, InsnNode(Opcodes.ICONST_0))
            methodNode.instructions.insert(
                it,
                MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Exception", "printStackTrace", "()V")
            )
            methodNode.instructions.insert(it, VarInsnNode(Opcodes.ALOAD, varIndex))
            methodNode.instructions.insert(it, VarInsnNode(Opcodes.ASTORE, varIndex))
            methodNode.instructions.insert(it, targetLabel)
        }
    }

    val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)
    classNode.accept(classWriter)
    classWriter.toByteArray().apply {
        toFile("files/CatchMethodInvokeTreeClass.class")
        loadClass("sample.CatchMethodInvokeTreeClass")?.apply {
            val returnValue = getMethod("calc").invoke(getConstructor().newInstance())
            println(returnValue)
        }
    }
}
