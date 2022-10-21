package common

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.MethodNode

/**
 * 校验是否是空方法体
 *
 * @return true: 是
 */
internal fun MethodNode.isEmpty(): Boolean {
    val instructionList = instructions.iterator().asSequence().filterNot {
        it is LabelNode || it is LineNumberNode
    }.toList()
    return instructionList.size == 1 && instructionList[0].opcode == Opcodes.RETURN
}