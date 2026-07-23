package re.lilith.strand.session

interface ClientHooks {
    fun profile(): Profile?
    fun joinServer(serverId: String)
    fun connectToLocal(port: Int)
    fun lanPort(): Int?
    fun notify(message: String)
    fun toast(title: String, body: String)
    fun openHostToLanScreen(onHosted: () -> Unit)
}
