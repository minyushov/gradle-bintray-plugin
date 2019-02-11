package com.minyushov.bintray

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

class BintrayPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project
      .extensions
      .create(EXTENSION_NAME, BintraySimpleExtension::class.java, project)
      .also { extension ->
        project
          .getType()
          .takeIf { it !is ProjectType.Unknown }
          ?.run { configure(project, extension) }
          ?: return
      }
  }

  private fun ProjectType.configure(project: Project, extension: BintraySimpleExtension) {
    pluginsConfigurators.forEach { it.configure(project) }

    project.afterEvaluate {
      createPublication(project, artifacts, extension)
      configureBintray(project, extension)
      configureTaskDependencies(project)
    }
  }

  private fun createPublication(project: Project, artifacts: List<Artifact>, extension: BintraySimpleExtension) {
    project.extensions.configure(PublishingExtension::class.java) { publishing ->
      val publication = publishing.publications.maybeCreate("maven", MavenPublication::class.java)

      val version = checkNotNull(extension.version.orNull) { "Bintray library version is not defined in '$EXTENSION_NAME' extension" }
      project.setProperty("version", version)
      publication.version = version

      val group = checkNotNull(extension.groupId.orNull) { "Bintray library groupId is not defined in '$EXTENSION_NAME' extension" }
      project.setProperty("group", group)
      publication.groupId = group

      val artifactId = checkNotNull(extension.artifactId.orNull) { "Bintray library artifactId is not defined in '$EXTENSION_NAME' extension" }
      project.setProperty("archivesBaseName", artifactId)
      publication.artifactId = artifactId

      artifacts.forEach { it.apply(project, extension, publication) }
    }
  }

  private fun configureBintray(project: Project, extension: BintraySimpleExtension) {
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
      project.logger.warn("Bintray api key is not defined in '$EXTENSION_NAME' extension")
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
      project.logger.warn("Bintray user is not defined in '$EXTENSION_NAME' extension")
    }

    bintray.pkg.userOrg = extension
      .organization
      .orNull
      ?: bintray.user

    bintray.pkg.repo = extension
      .repo
      .orNull
      ?: throw GradleException("Bintray repo is not defined in '$EXTENSION_NAME' extension")

    bintray.pkg.name = extension
      .pkgName
      .orNull
      ?: extension
        .artifactId
        .orNull
        ?: throw GradleException("Bintray library artifactId is not defined in '$EXTENSION_NAME' extension")

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

  private fun configureTaskDependencies(project: Project) {
    project.extensions.configure(PublishingExtension::class.java) { publishing ->
      val uploadTask = project.tasks.named("bintrayUpload")
      publishing.publications.forEach { publication ->
        uploadTask.dependsOn("publish${publication.name.capitalize()}PublicationToMavenLocal")
      }
    }
  }

  internal companion object {
    internal const val EXTENSION_NAME = "bintrayUpload"
  }
}