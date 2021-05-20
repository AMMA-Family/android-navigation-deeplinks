buildscript {
    repositories {
        google()
        maven("https://plugins.gradle.org/m2/")
    }

    dependencies {
        classpath(BuildPlugin.gradle)
        classpath(BuildPlugin.kotlin)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
        maven("https://s01.oss.sonatype.org/content/groups/public/")
        /*maven(url = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
            credentials {
                val localProperties = project.localProperties()
                username = localProperties.getProperty("publication.user.login")
                password = localProperties.getProperty("publication.user.password")
            }
        }*/
    }
}
