package task_3

import common.loadClass
import common.toFile
import org.objectweb.asm.*
import java.lang.reflect.Modifier

fun main() {
    printMethodParamsByCoreApi()
}

/**
 * 使用 Core Api 打印出 [PrintMethodParams] 方法的入参和出参
 */
private fun printMethodParamsByCoreApi() {
    val classReader = ClassReader(PrintMethodParams::class.java.canonicalName)
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
            // 更改类名和包名
            super.visit(version, access, "sample/PrintMethodParamsCoreClass", signature, superName, interfaces)
        }

        override fun visitMethod(
            access: Int,
            name: String?,
            descriptor: String,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
            // 不是构造方法、抽象方法、Native 方法
            if (name != "<init>" && !Modifier.isAbstract(access) && !Modifier.isNative(access)) {
                return InternalMethodVisitor(methodVisitor, access, descriptor)
            }
            return methodVisitor
        }
    }, ClassReader.SKIP_DEBUG)
    classWriter.toByteArray().apply {
        toFile("files/PrintMethodParamsCoreClass.class")
        loadClass("sample.PrintMethodParamsCoreClass")?.apply {
            val method = getMethod("print", String::class.java, Int::class.java)
            method.invoke(getConstructor().newInstance(), "Omooo", 25)
        }
    }
}

class InternalMethodVisitor(
    methodVisitor: MethodVisitor,
    private val methodAccess: Int,
    private val methodDesc: String
) :
    MethodVisitor(Opcodes.ASM9, methodVisitor) {

    override fun visitCode() {
        // 局部变量表的索引，非 static 方法，第 0 个是 this 指针
        var slotIndex = if (Modifier.isStatic(methodAccess)) 0 else 1
        val methodType = Type.getMethodType(methodDesc)
        methodType.argumentTypes.forEach { type ->
            when (type.sort) {
                Type.INT -> {
                    mv.visitVarInsn(Opcodes.ILOAD, slotIndex)
                    printInt()
                }

                Type.getType(String::class.java).sort -> {
                    mv.visitVarInsn(Opcodes.ALOAD, slotIndex)
                    printString()
                }
                // 其他类型大差不差就不写了
            }
            // type.size 就是该局部变量所占的槽位大小，对于 Long、Double 占两个槽位，其他类型占一个槽位
            slotIndex += type.size
        }
        super.visitCode()
    }

    override fun visitInsn(opcode: Int) {
        when (opcode) {
            Opcodes.ARETURN -> {
                mv.visitInsn(Opcodes.DUP)
                printObject()
            }

            Opcodes.IRETURN -> {
                mv.visitInsn(Opcodes.DUP)
                printInt()
            }
            // 其他类型大差不差就不写了
        }
        super.visitInsn(opcode)
    }

    private fun printInt() {
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        mv.visitInsn(Opcodes.SWAP)
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/io/PrintStream",
            "println",
            "(I)V",
            false
        )
    }

    private fun printString() {
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        mv.visitInsn(Opcodes.SWAP)
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/io/PrintStream",
            "println",
            "(Ljava/lang/String;)V",
            false
        )
    }

    private fun printObject() {
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        mv.visitInsn(Opcodes.SWAP)
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/io/PrintStream",
            "println",
            "(${Type.getDescriptor(Object::class.java)})V",
            false
        )
    }
}