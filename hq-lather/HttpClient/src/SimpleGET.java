import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


public class SimpleGET {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public static void main(String[] args) throws ClientProtocolException, IOException {
		//创建一个客户端，类似于打开一个浏览器
		DefaultHttpClient httpclient = new DefaultHttpClient();

		//创建一个get方法，类似于在浏览器地址栏中输入一个地址
		HttpGet httpget = new HttpGet("http://www.lietu.com/");

		//类似于在浏览器地址栏中输入回车，获得网页内容
		HttpResponse response = httpclient.execute(httpget);

		//查看返回的内容，类似于在浏览器察看网页源代码
        HttpEntity entity = response.getEntity();
        if (entity != null) {
        	//读入内容流，并以字符串形式返回，这里指定网页编码是UTF-8
            System.out.println(EntityUtils.toString(entity,"utf-8"));
            EntityUtils.consume(entity);//关闭内容流
        }
        
		//释放连接
		httpclient.getConnectionManager().shutdown();
	}

}
