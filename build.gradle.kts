/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "1.3.1"
}

group = "net.civmc"
version = "2.0.0-SNAPSHOT"
description = "ExilePearl"

repositories {
    fun civRepo(name: String) {
    	maven {
    		url = uri("https://maven.pkg.github.com/CivMC/${name}")
    		credentials {
    			// These need to be set in the user environment variables
    			username = System.getenv("GITHUB_ACTOR")
    			password = System.getenv("GITHUB_TOKEN")
    		}
    	}
    }
    mavenCentral()
    civRepo("CivModCore")
    civRepo("NameLayer")
    civRepo("Citadel")
    civRepo("CivChat2")
    civRepo("Bastion")
    civRepo("RandomSpawn")
    civRepo("WorldBorder")
    civRepo("CombatTagPlus")
    civRepo("BanStick")

    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.maven.apache.org/maven2/")
}

dependencies {
    paperDevBundle("1.18-R0.1-SNAPSHOT")
    compileOnly("net.civmc:civmodcore:2.0.0-SNAPSHOT:dev-all")
    compileOnly("net.civmc:namelayer-spigot:3.0.0-SNAPSHOT:dev")
    compileOnly("net.civmc:citadel:5.0.0-SNAPSHOT:dev")
    compileOnly("net.civmc:civchat2:2.0.0-SNAPSHOT:dev")
    compileOnly("net.civmc:bastion:3.0.0-SNAPSHOT:dev")
    compileOnly("net.civmc:randomspawn:3.1.0-SNAPSHOT:dev")
    compileOnly("net.civmc:worldborder:2.0.0-SNAPSHOT:dev")
    compileOnly("net.civmc:combattagplus:1.5.0-SNAPSHOT:dev")
    compileOnly("com.github.DieReicheErethons:Brewery:3.1")

    compileOnly("net.civmc:banstick:2.0.0-SNAPSHOT:dev")
}

java {
	toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
	build {
		dependsOn(reobfJar)
	}

	compileJava {
		options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

		// Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
		// See https://openjdk.java.net/jeps/247 for more information.
		options.release.set(17)
	}
	javadoc {
		options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
	}
	processResources {
		filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
	}

	test {
		useJUnitPlatform()
	}
}

publishing {
	repositories {
		maven {
			name = "GitHubPackages"
			url = uri("https://maven.pkg.github.com/CivMC/ExilePearl")
			credentials {
				username = System.getenv("GITHUB_ACTOR")
				password = System.getenv("GITHUB_TOKEN")
			}
		}
	}
	publications {
		register<MavenPublication>("gpr") {
			from(components["java"])
		}
	}
}
