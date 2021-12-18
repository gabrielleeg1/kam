/*
 *    Copyright 2021 Knoten
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

import java.lang.System.getenv

plugins {
  kotlin("multiplatform") version "1.6.10"
  id("io.gitlab.arturbosch.detekt") version "1.19.0"
  `maven-publish`
}

group = "co.knoten.kam"
version = getenv("GITHUB_REF")?.split("/")?.last() ?: "dev"

repositories {
  mavenCentral()
}

allprojects {
  apply(plugin = "maven-publish")
  
  getenv("GITHUB_REPOSITORY")?.let { repository ->
    publishing {
      repositories {
        maven {
          name = "github"
          url = uri("https://maven.pkg.github.com/$repository")
          credentials {
            username = getenv("GITHUB_USERNAME") ?: error("Unable to find GITHUB_USERNAME")
            password = getenv("GITHUB_TOKEN") ?: error("Unable to find GITHUB_TOKEN")
          }
        }
      }
    }
  }
}

subprojects {
  apply(plugin = "org.jetbrains.kotlin.multiplatform")

  group = "co.knoten.kam"
  version = rootProject.version

  repositories {
    mavenCentral()
  }

  kotlin {
    jvm {
      withJava()

      compilations.all {
        kotlinOptions.jvmTarget = "16"
      }

      testRuns["test"].executionTask.configure {
        useJUnitPlatform()
      }
    }

    sourceSets {
      all {
        languageSettings {
          optIn("kotlin.RequiresOptIn")
        }
      }

      val jvmMain by getting {
        dependencies {
          afterEvaluate {
            implementation(libs.arrow.core)
          }
        }
      }
    }
  }
}

kotlin {
  jvm()
}

detekt {
  buildUponDefaultConfig = true
  allRules = false

  config = files("$projectDir/config/detekt.yml")
  baseline = file("$projectDir/config/baseline.xml")
}
