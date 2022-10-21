package task_8

import common.loadClass
import common.toFile
import org.objectweb.asm.*

fun main() {
    replaceMethodInvokeByCoreApi()
}

/**
 * 使用 Core Api 替换方法调用
 */
private fun replaceMethodInvokeByCoreApi() {
    val classReader = ClassReader(ReplaceMethodInvoke::class.java.canonicalName)
    val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)
    classReader.accept(object : ClassVisitor(Opcodes.ASM9, classWriter) {

        private val className = "sample/ReplaceMethodInvokeCoreClass"

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
            return InternalMethodVisitor(className, methodVisitor)
        }
    }, ClassReader.SKIP_DEBUG)

    classWriter.toByteArray().apply {
        toFile("files/ReplaceMethodInvokeCoreClass.class")
        loadClass("sample.ReplaceMethodInvokeCoreClass")?.apply {
            val mainMethod = getMethod("main", Array<String>::class.java)
            mainMethod.invoke(getConstructor().newInstance(), null)
        }
    }
}

private class InternalMethodVisitor(val className: String, methodVisitor: MethodVisitor) :
    MethodVisitor(Opcodes.ASM9, methodVisitor) {
    override fun visitMethodInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String?,
        isInterface: Boolean
    ) {
        if (opcode == Opcodes.INVOKEVIRTUAL && owner == TOAST
            && name == "show" && descriptor == "()V"
        ) {
            mv.visitLdcInsn(className)
            mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                SHADOW_TOAST,
                name,
                "(L$TOAST;Ljava/lang/String;)V",
                isInterface
            )
            return
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
    }
}

private const val TOAST = "task_8/Toast"
private const val SHADOW_TOAST = "task_8/ShadowToast"
