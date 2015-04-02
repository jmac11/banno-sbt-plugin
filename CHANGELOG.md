# 6 (unreleased)

ADDITIONS:

- Adding a couple [more compiler flags](https://github.com/Banno/banno-sbt-plugin/commit/4f6f9fb9aa2f293a21a819c08c5a8e0296fa8732)

VERSION UPGRADES:

- [Spray](http://spray.io) deps upgraded from `1.k.1` to `1.k.3`. ([Their changelog](http://spray.io/project-info/changelog/))
- Bumping scala to `2.10.5`
- Bump samza-mesos to `0.21.1-1`
- Bump Kafka to `0.8.2.1`

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
