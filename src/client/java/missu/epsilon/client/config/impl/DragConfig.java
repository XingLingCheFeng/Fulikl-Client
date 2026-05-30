package missu.epsilon.client.config.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import missu.epsilon.client.Client;
import missu.epsilon.client.config.Config;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.ingameui.Dragging;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DragConfig extends Config {

    public static Map<String, Dragging> draggables = new java.util.HashMap<>();

    @Override
    public JsonObject saveConfig() {
        List<Module> modules = Client.moduleManager.getModules();
        modules.sort(Comparator.comparing(Module::getName));
        JsonObject object = new JsonObject();
        for (Dragging dragging : draggables.values()) {
            JsonObject dragObject = new JsonObject();
            JsonObject draggingPosObject = new JsonObject();
            draggingPosObject.addProperty("x", dragging.getXPos());
            draggingPosObject.addProperty("y", dragging.getYPos());
            dragObject.add("values", draggingPosObject);
            object.add(dragging.getName(), dragObject);
        }
        return object;
    }

    @Override
    public void loadConfig(JsonObject object) {
        List<Dragging> draggings = new ArrayList<>(draggables.values().stream().toList());
        draggings.sort(Comparator.comparing(Dragging::getName));
        for (Dragging dragging : draggings) {
            if (object.has(dragging.getName())) {
                JsonElement element = object.get(dragging.getName());
                if (element.isJsonObject()) {
                    JsonObject dragObject = element.getAsJsonObject();
                    if (dragObject.has("values")) {
                        JsonObject draggingPosObject = dragObject.get("values").getAsJsonObject();
                        if (draggingPosObject.has("x")) {
                            dragging.setXPos(draggingPosObject.get("x").getAsFloat());
                        }
                        if (draggingPosObject.has("y")) {
                            dragging.setYPos(draggingPosObject.get("y").getAsFloat());
                        }
                    }
                }
            }
        }
    }

}
