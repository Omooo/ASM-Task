package common


import java.io.FileOutputStream

internal fun ByteArray.toFile(fileName: String) {
    FileOutputStream(fileName).apply {
        write(this@toFile)
        flush()
        close()
    }
}

internal fun ByteArray.loadClass(fileName: String): Class<*>? {
    return Class.forName(fileName, true, Loader(this))
}

internal class Loader(private val bytes: ByteArray) : ClassLoader() {

    override fun findClass(name: String?): Class<*> {
        return defineClass(name, bytes, 0, bytes.size)
    }
}