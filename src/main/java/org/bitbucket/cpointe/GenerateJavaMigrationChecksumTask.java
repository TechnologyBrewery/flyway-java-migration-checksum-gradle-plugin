package org.bitbucket.cpointe;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

import javax.inject.Inject;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;

/**
 * Gradle task that uses Velocity to generate an enum class containing checksums of the specified Java-based Flyway
 * migration source files. The implemented checksum calculation approach is the same as that implemented by Flyway for
 * SQL-based migrations.
 */
public class GenerateJavaMigrationChecksumTask extends DefaultTask {

	private FlywayJavaMigrationChecksumPluginExtension extension;

	private VelocityEngine velocityEngine;

	@Inject
	public GenerateJavaMigrationChecksumTask() {
		this.velocityEngine = new VelocityEngine();
		this.velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");
		this.velocityEngine.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
		this.velocityEngine.init();
	}

	@Nested
	public FlywayJavaMigrationChecksumPluginExtension getExtension() {
		return this.extension;
	}

	protected void setExtension(FlywayJavaMigrationChecksumPluginExtension extension) {
		this.extension = extension;
	}

	/**
	 * Generates a new enum class with the configured name and location containing the checksum values of the specified
	 * Java source files.
	 */
	@TaskAction
	public void generateChecksums() {
		List<Pair<Integer, String>> checksumAndSrcFileNamePairs = new ArrayList<Pair<Integer, String>>();

		for (File migrationSourceFile : getExtension().getSource().getFiles()) {
			try (InputStream sourceFileInputStream = new FileInputStream(migrationSourceFile)) {
				int checksum = calculateChecksum(sourceFileInputStream);
				checksumAndSrcFileNamePairs.add(new ImmutablePair<Integer, String>(checksum,
						StringUtils.substringBeforeLast(migrationSourceFile.getName(), ".")));
			} catch (IOException e) {
				throw new RuntimeException(
						String.format("Could not read source file %s", migrationSourceFile.getAbsolutePath()), e);
			}
		}
		String checksumEnumClassName = getExtension().getChecksumEnumClassName();
		if (!StringUtils.contains(checksumEnumClassName, ".") || StringUtils.endsWith(checksumEnumClassName, ".java")) {
			throw new InvalidUserDataException(String.format(
					"Given checksum enum class name of %s is invalid - class "
							+ "name must include a non-default package and not end with a .java file extension",
					checksumEnumClassName));
		}
		Provider<RegularFile> checksumEnumFileProvider = getExtension().getDestination()
				.file(StringUtils.replace(checksumEnumClassName, ".", "/") + ".java");
		File checksumEnumFile = checksumEnumFileProvider.get().getAsFile();
		checksumEnumFile.getParentFile().mkdirs();

		Template template = velocityEngine.getTemplate("java-migration-checksum-enum.vm");
		VelocityContext context = new VelocityContext();
		context.put("package", StringUtils.substringBeforeLast(checksumEnumClassName, "."));
		context.put("className", StringUtils.substringAfterLast(checksumEnumClassName, "."));
		context.put("checksumAndSrcFileNamePairs", checksumAndSrcFileNamePairs);

		try (Writer enumWriter = new FileWriter(checksumEnumFile)) {
			template.merge(context, enumWriter);
		} catch (IOException e) {
			throw new GradleException(String.format("Could not write Java migration checksum Enum at %s",
					checksumEnumFile.getAbsolutePath()), e);
		}
	}

	/**
	 * Generate the checksum of the given {@link Reader} encapsulating the source file of a Flyway Java migration using
	 * the same algorithm as the one used by Flyway for SQL migration checksum calculation. First, strip the BOM from
	 * the start of the file, if it exists, using {@link BOMInputStream}. Next, iterate through each line of the source
	 * code, strip any line breaks, and compute the CRC32 checksum.
	 *
	 * @param migrationSrcFileStream
	 * @return
	 * @throws IOException
	 */
	protected int calculateChecksum(InputStream migrationSrcFileStream) throws IOException {
		CRC32 crc32 = new CRC32();

		try (BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(new BOMInputStream(migrationSrcFileStream)))) {
			bufferedReader.lines().map(line -> {
				return StringUtils.stripEnd(line, "\r\n");
			}).forEach(line -> {
				crc32.update(line.getBytes(StandardCharsets.UTF_8));
			});
		}

		return (int) crc32.getValue();
	}
}
