package github.businessdirt.eurybium.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import github.businessdirt.eurybium.Eurybium;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;

public class Reference {
    public static final String MOD_ID = "eurybium";
    public static final String MOD_NAME = "Eurybium";
    @NotNull
    public static final String VERSION = getVersion();
    public static final String UNKNOWN_VERSION = "unknown";

    private static String getVersion() {
        try {
            Enumeration<URL> urls = Eurybium.class.getClassLoader().getResources("fabric.mod.json");
            Gson gson = new Gson();
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                try (InputStream is = url.openStream()) {
                    JsonArray jsonArray = gson.fromJson(new InputStreamReader(is), JsonArray.class);
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JsonObject json = jsonArray.get(i).getAsJsonObject();
                        if (json.get("id").getAsString().equals(MOD_ID)) {
                            String version = json.get("version").getAsString();
                            return version.isEmpty() ? UNKNOWN_VERSION : version;
                        }
                    }
                }
            }
            return UNKNOWN_VERSION;
        } catch (IOException ioe) {
            LogManager.getLogger(Reference.class).fatal("Failed while getting Eurybium version", ioe);
            return UNKNOWN_VERSION;
        }
    }
}
