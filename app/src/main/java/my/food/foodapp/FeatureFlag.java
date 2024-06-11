package my.food.foodapp;

import android.content.Context;
import android.content.SharedPreferences;

public class FeatureFlag {

    private static final String PREFERENCES_NAME = "MyAppPrefs";
    private static final String BACKEND_KEY = "backend_service";
    private static final String DEFAULT_BACKEND = "spring";

    public static String getBackendService(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return preferences.getString(BACKEND_KEY, DEFAULT_BACKEND);
    }

    public static void setBackendService(Context context, String backend) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(BACKEND_KEY, backend);
        editor.apply();
    }
}
