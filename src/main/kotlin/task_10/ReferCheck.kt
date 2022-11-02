package task_10

import LibraryClass

internal class ReferCheck {

    fun testPrivateMethod() {
        LibraryClass().testPrivateMethod()
    }

    fun testModifyParamsMethod() {
        LibraryClass().testModifyParamsMethod()
    }

    fun testModifyReturnTypeMethod() {
        LibraryClass().testModifyReturnTypeMethod()
    }
}

fun main() {
    // throw IllegalAccessError:
    // class task_11.ReferCheckKt tried to access private method LibraryClass.testPrivateMethod()V
    LibraryClass().testPrivateMethod()
    // throw NoSuchMethodError: LibraryClass.testModifyParamsMethod()V
    LibraryClass().testModifyParamsMethod()
    // NoSuchMethodError: LibraryClass.testModifyReturnTypeMethod()V
    LibraryClass().testModifyReturnTypeMethod()
}