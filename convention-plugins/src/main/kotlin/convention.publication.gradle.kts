import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`
import org.gradle.kotlin.dsl.signing
import java.util.*

plugins {
    `maven-publish`
    signing
}

ext["signing.keyId"] = null
ext["signing.password"] = null
ext["signing.secretKeyRingFile"] = null
ext["ossrhPassword"] = null
ext["ossrhPassword"] = null

val publishGroupId: String by project
val publishArtifactId: String by project
val pomName: String by project
val pomDescription: String by project
val siteUrl: String by project
val gitUrl: String by project
val licenseName: String by project
val licenseUrl: String by project
val developerId: String by project
val developerName: String by project
val developerEmail: String by project
val properties = rootProject.file("local.properties").reader().use {
    Properties().apply {
        load(it)
    }
}.onEach { (name, value) ->
    ext[name.toString()] = value
}

afterEvaluate {
    publishing {
        repositories {
            maven {
                name = "sonatype"
                setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = properties["ossrhUsername"].toString()
                    password = properties["ossrhPassword"].toString()
                }
            }
        }
        publications {
            register<MavenPublication>("release") {
                groupId = publishGroupId
                artifactId = publishArtifactId
                version = "1.0.0"
                from(components.getByName("release"))

                pom {
                    name.set(pomName)
                    description.set(pomDescription)
                    url.set(siteUrl)

                    licenses {
                        license {
                            name.set(licenseName)
                            url.set(licenseUrl)
                        }
                    }
                    developers {
                        developer {
                            id.set(developerId)
                            name.set(developerName)
                            email.set(developerEmail)
                        }
                    }
                    scm {
                        url.set(gitUrl)
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications)
}
