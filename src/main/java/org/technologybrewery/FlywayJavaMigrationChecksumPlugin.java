package org.technologybrewery;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

/**
 * Creates a {@link GenerateJavaMigrationChecksumTask}, which generates an enum containing the checksums of the
 * specified set of Java-based Flyway migration classes, and appropriately injects this task into the {@link
 * JavaPlugin}'s task execution graph.  See {@link FlywayJavaMigrationChecksumPluginExtension} for configuration
 * options.
 */
public class FlywayJavaMigrationChecksumPlugin implements Plugin<Project> {

	/**
	 * Default base location (relative to the current project's {@code $projectDir}) at which the generated enum
	 * containing migration checksums will be placed by {@link GenerateJavaMigrationChecksumTask}.
	 */
	protected static final String DEFAULT_DESTINATION_DIR = "build/generated/migration-checksum";

	/**
	 * Default fully qualified class name of the generated enum class containing migration checksums.
	 */
	protected static final String DEFAULT_ENUM_CLASS_NAME = "db.migration.JavaMigrationChecksum";

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(JavaPlugin.class);

		FlywayJavaMigrationChecksumPluginExtension extension = project.getExtensions().create("flywayMigrationChecksum",
				FlywayJavaMigrationChecksumPluginExtension.class, project.getObjects());
		Provider<Directory> defaultEnumDestinationDir = project.getObjects().directoryProperty().dir(DEFAULT_DESTINATION_DIR);

		// Set default values on DSL extension properties, which will be propagated to the task's
		// corresponding properties
		extension.getDestination().convention(defaultEnumDestinationDir);
		extension.getChecksumEnumClassName().convention(DEFAULT_ENUM_CLASS_NAME);

		Provider<GenerateJavaMigrationChecksumTask> generateChecksumTask = project.getTasks()
				.register("generateJavaMigrationChecksum", GenerateJavaMigrationChecksumTask.class, task -> {
					// Apply all user-supplied configurations captured via the plugin extension DSL to the corresponding
					// properties within the GenerateJavaMigrationChecksumTask to appropriately map the task's
					// inputs/outputs and allow the task to participate in Gradle's in task avoidance capabilities
					task.getDestination().set(extension.getDestination());
					task.getChecksumEnumClassName().set(extension.getChecksumEnumClassName());
					task.getMigrationSourceFiles().setFrom(extension.getMigrationSourceFiles().getFrom());
					task.setPatternSet(extension.getPatternSet());
				});
		project.getPlugins().withType(JavaPlugin.class, plugin -> {
			// Adds the generated checksum enum destination directory to the list of Java source directories
			// such that it can be seamlessly referenced by any code in the module utilizing this plugin
			SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
			sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).getJava().srcDir(extension.getDestination());
		});

		// Adds {@link GenerateJavaMigrationChecksumTask} as a dependency for the "compileJava" such that the checksum
		// enum is always generated prior to compilation
		project.getTasks().named(JavaPlugin.COMPILE_JAVA_TASK_NAME)
				.configure(task -> task.dependsOn(generateChecksumTask));
	}

}
