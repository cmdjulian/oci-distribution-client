package de.cmdjulian.distribution.spec.image

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.OffsetDateTime

/*
 * {
 *     "created": "2015-10-31T22:22:56.015925234Z",
 *     "author": "Alyssa P. Hacker <alyspdev@example.com>",
 *     "architecture": "amd64",
 *     "os": "linux",
 *     "config": {
 *         "User": "alice",
 *         "ExposedPorts": {
 *             "8080/tcp": {}
 *         },
 *         "Env": [
 *             "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
 *             "FOO=oci_is_a",
 *             "BAR=well_written_spec"
 *         ],
 *         "Entrypoint": [
 *             "/bin/my-app-binary"
 *         ],
 *         "Cmd": [
 *             "--foreground",
 *             "--config",
 *             "/etc/my-app.d/default.cfg"
 *         ],
 *         "Volumes": {
 *             "/var/job-result-data": {},
 *             "/var/log/my-app-logs": {}
 *         },
 *         "WorkingDir": "/home/alice",
 *         "Labels": {
 *             "com.example.project.git.url": "https://example.com/project.git",
 *             "com.example.project.git.commit": "45a939b2999782a3f005621a8d0f29aa387e1d6b"
 *         }
 *     },
 *     "rootfs": {
 *       "diff_ids": [
 *         "sha256:c6f988f4874bb0add23a778f753c65efe992244e148a1d2ec2a8b664fb66bbd1",
 *         "sha256:5f70bf18a086007016e948b04aed3b82103a36bea41755b6cddfaf10ace3c6ef"
 *       ],
 *       "type": "layers"
 *     },
 *     "history": [
 *       {
 *         "created": "2015-10-31T22:22:54.690851953Z",
 *         "created_by": "/bin/sh -c #(nop) ADD file:a3bc1e842b69636f9df5256c49c5374fb4eef1e281fe3f282c65fb853ee171c5 in /"
 *       },
 *       {
 *         "created": "2015-10-31T22:22:55.613815829Z",
 *         "created_by": "/bin/sh -c #(nop) CMD [\"sh\"]",
 *         "empty_layer": true
 *       },
 *       {
 *         "created": "2015-10-31T22:22:56.329850019Z",
 *         "created_by": "/bin/sh -c apk add curl"
 *       }
 *     ]
 * }
 */
// https://github.com/opencontainers/image-spec/blob/main/config.md
@JsonNaming(PropertyNamingStrategies.LowerDotCaseStrategy::class)
data class OciImageConfigV1(
    override val created: OffsetDateTime?,
    override val author: String?,
    override val architecture: String,
    override val os: String,
    val osVersion: String?,
    val osFeatures: List<String>?,
    val variant: String?,
    override val config: ImageConfig.Config?,
    override val rootfs: RootFs,
    override val history: List<History>?,
) : ImageConfig {
    companion object {
        const val MediaType = "application/vnd.oci.image.config.v1+json"
    }
}
