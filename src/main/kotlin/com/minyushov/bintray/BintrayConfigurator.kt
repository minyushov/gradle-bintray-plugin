package com.minyushov.bintray

import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

internal class BintrayConfigurator {
  fun configure(project: Project, extension: BintraySimpleExtension, publication: MavenPublication) {
    val bintray = project.extensions.findByType(BintrayExtension::class.java)
        ?: throw GradleException("Unable to find 'bintray' extension")

    bintray.apply {
      dryRun = extension.dryRun
      user = extension.user
      key = extension.key
      setPublications(publication.name)
      pkg(closureOf<BintrayExtension.PackageConfig> {
        if (extension.organization != null) {
          userOrg = extension.organization
        }
        repo = extension.repo
        name = extension.artifactId
        version(closureOf<BintrayExtension.VersionConfig> {
          name = extension.version
        })
        vcsUrl = extension.vcsUrl
        setLicenses(extension.license)
      })
    }
  }
}