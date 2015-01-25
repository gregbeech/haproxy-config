package com.gregbeech.haproxy

import com.gregbeech.haproxy.Operators._
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Success

class ConfigParserSpec extends FlatSpec with Matchers {
  
  ////////////////////////// intrinsics //////////////////////////

  "ConfigParser" should "parse numbers as integers" in {
    new ConfigParser("""123""").number.run() should be (Success(123))
  }
  
  it should "parse strings with no escaped characters" in {
    new ConfigParser("""hello123""").string.run() should be (Success("hello123"))
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

  it should "parse the pidfile setting" in {
    new ConfigParser("""pidfile haproxy.pid""").pidfile.run() should be (Success(PidFile("haproxy.pid")))
  }

  ////////////////////////// proxies //////////////////////////

  it should "parse the defaults section declaration" in {
    new ConfigParser("""defaults a_n.a-m:e1""").defaults.run() should be (Success(Defaults("a_n.a-m:e1")))
  }

  it should "parse valid acls" in {
    new ConfigParser("""acl invalid_src  src          0.0.0.0/7 224.0.0.0/3""").acl.run() should be
      Success(Acl("invalid_src", "src", Seq(), None, "0.0.0.0/7 224.0.0.0/3"))
    new ConfigParser("""acl local_dst    hdr(host) -i localhost""").acl.run() should be
      Success(Acl("local_dst", "hdr(host)", Seq(Flag('i')), None, "localhost"))
    new ConfigParser("""acl negative-length hdr_val(content-length) lt 0""").acl.run() should be
      Success(Acl("negative-length", "hdr_val(content-length)", Seq(), Some(Lt), "0"))
    new ConfigParser("""acl valid-ua hdr(user-agent) -f exact-ua.lst -i -f generic-ua.lst test""").acl.run() should be
      Success(Acl("valid-ua", "hdr(user-agent)", Seq(Flag('f', "exact-ua.lst"), Flag('i'), Flag('f', "generic-ua.lst")), None, "test"))
  }

  it should "parse the mode setting" in {
    new ConfigParser("""mode tcp""").mode.run() should be (Success(TcpMode))
    new ConfigParser("""mode http""").mode.run() should be (Success(HttpMode))
    new ConfigParser("""mode health""").mode.run() should be (Success(HealthMode))
  }

  it should "parse the forceclose option" in {
    new ConfigParser("""option forceclose""").optionForceClose.run() should be (Success(ForceClose(true)))
    new ConfigParser("""no option forceclose""").optionForceClose.run() should be (Success(ForceClose(false)))
  }

  it should "parse the httpclose option" in {
    new ConfigParser("""option httpclose""").optionHttpClose.run() should be (Success(HttpClose(true)))
    new ConfigParser("""no option httpclose""").optionHttpClose.run() should be (Success(HttpClose(false)))
  }

  it should "parse the http-pretend-keepalive option" in {
    new ConfigParser("""option http-pretend-keepalive""").optionHttpPretendKeepAlive.run() should be (Success(HttpPretendKeepAlive(true)))
    new ConfigParser("""no option http-pretend-keepalive""").optionHttpPretendKeepAlive.run() should be (Success(HttpPretendKeepAlive(false)))
  }
  
}
