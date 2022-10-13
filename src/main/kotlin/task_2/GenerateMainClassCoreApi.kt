package task_2

import common.loadClass
import common.toFile
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

fun main() {
    generateMainClassByCoreApi()
}

/**
 * 使用 Core Api 生成 [Main] 类
 */
private fun generateMainClassByCoreApi() {
    val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    classWriter.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, "sample/MainCoreClass", null, "java/lang/Object", null)
    // 生成 [private static final String TAG = "Main";]
    classWriter.visitField(
        Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL,
        "TAG",
        Type.getDescriptor(String::class.java),
        null,
        "Main"
    )
    // 生成构造方法
    classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null).apply {
        visitVarInsn(Opcodes.ALOAD, 0)
        visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
        visitInsn(Opcodes.RETURN)
        visitMaxs(1, 1)
        visitEnd()
    }
    // 生成 main 方法 [public static void main(String[] args) {}]
    classWriter.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null)
        .apply {
            visitCode()
            visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
            visitLdcInsn("Hello World.")
            visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream",
                "println",
                "(${Type.getDescriptor(String::class.java)})V",
                false
            )
            visitInsn(Opcodes.RETURN)
            visitMaxs(1, 1)
            visitEnd()
        }
    classWriter.visitEnd()
    // 写入 class 文件
    classWriter.toByteArray().apply {
        toFile("files/MainCoreClass.class")
        loadClass("sample.MainCoreClass")?.apply {
            declaredFields.forEach {
                it.isAccessible = true
                println("field name: ${it.name}, value: ${it.get(null)}")
            }
            getMethod("main", Array<String>::class.java).invoke(null, null)
        }
    }
}