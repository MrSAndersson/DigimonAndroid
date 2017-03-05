package se.standersson.icingalert;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import javax.net.ssl.HttpsURLConnection;



class ServerInteraction {



    static String fetchData(final String[] prefs){

        /*
        * Try Catch to catch all errors in network communication
        * */
        String serverString = prefs[0];
        String username = prefs[1];
        String password = prefs[2];

        try {
            /*
            * Create a connection with input/output with a plaintext body
            * */
            URL url = new URL(serverString);
            String credentials = username + ":" + password;

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

            // Handle all known exceptions

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

    static boolean checkReply(Context context, String reply){
        /*
        * Check reply for exceptions caught in communication
         */
        switch (reply) {
            case "Wrong credentials\n":
                Toast.makeText(context, "Wrong Credentials", Toast.LENGTH_LONG).show();
                return false;
            case "Connection Timed Out":
                Toast.makeText(context, reply, Toast.LENGTH_LONG).show();
                return false;
            case "Invalid URL":
                Toast.makeText(context, reply, Toast.LENGTH_LONG).show();
                return false;
            case "Resolve Failed":
                Toast.makeText(context, reply, Toast.LENGTH_LONG).show();
                return false;
            case "FileNotFoundException":
                Toast.makeText(context, reply, Toast.LENGTH_LONG).show();
                return false;
            case "Unknown Exception":
                Toast.makeText(context, reply, Toast.LENGTH_LONG).show();
                return false;
            default:
                return true;
        }
    }

    static boolean isConnected(Context context){
        /*
        * Check Network Connectivity and then request data from Icinga
        * */

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

}
