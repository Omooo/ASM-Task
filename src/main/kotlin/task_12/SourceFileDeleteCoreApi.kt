package task_12

import common.loadClass
import common.toFile
import org.objectweb.asm.*

fun main() {
    sourceFileDeleteByCoreApi()
}

private fun sourceFileDeleteByCoreApi() {
    val classReader = ClassReader(SourceFileDelete::class.java.canonicalName)
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
            super.visit(version, access, "sample/SourceFileDeleteCoreClass", signature, superName, interfaces)
        }

        override fun visitSource(source: String?, debug: String?) {
            println("visitSource: $source $debug")
//            super.visitSource(source, debug)
        }

        override fun visitMethod(
            access: Int,
            name: String?,
            descriptor: String?,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
            println(name)
            return object : MethodVisitor(Opcodes.ASM9, methodVisitor) {
                override fun visitLineNumber(line: Int, start: Label?) {
                    println("line: $line $start")
//                    super.visitLineNumber(line, start)
                }
            }
        }
    }, ClassReader.SKIP_FRAMES)

    classWriter.toByteArray().apply {
        toFile("files/SourceFileDeleteCoreClass.class")
        loadClass("sample.SourceFileDeleteCoreClass")?.apply {
            getMethod("main", Array<String>::class.java).invoke(null, null)
        }
    }
}
