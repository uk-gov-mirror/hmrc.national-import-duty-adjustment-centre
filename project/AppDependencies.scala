import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-27" % "4.1.0"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-27" % "4.1.0"   % Test,
    "org.scalatest"          %% "scalatest"              % "3.2.3"   % Test,
    "com.typesafe.play"      %% "play-test"              % current   % Test,
    "org.scalatestplus"      %% "mockito-3-4"            % "3.2.3.0" % Test,
    "com.vladsch.flexmark"    % "flexmark-all"           % "0.36.8"  % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play"     % "4.0.3"   % "test, it",
    "com.github.tomakehurst"  % "wiremock-jre8"          % "2.27.2"  % "test, it"
  )

}
