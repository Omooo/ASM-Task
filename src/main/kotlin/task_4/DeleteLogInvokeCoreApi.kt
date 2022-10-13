package task_4

import common.loadClass
import common.toFile
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

fun main() {
    deleteLogInvokeByCoreApi()
}

/**
 * 使用 Core Api 删除 sout 日志语句
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
        loadClass("sample.DeleteLogInvokeCoreClass")
    }

}

private class DeleteMethodVisitor(methodVisitor: MethodVisitor) : MethodVisitor(Opcodes.ASM9, methodVisitor) {
    override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
        println("$name $owner $descriptor")

        super.visitFieldInsn(opcode, owner, name, descriptor)
    }
}