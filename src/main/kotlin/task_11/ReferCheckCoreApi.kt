package task_11

import LibraryClass
import common.accessible
import common.loadClass
import common.toFile
import org.objectweb.asm.*
import org.objectweb.asm.tree.ClassNode
import java.lang.reflect.Modifier

fun main() {
    referCheckByCoreApi()
}


/**
 * 使用 Core Api 检查是否调用了不存在的方法或字段
 */
private fun referCheckByCoreApi() {
    val classReader = ClassReader(ReferCheck::class.java.canonicalName)
    val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)
    val classMap = readLibrary()
    classReader.accept(object : ClassVisitor(Opcodes.ASM9, classWriter) {

        val className = "sample/ReferCheckCoreClass"

        override fun visit(
            version: Int,
            access: Int,
            name: String?,
            signature: String?,
            superName: String?,
            interfaces: Array<out String>?
        ) {
            super.visit(version, access, className, signature, superName, interfaces)
        }

        override fun visitMethod(
            access: Int,
            name: String,
            descriptor: String?,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            val visitMethod = super.visitMethod(access, name, descriptor, signature, exceptions)
            return InternalMethodVisitor(visitMethod, className, name, classMap)
        }
    }, ClassReader.SKIP_DEBUG)

    classWriter.toByteArray().apply {
        toFile("files/ReferCheckCoreClass.class")
        loadClass("sample.ReferCheckCoreClass")?.apply {
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

private class InternalMethodVisitor(
    methodVisitor: MethodVisitor,
    val className: String,
    val methodName: String,
    val classMap: Map<String, ClassNode>
) : MethodVisitor(Opcodes.ASM9, methodVisitor) {
    override fun visitMethodInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String?,
        isInterface: Boolean
    ) {
        if (classMap.containsKey(owner)) {
            classMap[owner]?.methods?.find {
                it.name == name && descriptor == it.desc
            }?.let {
                if (!opcode.accessible(it.access)) {
                    println(
                        """
                        在 $className${'$'}$methodName 方法里监测到非法调用: 
                            IllegalAccessError: 试图调用 ${Modifier.toString(it.access)} $owner.$name$descriptor 方法.
                           
                    """.trimIndent()
                    )
                }
            } ?: run {
                println(
                    """
                    在 $className${'$'}$methodName 方法里监测到非法调用: 
                        NoSuchMethodError: 找不到 $owner.$name$descriptor 方法.
                        
                """.trimIndent()
                )
            }
        } else {
            println("classMap 未包含 $owner 类.")
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
    }
}