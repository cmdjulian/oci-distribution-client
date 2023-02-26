package de.cmdjulian.kirc.model

@JvmInline
value class Registry(private val value: String) {
    //language=RegExp
    companion object {
        private const val IP_ADDRESS = "(((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4})"
        private const val DOMAIN = "(([a-zA-Z\\d_\\-]+)((\\.[a-zA-Z\\d_\\-]+)*))"
        private const val PORT =
            "(:(6553[0-5]|655[0-2][0-9]\\d|65[0-4](\\d){2}|6[0-4](\\d){3}|[1-5](\\d){4}|[1-9](\\d){0,3}))"
        val regex = Regex("^($IP_ADDRESS|$DOMAIN)$PORT?\$")
    }

    init {
        require(value.matches(regex)) { "invalid registry" }
    }

    override fun toString(): String = value
}
