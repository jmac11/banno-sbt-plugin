package com.banno

import com.github.tkawachi.doctest.DoctestPlugin
import com.github.tkawachi.doctest.DoctestPlugin._


object Doctest {
  val settings =
    DoctestPlugin.doctestSettings ++
    Seq(doctestTestFramework := DoctestTestFramework.Specs2, doctestWithDependencies := false)
}
