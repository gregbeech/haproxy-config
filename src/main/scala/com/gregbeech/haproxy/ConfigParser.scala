package com.gregbeech.haproxy

import com.gregbeech.haproxy.Operators._
import org.parboiled2._

class ConfigParser(val input: ParserInput) extends Parser {
  val CharsToEscape = " #\\"
  
  def inputLine = rule { padding ~ globalSetting ~ EOI }
  
  def sp = rule { oneOrMore(' ') }
  def padding = rule { zeroOrMore(anyOf(" \t")) }
  def number = rule { capture(oneOrMore(CharPredicate.Digit)) ~> (_.toInt) }
  
  def char = rule { noneOf(CharsToEscape) ~ push(lastChar) }
  def escapedChar = rule { '\\' ~ anyOf(CharsToEscape) ~ push(lastChar) }
  def string = rule { oneOrMore(char | escapedChar) ~> (_.mkString) }
  
  def name = rule { capture(oneOrMore(CharPredicate.AlphaNum ++ CharPredicate("-_.:"))) }
  
  def comment = rule { '#' ~ padding ~ capture(zeroOrMore(CharPredicate.Printable)) ~> Comment } // TODO: CharPredicate.All never terminates?
  
  def section = rule { (global | defaults) ~ padding ~ optional(comment)  }
  def global = rule { "global" ~ push(Global) }
  def defaults = rule { "defaults" ~ sp ~ name ~> Defaults }

  def globalSetting = rule { (daemon | maxconn) ~ padding ~ optional(comment) ~> ((s, c) => (s, c)) }
  
  def daemon = rule { "daemon" ~ push(Daemon) }
  def maxconn = rule { "maxconn" ~ sp ~ number ~> MaxConn }
  def pidfile = rule { "pidfile" ~ sp ~ string ~> PidFile }

  def criterion = string
  def flag0 = rule { '-' ~ anyOf("in-") ~ push(Flag(lastChar)) }
  def flag1 = rule { '-' ~ capture(anyOf("fmMu")) ~ sp ~ string ~> ((k, v) => Flag(k.head, v)) }
  def flag = rule { flag0 | flag1 }
  def eq = rule { "eq" ~ push(Eq) }
  def ne = rule { "ne" ~ push(Ne) }
  def le = rule { "le" ~ push(Le) }
  def ge = rule { "ge" ~ push(Ge) }
  def lt = rule { "lt" ~ push(Lt) }
  def gt = rule { "gt" ~ push(Gt) }
  def operator = rule { eq | ne | le | ge | lt | gt }
  def acl = rule { "acl" ~ sp ~ name ~ sp ~ criterion ~ zeroOrMore(sp ~ flag) ~ optional(sp ~ operator) ~ sp ~ string ~> Acl }
  
  def tcpMode = rule { "mode tcp" ~ push(TcpMode) }
  def httpMode = rule { "mode http" ~ push(HttpMode) }
  def healthMode = rule { "mode health" ~ push(HealthMode) }
  def mode = rule { tcpMode | httpMode | healthMode }
  
  def enabled = rule { capture(optional("no" ~ sp)) ~> (_.isEmpty) }
  def optionForceClose =  rule { enabled ~ "option forceclose" ~> ForceClose }
  def optionHttpClose =  rule { enabled ~ "option httpclose" ~> HttpClose }
  def optionHttpPretendKeepAlive =  rule { enabled ~ "option http-pretend-keepalive" ~> HttpPretendKeepAlive }
}
