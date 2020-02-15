trackable-compiler-plugin
========================

It's an under development plugin. more features would be added but currently only the followings are supported.
Overall, a Kotlin compiler plugin that generates trackable `val track : String` implementations for classes that
annotated by `@Trackable`. It's useful when your classes are obfuscated in the product and you need their names
to log an event.

Inspired by the [`redacted-compiler-plugin`](https://github.com/ZacSweers/redacted-compiler-plugin) extension.

## Usage

Include the gradle plugin in your project, define a `@Trackable` annotation, and apply it to any 
classes that you wish to track.

```kotlin
@Retention(BINARY)
@Target(CLASS)
annotation class Trackable

@Trackable
class TrackableClass
```

When you call `TrackableClass().track` you must receive the name of the class, which is `TrackableClass`. Currently
this is just achieved by reflection! Direct call to this property would throw `Unresolved reference: track`!

## Installation

Not published yet but it should be by applying the gradle plugin as follows.

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

  // Define whether or not this is enabled. Useful if you want to gate this behind a dynamic
  // build configuration.
  enabled = true // Default

  // Define the name of generated method.
  propertyName = "track" // Default
}
```

## Caveats

- Kotlin compiler plugins are not a stable API! Compiled outputs from this plugin _should_ be stable,
but usage in newer versions of kotlinc are not guaranteed to be stable.


## Contribution
Just create your branch from the master branch, change it, write additional tests, satisfy all 
tests, create your pull request, thank you, you're awesome.
