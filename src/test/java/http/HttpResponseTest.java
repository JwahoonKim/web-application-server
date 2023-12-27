package http;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class HttpResponseTest {

    private String testDirectory = "./src/test/java/http/resources/";

    @Test
    public void responseForward() throws FileNotFoundException {
        HttpResponse response = new HttpResponse(createOutputStream("Http_Forward.txt"));
        response.forward("/index.html");
    }

    @Test
    public void responseRedirect() throws FileNotFoundException {
        HttpResponse response = new HttpResponse(createOutputStream("Http_Redirect.txt"));
        response.sendRedirect("/index.html", null);
    }

    @Test
    public void responseCookies() throws IOException {
        HttpResponse response = new HttpResponse(createOutputStream("Http_Cookie.txt"));
        response.sendRedirect("/index.html", Boolean.TRUE);
    }

    private OutputStream createOutputStream(String fileName) throws FileNotFoundException {
        return new FileOutputStream(testDirectory + fileName);
    }
}
