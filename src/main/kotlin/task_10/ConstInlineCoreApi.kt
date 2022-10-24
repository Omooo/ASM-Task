package task_10

import common.loadClass
import common.toFile
import org.objectweb.asm.*
import org.objectweb.asm.tree.FieldNode
import java.lang.reflect.Modifier

fun main() {
    constInlineByCoreApi()
}

/**
 * 使用 Core Api 常量内联
 */
private fun constInlineByCoreApi() {
    val classReader = ClassReader(ConstInline::class.java.canonicalName)
    val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)

    val constFieldNodeMap = HashMap<String, FieldNode>()

    classReader.accept(object : ClassVisitor(Opcodes.ASM9, classWriter) {

        private val className = "task_10/ConstInline"
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

        override fun visitField(
            access: Int,
            name: String?,
            descriptor: String?,
            signature: String?,
            value: Any?
        ): FieldVisitor? {
            if (Modifier.isFinal(access) && Modifier.isStatic(access)) {
                constFieldNodeMap["$className.$name.$descriptor"] = FieldNode(
                    access, name, descriptor, signature, value
                )
                println("value: $name $value")
                return null
            }
            return super.visitField(access, name, descriptor, signature, value)
        }

        override fun visitMethod(
            access: Int,
            name: String?,
            descriptor: String?,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            println("visitMethod: $name")
            val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
            return InternalMethodVisitor(constFieldNodeMap, methodVisitor)
        }
    }, ClassReader.SKIP_DEBUG)

    classWriter.toByteArray().apply {
        toFile("files/ConstInlineCoreClass.class")
        loadClass("task_10.ConstInline")?.apply {

        }
    }
}

private class InternalMethodVisitor(val constFieldNodeMap: HashMap<String, FieldNode>, methodVisitor: MethodVisitor) :
    MethodVisitor(Opcodes.ASM9, methodVisitor) {

    override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
        val key = "$owner.$name.$descriptor"
        if (opcode == Opcodes.PUTSTATIC) {
            val fieldNode = constFieldNodeMap[key]
            println("put static: ${fieldNode?.name} ${fieldNode?.value}")
        }
        if (opcode == Opcodes.GETSTATIC && constFieldNodeMap.containsKey(key)) {
            mv.visitLdcInsn(constFieldNodeMap[key]?.value.toString())
            println("inline: ${constFieldNodeMap[key]?.value.toString()}")
            return
        }
        super.visitFieldInsn(opcode, owner, name, descriptor)
    }

    override fun visitLdcInsn(value: Any?) {
        println("visitLdcInsn: $value")
        super.visitLdcInsn(value)
    }
}
