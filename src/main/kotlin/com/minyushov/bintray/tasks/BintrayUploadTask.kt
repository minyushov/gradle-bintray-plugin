package com.minyushov.bintray.tasks

import com.minyushov.bintray.utils.localProperty
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okio.ByteString.Companion.encodeUtf8
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.property
import java.io.File

private const val API_URL = "https://api.bintray.com"

open class BintrayUploadTask : DefaultTask() {

  @get:Input
  @get:Optional
  val user: Property<String> = project.objects.property(String::class)

  @get:Input
  @get:Optional
  val organization: Property<String> = project.objects.property(String::class)

  @get:Input
  @get:Optional
  val key: Property<String> = project.objects.property(String::class)

  @get:Input
  val repository: Property<String> = project.objects.property(String::class)

  @get:Input
  @get:Optional
  val packageName: Property<String> = project.objects.property(String::class)

  @get:Input
  val artifactId: Property<String> = project.objects.property(String::class)

  @get:Input
  val license: Property<String> = project.objects.property(String::class)

  @get:Input
  val vcsUrl: Property<String> = project.objects.property(String::class)

  @get:Input
  val version: Property<String> = project.objects.property(String::class)

  @get:Input
  val dryRun: Property<Boolean> = project.objects.property(Boolean::class.javaObjectType)

  @TaskAction
  fun execute() {
    val organization = this.organization.getOrElse(project.localProperty("bintray.organization").orEmpty())
    val user = this.user.getOrElse(project.localProperty("bintray.user").orEmpty())
    if (user.isEmpty()) {
      throw GradleException("Bintray user is missing")
    }

    val subject = if (organization.isEmpty()) user else organization

    val key = this.key.getOrElse(project.localProperty("bintray.key").orEmpty())
    if (key.isEmpty()) {
      throw GradleException("Bintray key is missing")
    }

    val repository = Repository(
      user = subject,
      name = repository.get()
    )

    val pkg = Package(
      name = packageName.orNull ?: artifactId.get(),
      license = license.get(),
      vcs = vcsUrl.get()
    )

    val version = Version(
      name = this.version.get()
    )

    val client = createHttpClient(authToken = "Basic ${"$user:$key".encodeUtf8().base64()}")

    createRepository(client, repository)
    createPackage(client, repository, pkg)
    createVersion(client, repository, pkg, version)

    project.extensions.configure<PublishingExtension> {
      publications
        .mapNotNull { publication ->
          if (publication !is DefaultMavenPublication) {
            logger.warn("Publication '${publication.name}' is skipped. Only maven publications are supported")
            null
          } else {
            publication
              .publishableArtifacts
              .mapNotNull { artifact ->
                Artifact(
                  name = publication.artifactId,
                  groupId = publication.groupId,
                  extension = artifact.extension,
                  classifier = artifact.classifier,
                  file = artifact.file
                )
              }
          }
        }
        .flatten()
        .forEach { artifact -> uploadArtifact(client, repository, pkg, version, artifact) }
    }

  }

  private fun createRepository(
    client: OkHttpClient,
    repository: Repository
  ) {
    val path = "$API_URL/repos/${repository.user}/${repository.name}"

    logger.lifecycle("Creating repository '${repository.name}' at '$path'")

    if (dryRun.get()) {
      logger.lifecycle("[dryRun] Created repository '${repository.name}' at '$path'")
      return
    }

    val response = client.sendRequest(
      Request
        .Builder()
        .url(path)
        .post(
          """
            {
              "name": "${repository.name}",
              "type": "maven"
            }
          """
            .trimIndent()
            .toRequestBody("application/json".toMediaType())
        )
    )
    when {
      response.isSuccessful -> logger.lifecycle("Created repository '${repository.name}' at '$path'")
      response.code == 409 -> logger.lifecycle("Repository '${repository.name}' at '$path' already exists")
      else -> throw GradleException("Could not create repository '${repository.name}' at '$path': ${response.code} ${response.body?.string()}")
    }
  }

  private fun createPackage(
    client: OkHttpClient,
    repository: Repository,
    pkg: Package
  ) {
    val path = "${API_URL}/packages/${repository.user}/${repository.name}"

    logger.lifecycle("Creating package '${pkg.name}' at '$path'")

    if (dryRun.get()) {
      logger.lifecycle("[dryRun] Created package '${pkg.name}' at '$path'")
      return
    }

    val response = client.sendRequest(Request
      .Builder()
      .url(path)
      .post(
        """
            {
              "name": "${pkg.name}",
              "licenses": [ "${pkg.license}" ],
              "vcs_url": "${pkg.vcs}"
            }
          """
          .trimIndent()
          .toRequestBody("application/json".toMediaType())
      )
    )

    when {
      response.isSuccessful -> logger.lifecycle("Created package '${pkg.name}' at '$path'")
      response.code == 409 -> logger.lifecycle("Package '${pkg.name}' at '$path' already exists")
      else -> throw GradleException("Could not create package '${pkg.name}' at '$path': ${response.code} ${response.body?.string()}")
    }
  }

  private fun createVersion(
    client: OkHttpClient,
    repository: Repository,
    pkg: Package,
    version: Version
  ) {
    val path = "${API_URL}/packages/${repository.user}/${repository.name}/${pkg.name}/versions"

    logger.lifecycle("Creating version '${version.name}' at '$path'")

    if (dryRun.get()) {
      logger.lifecycle("[dryRun] Created version '${version.name}' at '$path'")
      return
    }

    val response = client.sendRequest(Request
      .Builder()
      .url(path)
      .post(
        """
            {
              "name": "${version.name}"
            }
          """
          .trimIndent()
          .toRequestBody("application/json".toMediaType())
      )
    )
    when {
      response.isSuccessful -> logger.lifecycle("Created version '${version.name}' at '$path'")
      response.code == 409 -> logger.lifecycle("Version '${version.name}' at '$path' already exists")
      else -> throw GradleException("Could not create version '${version.name}' at '$path': ${response.code} ${response.body?.string()}")
    }
  }

  private fun uploadArtifact(
    client: OkHttpClient,
    repository: Repository,
    pkg: Package,
    version: Version,
    artifact: Artifact
  ) {
    if (!artifact.file.exists()) {
      logger.error("Skip uploading missing file '${artifact.file}'.")
      return
    }

    val versionPath = "$API_URL/content/${repository.user}/${repository.name}/${pkg.name}/${version.name}"
    val groupPath = artifact.groupId.replace('.', '/')
    val classifier = if (artifact.classifier != null) "-${artifact.classifier}" else ""
    val path = "$versionPath/$groupPath/${artifact.name}/${version.name}/${artifact.name}-${version.name}$classifier.${artifact.extension}"

    logger.lifecycle("Uploading to $path...")
    if (dryRun.get()) {
      logger.lifecycle("[dryRun] Uploaded to '$path'")
      return
    }

    val response = client.sendRequest(Request
      .Builder()
      .url("$path?override=1")
      .put(artifact.file.asRequestBody("application/octet-stream".toMediaType()))
      .header("Content-Type", "*/*")
    )

    when {
      response.isSuccessful -> logger.lifecycle("Uploaded to '$path'")
      else -> throw GradleException("Could not upload to '$path': ${response.code} ${response.body?.string()}")
    }
  }

  private fun createHttpClient(authToken: String): OkHttpClient =
    OkHttpClient
      .Builder()
      .addInterceptor(
        HttpLoggingInterceptor(
          object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) = logger.info(message)
          }
        ).apply { level = HttpLoggingInterceptor.Level.BODY }
      )
      .addNetworkInterceptor { chain ->
        chain.proceed(
          chain
            .request()
            .newBuilder()
            .header("User-Agent", "gradle-bintray-plugin/1.8.4")
            .header("Authorization", authToken)
            .build()
        )
      }
      .build()

  private fun OkHttpClient.sendRequest(request: Request.Builder): Response =
    request
      .build()
      .let { newCall(it).execute() }

  private class Repository(
    val user: String,
    val name: String
  )

  private class Package(
    val name: String,
    val license: String,
    val vcs: String
  )

  private class Version(
    val name: String
  )

  private class Artifact(
    val name: String,
    val groupId: String,
    val extension: String,
    val classifier: String?,
    val file: File
  )

}