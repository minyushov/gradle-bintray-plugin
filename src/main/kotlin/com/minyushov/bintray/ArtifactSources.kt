package com.minyushov.bintray

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
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
    val javaPlugin = project.convention.getPlugin(JavaPluginConvention::class.java)
      ?: throw GradleException("Unable to find JavaPluginConvention")

    val sourceSet = javaPlugin.sourceSets.getByName("main")
      ?: throw GradleException("Unable to find main source set")

    val classesTask = project.tasks.named("classes")
    val sourcesJarTask = project.tasks.register("sourcesJar", Jar::class.java) { task ->
      task.archiveClassifier.set("sources")
      task.from(sourceSet.allSource)
    }

    sourcesJarTask.dependsOn(classesTask)
    publication.artifact(LazyPublishArtifact(sourcesJarTask))
  }
}

internal class ArtifactAndroidSources : ArtifactSources() {
  override fun apply(project: Project, publication: MavenPublication) {
    val android = project.extensions.getByType(LibraryExtension::class.java)
      ?: throw GradleException("Unable to find 'android' extension")

    val sourceSet = android.sourceSets.findByName("main")
      ?: throw GradleException("Unable to find 'main' source set")

    val sourcesJarTask = project.tasks.register("sourcesJar", Jar::class.java) { task ->
      task.archiveClassifier.set("sources")
      task.from(sourceSet.java.srcDirs)
    }

    publication.artifact(LazyPublishArtifact(sourcesJarTask))
  }
}