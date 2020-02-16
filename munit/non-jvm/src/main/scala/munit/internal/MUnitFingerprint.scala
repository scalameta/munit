package munit.internal

import munit.internal.junitinterface.CustomFingerprint

class MUnitFingerprint(isModule: Boolean)
    extends CustomFingerprint("munit.Suite", isModule)
