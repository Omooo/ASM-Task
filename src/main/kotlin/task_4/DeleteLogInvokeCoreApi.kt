package task_4

import common.loadClass
import common.toFile
import org.objectweb.asm.*

fun main() {
    deleteLogInvokeByCoreApi()
}

/**
 * 使用 Core Api 删除 sout 日志语句
 *
 * 思路: 删除 GETSTATIC out 和 INVOKEVIRTUAL print 之间的指令
 */
private fun deleteLogInvokeByCoreApi() {
    val classReader = ClassReader(DeleteLogInvoke::class.java.canonicalName)
    val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    classReader.accept(object : ClassVisitor(Opcodes.ASM9, classWriter) {

        override fun visit(
            version: Int,
            access: Int,
            name: String?,
            signature: String?,
            superName: String?,
            interfaces: Array<out String>?
        ) {
            super.visit(version, access, "sample/DeleteLogInvokeCoreClass", signature, superName, interfaces)
        }

        override fun visitMethod(
            access: Int,
            name: String?,
            descriptor: String?,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
            return DeleteMethodVisitor(methodVisitor)
        }
    }, ClassReader.SKIP_DEBUG)

    classWriter.toByteArray().apply {
        toFile("files/DeleteLogInvokeCoreClass.class")
        loadClass("sample.DeleteLogInvokeCoreClass")?.apply {
            val printMethod = getMethod("print", String::class.java, Int::class.java)
            printMethod.invoke(getConstructor().newInstance(), "Omooo", 25)
        }
    }

}

private class DeleteMethodVisitor(methodVisitor: MethodVisitor) : MethodVisitor(Opcodes.ASM9, methodVisitor) {
    /** 删除期间 */
    var deleteDuration = false

    /** 删除 pop 指令 */
    var deletePopInsn = false
    override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
        if (opcode == Opcodes.GETSTATIC && owner == "java/lang/System" && name == "out" && descriptor == "Ljava/io/PrintStream;") {
            deleteDuration = true
            return
        }
        deleteDuration = false
        super.visitFieldInsn(opcode, owner, name, descriptor)
    }

    override fun visitLdcInsn(value: Any?) {
        if (deleteDuration) {
            return
        }
        super.visitLdcInsn(value)
    }

    override fun visitInsn(opcode: Int) {
        if (deleteDuration) {
            return
        }
        if (deletePopInsn) {
            deletePopInsn = false
            return
        }
        super.visitInsn(opcode)
    }

    override fun visitVarInsn(opcode: Int, varIndex: Int) {
        if (deleteDuration) {
            return
        }
        super.visitVarInsn(opcode, varIndex)
    }

    override fun visitTypeInsn(opcode: Int, type: String?) {
        if (deleteDuration) {
            return
        }
        super.visitTypeInsn(opcode, type)
    }

    override fun visitInvokeDynamicInsn(
        name: String?,
        descriptor: String?,
        bootstrapMethodHandle: Handle?,
        vararg bootstrapMethodArguments: Any?
    ) {
        if (deleteDuration) {
            return
        }
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, *bootstrapMethodArguments)
    }

    override fun visitMethodInsn(
        opcode: Int,
        owner: String?,
        name: String,
        descriptor: String?,
        isInterface: Boolean
    ) {
        if (opcode == Opcodes.INVOKEVIRTUAL
            && owner == "java/io/PrintStream"
            && name.startsWith("print")
        ) {
            deletePopInsn = Type.getMethodType(descriptor).returnType != Type.VOID_TYPE
            deleteDuration = false
            return
        }
        if (deleteDuration) {
            return
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
    }

}
