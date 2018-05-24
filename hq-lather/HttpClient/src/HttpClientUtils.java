import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;


public class HttpClientUtils {

	private String accept = "*/*";
	private String user_agent = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; SV1; .NET CLR 1.1.4322; InfoPath.1; .NET CLR 2.0.50727)";
	private String accept_language = "zh-cn";
	private String accept_encoding = "x-compress; x-zip";
	private String keep_alive_time= "115";
	private String connection = "keep_alive";
	private int connectionTimeout = 5000;
	private int soTimeout = 10000;
		
    public static void main(String[] args) throws ClientProtocolException, IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        BasicHttpParams hp = new BasicHttpParams();
        hp.setParameter("http.useragent", "Mozilla/1.0 (compatible; linux 2015 plus; yep)"); // you oughta change this into your own UA string....
        httpclient.setParams(hp);

        try {
            HttpGet httpget = new HttpGet("http://www.lietu.com/");
            HttpContext HTTP_CONTEXT = new BasicHttpContext();
            HttpResponse response = httpclient.execute(httpget, HTTP_CONTEXT);
            
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                System.out.println(EntityUtils.toString(entity,"utf-8"));
                EntityUtils.consume(entity);
            }
        } finally {
            httpclient.getConnectionManager().shutdown();
        }

    }

}
