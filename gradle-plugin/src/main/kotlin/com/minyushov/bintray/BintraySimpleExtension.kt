package com.minyushov.bintray

import org.gradle.api.Project

open class BintraySimpleExtension(private val project: Project) {
  var user: String? = null
  var key: String? = null
  var organization: String? = null
  var repo: String? = null
  var groupId: String? = null
  var artifactId: String? = null
  var version: String? = null
  var vcsUrl: String? = null
  var license: String? = null
  var sources: Boolean = true
  var docs: Boolean = true
  var dryRun: Boolean = true
}