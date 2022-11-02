package common

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.lang.reflect.Modifier

internal fun Int.isMethodReturn(): Boolean {
    return (this >= Opcodes.IRETURN && this <= Opcodes.RETURN)
            || this == Opcodes.ATHROW
}

internal fun Type.const0Opcode(): Int {
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

/**
 * 是否是可达的
 *
 * @param access 方法或字段的访问权限
 * @return true: 可达
 */
internal fun Int.accessible(access: Int): Boolean {
    // 非静态调用静态方法
    if (Modifier.isStatic(access) && this != Opcodes.INVOKESTATIC) {
        return false
    }
    // 非私有调用私有方法
    if (Modifier.isPrivate(access) && this != Opcodes.INVOKESPECIAL) {
        return false
    }
    // todo 完善其他 case
    return true
}