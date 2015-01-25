package com.gregbeech.haproxy

import org.scalatest.{FlatSpec, Matchers}

import scala.util.Success

class ConfigParserSpec extends FlatSpec with Matchers {
  
  ////////////////////////// intrinsics //////////////////////////

  "ConfigParser" should "parse numbers as integers" in {
    new ConfigParser("""123""").number.run() should be (Success(123))
  }
  
  it should "parse strings with no escaped characters" in {
    new ConfigParser("""hello123""").string.run() should be (Success("""hello123"""))
  }

  it should "parse strings with escaped characters" in {
    new ConfigParser("""hello\ wor\#ld\\123""").string.run() should be (Success("""hello wor#ld\123"""))
  }

  ////////////////////////// common //////////////////////////

  it should "parse comments" in {
    new ConfigParser("""# a comment""").comment.run() should be (Success(Comment("a comment")))
  }

  ////////////////////////// globals //////////////////////////

  it should "parse the global section declaration" in {
    new ConfigParser("""global""").global.run() should be (Success(Global))
  }
  
  it should "parse the daemon setting" in {
    new ConfigParser("""daemon""").daemon.run() should be (Success(Daemon))
  }
  
  it should "parse the maxconn setting" in {
    new ConfigParser("""maxconn 256""").maxconn.run() should be (Success(MaxConn(256)))
  }

  ////////////////////////// defaults //////////////////////////

  it should "parse the defaults section declaration" in {
    new ConfigParser("""defaults a_n.a-m:e1""").defaults.run() should be (Success(Defaults("a_n.a-m:e1")))
  }

  it should "parse valid acls" in {
    new ConfigParser("""acl valid-ua hdr(user-agent) -f exact-ua.lst -i -f generic-ua.lst test""").acl.run() should be
      Success(Acl("valid-ua", "hdr(user-agent)", Seq(Flag('f', "exact-ua.lst"), Flag('i'), Flag('f', "generic-ua.lst")), None, "test"))
  }
  
}
