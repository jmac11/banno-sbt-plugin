import com.banno._

name := "sbt-docker-test"

BannoSettings.settings

Docker.settings

Docker.additionalRunCommands := Seq("echo", "hi")

Docker.entrypointArguments := Seq("arg1")
