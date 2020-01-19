#!/usr/bin/env bash
set -eux

version=$1
argumentsRest=${@:2}
suffix=${argumentsRest:-}

coursier fetch \
  org.scalameta:munit_2.11:$version \
  org.scalameta:munit_2.12:$version \
  org.scalameta:munit_2.13:$version \
  org.scalameta:munit_0.21:$version \
  org.scalameta:munit_sjs0.6_2.11:$version \
  org.scalameta:munit_sjs0.6_2.12:$version \
  org.scalameta:munit_sjs0.6_2.13:$version \
  org.scalameta:munit_sjs1.0-RC1_2.11:$version \
  org.scalameta:munit_sjs1.0-RC1_2.12:$version \
  org.scalameta:munit_sjs1.0-RC1_2.13:$version $suffix
