#!/usr/bin/env bash
set -eux

version=$1
argumentsRest=${@:2}
suffix=${argumentsRest:-}

coursier resolve \
  org.scalameta:munit_2.12:$version \
  org.scalameta:munit_2.13:$version \
  org.scalameta:munit_3.0.0:$version \
  org.scalameta:munit_native0.4_2.12:$version \
  org.scalameta:munit_native0.4_2.13:$version \
  org.scalameta:munit_sjs1_2.12:$version \
  org.scalameta:munit_sjs1_2.13:$version \
  org.scalameta:munit-scalacheck_2.12:$version \
  org.scalameta:munit-scalacheck_2.13:$version \
  org.scalameta:munit-scalacheck_3.0.0:$version \
  org.scalameta:munit-scalacheck_native0.4_2.12:$version \
  org.scalameta:munit-scalacheck_native0.4_2.13:$version \
  org.scalameta:munit-scalacheck_sjs1_2.12:$version \
  org.scalameta:munit-scalacheck_sjs1_2.13:$version \
  org.scalameta:munit-docs_2.12:$version \
  org.scalameta:munit-docs_2.13:$version $suffix

coursier resolve \
    "org.scalameta:sbt-munit;sbtVersion=1.0;scalaVersion=2.12:$version" \
    --sbt-plugin-hack $suffix
