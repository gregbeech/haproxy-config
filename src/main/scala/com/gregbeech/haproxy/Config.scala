package com.gregbeech.haproxy

case class Comment(text: String)

sealed trait Section
case class Global(settings: GlobalSetting*) extends Section
case class Defaults(name: String) extends Section

sealed trait Setting
sealed trait GlobalSetting extends Setting
sealed trait ProxySetting extends Setting
sealed trait DefaultsSetting extends ProxySetting
sealed trait FrontendSetting extends ProxySetting
sealed trait ListenSetting extends ProxySetting
sealed trait BackendSetting extends ProxySetting

case object Daemon extends GlobalSetting
case class MaxConn(number: Int) extends GlobalSetting with BindOption
case class PidFile(pidfile: String) extends GlobalSetting

case class Acl(name: String, criterion: String, flags: Seq[Flag], operator: Option[Operator], values: Seq[String]) extends FrontendSetting with ListenSetting with BackendSetting
case class DefaultBackend(name: String) extends DefaultsSetting with FrontendSetting with ListenSetting
sealed trait Mode extends DefaultsSetting with FrontendSetting with ListenSetting with BackendSetting
case object TcpMode extends Mode
case object HttpMode extends Mode
case object HealthMode extends Mode
case class UseBackend(name: String, condition: Condition) extends FrontendSetting with ListenSetting

// TODO: Could parse the condition here, though possibly not worth it
sealed trait Condition
case class If(condition: String) extends Condition
case class Unless(condition: String) extends Condition

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

// http://cbonte.github.io/haproxy-dconv/configuration-1.5.html#5
sealed trait BindOption
object BindOptions {
  case object AcceptProxy extends BindOption
  case class Apln(protocols: Seq[String]) extends BindOption
  case class Backlog(backlog: Int) extends BindOption
  case class Ecdhe(namedCurve: String) extends BindOption
  case class CaFile(file: String) extends BindOption
  case class CaIgnoreErr(all: Boolean, errorIds: Seq[String]) extends BindOption
  case class Ciphers(ciphers: String) extends BindOption
  case class CrlFile(file: String) extends BindOption
  case class Crt(cert: String) extends BindOption
  case class CrtIgnoreErr(errorIds: Seq[String]) extends BindOption
  case class CrtList(file: String) extends BindOption
  case object DeferAccept extends BindOption
  case object ForceSSLv3 extends BindOption
  case object ForceTLSv10 extends BindOption
  case object ForceTLSv11 extends BindOption
  case object ForceTLSv12 extends BindOption
  case class Gid(gid: Int) extends BindOption
  case class Group(group: String) extends BindOption
  case class Id(id: Int) extends BindOption
  case class Interface(interface: String) extends BindOption
  case class Level(level: BindLevel) extends BindOption
  type MaxConn = com.gregbeech.haproxy.MaxConn
  case class Mode(mode: Int) extends BindOption
  case class Mss(maxseg: Int) extends BindOption
  case class Name(name: String) extends BindOption
  case class Nice(nice: Int) extends BindOption
  case object NoSSLv3 extends BindOption
  case object NoTLSv10 extends BindOption
  case object NoTLSv11 extends BindOption
  case object NoTLSv12 extends BindOption
  case class Npn(protocols: Seq[String]) extends BindOption
  case class Process(restriction: ProcessRestriction) extends BindOption
  case object Ssl extends BindOption
  case object StrictSni extends BindOption
  case object Tfo extends BindOption
  case object Transparent extends BindOption
  case object v4v6 extends BindOption
  case object v6Only extends BindOption
  case class Uid(uid: Int) extends BindOption
  case class User(user: String) extends BindOption
  sealed trait Verify extends BindOption
  case object VerifyNone extends Verify
  case object VerifyOptional extends Verify
  case object VerifyRequired extends Verify
}

sealed trait BindLevel
object BindLevels {
  case object User extends BindLevel
  case object Operator extends BindLevel
  case object Admin extends BindLevel
}

sealed trait ProcessRestriction
object ProcessRestrictions {
  case object All extends ProcessRestriction
  case object Odd extends ProcessRestriction
  case object Even extends ProcessRestriction
  case class Range(from: Int, to: Int) extends ProcessRestriction
}

sealed trait HAOption extends ProxySetting
case class ForceClose(enabled: Boolean) extends HAOption with DefaultsSetting with FrontendSetting with ListenSetting with BackendSetting
case class HttpClose(enabled: Boolean) extends HAOption with DefaultsSetting with FrontendSetting with ListenSetting with BackendSetting
case class HttpPretendKeepAlive(enabled: Boolean) extends HAOption with DefaultsSetting with FrontendSetting with ListenSetting with BackendSetting
