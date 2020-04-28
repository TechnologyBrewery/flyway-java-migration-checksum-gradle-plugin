package org.bitbucket.cpointe;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.file.*;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;

import java.io.File;

/**
 * Configuration for {@link FlywayJavaMigrationChecksumPlugin} that enables plugin users to specify for which Java
 * source files will checksums be generated, as well as the name and target location of the generated checksum enum
 * class.
 */
public class FlywayJavaMigrationChecksumPluginExtension {

	protected static final String DEFAULT_DESTINATION_DIR = "build/generated/migration-checksum";
	protected static final String DEFAULT_ENUM_CLASS_NAME = "db.migration.JavaMigrationChecksum";

	private DirectoryProperty destination;
	private String checksumEnumClassName;
	private ConfigurableFileCollection migrationSourceFiles;
	private final PatternFilterable patternSet = new PatternSet();

	public FlywayJavaMigrationChecksumPluginExtension(Project project) {
		this.destination = project.getObjects().directoryProperty().fileValue(new File(DEFAULT_DESTINATION_DIR));
		this.checksumEnumClassName = DEFAULT_ENUM_CLASS_NAME;
		this.migrationSourceFiles = project.getObjects().fileCollection();
	}

	@OutputDirectory
	public DirectoryProperty getDestination() {
		return this.destination;
	}

	/**
	 * Sets the destination directory at which the generated enum will be created.
	 *
	 * @param destination
	 */
	public void setDestination(File destination) {
		this.destination.fileValue(destination);
	}

	/**
	 * Sets the destination directory at which the generated enum will be created.
	 *
	 * @param destination
	 */
	public void setDestination(Directory destination) {
		this.destination.value(destination);
	}

	@Input
	public String getChecksumEnumClassName() {
		return this.checksumEnumClassName;
	}

	/**
	 * Sets the fully qualified name of the generated enum class.
	 *
	 * @param checksumEnumClassName fully qualified name for which to use for the generated enum class.
	 */
	public void setChecksumEnumClassName(String checksumEnumClassName) {
		this.checksumEnumClassName = checksumEnumClassName;
	}

	/**
	 * Gets a {@link FileTree} representing the filtered set of migration Java files for which checksums will be
	 * calculated.
	 *
	 * @return
	 */
	@InputFiles
	@SkipWhenEmpty
	@PathSensitive(PathSensitivity.ABSOLUTE)
	public FileTree getSource() {
		return migrationSourceFiles.getAsFileTree().matching(patternSet);
	}

	public void setSource(FileTree source) {
		setSource((Object) source);
	}

	/**
	 * Sets the source migration files, which are evaluated as via {@link org.gradle.api.Project#files(Object...)}.
	 *
	 * @param source The source.
	 */
	public void setSource(Object source) {
		this.migrationSourceFiles.setFrom(source);
	}

	public FlywayJavaMigrationChecksumPluginExtension source(Object... sources) {
		migrationSourceFiles.from(sources);
		return this;
	}

	/**
	 * Adds an ANT style include path, which may be further augmented with other inclusion or exclusion patterns
	 *
	 * @param includes
	 * @return
	 */
	public FlywayJavaMigrationChecksumPluginExtension include(String... includes) {
		patternSet.include(includes);
		return this;
	}

	/**
	 * Adds an ANT style include path, which may be further augmented with other inclusion or exclusion patterns
	 *
	 * @param includes
	 * @return
	 */
	public FlywayJavaMigrationChecksumPluginExtension include(Iterable<String> includes) {
		patternSet.include(includes);
		return this;
	}

	/**
	 * Adds an include spec, which may be further augmented with other include/exclude specs
	 *
	 * @param includeSpec
	 * @return
	 */
	public FlywayJavaMigrationChecksumPluginExtension include(Spec<FileTreeElement> includeSpec) {
		patternSet.include(includeSpec);
		return this;
	}

	/**
	 * Adds an include spec, which may be further augmented with other include/exclude specs
	 *
	 * @param includeSpec
	 * @return
	 */
	public FlywayJavaMigrationChecksumPluginExtension include(Closure includeSpec) {
		patternSet.include(includeSpec);
		return this;
	}

	/**
	 * Adds an ANT style exclude path, which may be further augmented with other inclusion or exclusion patterns
	 *
	 * @param excludes
	 * @return
	 */
	public FlywayJavaMigrationChecksumPluginExtension exclude(String... excludes) {
		patternSet.exclude(excludes);
		return this;
	}

	/**
	 * Adds an ANT style exclude path, which may be further augmented with other inclusion or exclusion patterns
	 *
	 * @param excludes
	 * @return
	 */
	public FlywayJavaMigrationChecksumPluginExtension exclude(Iterable<String> excludes) {
		patternSet.exclude(excludes);
		return this;
	}

	/**
	 * Adds an exclude spec, which may be further augmented with other include/exclude specs
	 *
	 * @param excludeSpec
	 * @return
	 */
	public FlywayJavaMigrationChecksumPluginExtension exclude(Spec<FileTreeElement> excludeSpec) {
		patternSet.exclude(excludeSpec);
		return this;
	}

	/**
	 * Adds an exclude spec, which may be further augmented with other include/exclude specs
	 *
	 * @param excludeSpec
	 * @return
	 */
	public FlywayJavaMigrationChecksumPluginExtension exclude(Closure excludeSpec) {
		patternSet.exclude(excludeSpec);
		return this;
	}
}
