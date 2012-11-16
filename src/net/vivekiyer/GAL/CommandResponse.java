package net.vivekiyer.GAL;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

public class CommandResponse {

	private int statusCode = 0;
	public int getStatusCode() {
		return statusCode;
	}

	private InputStream wbxmlInputStream;
	public InputStream getWBXMLInputStream(){
		return wbxmlInputStream;
	}

	private String errorString = "";
	protected String getErrorString(){
		return errorString;
	}
	
	protected CommandResponse(HttpResponse response) throws IllegalStateException, IOException 
	{
		statusCode = response.getStatusLine().getStatusCode() ; 

		// 200 indicates a success
		if( statusCode == 200){
			HttpEntity entity = response.getEntity();
			
			if (entity != null) {

				String contentType = entity.getContentType().getValue();
				
				// GZipped entities
				if (contentType
						.compareToIgnoreCase("application/vnd.ms-sync.wbxml") == 0) {
					
					if(entity.getContentEncoding() != null 
							&& entity.getContentEncoding().getValue().equalsIgnoreCase("gzip"))
					{
						InputStream gzippedResponse = entity.getContent();
					    InputStream ungzippedResponse = new GZIPInputStream(gzippedResponse);
					    ByteArrayOutputStream out = new ByteArrayOutputStream(); 
					    
					    byte[] buffer = new byte[1024];
					    int c = 0;
					    while (( c = ungzippedResponse.read(buffer, 0, 1024)) > 0) {
					       out.write(buffer, 0, c);
					    }				 
					    wbxmlInputStream = new ByteArrayInputStream(out.toByteArray());
					}
					else
					{
						wbxmlInputStream = entity.getContent();
					}
					
				}
				// Text / HTML entities
				// We will see this only in case of an error
				else if (contentType.compareToIgnoreCase("text/html") == 0) {
					errorString = EntityUtils.toString(entity);
				}
			}
			else
			{
				errorString = "No response received from server";
			}
		}
		else {
			errorString = String.format("Server responded %d", statusCode);
		}
	}
}
