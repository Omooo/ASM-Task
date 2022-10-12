package task_1

import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import java.lang.reflect.Modifier
import java.util.ArrayList

fun main() {
    readArrayListByTreeApi()
}

/**
 * 使用 Tree Api 读取 ArrayList 类
 */
private fun readArrayListByTreeApi() {
    val classReader = ClassReader(ArrayList::class.java.canonicalName)
    val classNode = ClassNode(Opcodes.ASM9)
    classReader.accept(classNode, ClassReader.SKIP_CODE)
    classNode.apply {
        println("name: $name\n")
        fields.forEach {
            println("field: ${it.name} ${Modifier.toString(it.access)} ${it.desc} ${it.value}")
        }
        println()
        methods.forEach {
            println("method: ${it.name} ${Modifier.toString(it.access)} ${it.desc}")
        }
    }
}