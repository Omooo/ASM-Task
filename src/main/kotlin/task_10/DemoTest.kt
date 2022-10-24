package task_10

import common.toFile
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

fun main() {
    val classReader = ClassReader(Demo::class.java.canonicalName)
    val classNode = ClassNode(Opcodes.ASM9)
    classReader.accept(classNode, ClassReader.SKIP_DEBUG)

    // 不加这行啥事没有
    classNode.name = "sample/Demo"

    val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)
    classNode.accept(classWriter)
    classWriter.toByteArray().apply {
        toFile("files/Demo.class")
    }
}