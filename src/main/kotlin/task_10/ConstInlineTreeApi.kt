package task_10

import common.loadClass
import common.toFile
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.VarInsnNode
import java.lang.reflect.Modifier

fun main() {
    constInlineByTreeApi()
}

/**
 * 使用 Tree Api 常量内联
 */
private fun constInlineByTreeApi() {
    val classReader = ClassReader(ConstInline::class.java.canonicalName)
    val classNode = ClassNode(Opcodes.ASM9)
    classReader.accept(classNode, ClassReader.SKIP_DEBUG)

//    classNode.name = "sample/ConstInlineTreeClass"
    val fieldMap = HashMap<String, String>()
    classNode.fields.filter {
        Modifier.isStatic(it.access) && Modifier.isFinal(it.access)
    }.forEach {
        fieldMap[it.name] = it.value.toString()
        println("field: ${it.name} ${it.value}")
    }
    classNode.methods.filter {
        it.name == "<clinit>"
    }.forEach { methodNode ->
        methodNode.instructions.filterIsInstance(FieldInsnNode::class.java).filter {
            it.opcode == Opcodes.PUTSTATIC
        }.forEach {
            println("code: ${it.owner} ${it.name} ${it.desc}")
            methodNode.instructions.insertBefore(it, InsnList().apply {
                add(VarInsnNode(Opcodes.ASTORE, 1))
                add(VarInsnNode(Opcodes.ALOAD, 1))

            })
        }
    }

    val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)
    classNode.accept(classWriter)
    classWriter.toByteArray().apply {
        toFile("files/ConstInlineTreeClass.class")
        loadClass("task_10.ConstInline")?.apply {

        }
    }
}
