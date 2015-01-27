package com.gregbeech.haproxy

case class Comment(text: String)

sealed trait Section
case object Global extends Section
case class Defaults(name: String) extends Section

sealed trait Setting
sealed trait GlobalSetting extends Setting
sealed trait ProxySetting extends Setting
sealed trait DefaultsSetting extends ProxySetting
sealed trait FrontendSetting extends ProxySetting
sealed trait ListenSetting extends ProxySetting
sealed trait BackendSetting extends ProxySetting

case object Daemon extends GlobalSetting
case class MaxConn(number: Int) extends GlobalSetting
case class PidFile(pidfile: String) extends GlobalSetting

case class Acl(name: String, criterion: String, flags: Seq[Flag], operator: Option[Operator], values: Seq[String]) extends FrontendSetting with ListenSetting with BackendSetting
sealed trait Mode extends DefaultsSetting with FrontendSetting with ListenSetting with BackendSetting
case object TcpMode extends Mode
case object HttpMode extends Mode
case object HealthMode extends Mode

sealed trait Flag
case class Flag0(key: Char) extends Flag
case class Flag1(key: Char, value: String) extends Flag
object Flag {
  def apply(key: Char): Flag = Flag0(key)
  def apply(key: Char, value: String): Flag = Flag1(key, value)
}

sealed trait Operator
object Operators {
  case object Eq extends Operator
  case object Ne extends Operator
  case object Le extends Operator
  case object Ge extends Operator
  case object Lt extends Operator
  case object Gt extends Operator
}

sealed trait Bind extends FrontendSetting with ListenSetting
case class BindEndpoint(endpoints: Seq[Endpoint], params: Seq[BindOption]) extends Bind
case class BindPath(paths: Seq[String], params: Seq[BindOption]) extends Bind
case class Endpoint(prefix: Option[Prefix], address: Option[String], portRange: Option[PortRange])
case class PortRange(from: Int, to: Int)
object Port {
  def apply(port: Int): PortRange = PortRange(port, port)
}
sealed trait Prefix
object Prefixes {
  case object IPv4 extends Prefix
  case object IPv6 extends Prefix
  case object Unix extends Prefix
  case object Abns extends Prefix
  case object Fd extends Prefix
}

sealed trait BindOption
// TODO: Bind options, see http://cbonte.github.io/haproxy-dconv/configuration-1.5.html#5

sealed trait HAOption extends ProxySetting
case class ForceClose(enabled: Boolean) extends HAOption with DefaultsSetting with FrontendSetting with ListenSetting with BackendSetting
case class HttpClose(enabled: Boolean) extends HAOption with DefaultsSetting with FrontendSetting with ListenSetting with BackendSetting
case class HttpPretendKeepAlive(enabled: Boolean) extends HAOption with DefaultsSetting with FrontendSetting with ListenSetting with BackendSetting
