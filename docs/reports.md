---
id: reports
title: Generating test reports
---

The MUnit sbt plugin supports collecting historical data about your test reports
and displaying HTML report summaries about your test suites. The test report
data is stored in Google Cloud Storage and the HTML report is rendered with an
[MDoc markdown modifier](https://scalameta.org/mdoc/docs/modifiers.html).

## Example

Below is the generated report from the tests in the MUnit codebase.

```scala mdoc:munit

```

## Installation

> These installation instructions are experimental and subject to change.

First, install the sbt-munit plugin.

```scala
// project/plugins.sbt
addSbtPlugin("org.scalameta" % "sbt-munit" % "@VERSION@")
```

and set the name of the bucket via:

```scala
// build.sbt
ThisBuild / munitBucketName := Some("my-unique-bucket-name"),
```

Next, setup up
[Google Cloud authentication credentials](https://console.cloud.google.com/apis/credentials/serviceaccountkey).
You should have a JSON file that looks like this.

```json
{
  "type": "service_account",
  "project_id": "...",
  "private_key": "---BEGIN PRIVATE KEY---...."
  // ...
}
```

You will need the `Storage Admin` role for the created service account.

Next, add the following two secret environment variables to your CI settings.

- `GOOGLE_APPLICATION_CREDENTIALS_JSON`: the base64 encoded value of the JSON
  file containing the credentials.

```sh
export GCP_CREDENTIALS="/path/to/json/file/on/your/computer"
# macOS
cat $GCP_CREDENTIALS | base64 | pbcopy
# Ubuntu (assuming GNU base64)
cat $GCP_CREDENTIALS | base64 -w0 | xclip
# Arch
cat $GCP_CREDENTIALS | base64 | sed -z 's;\n;;g' | xclip -selection clipboard -i
# FreeBSD (assuming BSD base64)
cat $GCP_CREDENTIALS | base64 | xclip
```

- `GOOGLE_APPLICATION_CREDENTIALS`: an arbitrary relative filename like
  `"gcp.json"`. This should **not** match the path on your personal computer.
  The exact value of this variable doesn't matter as long as the file path is
  relative and the file does not exists in your repository. The MUnit sbt plugin
  will generate this file from the base64 secret variable.

> Verify that your CI provider only exposes secret environment variables to the
> jobs that run on master branch and not pull requests.

If you use GitHub actions, open the following URL for your repository
https://github.com/scalameta/munit/settings/secrets to configure secret
environment variables. Once the secrets are correctly configured, the page
should looks something like this:

![Example screenshot from GitHub Actions secrets page](https://i.imgur.com/Zdvc7cc.png)

Merge some changes into master and verify that the test reports are getting
uploaded to Google Cloud.

```sh
❯ gsutil ls -r 'gs://munit-test-reports/**'
gs://munit-test-reports/scalameta/munit/2020-01-26/refs/heads/master/670f475b49a44a87d515ec48b9749ec196336f78/testsJVM/0.21.0-RC1/1.8.0_232.json
...
```

Next, [setup MDoc](https://scalameta.org/mdoc/docs/installation.html) to
generate documentation. Once you have MDoc setup, enable `MUnitReportPlugin` in
the same project where `MdocPlugin` is enabled.

```diff
  // build.sbt
  lazy val docs = project
    .in(file("myproject-docs"))
-   .enablePlugins(MdocPlugin)
+   .enablePlugins(MdocPlugin, MUnitReportPlugin)
    .settings(...)
```

Next, you should be able to use the `mdoc:munit` modifier to generate test
reports from the stored data in Google Cloud.

````md
```scala mdoc:munit

```
````
