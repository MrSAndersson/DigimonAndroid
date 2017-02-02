package se.standersson.digimon;



import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

final class IcingaInteraction {

    static void updateData(HashMap<String, String> prefs) {
        try {
            URL url = new URL(prefs.get("serverString"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String credentials = prefs.get("username") + prefs.get("password");


    }

}
