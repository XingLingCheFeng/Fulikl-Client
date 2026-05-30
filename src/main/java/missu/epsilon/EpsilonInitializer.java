package missu.epsilon;

import net.fabricmc.api.ClientModInitializer;
import tech.skidonion.obfuscator.annotations.Renamer;

@Renamer(obfuscated = false)
public class EpsilonInitializer implements ClientModInitializer {

	@Override
    @Renamer(obfuscated = false)
	public void onInitializeClient() {}

}