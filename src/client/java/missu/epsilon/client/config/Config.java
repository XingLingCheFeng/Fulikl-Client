package missu.epsilon.client.config;

import com.google.gson.JsonObject;

public abstract class Config {

    public abstract JsonObject saveConfig();

    public abstract void loadConfig(JsonObject object);

}
