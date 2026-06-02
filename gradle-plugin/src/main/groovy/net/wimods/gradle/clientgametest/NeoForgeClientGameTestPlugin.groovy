package net.wimods.gradle.clientgametest

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.jvm.tasks.Jar

class NeoForgeClientGameTestPlugin implements Plugin<Project> {
	static final String SOURCE_SET = 'gametest'
	static final String API_GROUP = 'net.wimods'
	static final String API_ARTIFACT = 'neoforge-client-gametest-api'

	@Override
	void apply(Project project) {
		project.pluginManager.apply('java')

		def sourceSets = project.extensions.getByName('sourceSets')
		SourceSet main = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
		SourceSet gametest = sourceSets.maybeCreate(SOURCE_SET)

		gametest.compileClasspath += main.output + main.compileClasspath
		gametest.runtimeClasspath += main.output + main.runtimeClasspath

		project.dependencies.add(gametest.implementationConfigurationName,
			"$API_GROUP:$API_ARTIFACT:${apiVersion()}")

		def gametestJar = project.tasks.register('clientGameTestJar', Jar) {
			from gametest.output
			archiveBaseName = "${project.name}-client-gametest"
			destinationDirectory = project.layout.buildDirectory.dir('devlibs')
		}

		project.pluginManager.withPlugin('net.neoforged.gradle.userdev') {
			def run = project.extensions.getByName('runs').maybeCreate('clientGameTest')
			run.runType('client')
			run.modSource(main)
			run.runtimeClasspath(gametestJar)
			run.runtimeClasspath(project.configurations.getByName(gametest.runtimeClasspathConfigurationName))
			run.systemProperty('fabric.client.gametest', 'true')
			run.workingDirectory(project.layout.buildDirectory.dir('run/clientGameTest'))

			project.tasks.matching { it.name == 'runClientGameTest' }.configureEach {
				dependsOn gametestJar
			}
		}
	}

	private static String apiVersion() {
		String version = NeoForgeClientGameTestPlugin.package.implementationVersion

		if (!version) {
			throw new GradleException('Missing plugin Implementation-Version; cannot choose matching client gametest API version.')
		}

		return version
	}
}
