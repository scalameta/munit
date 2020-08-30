# Contributing

## Importing the build into IntelliJ

This codebase is primarily developed with VS Code but it's possible to work on
this codebase with IntelliJ. There are a few quirks when importing the build
into IntelliJ since MUnit is cross-compiled against multiple platforms (JVM,
JavaScript and Native).

- Use "Open or import" and select the MUnit directory.
- If prompted to select import via sbt or bsp, select sbt.

  ![Screenshot 2020-08-30 at 08 17 39](https://user-images.githubusercontent.com/1408093/91652688-dacd3980-ea99-11ea-949e-8d9a09ea566e.png)

- If you get a "No scalac found to compile scala sources" error, you may need to
  manually add Scala SDK 2.13.3 to the `junit` module.

  ![Screenshot 2020-08-30 at 08 18 56](https://user-images.githubusercontent.com/1408093/91652687-dacd3980-ea99-11ea-9bd3-18eb60113023.png)
  ![Screenshot 2020-08-30 at 08 20 26](https://user-images.githubusercontent.com/1408093/91652686-da34a300-ea99-11ea-86bb-21bce663766a.png)

- If you get a "Class not found" error when running tests, you may need to
  manually edit the run configuration to use the `testsJVM` module instead of
  `tests-sources`.

  ![Screenshot 2020-08-30 at 08 20 51](https://user-images.githubusercontent.com/1408093/91652685-d99c0c80-ea99-11ea-9b85-f8067f9b9dec.png)
  ![Screenshot 2020-08-30 at 08 21 06](https://user-images.githubusercontent.com/1408093/91652684-d99c0c80-ea99-11ea-8ac8-8b1279cdf0da.png)

- There may be highlighting errors for `PlatformCompat` and `BuildInfo`. You can
  ignore these highlighting errors, the project should still compile
  successfully.

If everything is setup correctly, you should be able to run tests and debug via
IntelliJ like normal:

![Screenshot 2020-08-30 at 08 21 21](https://user-images.githubusercontent.com/1408093/91652682-d6a11c00-ea99-11ea-8792-19eaa377bc9e.png)

Please ask on [Gitter](https://gitter.im/scalameta/munit) if you have any issues
working on the MUnit codebase via IntelliJ.
