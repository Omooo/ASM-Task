package task_8

import common.loadClass
import common.toFile
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode

fun main() {
    replaceMethodInvokeByTreeApi()
}

/**
 * 使用 Tree Api 替换方法调用
 */
fun replaceMethodInvokeByTreeApi() {
    val classReader = ClassReader(ReplaceMethodInvoke::class.java.canonicalName)
    val classNode = ClassNode(Opcodes.ASM9)
    classReader.accept(classNode, ClassReader.SKIP_DEBUG)
    classNode.name = "sample/ReplaceMethodInvokeTreeClass"

    classNode.methods.asIterable().forEach { methodNode ->
        methodNode.instructions.filterIsInstance<MethodInsnNode>().filter {
            it.opcode == Opcodes.INVOKEVIRTUAL && it.owner == TOAST
                    && it.name == "show" && it.desc == "()V"
        }.forEach {
            methodNode.instructions.insertBefore(it, LdcInsnNode(classNode.name))
            it.owner = SHADOW_TOAST
            it.desc = "(L$TOAST;Ljava/lang/String;)V"
            it.opcode = Opcodes.INVOKESTATIC
        }
    }

    val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)
    classNode.accept(classWriter)
    classWriter.toByteArray().apply {
        toFile("files/ReplaceMethodInvokeTreeClass.class")
        loadClass("sample.ReplaceMethodInvokeTreeClass")?.apply {
            val mainMethod = getMethod("main", Array<String>::class.java)
            mainMethod.invoke(getConstructor().newInstance(), null)
        }
    }
}

private const val TOAST = "task_8/Toast"
private const val SHADOW_TOAST = "task_8/ShadowToast"
