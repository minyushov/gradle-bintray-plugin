package com.minyushov.bintray

import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
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
      .also {
        project
          .getType()
          .takeIf { it !is ProjectType.Unknown }
          ?.run { configure(project, it) }
            ?: return
      }
  }

  private fun ProjectType.configure(project: Project, extension: BintraySimpleExtension) {
    pluginsConfigurators.forEach { it.configure(project) }

    project.afterEvaluate {
      project.extensions.configure(PublishingExtension::class.java) { publishing ->
        val publication = publishing.publications.maybeCreate("maven", MavenPublication::class.java)

        extension
          .version
          .orNull
          ?.apply { project.setProperty("version", this) }
          ?.apply { publication.version = this }
            ?: throw GradleException("Bintray library version is not defined in '$EXTENSION_NAME' extension")

        extension
          .groupId
          .orNull
          ?.apply { project.setProperty("group", this) }
          ?.apply { publication.groupId = this }
            ?: throw GradleException("Bintray library groupId is not defined in '$EXTENSION_NAME' extension")

        extension
          .artifactId
          .orNull
          ?.apply { project.setProperty("archivesBaseName", this) }
          ?.apply { publication.artifactId = this }
            ?: throw GradleException("Bintray library artifactId is not defined in '$EXTENSION_NAME' extension")

        artifacts
          .forEach { it.apply(project, extension, publication) }

        publishing
          .publications
          .mapNotNull { it.name }
          .also { publications ->

            project
              .tasks
              .findByName("bintrayUpload")
              .let { it as? BintrayUploadTask }
              ?.apply {

                dryRun = extension
                  .dryRun
                  .getOrElse(true)

                apiKey = extension
                  .key
                  .apply {
                    if (orNull.isNullOrEmpty()) {
                      set(project.localProperty("bintray.key"))
                    }
                  }
                  .orNull
                    ?: throw GradleException("Bintray api key is not defined in '$EXTENSION_NAME' extension")

                user = extension
                  .user
                  .apply {
                    if (orNull.isNullOrEmpty()) {
                      set(project.localProperty("bintray.user"))
                    }
                  }
                  .orNull
                    ?: throw GradleException("Bintray user is not defined in '$EXTENSION_NAME' extension")

                userOrg = extension
                  .organization
                  .orNull

                repoName = extension
                  .repo
                  .orNull
                    ?: throw GradleException("Bintray repo is not defined in '$EXTENSION_NAME' extension")

                packageName = extension
                  .pkgName
                  .orNull
                    ?: extension
                  .artifactId
                  .orNull
                    ?: throw GradleException("Bintray library artifactId is not defined in '$EXTENSION_NAME' extension")

                packageVcsUrl = extension
                  .vcsUrl
                  .orNull
                    ?: throw GradleException("Bintray library vcsUrl is not defined in '${BintrayPlugin.EXTENSION_NAME}' extension")

                versionName = extension
                  .version
                  .orNull
                    ?: throw GradleException("Bintray library version is not defined in '${BintrayPlugin.EXTENSION_NAME}' extension")

                setPackageLicenses(
                  extension
                    .license
                    .orNull
                      ?: throw GradleException("Bintray library license is not defined in '${BintrayPlugin.EXTENSION_NAME}' extension")
                )

                publications
                  .toTypedArray()
                  .apply { setPublications(*this) }
                  .forEach {
                    publishing
                      .publications
                      .findByName(it)
                      ?.also {
                        dependsOn("publish${it.name.capitalize()}PublicationToMavenLocal")
                      } ?: project.logger.warn("Publication $it not found in project ${project.path}.")
                  }

              } ?: throw GradleException("Unable to find 'bintrayUpload' task")
          }
      }
    }
  }

  internal companion object {
    internal const val EXTENSION_NAME = "bintrayUpload"
  }
}