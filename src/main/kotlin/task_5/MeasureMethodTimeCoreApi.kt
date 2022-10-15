package task_5

import common.isMethodReturn
import common.loadClass
import common.toFile
import org.objectweb.asm.*
import org.objectweb.asm.commons.LocalVariablesSorter

fun main() {
    measureMethodTimeByCoreApi()
}

/**
 * 使用 Core Api 测量被 @MeasureTime 标记的方法的耗时
 */
private fun measureMethodTimeByCoreApi() {
    val classReader = ClassReader(MeasureMethodTime::class.java.canonicalName)
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
            super.visit(version, access, "sample/MeasureMethodTimeCoreClass", signature, superName, interfaces)
        }

        override fun visitMethod(
            access: Int,
            name: String,
            descriptor: String,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
            return InternalMethodVisitor(access, descriptor, methodVisitor)
        }
    }, ClassReader.EXPAND_FRAMES)

    classWriter.toByteArray().apply {
        toFile("files/MeasureMethodTimeCoreClass.class")
        loadClass("sample.MeasureMethodTimeCoreClass")?.apply {
            getMethod("measure").invoke(getConstructor().newInstance())
        }
    }
}

/**
 * MethodVisitor
 *
 * 这里使用 LocalVariablesSorter 是为了方便生成局部变量获取其索引
 */
private class InternalMethodVisitor(methodAccess: Int, methodDesc: String, methodVisitor: MethodVisitor) :
    LocalVariablesSorter(Opcodes.ASM9, methodAccess, methodDesc, methodVisitor) {

    /** 标记需要测量的方法 */
    var trackMethodFlag = false

    /** 起始时间索引 */
    var startTimeIndex = 0

    /** 终止时间索引 */
    var endTimeIndex = 0

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        trackMethodFlag = Type.getDescriptor(MeasureTime::class.java) == descriptor
        return super.visitAnnotation(descriptor, visible)
    }

    override fun visitCode() {
        if (trackMethodFlag) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
            startTimeIndex = newLocal(Type.LONG_TYPE)
            mv.visitVarInsn(Opcodes.LSTORE, startTimeIndex)
        }
        super.visitCode()
    }

    override fun visitInsn(opcode: Int) {
        if (trackMethodFlag && opcode.isMethodReturn()) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
            endTimeIndex = newLocal(Type.LONG_TYPE)
            mv.visitVarInsn(Opcodes.LSTORE, endTimeIndex)
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
            mv.visitVarInsn(Opcodes.LLOAD, endTimeIndex)
            mv.visitVarInsn(Opcodes.LLOAD, startTimeIndex)
            mv.visitInsn(Opcodes.LSUB)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(J)V", false)
        }
        super.visitInsn(opcode)
    }
}