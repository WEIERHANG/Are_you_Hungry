package com.hangandkai.areyouhungry;

import android.content.Context;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtils {
    public static String getProperty(Context context, String fileName, String key) {
        Properties properties = new Properties();
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            properties.load(inputStream);
            return properties.getProperty(key);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
