package com.gregbeech.haproxy

import org.parboiled2._

class ConfigParser(val input: ParserInput) extends Parser {
  val CharsToEscape = " #\\"
  
  def inputLine = rule { padding ~ globalSetting ~ EOI }
  
  def sp = rule { ' ' }
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
  
  def aclname = name
  def criterion = string
  def flag0 = rule { '-' ~ anyOf("in-") ~ push(Flag(lastChar)) }
  def flag1 = rule { '-' ~ capture(anyOf("fmMu")) ~ sp ~ string ~> ((k, v) => Flag(k.head, v)) }
  def flag = rule { flag0 | flag1 }
  def operator = rule { capture("eq" | "ne" | "le" | "ge" | "lt" | "gt") ~> (Operator(_)) }
  def acl = rule { "acl" ~ sp ~ aclname ~ sp ~ criterion ~ zeroOrMore(sp ~ flag) ~ optional(sp ~ operator) ~ sp ~ string ~> Acl }
  
}

case class Comment(text: String)

sealed trait Section
case object Global extends Section
case class Defaults(name: String) extends Section

sealed trait Setting

sealed trait GlobalSetting extends Setting
case object Daemon extends GlobalSetting
case class MaxConn(number: Int) extends GlobalSetting

sealed trait ProxySetting extends Setting
case class Acl(name: String, criterion: String, flags: Seq[Flag], operator: Option[Operator], value: String) extends ProxySetting

sealed trait Flag
object Flag {
  case class Flag0(key: Char) extends Flag
  case class Flag1(key: Char, value: String) extends Flag
  def apply(key: Char): Flag = Flag0(key)
  def apply(key: Char, value: String): Flag = Flag1(key, value)
}

sealed trait Operator
object Operator {
  case object Eq extends Operator
  case object Ne extends Operator
  case object Le extends Operator
  case object Ge extends Operator
  case object Lt extends Operator
  case object Gt extends Operator
  def apply(s: String): Operator = s match {
    case "eq" => Eq
    case "ne" => Ne
    case "le" => Le
    case "ge" => Ge
    case "lt" => Lt
    case "gt" => Gt
    case x => throw new IllegalArgumentException(s"Invalid operator $x")
  }
}