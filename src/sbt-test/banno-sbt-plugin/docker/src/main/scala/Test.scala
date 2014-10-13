package com.banno

object Test {
  def main(args: Array[String]) = {
    println(s"${BuildInfo.name} - Ok - ${args(0)} - ${args(1)}")
  }
}
