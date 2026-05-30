package missu.epsilon.client.features.modules.combat;

import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.MultiBoolValue;
import missu.epsilon.client.features.value.impl.NumberValue;

@ModuleInfo(name = "ProjectilesAura", description = "Automatically hurl projectiles at enemies within attack range", category = ModuleCategory.COMBAT)
public class ProjectilesAura extends Module {
    public static MultiBoolValue addons = new MultiBoolValue("Projectile Addons",new BoolValue[]{
       new BoolValue("Snowball"),
       new BoolValue("Egg"),
       new BoolValue("Rod")
    });

    public static NumberValue farRange = new NumberValue("Far Range", 8, 0, 20);
    public static NumberValue nearRange = new NumberValue("Near Range", 3, 0, 6);
}
