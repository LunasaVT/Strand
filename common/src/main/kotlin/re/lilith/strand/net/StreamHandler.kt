package re.lilith.strand.net

interface StreamHandler {
    fun onData(data: ByteArray, off: Int, len: Int)
    fun onClose()
}