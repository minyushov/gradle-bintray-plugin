package com.minyushov.bintray

import com.android.build.gradle.LibraryExtension
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar

internal abstract class ArtifactSources : Artifact {
  override fun apply(project: Project, extension: BintraySimpleExtension, publication: MavenPublication) {
    if (extension.sources.getOrElse(true)) {
      apply(project, publication)
    }
  }

  abstract fun apply(project: Project, publication: MavenPublication)
}

internal class ArtifactJavaSources : ArtifactSources() {
  override fun apply(project: Project, publication: MavenPublication) {
    val classesTask = project.tasks.findByName("classes")
      ?: throw GradleException("Unable to find 'classes' task")

    val javaPlugin = project.convention.getPlugin(JavaPluginConvention::class.java)
      ?: throw GradleException("Unable to find JavaPluginConvention")

    val sourceSet = javaPlugin.sourceSets.getByName("main")
      ?: throw GradleException("Unable to find main source set")

    val task = project.task(
      mapOf(
        Task.TASK_TYPE to Jar::class.java,
        Task.TASK_DEPENDS_ON to classesTask
      ),
      "sourcesJar",
      project.closureOf<Jar> {
        classifier = "sources"
        from(sourceSet.allSource)
      }
    )

    publication.artifact(task)
  }
}

internal class ArtifactAndroidSources : ArtifactSources() {
  override fun apply(project: Project, publication: MavenPublication) {
    val android = project.extensions.getByType(LibraryExtension::class.java)
      ?: throw GradleException("Unable to find 'android' extension")

    val sourceSet = android.sourceSets.findByName("main")
      ?: throw GradleException("Unable to find 'main' source set")

    val task = project.task(
      mapOf(
        Task.TASK_TYPE to Jar::class.java
      ),
      "sourcesJar",
      project.closureOf<Jar> {
        classifier = "sources"
        from(sourceSet.java.srcDirs)
      }
    )

    publication.artifact(task)
  }
}