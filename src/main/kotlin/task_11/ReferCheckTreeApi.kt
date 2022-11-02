package task_11

import LibraryClass
import common.accessible
import common.loadClass
import common.toFile
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import java.lang.reflect.Modifier

fun main() {
    referCheckByTreeApi()
}

/**
 * 使用 Core Api 检查是否调用了不存在的方法或字段
 */
private fun referCheckByTreeApi() {
    val classReader = ClassReader(ReferCheck::class.qualifiedName)
    val classNode = ClassNode(Opcodes.ASM9)
    classReader.accept(classNode, ClassReader.SKIP_DEBUG)
    classNode.name = "sample/ReferCheckTreeClass"

    val classMap = readLibrary()
    classNode.methods.forEach { methodNode ->
        methodNode.instructions.filterIsInstance<MethodInsnNode>().forEach { methodInsnNode ->
            if (classMap.containsKey(methodInsnNode.owner)) {
                classMap[methodInsnNode.owner]?.methods?.find {
                    it.name == methodInsnNode.name && methodInsnNode.desc == it.desc
                }?.let {
                    if (!methodInsnNode.opcode.accessible(it.access)) {
                        println(
                            """
                        在 ${classNode.name}$${methodNode.name} 方法里监测到非法调用: 
                            IllegalAccessError: 试图调用 ${Modifier.toString(it.access)} ${methodInsnNode.owner}.${methodInsnNode.name}${methodInsnNode.desc} 方法.
                           
                    """.trimIndent()
                        )
                    }
                } ?: run {
                    println(
                        """
                    在 ${classNode.name}${'$'}${methodNode.name} 方法里监测到非法调用: 
                        NoSuchMethodError: 找不到 ${methodInsnNode.owner}.${methodInsnNode.name}${methodInsnNode.desc} 方法.
                        
                """.trimIndent()
                    )
                }
            } else {
                println("classMap 未包含 ${methodInsnNode.owner} 类.")
            }
        }
    }

    val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)
    classNode.accept(classWriter)
    classWriter.toByteArray()?.apply {
        toFile("files/ReferCheckTreeClass.class")
        loadClass("sample.ReferCheckTreeClass")?.apply {
            // 抛异常
            getMethod("testPrivateMethod").invoke(getConstructor().newInstance())
            getMethod("testModifyParamsMethod").invoke(getConstructor().newInstance())
            getMethod("testModifyReturnTypeMethod").invoke(getConstructor().newInstance())
        }
    }
}

/**
 * 读取依赖类生成 Map
 *
 * @return Map, key: 类名；value: [ClassNode]，选择 [ClassNode] 是因为很好表示
 */
private fun readLibrary(): Map<String, ClassNode> {
    val classReader = ClassReader(LibraryClass::class.java.canonicalName)
    val classNode = ClassNode(Opcodes.ASM9)
    classReader.accept(classNode, ClassReader.SKIP_DEBUG)
    return mapOf(classNode.name to classNode)
}
