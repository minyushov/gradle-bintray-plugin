package com.minyushov.bintray.project

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.jfrog.bintray.gradle.BintrayExtension
import com.minyushov.bintray.BintrayPlugin
import com.minyushov.bintray.BintrayPluginExtension
import com.minyushov.bintray.artifact.ArtifactProvider
import com.minyushov.bintray.artifact.ComponentProvider
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import java.io.DataInputStream
import java.io.FileNotFoundException
import java.util.Properties

internal abstract class BintrayProject(
  protected val project: Project,
  protected val extension: BintrayPluginExtension
) {

  protected val componentProvider = ComponentProvider(project, extension)
  protected val artifactProvider = ArtifactProvider(project, extension)

  protected abstract val components: List<SoftwareComponent>
  protected abstract val artifacts: List<PublishArtifact>

  fun configure() {
    applyPlugins()
    project.afterEvaluate {
      createPublication()
      configureBintray()
      configureTaskDependencies()
    }
  }

  protected open fun applyPlugins() {
    project.pluginManager.apply {
      apply(MavenPublishPlugin::class.java)
      apply(com.jfrog.bintray.gradle.BintrayPlugin::class.java)
    }
  }

  private fun createPublication() {
    project.extensions.configure(PublishingExtension::class.java) { publishing ->
      val publication = publishing.publications.maybeCreate("maven", MavenPublication::class.java)

      val version = checkNotNull(extension.version.orNull) { "Bintray library version is not defined in '${BintrayPlugin.EXTENSION_NAME}' extension" }
      project.setProperty("version", version)
      publication.version = version

      val group = checkNotNull(extension.groupId.orNull) { "Bintray library groupId is not defined in '${BintrayPlugin.EXTENSION_NAME}' extension" }
      project.setProperty("group", group)
      publication.groupId = group

      val artifactId = checkNotNull(extension.artifactId.orNull) { "Bintray library artifactId is not defined in '${BintrayPlugin.EXTENSION_NAME}' extension" }
      project.setProperty("archivesBaseName", artifactId)
      publication.artifactId = artifactId

      components.forEach { publication.from(it) }
      artifacts.forEach { publication.artifact(it) }
    }
  }

  private fun configureBintray() {
    val bintray = checkNotNull(project.extensions.findByType(BintrayExtension::class.java)) { "Unable to find 'bintray' extension" }

    bintray.dryRun = extension
      .dryRun
      .getOrElse(true)

    bintray.key = extension
      .key
      .apply {
        if (orNull.isNullOrEmpty()) {
          set(project.localProperty("bintray.key"))
        }
      }
      .orNull

    if (bintray.key.isNullOrEmpty()) {
      project.logger.warn("Bintray api key is not defined in '${BintrayPlugin.EXTENSION_NAME}' extension")
    }

    bintray.user = extension
      .user
      .apply {
        if (orNull.isNullOrEmpty()) {
          set(project.localProperty("bintray.user"))
        }
      }
      .orNull

    if (bintray.user.isNullOrEmpty()) {
      project.logger.warn("Bintray user is not defined in '${BintrayPlugin.EXTENSION_NAME}' extension")
    }

    bintray.pkg.userOrg = extension
      .organization
      .orNull
      ?: bintray.user

    bintray.pkg.repo = extension
      .repo
      .orNull
      ?: throw GradleException("Bintray repo is not defined in '${BintrayPlugin.EXTENSION_NAME}' extension")

    bintray.pkg.name = extension
      .pkgName
      .orNull
      ?: extension
        .artifactId
        .orNull
        ?: throw GradleException("Bintray library artifactId is not defined in '${BintrayPlugin.EXTENSION_NAME}' extension")

    bintray.pkg.vcsUrl = extension
      .vcsUrl
      .orNull
      ?: throw GradleException("Bintray library vcsUrl is not defined in '${BintrayPlugin.EXTENSION_NAME}' extension")

    bintray.pkg.version.name = extension
      .version
      .orNull
      ?: project.version.toString()
        ?: throw GradleException("Bintray library version is not defined in '${BintrayPlugin.EXTENSION_NAME}' extension")

    bintray.pkg.setLicenses(
      extension
        .license
        .orNull
        ?: throw GradleException("Bintray library license is not defined in '${BintrayPlugin.EXTENSION_NAME}' extension")
    )

    project.extensions.configure(PublishingExtension::class.java) { publishing ->
      bintray.setPublications(*publishing.publications.mapNotNull { it.name }.toTypedArray())
    }
  }

  private fun configureTaskDependencies() {
    project.extensions.configure(PublishingExtension::class.java) { publishing ->
      val uploadTask = project.tasks.named("bintrayUpload")
      publishing.publications.forEach { publication ->
        uploadTask.dependsOn("publish${publication.name.capitalize()}PublicationToMavenLocal")
      }
    }
  }
}

internal fun Project.createBintrayProject(extension: BintrayPluginExtension): BintrayProject? =
  pluginManager.let { manager ->
    when {
      manager.hasPlugin("com.android.library") && manager.hasPlugin("kotlin-android") -> AndroidKotlinLibraryProject(this, extension)
      manager.hasPlugin("com.android.library") -> AndroidJavaLibraryProject(this, extension)
      manager.hasPlugin("kotlin") && !manager.hasPlugin("com.android.application") -> KotlinLibraryProject(this, extension)
      manager.hasPlugin("java-library") || manager.hasPlugin("java") || manager.hasPlugin("groovy") -> JavaLibraryProject(this, extension)
      else -> null
    }
  }

private const val PROPERTIES_NAME = "local.properties"

private fun Project.localProperty(key: String): String? =
  localProperties(PROPERTIES_NAME) { getProperty(key) }
    ?: rootProject.localProperties(PROPERTIES_NAME) { getProperty(key) }

private inline fun Project.localProperties(name: String, action: Properties.() -> String?): String? =
  try {
    DataInputStream(file(name).inputStream()).use {
      action(Properties().apply { load(it) })
    }
  } catch (exception: FileNotFoundException) {
    null
  }