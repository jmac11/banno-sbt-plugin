import com.banno._
import Docker._

name := "sbt-docker-test"

BannoSettings.settings

Docker.settings

additionalRunCommands in docker := Seq("uname -a ", "whoami")

command in docker := Seq("testing", "arg1")
