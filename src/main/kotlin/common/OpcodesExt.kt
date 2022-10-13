package common

import org.objectweb.asm.Opcodes

internal fun Int.isMethodReturn(): Boolean {
    return (this >= Opcodes.IRETURN && this <= Opcodes.RETURN)
            || this == Opcodes.ATHROW
}