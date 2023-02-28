package de.cmdjulian.kirc.image

/**
 * Parses a docker image name from a provided string.
 *
 *
 *     `name`,
 *     `name:tag`,
 *     `path/name`,
 *     `path/name:tag`,
 *     `some.registry/name`,
 *     `some.registry/name:tag`,
 *     `some.registry/path/name`,
 *     `some.registry/path/name:tag`,
 *     `some.registry/path/name@sha256:abcdef...`,
 *     `some.registry/path/name:tag@sha256:abcdef...`
 *
 * etc.
 */
class ContainerImageName(
    val registry: Registry = Registry(DOCKER_HUB_REGISTRY),
    val repository: Repository,
    tag: Tag? = null,
    @get:JvmName("getDigest") val digest: Digest? = null,
) {

    @get:JvmName("getTag")
    val tag = if (tag == null && digest == null) Tag.LATEST else tag
    val reference get() = digest ?: this.tag!!

    fun copy(
        registry: Registry = this.registry,
        repository: Repository = this.repository,
        tag: Tag? = this.tag,
        digest: Digest? = this.digest,
    ) = ContainerImageName(registry, repository, tag, digest)

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is ContainerImageName -> false
        registry != other.registry -> false
        repository != other.repository -> false
        reference != other.reference -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = registry.hashCode()
        result = 31 * result + repository.hashCode()
        result = 31 * result + reference.hashCode()
        return result
    }

    override fun toString(): String {
        val tagComponent = tag?.asImagePart() ?: ""
        val digestComponent = digest?.asImagePart() ?: ""

        return "$registry/$repository$tagComponent$digestComponent"
    }

    companion object {
        const val DOCKER_HUB_REGISTRY = "docker.io"

        @JvmStatic
        fun parse(image: String): ContainerImageName {
            val slashIndex = image.indexOf('/')
            val isRegistryMissing = slashIndex == -1 ||
                "." !in image.substring(0, slashIndex) &&
                ":" !in image.substring(0, slashIndex) &&
                image.substring(0, slashIndex) != "localhost"
            val remoteName: String = if (isRegistryMissing) image else image.substring(slashIndex + 1)

            val registry = if (isRegistryMissing) null else Registry(image.substring(0, slashIndex))
            val (repository, tag, digest) = parseRepositoryAndVersion(remoteName)

            return if (registry == null) {
                ContainerImageName(repository = repository, tag = tag, digest = digest)
            } else {
                ContainerImageName(registry, repository, tag, digest)
            }
        }

        private fun parseRepositoryAndVersion(remoteName: String) = when {
            Digest.separator in remoteName -> {
                val repository: String
                val tag: String?
                val parts = remoteName.split('@').toTypedArray()
                // If a Tag is present remove it, because content is already identified by hash
                repository = if (':' in parts[0]) {
                    val subParts = parts[0].split(':').toTypedArray()
                    tag = subParts[1]
                    subParts[0]
                } else {
                    tag = null
                    parts[0]
                }
                Triple(Repository(repository), tag?.let(::Tag), Digest(parts[1]))
            }

            Tag.separator in remoteName -> {
                val parts = remoteName.split(':').toTypedArray()
                Triple(Repository(parts[0]), Tag(parts[1]), null)
            }

            else -> Triple(Repository(remoteName), null, null)
        }
    }
}