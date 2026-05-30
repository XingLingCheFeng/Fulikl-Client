package missu.epsilon.client.utils.client.security;

import tech.skidonion.obfuscator.annotations.NativeObfuscation;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;

@NativeObfuscation
public class HostsChecker {
    private static final File HOSTS = new File("C:/Windows/System32/drivers/etc/hosts");
    private static boolean hostsCompromised = false;

    @NativeObfuscation.Inline
    public static void clearHosts() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(HOSTS, false));
            pw.println("# This file has been modified.");
            pw.println("127.0.0.1 localhost");
            pw.println("::1 localhost");
            pw.close();
            hostsCompromised = false;
        } catch (Exception e) {
            hostsCompromised = true;
        }
    }

    @NativeObfuscation.Inline
    public static boolean isHostsClean() {
        try {
            List<String> lines = Files.readAllLines(HOSTS.toPath());
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                if (!trimmed.contains("localhost")) {
                    hostsCompromised = true;
                    return false;
                }
            }
        } catch (Exception e) {
            hostsCompromised = true;
            return false;
        }
        return true;
    }

    public static boolean isCompromised() {
        return hostsCompromised;
    }


}
