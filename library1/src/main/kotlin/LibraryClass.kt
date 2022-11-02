class LibraryClass {

    /**
     * libs 里的该类，该方法是 private 的
     */
    fun testPrivateMethod() {
        println("LibraryClass#testPrivateMethod")
    }

    /**
     * libs 里的该类，该方法多了一个 int 参数
     */
    fun testModifyParamsMethod() {
        println("LibraryClass#testModifyParamsMethod")
    }

    /**
     * libs 里的该类，该方法有了返回值
     */
    fun testModifyReturnTypeMethod() {
        println("LibraryClass#testModifyReturnTypeMethod")
    }
}