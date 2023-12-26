package webserver;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class RequestHandlerTest {

    @Test
    public void test() throws IOException {
        String request = "GET /index.html HTTP/1.1\n" +
                "Host: localhost:8080\n" +
                "Connection: keep-alive\n" +
                "Accept: */*\n" +
                "Referer: http://localhost:8080/index.html\n" +
                "Accept-Encoding: gzip, deflate, br\n" +
                "Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7\n" +
                "Cookie: logined=true\n" +
                "If-None-Match: W/\"2d-16b1f795b70\"\n" +
                "If-Modified-Since: Thu, 26 Sep 2019 13:18:26 GMT\n";

        StringReader sr = new StringReader(request);
        BufferedReader br = new BufferedReader(sr);

        System.out.println(br.readLine());
        // 첫줄 제외하고 모두 맵형식으로 만들기
        Map<String, String> map = new HashMap<>();
        String line;
        while (true) {
            line = br.readLine();
            if (line == null || line.isEmpty()) {
                break;
            }
            String[] split = line.split(": ");
            map.put(split[0], split[1]);
        }

        System.out.println("map = " + map);
        System.out.println("map.get(\"Re\") = " + map.get("Referer"));
    }
}