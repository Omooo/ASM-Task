package task_9

import common.loadClass
import common.toFile
import org.objectweb.asm.*
import java.io.Serializable
import java.lang.reflect.Modifier

fun main() {
    serializationCheckByCoreApi()
}

/**
 * 使用 Core Api 进行序列化检查
 */
private fun serializationCheckByCoreApi() {
    val classReader = ClassReader(SerializationCheck::class.java.canonicalName)
    val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)
    classReader.accept(object : ClassVisitor(Opcodes.ASM9, classWriter) {

        var isSerializable = false
        var isStaticClass = false
        var hasSerialVersionUID = false
        val className = "sample/SerializationCheckCoreClass"

        override fun visit(
            version: Int,
            access: Int,
            name: String?,
            signature: String?,
            superName: String?,
            interfaces: Array<out String>?
        ) {
            isSerializable = interfaces?.contains(Type.getInternalName(Serializable::class.java)) == true
            isStaticClass = Modifier.isStatic(access)
            super.visit(version, access, className, signature, superName, interfaces)
        }

        override fun visitField(
            access: Int,
            name: String?,
            descriptor: String?,
            signature: String?,
            value: Any?
        ): FieldVisitor {
            if (!hasSerialVersionUID) {
                hasSerialVersionUID = name == "serialVersionUID" && descriptor == "J" && value != null
            }
            if (!isSerializable && Modifier.isTransient(access)) {
                // 未实现 Serializable 接口的类，包含 transient 字段
                println("Attention: Non-serializable [$className] class contains transient field '$name'.")
                return super.visitField(access, name, descriptor, signature, value)
            }
            if (!Modifier.isStatic(access) && !Modifier.isTransient(access)
                && Type.getType(descriptor).sort == Type.OBJECT
            ) {
                val isSerializableField =
                    Class.forName(Type.getType(descriptor).className)?.interfaces?.contains(Serializable::class.java)
                if (isSerializableField == false) {
                    // 实现了 Serializable 的类包含非 transient、static 的字段，这些字段并未实现 Serializable 接口
                    println("Attention: Non-serializable field '$name' in a Serializable class [$className]")
                }
            }
            return super.visitField(access, name, descriptor, signature, value)
        }

        override fun visitEnd() {
            // 实现了 Serializable 的类未提供 serialVersionUID 字段
            if (isSerializable && !hasSerialVersionUID) {
                println("Attention: This [$className] class is serializable, but does not define a 'serialVersionUID' field.")
            }
            // 未实现 Serializable 接口的类，包含 serialVersionUID 字段
            if (!isSerializable && hasSerialVersionUID) {
                println("Attention: This [$className] class is non-serializable, but defines a 'serialVersionUID' field.")
            }
            super.visitEnd()
        }
    }, ClassReader.SKIP_DEBUG)

    classWriter.toByteArray().apply {
        toFile("files/SerializationCheckCoreClass.class")
        loadClass("sample.SerializationCheckCoreClass")?.apply {

        }
    }
}