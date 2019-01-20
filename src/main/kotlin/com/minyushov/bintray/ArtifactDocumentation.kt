package com.minyushov.bintray

import com.android.build.gradle.LibraryExtension
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.jetbrains.dokka.gradle.DokkaTask
import java.io.File

internal abstract class ArtifactDocumentation : Artifact {
  override fun apply(project: Project, extension: BintraySimpleExtension, publication: MavenPublication) {
    if (extension.docs.getOrElse(true)) {
      configure(project, extension, publication)
    }
  }

  abstract fun configure(project: Project, extension: BintraySimpleExtension, publication: MavenPublication)
}

internal class ArtifactJavaDoc : ArtifactDocumentation() {
  override fun configure(project: Project, extension: BintraySimpleExtension, publication: MavenPublication) {
    val javadocTask = project.tasks.findByName("javadoc") as? Javadoc
      ?: throw GradleException("Unable to find 'javadoc' task")

    extension
      .docsSettings
      ?.apply { delegate = javadocTask }
      ?.call()

    val task = project.task(
      mapOf(
        Task.TASK_TYPE to Jar::class.java,
        Task.TASK_DEPENDS_ON to javadocTask
      ),
      "javadocJar",
      project.closureOf<Jar> {
        archiveClassifier.set("javadoc")
        from(javadocTask.destinationDir)
      }
    )

    publication.artifact(task)
  }
}

internal class ArtifactAndroidDoc : ArtifactDocumentation() {
  override fun configure(project: Project, extension: BintraySimpleExtension, publication: MavenPublication) {
    val android = project.extensions.getByType(LibraryExtension::class.java)
      ?: throw GradleException("Unable to find 'android' extension")

    val sourceSet = android.sourceSets.findByName("main")
      ?: throw GradleException("Unable to find 'main' source set")

    val androidJavadocs = project.task(
      mapOf(
        Task.TASK_TYPE to Javadoc::class.java
      ),
      "androidJavadocs",
      project.closureOf<Javadoc> {
        source(sourceSet.java.sourceFiles)
        classpath += project.files(android.bootClasspath.joinToString(File.pathSeparator))
      }
    ) as Javadoc

    extension
      .docsSettings
      ?.apply { delegate = androidJavadocs }
      ?.call()

    val task = project.task(
      mapOf(
        Task.TASK_TYPE to Jar::class.java,
        Task.TASK_DEPENDS_ON to androidJavadocs
      ),
      "javadocJar",
      project.closureOf<Jar> {
        archiveClassifier.set("javadoc")
        from(androidJavadocs.destinationDir)
      }
    )

    publication.artifact(task)
  }
}

internal class ArtifactKotlinDoc : ArtifactDocumentation() {
  override fun configure(project: Project, extension: BintraySimpleExtension, publication: MavenPublication) {
    val dokka = project.tasks.findByName("dokka") as? DokkaTask
      ?: throw GradleException("Unable to find 'dokka' task")

    dokka.outputFormat = "javadoc"

    extension
      .docsSettings
      ?.apply { delegate = dokka }
      ?.call()

    val task = project.task(
      mapOf(
        Task.TASK_TYPE to Jar::class.java,
        Task.TASK_DEPENDS_ON to dokka
      ),
      "javadocJar",
      project.closureOf<Jar> {
        archiveClassifier.set("javadoc")
        from(dokka.outputDirectory)
      }
    )

    publication.artifact(task)
  }
}