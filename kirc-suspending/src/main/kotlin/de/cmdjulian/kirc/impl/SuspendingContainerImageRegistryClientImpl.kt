package de.cmdjulian.kirc.impl

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.result.getOrElse
import com.github.kittinunf.result.map
import com.github.kittinunf.result.onError
import de.cmdjulian.kirc.client.SuspendingContainerImageClient
import de.cmdjulian.kirc.client.SuspendingContainerImageRegistryClient
import de.cmdjulian.kirc.exception.RegistryClientException
import de.cmdjulian.kirc.exception.RegistryClientException.ClientException.AuthenticationException
import de.cmdjulian.kirc.exception.RegistryClientException.ClientException.AuthorizationException
import de.cmdjulian.kirc.exception.RegistryClientException.ClientException.MethodNotAllowed
import de.cmdjulian.kirc.exception.RegistryClientException.ClientException.NotFoundException
import de.cmdjulian.kirc.exception.RegistryClientException.ClientException.UnexpectedErrorException
import de.cmdjulian.kirc.exception.RegistryClientException.NetworkErrorException
import de.cmdjulian.kirc.exception.RegistryClientException.UnknownErrorException
import de.cmdjulian.kirc.image.ContainerImageName
import de.cmdjulian.kirc.image.Digest
import de.cmdjulian.kirc.image.Reference
import de.cmdjulian.kirc.image.Repository
import de.cmdjulian.kirc.image.Tag
import de.cmdjulian.kirc.impl.response.Catalog
import de.cmdjulian.kirc.impl.response.TagList
import de.cmdjulian.kirc.spec.image.DockerImageConfigV1
import de.cmdjulian.kirc.spec.image.ImageConfig
import de.cmdjulian.kirc.spec.image.OciImageConfigV1
import de.cmdjulian.kirc.spec.manifest.DockerManifestV2
import de.cmdjulian.kirc.spec.manifest.Manifest
import de.cmdjulian.kirc.spec.manifest.ManifestSingle
import de.cmdjulian.kirc.spec.manifest.OciManifestV1

internal class SuspendingContainerImageRegistryClientImpl(private val api: ContainerRegistryApi) :
    SuspendingContainerImageRegistryClient {

    override suspend fun testConnection() {
        api.ping().onError { throw it.toRegistryClientError() }
    }

    override suspend fun repositories(limit: Int?, last: Int?): List<Repository> =
        api.repositories(limit, last)
            .map(Catalog::repositories)
            .getOrElse { throw it.toRegistryClientError() }

    override suspend fun tags(repository: Repository, limit: Int?, last: Int?): List<Tag> =
        api.tags(repository, limit, last)
            .map(TagList::tags)
            .getOrElse { throw it.toRegistryClientError() }

    override suspend fun exists(repository: Repository, reference: Reference): Boolean =
        api.digest(repository, reference)
            .map { true }
            .getOrElse { if (it.response.statusCode == 404) false else throw it.toRegistryClientError() }

    override suspend fun manifest(repository: Repository, reference: Reference): Manifest =
        api.manifests(repository, reference).getOrElse { throw it.toRegistryClientError() }

    override suspend fun manifestDelete(repository: Repository, reference: Reference): Digest =
        api.deleteManifest(repository, reference).getOrElse { throw it.toRegistryClientError() }

    override suspend fun manifestDigest(repository: Repository, reference: Reference): Digest =
        api.digest(repository, reference).getOrElse { throw it.toRegistryClientError() }

    override suspend fun blob(repository: Repository, digest: Digest): ByteArray =
        api.blob(repository, digest).getOrElse { throw it.toRegistryClientError() }

    override suspend fun config(repository: Repository, reference: Reference): ImageConfig =
        api.manifest(repository, reference)
            .map { config(repository, it) }
            .getOrElse { throw it.toRegistryClientError() }

    override suspend fun config(repository: Repository, manifest: ManifestSingle): ImageConfig =
        api.blob(repository, manifest.config.digest)
            .map { config ->
                when (manifest) {
                    is DockerManifestV2 -> JsonMapper.readValue<DockerImageConfigV1>(config)
                    is OciManifestV1 -> JsonMapper.readValue<OciImageConfigV1>(config)
                }
            }
            .getOrElse { throw it.toRegistryClientError() }

    override suspend fun toImageClient(repository: Repository, reference: Reference): SuspendingContainerImageClient =
        when (reference) {
            is Tag -> SuspendingContainerImageClientImpl(
                this,
                ContainerImageName(repository = repository, tag = reference),
            )

            is Digest -> SuspendingContainerImageClientImpl(
                this,
                ContainerImageName(repository = repository, digest = reference),
            )
        }

    override fun toImageClient(
        repository: Repository,
        reference: Reference,
        manifest: ManifestSingle,
    ) = when (reference) {
        is Tag -> SuspendingContainerImageClientImpl(
            this,
            ContainerImageName(repository = repository, tag = reference),
            manifest,
        )

        is Digest -> SuspendingContainerImageClientImpl(
            this,
            ContainerImageName(repository = repository, digest = reference),
            manifest,
        )
    }
}

private fun FuelError.toRegistryClientError(): RegistryClientException = when (response.statusCode) {
    -1 -> NetworkErrorException(this)
    401 -> AuthenticationException(tryOrNull { JsonMapper.readValue(response.data) }, this)
    403 -> AuthorizationException(tryOrNull { JsonMapper.readValue(response.data) }, this)
    404 -> NotFoundException(tryOrNull { JsonMapper.readValue(response.data) }, this)
    405 -> MethodNotAllowed(tryOrNull { JsonMapper.readValue(response.data) }, this)
    in 406..499 -> UnexpectedErrorException(tryOrNull { JsonMapper.readValue(response.data) }, this)

    else -> UnknownErrorException(this)
}

private inline fun <T> tryOrNull(block: () -> T): T? = runCatching(block).getOrNull()
