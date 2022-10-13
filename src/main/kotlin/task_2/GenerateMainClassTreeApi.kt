package task_2

import common.loadClass
import common.toFile
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*

fun main() {
    generateMainClassByTreeApi()
}

/**
 * 使用 Tree Api 生成 [Main] 类
 */
private fun generateMainClassByTreeApi() {
    val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    val classNode = ClassNode(Opcodes.ASM9)
    classNode.apply {
        version = Opcodes.V11
        name = "sample/MainTreeClass"
        access = Opcodes.ACC_PUBLIC
        superName = "java/lang/Object"

        // 生成 [private static final String TAG = "Main";]
        fields.add(
            FieldNode(
                Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL,
                "TAG", Type.getDescriptor(String::class.java), null, "Main"
            )
        )
        // 生成构造方法
        methods.add(MethodNode(
            Opcodes.ACC_PUBLIC, "<init>", "()V", null, null
        ).apply {
            instructions.add(InsnList().apply {
                add(VarInsnNode(Opcodes.ALOAD, 0))
                add(MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false))
                add(InsnNode(Opcodes.RETURN))
            })
            maxLocals = 1
            maxStack = 1
        })
        // 生成 main 方法 [public static void main(String[] args) {}]
        methods.add(MethodNode(
            Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null
        ).apply {
            instructions.apply {
                add(FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"))
                add(LdcInsnNode("Hello World."))
                add(
                    MethodInsnNode(
                        Opcodes.INVOKEVIRTUAL,
                        "java/io/PrintStream",
                        "println",
                        "(Ljava/lang/String;)V",
                        false
                    )
                )
                add(InsnNode(Opcodes.RETURN))
            }
        })
    }
    classNode.accept(classWriter)
    classWriter.toByteArray().apply {
        toFile("files/MainTreeClass.class")
        loadClass("sample.MainTreeClass")?.apply {
            declaredFields.forEach {
                it.isAccessible = true
                println("field name: ${it.name}, value: ${it.get(null)}")
            }
            getMethod("main", Array<String>::class.java).invoke(null, null)
        }
    }
}