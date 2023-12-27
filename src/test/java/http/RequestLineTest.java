package http;

import org.junit.Test;

import static org.junit.Assert.*;

public class RequestLineTest {

    @Test
    public void GET_테스트() {
        RequestLine line = new RequestLine("GET /index.html HTTP/1.1");
        assertEquals(HttpMethod.GET, line.getMethod());
        assertEquals("/index.html", line.getPath());
    }

    @Test
    public void GET_파라미터_테스트() {
        RequestLine line = new RequestLine("GET /user/create?userId=javajigi&password=password&name=JaeSung HTTP/1.1");
        assertEquals(HttpMethod.GET, line.getMethod());
        assertEquals("/user/create", line.getPath());
        assertEquals("javajigi", line.getParameters().get("userId"));
        assertEquals("password", line.getParameters().get("password"));
        assertEquals("JaeSung", line.getParameters().get("name"));
    }

    @Test
    public void POST_테스트() {
        RequestLine line = new RequestLine("POST /index.html HTTP/1.1");
        assertEquals(HttpMethod.POST, line.getMethod());
        assertEquals("/index.html", line.getPath());
    }
}