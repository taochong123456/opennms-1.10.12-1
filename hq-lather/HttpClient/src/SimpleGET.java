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
		//����һ���ͻ��ˣ������ڴ�һ�������
		DefaultHttpClient httpclient = new DefaultHttpClient();

		//����һ��get���������������������ַ��������һ����ַ
		HttpGet httpget = new HttpGet("http://www.lietu.com/");

		//���������������ַ��������س��������ҳ����
		HttpResponse response = httpclient.execute(httpget);

		//�鿴���ص����ݣ���������������쿴��ҳԴ����
        HttpEntity entity = response.getEntity();
        if (entity != null) {
        	//�����������������ַ�����ʽ���أ�����ָ����ҳ������UTF-8
            System.out.println(EntityUtils.toString(entity,"utf-8"));
            EntityUtils.consume(entity);//�ر�������
        }
        
		//�ͷ�����
		httpclient.getConnectionManager().shutdown();
	}

}
