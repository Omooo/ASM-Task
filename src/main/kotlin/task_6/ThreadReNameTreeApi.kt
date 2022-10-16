package task_6

import common.loadClass
import common.toFile
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.TypeInsnNode

fun main() {
    threadReNameByTreeApi()
}

/**
 * 使用 Tree Api 线程重命名
 */
fun threadReNameByTreeApi() {
    val classReader = ClassReader(ThreadReName::class.java.canonicalName)
    val classNode = ClassNode(Opcodes.ASM9)
    classReader.accept(classNode, ClassReader.SKIP_DEBUG)
    classNode.name = "sample/ThreadReNameTreeClass"

    var threadNameIndex = 0
    classNode.methods.forEach { methodNode ->
        val threadNamePrefix = "${classNode.name}#${methodNode.name}-Thread-"
        methodNode.instructions.forEach {
            if (it is TypeInsnNode && it.opcode == Opcodes.NEW && it.desc == THREAD) {
                // 替换类型
                it.desc = SHADOW_THREAD
            }
            if (it is MethodInsnNode && it.owner == THREAD && it.name == "<init>") {
                // 调用 ShadowThread 对应的参数列表最后多一个 String 参数的构造方法
                val rp = it.desc.lastIndexOf(')')
                val newDesc = "${it.desc.substring(0, rp)}Ljava/lang/String;${it.desc.substring(rp)}"
                it.owner = SHADOW_THREAD
                it.desc = newDesc
                methodNode.instructions.insertBefore(it, LdcInsnNode("$threadNamePrefix${threadNameIndex++}"))
            }

            if (it is MethodInsnNode && it.opcode == Opcodes.INVOKEVIRTUAL && it.owner == THREAD
                && it.name == "setName" && it.desc == "(Ljava/lang/String;)V"
            ) {
                // 由于 Thread#setName 没法重写，所以直接搞一个静态方法调用
                methodNode.instructions.insertBefore(it, LdcInsnNode("$threadNamePrefix${threadNameIndex++}"))
                methodNode.instructions.insertBefore(
                    it, MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        SHADOW_THREAD,
                        "makeThreadName",
                        "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
                        false
                    )
                )
            }
        }
    }

    val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)
    classNode.accept(classWriter)
    classWriter.toByteArray().apply {
        toFile("files/ThreadReNameTreeClass.class")
        loadClass("sample.ThreadReNameTreeClass")?.apply {
            getMethod("main", Array<String>::class.java).invoke(null, null)
        }
    }
}

private const val THREAD = "java/lang/Thread"

private const val SHADOW_THREAD = "task_6/ShadowThread"
