# Contributing

## Submitting a PR

Please run `preparePR` before creating a PR.

## Importing the build into IntelliJ

This codebase is primarily developed with VS Code but it's possible to work on
this codebase with IntelliJ. There are a few quirks when importing the build
into IntelliJ since MUnit is cross-compiled against multiple platforms (JVM,
JavaScript and Native).

sbt builds each project of this build once per Scala version and platform.
Several of those rows use the same source directories. Two system properties
control which rows an IDE imports: the build sets `bspEnabled := false` on the
other rows, and sbt then leaves them out of the BSP workspace.

- `-Dide.scala=X` — sbt keeps only the rows for Scala version `X`.
  - matches full or binary version
  - if unspecified or empty: keep all scala versions
    - IntelliJ only: will be forced to `2.13`; see below why IntelliJ can't
      load multiple versions.
- `-Dide.platform=Y` — sbt keeps only the rows for the platforms in `Y`, a
  comma-separated list.
  - matches `jvm`, `js`, or `native`
  - if unspecified: keep the JVM alone
  - `-Dide.platform=`, with nothing after it, keeps every platform
    - IntelliJ only: will be forced to `jvm`, which is what it imported before
      these properties existed.

IntelliJ cannot import the whole matrix. It puts the sources that several rows
use into one module, and then compiles the Scala 2 and the Scala 3 sources of a
project together. It starts sbt with `-Didea.managed=true`. If you do not set
`-Dide.scala`, that property selects 2.13. To choose another version, add
`-Dide.scala=X` under `Settings -> Build, Execution, Deployment -> Build Tools
-> sbt -> VM parameters`, then reload the sbt project.

These properties change what an IDE imports over BSP. A command-line `sbt`
is not a BSP client, so it still sees every row and builds and tests them
all.

An sbt server runs with the system properties from its own command line. A
later `sbt` in the same directory attaches to that server, so a property you
pass then changes nothing. Run `sbt shutdown` before you test a change to
these properties.

- Use "Open or import" and select the MUnit directory.
- If prompted to select import via sbt or bsp, select sbt.

  ![Screenshot 2020-08-30 at 08 17 39](https://user-images.githubusercontent.com/1408093/91652688-dacd3980-ea99-11ea-949e-8d9a09ea566e.png)

- If you get a "No scalac found to compile scala sources" error, you may need to
  add the Scala 2.13 SDK to the `junit` module by hand.

- The test modules are one per row: `tests2_13` and `tests2_12`. A run
  configuration saved before the build moved to `projectMatrix` names a module
  that no longer exists.

- There may be highlighting errors for `PlatformCompat` and `BuildInfo`. You can
  ignore these highlighting errors, the project should still compile
  successfully.

If everything is setup correctly, you should be able to run tests and debug via
IntelliJ like normal:

![Screenshot 2020-08-30 at 08 21 21](https://user-images.githubusercontent.com/1408093/91652682-d6a11c00-ea99-11ea-8792-19eaa377bc9e.png)

Please ask on [Gitter](https://gitter.im/scalameta/munit) if you have any issues
working on the MUnit codebase via IntelliJ.
