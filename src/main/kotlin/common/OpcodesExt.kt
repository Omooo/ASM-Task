package common

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

internal fun Int.isMethodReturn(): Boolean {
    return (this >= Opcodes.IRETURN && this <= Opcodes.RETURN)
            || this == Opcodes.ATHROW
}

fun Type.const0Opcode(): Int {
    return when (sort) {
        Type.BOOLEAN,
        Type.BYTE,
        Type.CHAR,
        Type.INT -> Opcodes.ICONST_0

        Type.FLOAT -> Opcodes.FCONST_0
        Type.DOUBLE -> Opcodes.DCONST_0
        Type.LONG -> Opcodes.LCONST_0
        Type.OBJECT -> Opcodes.ACONST_NULL
        else -> throw IllegalStateException("Type is illegal.")
    }
}