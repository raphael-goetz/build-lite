package de.raphaelgoetz.buildLite.world

import de.raphaelgoetz.buildLite.config.PluginConfig
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.io.File
import java.net.URI
import java.time.Duration

/** Thin wrapper around Cloudflare R2's S3-compatible API. Only constructed
 * once R2 credentials are present -- see [PluginConfig.r2Configured]. */
object R2Client {

    private fun endpoint(config: PluginConfig): URI =
        URI.create("https://${config.r2AccountId}.r2.cloudflarestorage.com")

    private fun credentials(config: PluginConfig) =
        StaticCredentialsProvider.create(
            AwsBasicCredentials.create(config.r2AccessKeyId, config.r2SecretAccessKey)
        )

    private fun client(config: PluginConfig): S3Client =
        S3Client.builder()
            .endpointOverride(endpoint(config))
            .region(Region.of("auto"))
            .credentialsProvider(credentials(config))
            .forcePathStyle(true)
            .httpClientBuilder(UrlConnectionHttpClient.builder())
            .build()

    fun upload(config: PluginConfig, key: String, file: File) {
        client(config).use { s3 ->
            s3.putObject(
                PutObjectRequest.builder().bucket(config.r2Bucket).key(key).build(),
                file.toPath(),
            )
        }
    }

    fun delete(config: PluginConfig, key: String) {
        client(config).use { s3 ->
            s3.deleteObject(DeleteObjectRequest.builder().bucket(config.r2Bucket).key(key).build())
        }
    }

    fun presignGet(config: PluginConfig, key: String, ttl: Duration): String {
        S3Presigner.builder()
            .endpointOverride(endpoint(config))
            .region(Region.of("auto"))
            .credentialsProvider(credentials(config))
            .build().use { presigner ->
                val getRequest = GetObjectRequest.builder().bucket(config.r2Bucket).key(key).build()
                val presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(ttl)
                    .getObjectRequest(getRequest)
                    .build()

                return presigner.presignGetObject(presignRequest).url().toString()
            }
    }
}
