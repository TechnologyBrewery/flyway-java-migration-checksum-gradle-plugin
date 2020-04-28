package org.bitbucket.cpointe;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

/**
 * Creates a {@link GenerateJavaMigrationChecksumTask}, which generates an enum containing the checksums of the
 * specified set of Java-based Flyway migration classes, and appropriately injects this task into the {@link
 * JavaPlugin}'s task execution graph.  See {@link FlywayJavaMigrationChecksumPluginExtension} for configuration
 * options.
 */
public class FlywayJavaMigrationChecksumPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(JavaPlugin.class);

		FlywayJavaMigrationChecksumPluginExtension extension = project.getExtensions().create("flywayMigrationChecksum",
				FlywayJavaMigrationChecksumPluginExtension.class, project);
		GenerateJavaMigrationChecksumTask generateChecksumTask = project.getTasks()
				.create("generateJavaMigrationChecksum", GenerateJavaMigrationChecksumTask.class);
		project.getPlugins().withType(JavaPlugin.class, new Action<JavaPlugin>() {
			@Override
			public void execute(JavaPlugin plugin) {
				// Adds the generated checksum enum destination directory to the list of Java source directories
				// such that it can be seamlessly referenced by any code in the module utilizing this plugin
				SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
				sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).getJava().srcDir(extension.getDestination());
			}
		});

		// Adds {@link GenerateJavaMigrationChecksumTask} as a dependency for the "compileJava" such that the checksum
		// enum is always generated prior to compilation
		project.getTasks().named(JavaPlugin.COMPILE_JAVA_TASK_NAME)
				.configure(task -> task.dependsOn(generateChecksumTask));
		generateChecksumTask.setExtension(extension);
	}

}
