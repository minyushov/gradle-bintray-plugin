package com.minyushov.bintray

import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.GradleException
import org.gradle.api.Project

internal class BintrayConfigurator {
  fun configure(project: Project, extension: BintraySimpleExtension, publications: List<String>) {
    val bintray = project.extensions.findByType(BintrayExtension::class.java)
        ?: throw GradleException("Unable to find 'bintray' extension")

    bintray.apply {
      dryRun = extension.dryRun
      user = extension.user
      key = extension.key

      publications
        .toTypedArray()
        .apply { setPublications(*this) }

      pkg(closureOf<BintrayExtension.PackageConfig> {
        if (extension.organization != null) {
          userOrg = extension.organization
        }
        repo = extension.repo
        name = extension.pkgName ?: extension.artifactId
        version(closureOf<BintrayExtension.VersionConfig> {
          name = extension.version
        })
        vcsUrl = extension.vcsUrl
        setLicenses(extension.license)
      })
    }
  }
}
