package com.gregbeech.haproxy

import com.gregbeech.haproxy.Operators._
import com.gregbeech.haproxy.Prefixes._
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

  it should "parse a basic global section" in {
    new ConfigParser(
      """|global
         |  maxconn 256
         |  pidfile haproxy.pid
         |  daemon
         |""".stripMargin).global.run() should be (Success(Global(MaxConn(256), PidFile("haproxy.pid"), Daemon)))
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
    new ConfigParser("""acl invalid_src  src          0.0.0.0/7 224.0.0.0/3""").acl.run() should be (Success(Acl("invalid_src", "src", Seq(), None, Seq("0.0.0.0/7", "224.0.0.0/3"))))
    new ConfigParser("""acl local_dst    hdr(host) -i localhost""").acl.run() should be (Success(Acl("local_dst", "hdr(host)", Seq(Flag('i')), None, Seq("localhost"))))
    new ConfigParser("""acl negative-length hdr_val(content-length) lt 0""").acl.run() should be (Success(Acl("negative-length", "hdr_val(content-length)", Seq(), Some(Lt), Seq("0"))))
    new ConfigParser("""acl valid-ua hdr(user-agent) -f exact-ua.lst -i -f generic-ua.lst test""").acl.run() should be (Success(Acl("valid-ua", "hdr(user-agent)", Seq(Flag('f', "exact-ua.lst"), Flag('i'), Flag('f', "generic-ua.lst")), None, Seq("test"))))
  }

  it should "parse the bind setting for addresses and ports" in {
    // TODO: These don't check the bind options yet
    new ConfigParser("""bind :80""").bind.run() should be (Success(BindEndpoint(Seq(Endpoint(None, None, Some(Port(80)))), Seq())))
    new ConfigParser("""bind :80,:443""").bind.run() should be (Success(BindEndpoint(Seq(Endpoint(None, None, Some(Port(80))), Endpoint(None, None, Some(Port(443)))), Seq())))
    new ConfigParser("""bind 10.0.0.1:10080,10.0.0.1:10443""").bind.run() should be (Success(BindEndpoint(Seq(Endpoint(None, Some("10.0.0.1"), Some(Port(10080))), Endpoint(None, Some("10.0.0.1"), Some(Port(10443)))), Seq())))
    new ConfigParser("""bind ipv6@:80""").bind.run() should be (Success(BindEndpoint(Seq(Endpoint(Some(IPv6), None, Some(Port(80)))), Seq())))
    new ConfigParser("""bind ipv6@1:::80""").bind.run() should be (Success(BindEndpoint(Seq(Endpoint(Some(IPv6), Some("1::"), Some(Port(80)))), Seq())))
    new ConfigParser("""bind ipv4@public_ssl:443 ssl crt /etc/haproxy/site.pem""").bind.run() should be (Success(BindEndpoint(Seq(Endpoint(Some(IPv4), Some("public_ssl"), Some(Port(443)))), Seq())))
    new ConfigParser("""bind unix@ssl-frontend.sock user root mode 600 accept-proxy""").bind.run() should be (Success(BindEndpoint(Seq(Endpoint(Some(Unix), Some("ssl-frontend.sock"), None)), Seq())))
    new ConfigParser("""bind fd@${FD_APP1}""").bind.run() should be (Success(BindEndpoint(Seq(Endpoint(Some(Fd), Some("${FD_APP1}"), None)), Seq())))
  }

  it should "parse the bind setting for paths" in {
    // TODO: These don't check the bind options yet
    new ConfigParser("""bind /var/run/ssl-frontend.sock user root mode 600 accept-proxy""").bind.run() should be (Success(BindPath(Seq("/var/run/ssl-frontend.sock"), Seq())))
  }

  it should "parse the default_backend setting" in {
    new ConfigParser("""default_backend dynamic""").defaultBackend.run() should be (Success(DefaultBackend("dynamic")))
  }

  it should "parse the mode setting" in {
    new ConfigParser("""mode tcp""").mode.run() should be (Success(TcpMode))
    new ConfigParser("""mode http""").mode.run() should be (Success(HttpMode))
    new ConfigParser("""mode health""").mode.run() should be (Success(HealthMode))
  }

  it should "parse the use_backend setting" in {
    new ConfigParser("""use_backend static if host_static or host_www url_static""").useBackend.run() should be (Success(UseBackend("static", If("host_static or host_www url_static"))))
    new ConfigParser("""use_backend www    unless host_static""").useBackend.run() should be (Success(UseBackend("www", Unless("host_static"))))
  }

  it should "parse the forceclose option" in {
    new ConfigParser("""option forceclose""").forceClose.run() should be (Success(ForceClose(true)))
    new ConfigParser("""no option forceclose""").forceClose.run() should be (Success(ForceClose(false)))
  }

  it should "parse the httpclose option" in {
    new ConfigParser("""option httpclose""").httpClose.run() should be (Success(HttpClose(true)))
    new ConfigParser("""no option httpclose""").httpClose.run() should be (Success(HttpClose(false)))
  }

  it should "parse the http-pretend-keepalive option" in {
    new ConfigParser("""option http-pretend-keepalive""").httpPretendKeepAlive.run() should be (Success(HttpPretendKeepAlive(true)))
    new ConfigParser("""no option http-pretend-keepalive""").httpPretendKeepAlive.run() should be (Success(HttpPretendKeepAlive(false)))
  }
  
}
