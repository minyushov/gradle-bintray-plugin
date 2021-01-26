### Setup
```groovy
buildscript {
  dependencies {
    classpath "com.minyushov.gradle:bintray:x"
  }
}
```
Please replace `x` with the latest version: [![Download](https://api.bintray.com/packages/minyushov/gradle/bintray/images/download.svg)](https://bintray.com/minyushov/gradle/bintray/_latestVersion)

```groovy
apply plugin: 'com.minyushov.bintray'
```
Please apply the plugin after `com.android.library`, `kotlin-android`, `kotlin`, or `java-library`. 

### Compatibility
Plugin Version | Gradle Version
-------------- | --------------
2.0.x | 6.5-6.7
2.1.x | 6.8

### Configuration
```groovy
bintray {
  // Bintray username. Required.
  user = "user"
  
  // Your Bintray API Key. Required.
  key = "key"
  
  // Name of existing Bintray repository. Required.
  repo = "repo"

  // Group ID of the publication. Required.
  groupId = "com.example"

  // Artifact ID of the publication. Required.
  artifactId = "library"
  
  // Version of the publication. Required
  version = "1.0"

  // Bintray package name. Optional, same as 'artifactId' by default.
  pkgName = "library"

  // VCS URL. Required.
  vcsUrl = "https://github.com/example/example.git"

  // License name. Required.
  license = "Apache-2.0"

  // Organization name. Optional, same as 'user' by default.
  organization = "organization"

  // Include sources artifact to publication, 'true' by default.
  sources = true
  
  // Include javadocs artifact to publication, 'true' by default.
  docs = true 
  
  // Skip publishing to Bintray, 'true' by default.
  dryRun = true

  // Additional settings for documentation
  docsSettings = {

    // When 'kotlin' or 'kotlin-android' plugin is applied 
    // to the project write here Dokka settings, e.g.
    //
    // dokkaSourceSets {
    //   configureEach {
    //     skipEmptyPackages.set(true)
    //     jdkVersion.set(8)
    //     externalDocumentationLink {
    //       url.set(URL("https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/"))
    //     }
    //   }
    // }
    //
    // See complete list of Dokka configuration options at 
    // https://kotlin.github.io/dokka/1.4.10/user_guide/gradle/usage/#configuration-options 
    // 
    // Please note that versions below 2.1.x use Dokka 0.10.x which has different API
    // See https://github.com/Kotlin/dokka/blob/master/runners/gradle-plugin/MIGRATION.md
    
    // When Kotlin is not used in the project
    // write here Javadoc settings, e.g.
    //
    // title "Android Library 1.0"
    // options {
    //   links "https://developer.android.com/reference/"
    // }
    //
    // See complete list of Javadoc configuration options at
    // https://docs.gradle.org/current/dsl/org.gradle.api.tasks.javadoc.Javadoc.html
  }
}
```

### Usage
```sh
./gradlew bintrayUpload
```

You can remove username and API key from the plugin extension and specify them in local.properties like this
```properties
bintray.user=username
bintray.key=key
```

You can also pass these values as gradle properties by configuring the plugin extension like this
```groovy
bintrayUpload {
  user = findProperty('user')
  key = findProperty('key')
  ...
}
```
and running 
```sh
./gradlew -Puser=`<user-name>` -Pkey=`<bintray-api-key>` bintrayUpload
```
