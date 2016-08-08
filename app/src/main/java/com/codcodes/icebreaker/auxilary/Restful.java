package com.codcodes.icebreaker.auxilary;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by Casper on 2016/08/08.
 */
public class Restful
{

    public static String getJsonFromURL(String url) throws IOException
    {
        URL urlConn = urlConn = new URL("http://icebreak.azurewebsites.net/IBUserRequestService.svc/" + url);
        HttpURLConnection httpConn =  (HttpURLConnection)urlConn.openConnection();
        //httpConn.setRequestProperty("Transfer-Encoding","chunked");
        Scanner s = new Scanner(httpConn.getInputStream());
        String response = "";

        while (s.hasNextLine())
            response += s.nextLine();

        if(httpConn.getResponseCode() == HttpURLConnection.HTTP_OK)
        {
            System.out.println(response);
            return response;
        }else
            return  "";
    }
}
