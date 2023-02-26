package de.cmdjulian.kirc.spec.manifest

import de.cmdjulian.kirc.model.Digest
import de.cmdjulian.kirc.spec.Architecture
import de.cmdjulian.kirc.spec.OS

data class ManifestListEntry(val mediaType: String, val digest: Digest, val size: UInt, val platform: Platform?) {
    data class Platform(val os: OS, val architecture: Architecture, val features: List<String>)
}