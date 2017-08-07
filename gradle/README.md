# Gradle

## The [project-schema.json](project-schema.json) file

### What is it?

This helps the Gradle Kotlin DSL generate static methods that it uses when it is compiling your build files.

You can read more about [how it works here](https://github.com/gradle/kotlin-dsl/releases/tag/v0.8.0).

### When might I need to update it?

If you are reorganising the project or adding a new plugin you may find that in order to get the static
plugin accessors to appear in your IDE you need to update this file.

### How to update the file

To regenerate the [project-schema.json](project-schema.json) file run this in the project's root directory:
```bash
./gradlew kotlinDslAccessorsSnapshot
```