import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;

public class UATest {
	private static String userAgent = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; SV1; .NET CLR 1.1.4322; InfoPath.1; .NET CLR 2.0.50727)";

	public static void main(String[] args) {
		// 创建 HttpParams 以用来设置 HTTP 参数（这一部分不是必需的）
		HttpParams params = new BasicHttpParams();

		// 设置连接超时和 Socket 超时，以及 Socket 缓存大小
		HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
		HttpConnectionParams.setSoTimeout(params, 20 * 1000);
		HttpConnectionParams.setSocketBufferSize(params, 8192);

		// 设置重定向，缺省为 true
		HttpClientParams.setRedirecting(params, true);

		// 设置 user agent
		HttpProtocolParams.setUserAgent(params, userAgent);

		// 创建一个 HttpClient 实例
		HttpClient httpClient = new DefaultHttpClient(params);
		try {
			// 创建 HttpGet 方法，该方法会自动处理 URL 地址的重定向
			HttpGet httpGet = new HttpGet("http://www.lietu.com/");

			HttpResponse response = httpClient.execute(httpGet);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				// 错误处理，例如可以在该请求正常结束前将其中断
				httpGet.abort();
			}

			// 读取更多信息
			Header[] headers = response.getAllHeaders();
			HttpEntity entity = response.getEntity();
			Header header = response.getFirstHeader("Content-Type");
			if (entity != null) {
				System.out.println(EntityUtils.toString(entity, "utf-8"));
				EntityUtils.consume(entity);
			}
		} catch (Exception ee) {
			//  
		} finally {
			// 释放连接
			httpClient.getConnectionManager().shutdown();
		}
	}

}
