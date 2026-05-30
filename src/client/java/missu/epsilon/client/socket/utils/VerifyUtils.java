package missu.epsilon.client.socket.utils;

import lombok.SneakyThrows;
import missu.epsilon.client.Client;
import missu.epsilon.client.socket.VerifySocket;
import missu.epsilon.client.utils.client.security.KillSwitch;
import tech.skidonion.obfuscator.annotations.NativeObfuscation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Objects;

@NativeObfuscation
public class VerifyUtils {

    @NativeObfuscation.Inline
    private static Client.Systems INFO = Client.Systems.track();

    public static String getUniqueMachineID() {
        try {
            StringBuilder machineInfo = new StringBuilder();

            machineInfo.append(System.getProperty("os.name", ""));
            machineInfo.append(System.getProperty("os.arch", ""));

            String hardwareInfo = getHardwareInfo();
            if (hardwareInfo != null) {
                machineInfo.append(hardwareInfo);
            }

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(machineInfo.toString().getBytes());

            StringBuilder result = new StringBuilder();
            for (byte b : hashBytes) {
                result.append(String.format("%02x", b));
            }

            return "大男娘丹尼尔:" + CryptUtil.Base64Crypt.encrypt((result + "-" + Client.username + ":大男娘丹尼尔"));
        } catch (Exception e) {
            return "error_generating_id";
        }
    }

    @SuppressWarnings("deprecation")
    private static String getHardwareInfo() {
        try {
            Process process;
            StringBuilder result = new StringBuilder();

            if (INFO == Client.Systems.WINDOWS) {
                process = Runtime.getRuntime().exec("wmic baseboard get serialnumber");
                result.append(readProcessOutput(process));

                process = Runtime.getRuntime().exec("wmic cpu get processorid");
                result.append(readProcessOutput(process));
            } else if (INFO == Client.Systems.LINUX) {
                process = Runtime.getRuntime().exec("dmidecode -s baseboard-serial-number");
                result.append(readProcessOutput(process));

                process = Runtime.getRuntime().exec("cat /proc/cpuinfo");
                result.append(readProcessOutput(process));
            } else if (INFO == Client.Systems.MAC) {
                process = Runtime.getRuntime().exec("system_profiler SPHardwareDataType | grep 'Serial Number'");
                result.append(readProcessOutput(process));
            }

            return result.toString().replaceAll("\\s+", "").toLowerCase();
        } catch (Exception e) {
            return null;
        }
    }

    private static String readProcessOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() &&
                        !line.toLowerCase().contains("serialnumber") &&
                        !line.toLowerCase().contains("processorid") &&
                        !line.toLowerCase().contains("serial number")) {
                    output.append(line.trim());
                }
            }
        }
        return output.toString();
    }

    @SneakyThrows
    @NativeObfuscation
    public static void doVerify() {
        System.clearProperty("http.proxyHost"); // 小小伪站神看我吊打你
        System.clearProperty("http.proxyPort"); // 小小伪站神看我吊打你

        try {
            if (System.getProperty("http.proxyHost").equals("localhost") || System.getProperty("http.proxyHost").equals("127.0.0.1")) {
                KillSwitch.suicide();
                return;
            }
        } catch (Throwable ignored) {
        }

        String token = System.getProperty("verify.token");

        if (Objects.nonNull(token) && !token.isEmpty()) {

            byte[] encryptedBytes = Base64.getDecoder().decode(token);

            byte[] decryptedCompressedData = CryptUtil.RSA.decryptByPrivateKey(encryptedBytes);

            String originalData = CryptUtil.Deflate.decompress(decryptedCompressedData);

            String[] split = originalData.split(":");

            String username = split[0];

            if (username != null && !username.isEmpty()) {
                Client.username = username;
            }

        } else {
            KillSwitch.suicide();
        }
    }
}
