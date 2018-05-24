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
		// ���� HttpParams ���������� HTTP ��������һ���ֲ��Ǳ���ģ�
		HttpParams params = new BasicHttpParams();

		// �������ӳ�ʱ�� Socket ��ʱ���Լ� Socket �����С
		HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
		HttpConnectionParams.setSoTimeout(params, 20 * 1000);
		HttpConnectionParams.setSocketBufferSize(params, 8192);

		// �����ض���ȱʡΪ true
		HttpClientParams.setRedirecting(params, true);

		// ���� user agent
		HttpProtocolParams.setUserAgent(params, userAgent);

		// ����һ�� HttpClient ʵ��
		HttpClient httpClient = new DefaultHttpClient(params);
		try {
			// ���� HttpGet �������÷������Զ����� URL ��ַ���ض���
			HttpGet httpGet = new HttpGet("http://www.lietu.com/");

			HttpResponse response = httpClient.execute(httpGet);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				// ��������������ڸ�������������ǰ�����ж�
				httpGet.abort();
			}

			// ��ȡ������Ϣ
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
			// �ͷ�����
			httpClient.getConnectionManager().shutdown();
		}
	}

}
