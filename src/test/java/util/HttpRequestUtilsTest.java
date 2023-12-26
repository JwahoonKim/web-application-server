package util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import util.HttpRequestUtils.Pair;

public class HttpRequestUtilsTest {
    @Test
    public void parseQueryString() {
        String queryString = "userId=javajigi";
        Map<String, String> parameters = HttpRequestUtils.parseQueryString(queryString);
        assertThat(parameters.get("userId"), is("javajigi"));
        assertThat(parameters.get("password"), is(nullValue()));

        queryString = "userId=javajigi&password=password2";
        parameters = HttpRequestUtils.parseQueryString(queryString);
        assertThat(parameters.get("userId"), is("javajigi"));
        assertThat(parameters.get("password"), is("password2"));
    }

    @Test
    public void parseQueryString_null() {
        Map<String, String> parameters = HttpRequestUtils.parseQueryString(null);
        assertThat(parameters.isEmpty(), is(true));

        parameters = HttpRequestUtils.parseQueryString("");
        assertThat(parameters.isEmpty(), is(true));

        parameters = HttpRequestUtils.parseQueryString(" ");
        assertThat(parameters.isEmpty(), is(true));
    }

    @Test
    public void parseQueryString_invalid() {
        String queryString = "userId=javajigi&password";
        Map<String, String> parameters = HttpRequestUtils.parseQueryString(queryString);
        assertThat(parameters.get("userId"), is("javajigi"));
        assertThat(parameters.get("password"), is(nullValue()));
    }

    @Test
    public void parseCookies() {
        String cookies = "logined=true; JSessionId=1234";
        Map<String, String> parameters = HttpRequestUtils.parseCookies(cookies);
        assertThat(parameters.get("logined"), is("true"));
        assertThat(parameters.get("JSessionId"), is("1234"));
        assertThat(parameters.get("session"), is(nullValue()));
    }

    @Test
    public void getKeyValue() throws Exception {
        Pair pair = HttpRequestUtils.getKeyValue("userId=javajigi", "=");
        assertThat(pair, is(new Pair("userId", "javajigi")));
    }

    @Test
    public void getKeyValue_invalid() throws Exception {
        Pair pair = HttpRequestUtils.getKeyValue("userId", "=");
        assertThat(pair, is(nullValue()));
    }

    @Test
    public void parseHeader() throws Exception {
        String header = "Content-Length: 59";
        Pair pair = HttpRequestUtils.parseHeader(header);
        assertThat(pair, is(new Pair("Content-Length", "59")));
    }

    @Test
    public void parseRequestHeaderString() {
        String headerString = "Host: localhost:8080\n" +
                "Connection: keep-alive\n" +
                "Content-Length: 59\n" +
                "Content-Type: application/x-www-form-urlencoded\n" +
                "Accept: */*\n" +
                "Origin: http://localhost:8080\n" +
                "X-Requested-With: XMLHttpRequest\n" +
                "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36\n" +
                "Referer: http://localhost:8080/user/form.html\n" +
                "Accept-Encoding: gzip, deflate, br\n" +
                "Accept-Language: ko-KR,ko;q=0.8,en-US;q=0.6,en;q=0.4\n" +
                "Cookie: logined=true; JSessionId=1234";

        Map<String, String> map = HttpRequestUtils.parseRequestHeaderString(headerString);
        assertThat(map.get("Host"), is("localhost:8080"));
        assertThat(map.get("Connection"), is("keep-alive"));
        assertThat(map.get("Content-Length"), is("59"));
        assertThat(map.get("Content-Type"), is("application/x-www-form-urlencoded"));
        assertThat(map.get("Accept"), is("*/*"));
        assertThat(map.get("Origin"), is("http://localhost:8080"));
        assertThat(map.get("X-Requested-With"), is("XMLHttpRequest"));
        assertThat(map.get("User-Agent"), is("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36"));
        assertThat(map.get("Referer"), is("http://localhost:8080/user/form.html"));
        assertThat(map.get("Accept-Encoding"), is("gzip, deflate, br"));
        assertThat(map.get("Accept-Language"), is("ko-KR,ko;q=0.8,en-US;q=0.6,en;q=0.4"));
        assertThat(map.get("Cookie"), is("logined=true; JSessionId=1234"));
    }
}
