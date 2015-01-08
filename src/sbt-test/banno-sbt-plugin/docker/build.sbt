import com.banno._
import Docker._

name := "sbt-docker-test"

BannoSettings.settings

Docker.settings

appDir in docker := file("/test")

additionalRunCommands in docker := Seq("uname -a ", "whoami")

entryPointPrelude in docker := "TEST_VAR=`echo -n test_env_var`"

command in docker := Seq("testing", "arg1")
