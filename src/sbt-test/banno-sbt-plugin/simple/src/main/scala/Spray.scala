package com.banno.spray
import spray.routing.{Directive, Directives}
import spray.routing.directives._
import shapeless.{::, HNil}

object Spray extends Directives {

  // compiles
  def singleParam =
    parameters('thing.as[Boolean])

  // compiles
  def multiParamsFix =
    parameters('thing.as[Boolean]) & parameters('thing2.as[Boolean])

  // fails compile
  def multiParams =
    parameters('thing.as[Boolean], 'thing2.as[Boolean])

  // fails compile
  def multiParams2: Directive[Boolean :: Boolean :: HNil] =
    parameters('thing.as[Boolean] ? true, 'thing2.as[Boolean] ? false)
}
