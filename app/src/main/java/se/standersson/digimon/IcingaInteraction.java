package se.standersson.digimon;


import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;



class IcingaInteraction {


    static String fetchData(final HashMap<String, String> prefs){

        /*
        * Try Catch to catch all errors in network communication
        * */

        try {

            /*
            * Create a connection with input/output with a plaintext body
            * */
            URL url = new URL(prefs.get("serverString"));
            String credentials = prefs.get("username") + ":" + prefs.get("password");

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setDoInput(true);
            connection.setUseCaches(false);

            // Create an OutputStream and write the credentials to the server
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(credentials);
            writer.flush();
            writer.close();

            // Create the BufferedReader and add all batches together
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String json = "";
            String tmp;

            while ((tmp = reader.readLine()) != null) {
                json += tmp + "\n";
            }
            reader.close();
            connection.disconnect();
            return json;

        }catch (SocketTimeoutException e) {
            return "Connection Timed Out";
        } catch (MalformedURLException e) {
            return "Invalid URL";
        } catch (UnknownHostException e) {
            return "Resolve Failed";
        } catch (FileNotFoundException e) {
            return "FileNotFoundException";
        } catch (Exception e) {
            Log.e("NetworkException", e.toString());
            return "Unknown Exception";
        }
    }



}
