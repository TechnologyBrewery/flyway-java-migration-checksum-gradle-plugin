# flyway-java-migration-checksum-gradle-plugin #
[![License](https://img.shields.io/github/license/mashape/apistatus.svg)](https://opensource.org/licenses/mit)

Inspired by the following Maven [plugin](https://github.com/agileek/java-checksum-flyway-maven-plugin), this project
provides a Gradle plugin that helps developers more easily use Java-based Flyway [migrations](https://flywaydb.org/documentation/migrations#java-based-migrations)
by providing a baseline approach to checksum calculation.  Specifically, this plugin utilizes the same checksum calculation approach
as implemented for Flyway's SQL-based migrations and applies them to specified Java source files.  An `Enum` containing these
checksums is generated, which may be easily used in Java-based migration implementations.

## Usage ##

Apply the plugin in your buildscript:
```
plugins {
  id 'org.bitbucket.cpointe.flyway-java-migration-checksum-gradle-plugin' version '2.0.0'
}
```
Configure the plugin to specify the desired source files, which should be Java-based Flyway migrations, for which to
calculate checksums, and the name/location of the generated `Enum` class:
```
flywayMigrationChecksum {
  // *REQUIRED* - location at which Java source files marked for checksum calculation may be found
  source file('src/main/java/org/technologybrewery/db/migration')

  // Optional - base location at which the generated Enum file containing migration checksums will be created
  // Default value - build/generated/migration-checksum
  destination = file("$project.buildDir/generated/src/main/java")

  // Optional - fully qualified class name of the generated Enum
  // Default value - db.migration.JavaMigrationChecksum
  checksumEnumClassName = 'org.technologybrewery.db.JavaMigrationChecksumEnum'
}
```
If more granularity is desired for selecting the input source files for which to calculate
checksums, the `source` attribute may be configured in a similar manner as Gradle's [SourceTask](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/SourceTask.html)
through Ant-style includes and excludes patterns:
```
flywayMigrationChecksum {
  source 'src/special-src-dir'
  include '**/*.java'
  exclude '**/*NotFlywayMigration.java'
}
```
The plugin automatically adds the `generateJavaMigrationChecksum` to the [compileJava](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.compile.JavaCompile.html)
task and adds the configured `destination` to the appropriate `java` [SourceSet](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.SourceSet.html).
Developers may wire checksums contained in the generated `Enum` into their migrations:
```
package org.technologybrewery.db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.technologybrewery.db.JavaMigrationChecksumEnum;

public class R__create_complex_business_objects extends BaseJavaMigration {
  @Override
  public Integer getChecksum() {
    return JavaMigrationChecksumEnum.valueOf(R__create_complex_business_objects.class.getSimpleName()).getChecksum();
  }
}
```
## Releasing ##

```
$ ./gradlew publishPlugins
```

## Licensing ##
The `flyway-java-migration-checksum-gradle-plugin` Gradle plugin is available under the [MIT License](http://opensource.org/licenses/mit-license.php).
