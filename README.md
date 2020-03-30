trackable-compiler-plugin
========================

A Kotlin compiler plugin that generates trackable method `fun track() : String` implementations for classes that
annotated by `@Trackable`. This method would return the class name String. Also for classes that their parent
annotated with `@Trackable`, this method would be generated with current class name. However, if you want to override
the return String you can use `@Trackable(trackWith = "Something else!")` to change the return value. It's useful when
your classes are obfuscated in the product and you need their names to log an event.

## Usage

Include the gradle plugin in your project, apply `@Trackable` annotation to any classes that you wish to track or
on their parent class or interfaces.

```kotlin
@Retention(BINARY)
@Target(CLASS)
annotation class Trackable(val trackWith: String = "")

@Trackable
class TrackableClass
```

When you call `TrackableClass().track()` you must receive the name of the class, which is `TrackableClass`, Or
if you want to change that string use `trackWith` params as below. 

```kotlin
@Retention(BINARY)
@Target(CLASS)
annotation class Trackable(val trackWith: String = "")

@Trackable(trackWith = "Something else!")
class TrackableClass
```

So the `track` method would return `Something else!`.

## Installation

Not published yet but it should be by applying the gradle plugin as follows in the future.

```gradle
buildscript {
  dependencies {
    classpath "com.github.hadilq.trackable:trackable-compiler-plugin-gradle:x.y.z"
  }  
}

apply plugin: 'com.github.hadilq.trackable.trackable-gradle-plugin'
```

And that's it! The default configuration will add the `-annotations` artifact (which has a
`@Trackable` annotation you can use) and wire it all automatically. Just annotate what you want to
your trackable class.

You can configure custom behavior with properties on the `trackable` extension.

```
trackable {
  // Define a custom annotation. The -annotations artifact won't be automatically added to
  // dependencies if you define your own!
  trackableAnnotation = "com.github.hadilq.trackable.annotations.Trackable" // Default

  // In case of a custom annotation above you can change the name of `trackWith` param by this variable.
  trackWith = "trackWith" // Default

  // Define whether or not this is enabled. Useful if you want to gate this behind a dynamic
  // build configuration.
  enabled = true // Default

  // Define the name of generated method.
  getterName = "track" // Default
}
```

## Caveats

- Kotlin compiler plugins are not a stable API! Compiled outputs from this plugin _should_ be stable,
but usage in newer versions of kotlinc are not guaranteed to be stable.


## Contribution
Just create your branch from the master branch, change it, write additional tests, satisfy all 
tests, create your pull request, thank you, you're awesome.
