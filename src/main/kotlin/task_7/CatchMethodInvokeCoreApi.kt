package task_7

import common.const0Opcode
import common.loadClass
import common.toFile
import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter
import java.lang.reflect.Modifier


fun main() {
    catchMethodInvokeByCoreApi()
}

/**
 * 使用 Core Api try-catch 特定方法
 */
fun catchMethodInvokeByCoreApi() {
    val classReader = ClassReader(CatchMethodInvoke::class.java.canonicalName)
    val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)
    classReader.accept(object : ClassVisitor(Opcodes.ASM9, classWriter) {

        override fun visit(
            version: Int,
            access: Int,
            name: String?,
            signature: String?,
            superName: String?,
            interfaces: Array<out String>?
        ) {
            super.visit(version, access, "sample/CatchMethodInvokeCoreClass", signature, superName, interfaces)
        }

        override fun visitMethod(
            access: Int,
            name: String,
            descriptor: String,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
            if (name == "<init>" || Modifier.isNative(access) || Modifier.isAbstract(access)) {
                return methodVisitor
            }
            return InternalMethodVisitor(methodVisitor, access, name, descriptor)
        }
    }, ClassReader.EXPAND_FRAMES)

    classWriter.toByteArray().apply {
        toFile("files/CatchMethodInvokeCoreClass.class")
        loadClass("sample.CatchMethodInvokeCoreClass")?.apply {
            val returnValue = getMethod("calc").invoke(getConstructor().newInstance())
            println(returnValue)
        }
    }
}

private class InternalMethodVisitor(
    methodVisitor: MethodVisitor,
    methodAccess: Int,
    methodName: String,
    methodDesc: String,
) :
    AdviceAdapter(Opcodes.ASM9, methodVisitor, methodAccess, methodName, methodDesc) {

    val fromLabel = Label()
    val toLabel = Label()
    val targetLabel = Label()

    override fun onMethodEnter() {
        super.onMethodEnter()
        mv.visitTryCatchBlock(fromLabel, toLabel, targetLabel, "java/lang/Exception")
        mv.visitLabel(fromLabel)
    }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
        mv.visitLabel(toLabel)
        mv.visitLabel(targetLabel)
        val varIndex = newLocal(Type.getType("Ljava/lang/Exception;"))
        mv.visitVarInsn(Opcodes.ASTORE, varIndex)
        mv.visitVarInsn(Opcodes.ALOAD, varIndex)
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Exception", "printStackTrace", "()V", false)

        Type.getMethodType(methodDesc).returnType.apply {
            if (sort == Type.VOID) {
                mv.visitInsn(Opcodes.RETURN)
            } else {
                // 异常时返回默认值
                mv.visitInsn(this.const0Opcode())
                mv.visitInsn(this.getOpcode(IRETURN))
            }
        }
        super.visitMaxs(maxStack, maxLocals)
    }

}
