package missu.epsilon.client.config.impl;

import com.google.gson.JsonObject;
import missu.epsilon.client.Client;
import missu.epsilon.client.config.Config;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.value.Value;
import missu.epsilon.client.features.value.impl.*;
import missu.epsilon.client.utils.client.ClientUtils;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class ModuleConfig extends Config {
    @Override
    public JsonObject saveConfig() {
        List<Module> modules = Client.moduleManager.getModules();
        modules.sort(Comparator.comparing(Module::getName));
        JsonObject object = new JsonObject();

        try {
            for (Module module : modules) {
                JsonObject moduleObject = new JsonObject();

                moduleObject.addProperty("state", module.isEnabled());
                moduleObject.addProperty("hide", module.hide.getValue());

                JsonObject valuesObject = new JsonObject();

                for (Value<?> value : module.getValues()) {
                    if (value instanceof NumberValue nv) {
                        valuesObject.addProperty(value.name, nv.getValue());
                    } else if (value instanceof BoolValue bv) {
                        valuesObject.addProperty(value.name, bv.getValue());
                    } else if (value instanceof ListValue mv) {
                        valuesObject.addProperty(value.name, mv.getValue());
                    } else if (value instanceof BindValue bv) {
                        valuesObject.addProperty(value.name, bv.getValue());
                    } else if (value instanceof ColorValue cv) {
                        valuesObject.addProperty(value.name, cv.getColor().getRGB());
                        valuesObject.addProperty(value.name + "_alpha", cv.getColor().getAlpha());
                    } else if (value instanceof TextValue tv) {
                        String encoded = java.util.Base64.getEncoder().encodeToString(tv.getValue().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        valuesObject.addProperty(value.name, encoded);
                    } else if (value instanceof MultiBoolValue mb) {
                        JsonObject mbObject = new JsonObject();
                        mb.getMap().values().forEach(mbValue -> mbObject.addProperty(mbValue.name, mbValue.getValue()));
                        valuesObject.add(value.name, mbObject);
                    }
                }

                moduleObject.add("values", valuesObject);
                object.add(module.name, moduleObject);
            }
        } catch (Exception e) {
            ClientUtils.displayChat(e.toString());
        }
        return object;
    }

    @Override
    public void loadConfig(JsonObject object) {
        List<Module> modules = Client.moduleManager.getModules();
        modules.sort(Comparator.comparing(Module::getName));

        for (Module module : modules) {
            if (object.has(module.name)) {
                JsonObject moduleObject = object.get(module.name).getAsJsonObject();

                if (moduleObject.has("state")) {
                    module.setState(moduleObject.get("state").getAsBoolean());
                }

                if (moduleObject.has("hide")) {
                    module.hide.set(moduleObject.get("hide").getAsBoolean());
                }

                if (moduleObject.has("values")) {
                    JsonObject valuesObject = moduleObject.get("values").getAsJsonObject();

                    try {
                        for (Value<?> value : module.getValues()) {
                            if (valuesObject.has(value.name)) {
                                switch (value) {
                                    case NumberValue nv ->
                                            nv.setValue(valuesObject.get(value.name).getAsNumber().doubleValue());
                                    case BoolValue bv -> bv.setValue(valuesObject.get(value.name).getAsBoolean());
                                    case ListValue mv -> mv.setValue(valuesObject.get(value.name).getAsString());
                                    case BindValue bindValue -> bindValue.setValue(valuesObject.get(value.name).getAsString());
                                    case ColorValue cv -> {
                                        int rgb = valuesObject.get(value.name).getAsInt();
                                        int alpha = valuesObject.has(value.name + "_alpha") ?
                                                valuesObject.get(value.name + "_alpha").getAsInt() : 255;
                                        Color color = new Color(rgb, true);
                                        cv.setValue(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha).getRGB());
                                    }
                                    case TextValue tv -> {
                                        try {
                                            String decoded = new String(java.util.Base64.getDecoder().decode(valuesObject.get(value.name).getAsString()), java.nio.charset.StandardCharsets.UTF_8);
                                            tv.setValue(decoded);
                                        } catch (Exception e) {
                                            tv.setValue(valuesObject.get(value.name).getAsString());
                                        }
                                    }

                                    case MultiBoolValue mb -> {
                                        JsonObject mbObject = valuesObject.get(value.name).getAsJsonObject();
                                        mb.getMap().values().forEach(mbValue -> {
                                            try {
                                                mbValue.setValue(mbObject.get(mbValue.name).getAsBoolean());
                                            } catch (Exception e) {
                                                mbValue.setValue(false);
                                            }
                                        });
                                    }
                                    default -> throw new IllegalStateException("Unexpected value: " + value);
                                }
                            }
                        }
                    } catch (IllegalStateException | UnsupportedOperationException e) {
                        Client.logger.error(e.getMessage());
                    }
                }
            }
        }
    }
}
