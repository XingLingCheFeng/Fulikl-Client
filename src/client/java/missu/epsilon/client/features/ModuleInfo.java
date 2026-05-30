package missu.epsilon.client.features;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleInfo {

    String name();

    String description() default "";

    ModuleCategory category();

    EnumAutoDisableType autoDisable() default EnumAutoDisableType.NONE;

    boolean defaultOn() default false;

    boolean hide() default false;

}