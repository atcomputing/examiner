package nl.atcomputing.lpic1examtrainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * @author martijn brekhof
 *
 */
public class RetrieveExamQuestions extends IntentService {
	private String examURL;
	
	public RetrieveExamQuestions() {
		super("RetrieveExameQuestionsService");
	}

	/**
	 * The IntentService calls this method from the default worker thread with
	 * the intent that started the service. When this method returns, IntentService
	 * stops the service, as appropriate.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		//examURL = "http://www.watbenjedan.nl/";
		examURL = "http://10.0.0.1/test.txt";
		try {
			String result = fetchExam(examURL);
			Log.d(this.getClass().getSimpleName(), result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String fetchExam(String urlString) throws MalformedURLException, IOException {
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		DefaultHttpClient httpClient = new DefaultHttpClient();
		
        HttpGet request = new HttpGet(urlString);
        HttpResponse response = httpClient.execute(request);
        InputStream is = response.getEntity().getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		 while ((line = reader.readLine()) != null)
	      {
	        stringBuilder.append(line + "\n");
	      }
		return stringBuilder.toString();
    }
}