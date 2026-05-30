package missu.epsilon.client.utils.client.security.dev;

public class DevelopmentUtil {
    public static void runInRelease(Executor executor) {
        if (!isDevelopment()) {
            executor.run();
        }
    }

    public static boolean isDevelopment() {
        String classPath = System.getProperty("java.class.path");
        String javaCommand = System.getProperty("sun.java.command");

        return (classPath != null && classPath.contains("idea_rt.jar")) ||
                (javaCommand != null && javaCommand.contains("com.intellij.rt.execution")) ||
                new java.io.File(".idea").exists();
    }
}