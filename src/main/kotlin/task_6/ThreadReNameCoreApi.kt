package task_6

import common.loadClass
import common.toFile
import org.objectweb.asm.*

fun main() {
    threadReNameByCoreApi()
}

/**
 * 使用 Core Api 线程重命名
 */
private fun threadReNameByCoreApi() {
    val classReader = ClassReader(ThreadReName::class.java.canonicalName)
    val classWrite = ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)
    classReader.accept(object : ClassVisitor(Opcodes.ASM9, classWrite) {

        private val className = "sample/ThreadReNameCoreClass"

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
            name: String?,
            descriptor: String?,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
            val threadNamePrefix = "$className#${name}-Thread-"
            return InternalMethodVisitor(threadNamePrefix, methodVisitor)
        }
    }, ClassReader.SKIP_DEBUG)

    classWrite.toByteArray().apply {
        toFile("files/ThreadReNameCoreClass.class")
        loadClass("sample.ThreadReNameCoreClass")?.apply {
            getMethod("main", Array<String>::class.java).invoke(null, null)
        }
    }
}

private class InternalMethodVisitor(private val threadNamePrefix: String, methodVisitor: MethodVisitor) :
    MethodVisitor(Opcodes.ASM9, methodVisitor) {

    var threadNameIndex = 0

    override fun visitTypeInsn(opcode: Int, type: String?) {
        if (opcode == Opcodes.NEW && type == THREAD) {
            super.visitTypeInsn(opcode, SHADOW_THREAD)
            return
        }
        super.visitTypeInsn(opcode, type)
    }

    override fun visitMethodInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String,
        isInterface: Boolean
    ) {
        if (owner == THREAD && name == "<init>") {
            // 调用 ShadowThread 对应的参数列表最后多一个 String 参数的构造方法
            val rp = descriptor.lastIndexOf(')')
            val newDesc = "${descriptor.substring(0, rp)}Ljava/lang/String;${descriptor.substring(rp)}"
            visitLdcInsn("$threadNamePrefix${threadNameIndex++}")
            super.visitMethodInsn(opcode, SHADOW_THREAD, name, newDesc, isInterface)
            return
        }

        // 由于 Thread#setName 没法重写，所以直接搞一个静态方法调用
        if (opcode == Opcodes.INVOKEVIRTUAL && owner == THREAD && name == "setName" && descriptor == "(Ljava/lang/String;)V") {
            visitLdcInsn("$threadNamePrefix${threadNameIndex++}")
            super.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                SHADOW_THREAD,
                "makeThreadName",
                "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
                false
            )
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            return
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
    }
}

private const val THREAD = "java/lang/Thread"

private const val SHADOW_THREAD = "task_6/ShadowThread"
