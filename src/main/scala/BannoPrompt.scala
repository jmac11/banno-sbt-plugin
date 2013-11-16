package com.banno
import sbt._
import Keys._

object BannoPrompt {

  val shellPromptDisplayProjectId = SettingKey[Boolean]("shell-prompt-display-project-id")

  val settings =
    Seq(
      shellPromptDisplayProjectId := false,

      shellPrompt <<= (name, shellPromptDisplayProjectId) { (name, displayProjectId) => (state: State) =>
        lazy val projectId = Project.extract(state).currentProject.id
        val prompt = if (displayProjectId) name + "::" + projectId else name
        prompt + " > "
      }
    )
}
