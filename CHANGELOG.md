# 6 (unreleased)

ADDITIONS:

- Adding a couple [more compiler flags](https://github.com/Banno/banno-sbt-plugin/commit/4f6f9fb9aa2f293a21a819c08c5a8e0296fa8732)
- Added the ability for [gulp](http://gulpjs.com/) to be supported for client side resources. [#88](https://github.com/Banno/banno-sbt-plugin/pull/88)
- Added support for [big](https://github.com/banno/big) to be started via the plugin on tests. [#89](https://github.com/Banno/banno-sbt-plugin/pull/89)

VERSION UPGRADES:

- [Spray](http://spray.io) deps upgraded from `1.k.1` to `1.k.3`. ([Their changelog](http://spray.io/project-info/changelog/))
- Bumping scala to `2.10.5`
- Bump samza-mesos to `0.21.1-1`
- Bump Kafka to `0.8.2.1`
- Bumped sbt-docker to `1.1.0` [Commit](https://github.com/Banno/banno-sbt-plugin/commit/301d4660550f6d0ce7f935094679609024a5b625)
- Bumped the codahale metrics to fix a [reconnection issue](https://github.com/Banno/banno-sbt-plugin/pull/91)

REMOVALS:

- Removing `buildTime` from `BuildInfo` object. It was causing stack overflows with Play's auto compilation.

# 5

IMPROVEMENTS:

- Making the plugin an AutoPlugin
- Improvements to customizing docker builds

ADDITIONS:

- Adding a timestamp for when the build is compiled
  - This goes under the `com.banno.BuildInfo` object -- https://github.com/Banno/banno-sbt-plugin/commit/b0424e3bc22a11936bf9e532f8b55439de7955eb

VERSION UPGRADES:

- Adding Kafka and Samza settings (Kafka was bumped to 0.8.2)
- Bumping Metrics version to "3.1.0" -- https://github.com/dropwizard/metrics/releases/tag/v3.1.0
