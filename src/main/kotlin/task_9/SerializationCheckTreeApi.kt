package task_9

import common.loadClass
import common.toFile
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import java.io.Serializable
import java.lang.reflect.Modifier

fun main() {
    serializationCheckByTreeApi()
}

/**
 * 使用 Tree Api 进行序列化检查
 */
fun serializationCheckByTreeApi() {
    val classReader = ClassReader(SerializationCheck::class.java.canonicalName)
    val classNode = ClassNode(Opcodes.ASM9)
    classReader.accept(classNode, ClassReader.SKIP_DEBUG)
    classNode.name = "sample/SerializationCheckTreeClass"

    val isSerializable = classNode.interfaces?.contains(Type.getInternalName(Serializable::class.java)) == true
    classNode.fields.asIterable().find {
        it.name == "serialVersionUID" && it.desc == "J" && it.value != null
    }.let {
        // 实现了 Serializable 的类未提供 serialVersionUID 字段
        if (isSerializable && it == null) {
            println("Attention: This [${classNode.name}] class is serializable, but does not define a 'serialVersionUID' field.")
        }
        // 未实现 Serializable 接口的类，包含 serialVersionUID 字段
        if (!isSerializable && it != null) {
            println("Attention: This [${classNode.name}] class is non-serializable, but defines a 'serialVersionUID' field.")
        }
    }

    if (isSerializable) {
        classNode.fields.filter {
            !Modifier.isStatic(it.access) && !Modifier.isTransient(it.access)
                    && Type.getType(it.desc).sort == Type.OBJECT
        }.filter {
            Class.forName(Type.getType(it.desc).className)?.interfaces?.contains(Serializable::class.java) == false
        }.forEach {
            // 实现了 Serializable 的类包含非 transient、static 的字段，这些字段并未实现 Serializable 接口
            println("Attention: Non-serializable field '${it.name}' in a Serializable class [${classNode.name}]")
        }
    } else {
        classNode.fields.filter {
            Modifier.isTransient(it.access)
        }.forEach {
            // 未实现 Serializable 接口的类，包含 transient 字段
            println("Attention: Non-serializable [${classNode.name}] class contains transient field '${it.name}'.")
        }
    }

    if (!isSerializable) {
        classNode.innerClasses.filter {
            !Modifier.isStatic(it.access) &&
                    Class.forName(it.name.replace("/", "."))?.interfaces?.contains(Serializable::class.java) == true
        }.forEach {
            // 实现了 Serializable 的非静态内部类，它的外层类并未实现 Serializable 接口
            println("Inner class '${it.name}' is serializable while its outer class is not.")
        }
    }

    val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)
    classNode.accept(classWriter)
    classWriter.toByteArray().apply {
        toFile("files/SerializationCheckTreeClass.class")
        loadClass("sample.SerializationCheckTreeClass")?.apply {

        }
    }
}
