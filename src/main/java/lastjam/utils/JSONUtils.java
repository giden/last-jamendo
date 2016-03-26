package lastjam.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class JSONUtils {
	public static String downloadFileFromInternet(String url)
    {
        if(url == null || url.isEmpty() == true)
            throw new IllegalArgumentException("url is empty/null");
        StringBuilder sb = new StringBuilder();
        InputStream inStream = null;
        try
        {
            url = urlEncode(url);
            URL link = new URL(url);
            inStream = link.openStream();
            int i;
            int total = 0;
            byte[] buffer = new byte[8 * 1024];
            while((i=inStream.read(buffer)) != -1)
            {
                if(total >= (1024 * 1024))
                {
                    return "";
                }
                total += i;
                sb.append(new String(buffer,0,i));
            }
        }catch(OutOfMemoryError e)
        {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return sb.toString();
    }
	
	private static String urlEncode(String url)
    {
        url = url.replace("[","");
        url = url.replace("]","");
        url = url.replaceAll(" ","%20");
        return url;
    }
}
