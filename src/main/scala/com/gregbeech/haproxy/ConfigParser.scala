package com.gregbeech.haproxy

import com.gregbeech.haproxy.Operators._
import com.gregbeech.haproxy.Prefixes._
import org.parboiled2._

import scala.collection.immutable.Seq

class ConfigParser(val input: ParserInput) extends Parser {
  val CharsToEscape = " #\\"

  ////////////////////////// intrinsics //////////////////////////

  // TODO: At the moment whitespace is being handled specifically; can we fold ~ sp into keyword matches?
  def sp = rule { oneOrMore(' ') }
  def ws = rule { zeroOrMore(anyOf(" \t")) }
  def eol = rule { ws ~ optional('\r') ~ '\n' }

  def number = rule { capture(oneOrMore(CharPredicate.Digit)) ~> (_.toInt) }

  // TODO: These are a little iffy
  def char = rule { noneOf(CharsToEscape + "\r\n\t") ~ push(lastChar) }
  def escapedChar = rule { '\\' ~ anyOf(CharsToEscape) ~ push(lastChar) }
  def string = rule { oneOrMore(char | escapedChar) ~> (_.mkString) }

  def name = rule { capture(oneOrMore(CharPredicate.AlphaNum ++ CharPredicate("-_.:"))) }
  
  def comment = rule { '#' ~ ws ~ capture(zeroOrMore(CharPredicate.Printable)) ~> Comment }

  ////////////////////////// sections //////////////////////////

  private def globalSetting = rule { (daemon | maxconn | pidfile) /* ~ ws ~ optional(comment) ~> ((s, c) => (s, c)) */ }
  def global = rule { "global" ~ /* optional(comment) ~ */ eol ~ zeroOrMore(ws ~ globalSetting).separatedBy(eol) ~> (Global(_: _*)) }
  def defaults = rule { "defaults" ~ sp ~ name ~> Defaults }
  def section = rule { (global | defaults) }

  ////////////////////////// global settings //////////////////////////

  def daemon = rule { "daemon" ~ push(Daemon) }
  def maxconn = rule { "maxconn" ~ sp ~ number ~> MaxConn }
  def pidfile = rule { "pidfile" ~ sp ~ string ~> PidFile }

  private def criterion = string
  private def flag0 = rule { '-' ~ anyOf("in-") ~ push(Flag(lastChar)) }
  private def flag1 = rule { '-' ~ capture(anyOf("fmMu")) ~ sp ~ string ~> ((k, v) => Flag(k.head, v)) }
  private def flag = rule { flag0 | flag1 }
  private def eq = rule { "eq" ~ push(Eq) }
  private def ne = rule { "ne" ~ push(Ne) }
  private def le = rule { "le" ~ push(Le) }
  private def ge = rule { "ge" ~ push(Ge) }
  private def lt = rule { "lt" ~ push(Lt) }
  private def gt = rule { "gt" ~ push(Gt) }
  private def operator = rule { eq | ne | le | ge | lt | gt }
  def acl = rule { "acl" ~ sp ~ name ~ sp ~ criterion ~ zeroOrMore(sp ~ flag) ~ optional(sp ~ operator) ~ sp ~ oneOrMore(string).separatedBy(sp) ~> Acl }

  private def tcpMode = rule { "mode tcp" ~ push(TcpMode) }
  private def httpMode = rule { "mode http" ~ push(HttpMode) }
  private def healthMode = rule { "mode health" ~ push(HealthMode) }
  def mode = rule { tcpMode | httpMode | healthMode }

  ////////////////////////// proxy settings //////////////////////////

  private def ipv4 = rule { "ipv4@" ~ push(IPv4) }
  private def ipv6 = rule { "ipv6@" ~ push(IPv6) }
  private def unix = rule { "unix@" ~ push(Unix) }
  private def abns = rule { "abns@" ~ push(Abns) }
  private def fd = rule { "fd@" ~ push(Fd) }
  private def prefix = rule { ipv4 | ipv6 | unix | abns | fd }
  private def addressPart = rule { zeroOrMore(noneOf(":, ")) }
  private def addressPartSeparator = rule { ':' ~ &(addressPart ~ ':') }
  private def address = rule { capture(oneOrMore(addressPart).separatedBy(addressPartSeparator)) }
  private def portRange = rule { number ~ optional('-' ~ number) ~> ((f, t) => PortRange(f, t.getOrElse(f))) }
  private def endpoint = rule { optional(prefix) ~ optional(address) ~ optional(':' ~ portRange) ~> (Endpoint(_, _, _)) }
  private def bindEndpoint = rule { oneOrMore(endpoint).separatedBy(',') ~> (eps => BindEndpoint(eps, Seq())) } // TODO: Params
  private def path = rule { capture("/" ~ zeroOrMore(noneOf(", "))) }
  private def bindPath = rule { oneOrMore(path).separatedBy(',') ~> (ps => BindPath(ps, Seq())) } // TODO: Params
  def bind = rule { "bind" ~ sp ~ (bindPath | bindEndpoint) }

  def defaultBackend = rule { "default_backend" ~ sp ~ name ~> DefaultBackend }

  private def conditionString = rule { capture(oneOrMore(CharPredicate.Printable)) } // TODO: This is pretty shoddy
  private def ifCondition = rule { "if" ~ sp ~ conditionString ~> If }
  private def unlessCondition = rule { "unless" ~ sp ~ conditionString ~> Unless }
  private def condition = rule { ifCondition | unlessCondition }
  def useBackend = rule { "use_backend" ~ sp ~ name ~ sp ~ condition ~> UseBackend }
  
  private def option = rule { capture(optional("no" ~ sp)) ~ "option" ~ sp ~> (_.isEmpty) }
  def forceClose =  rule { option ~ "forceclose" ~> ForceClose }
  def httpClose =  rule { option ~ "httpclose" ~> HttpClose }
  def httpPretendKeepAlive =  rule { option ~ "http-pretend-keepalive" ~> HttpPretendKeepAlive }
}
