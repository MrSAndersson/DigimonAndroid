package se.standersson.icingalert;


import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


class VolleySingleton {
    @SuppressLint("StaticFieldLeak")
    private static VolleySingleton myVolley;
    private RequestQueue myRequestQueue;
    @SuppressLint("StaticFieldLeak")
    private static Context myContext;

    private VolleySingleton(Context context) {
        myContext = context;
        myRequestQueue = getRequestQueue();
    }

    public static synchronized VolleySingleton getInstance(Context context) {
        if (myVolley == null) {
            myVolley = new VolleySingleton(context);
        }
        return myVolley;
    }


    public RequestQueue getRequestQueue() {
        if (myRequestQueue == null) {
            myRequestQueue = Volley.newRequestQueue(myContext.getApplicationContext());
        }
        return myRequestQueue;
    }



    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
