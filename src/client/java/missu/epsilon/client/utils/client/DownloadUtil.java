package missu.epsilon.client.utils.client;


import missu.epsilon.client.Client;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class DownloadUtil {
    private static final ReentrantLock lock = new ReentrantLock(); // 线程锁
    public static volatile long totalFileSize = 0; // 文件总大小
    public static volatile long totalDownloaded = 0; // 已下载大小

    public static void resetProgress() {
        totalFileSize = 0;
        totalDownloaded = 0;
    }

    public static void download(String fileUrl, File destFile, int threads) {
        try {
            if (!isFileURL(fileUrl)) {
                fileUrl = extractDownloadLinkFromHtml(fileUrl);
            }
            long fileSize = getFileSize(fileUrl);
            final long nowTime = System.currentTimeMillis();
            if (fileSize > 1024 * 1024 && threads > 1 && isRangeSupported(fileUrl)) {
                Client.logger.info("Download task submitted, using thread count:{}, file size:{}mb", threads, String.format("%.2f", fileSize / 1024.0 / 1024.0));
                downloadFileWithMultipleThreads(fileUrl, destFile, fileSize, threads);
            } else {
                Client.logger.info("Download task submitted, using single-threaded download, file size:{}mb", fileSize / 1024.0 / 1024.0);
                downloadFile(fileUrl, destFile);
            }
            Client.logger.info("Download task completed! Total time taken:{} milliseconds", System.currentTimeMillis() - nowTime);
            resetProgress();
        } catch (Throwable e) {
            Client.logger.error("Failed to download file: {} {}", e, destFile.getName());
        }
    }

    public static boolean isRangeSupported(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");
        connection.connect();
        String acceptRanges = connection.getHeaderField("Accept-Ranges");
        connection.disconnect();
        return acceptRanges != null && acceptRanges.equalsIgnoreCase("bytes");
    }

    public static long getFileSize(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");
        connection.connect();
        long fileSize = connection.getContentLengthLong();
        connection.disconnect();
        return fileSize;
    }

    public static boolean isFileURL(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");
        connection.connect();
        String contentType = connection.getContentType();
        connection.disconnect();
        return contentType != null && !contentType.startsWith("text/html");
    }

    private static void downloadFileWithMultipleThreads(String fileUrl, File destFile, long fileSize, int threads) throws IOException {
        totalFileSize = fileSize; // 设置文件总大小
        totalDownloaded = 0; // 重置已下载大小
        long chunkSize = fileSize / threads;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        AtomicLong downloaded = new AtomicLong(0);

        try (RandomAccessFile outputFile = new RandomAccessFile(destFile, "rw")) {
            outputFile.setLength(fileSize);

            // 启动一个新的线程来监控下载进度
            Thread progressThread = new Thread(() -> {
                while (totalDownloaded < totalFileSize) {
                    double progress = (double) totalDownloaded / totalFileSize * 100;
                    Client.logger.info("Donwload progress: {}%", (int) progress);
                    try {
                        Thread.sleep(1000); // 每秒输出一次进度
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "Download Progress Thread");

            progressThread.start(); // 启动进度监控线程


            for (int i = 0; i < threads; i++) {
                long startByte = i * chunkSize;
                long endByte = (i == threads - 1) ? fileSize : (startByte + chunkSize - 1);
                executor.submit(() -> {
                    int retries = 3; // 重试次数
                    while (retries > 0) {
                        try {
                            URL url = new URL(fileUrl);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                            connection.setRequestProperty("Range", "bytes=" + startByte + "-" + endByte);
                            connection.connect();

                            if (connection.getResponseCode() == 206) { // 206 Partial Content
                                try (InputStream inputStream = connection.getInputStream();
                                     RandomAccessFile file = new RandomAccessFile(destFile, "rw")) {
                                    file.seek(startByte);
                                    byte[] buffer = new byte[1024];
                                    int bytesRead;
                                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                                        lock.lock();
                                        try {
                                            file.write(buffer, 0, bytesRead);
                                            downloaded.addAndGet(bytesRead);
                                            totalDownloaded = downloaded.get(); // 更新全局已下载大小
                                        } finally {
                                            lock.unlock();
                                        }
                                    }
                                }
                            } else {
                                throw new IOException("Server response exception, response code:" + connection.getResponseCode());
                            }
                            break; // 下载成功，退出重试循环
                        } catch (IOException e) {
                            retries--;
                            if (retries == 0) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }

            executor.shutdown();
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }

            progressThread.join(); // 等待进度监控线程结束
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    private static void downloadFile(String fileUrl, File destFile) throws Throwable {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setDoInput(true);

        totalFileSize = connection.getContentLengthLong(); // 设置文件总大小
        totalDownloaded = 0; // 重置已下载大小

        // 启动一个新的线程来监控下载进度
        Thread progressThread = new Thread(() -> {
            while (totalDownloaded < totalFileSize) {
                double progress = (double) totalDownloaded / totalFileSize * 100;
                Client.logger.info("Donwload progress: {}%", (int) progress);
                try {
                    Thread.sleep(1000); // 每秒输出一次进度
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Download Progress Thread");

        progressThread.start(); // 启动进度监控线程


        try (InputStream inputStream = connection.getInputStream();
             FileOutputStream fileOutputStream = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
                totalDownloaded += bytesRead; // 更新已下载大小
            }
        } finally {
            connection.disconnect();
            progressThread.join(); // 等待进度监控线程结束
        }
    }


    public static String extractDownloadLink(String html) {
        Document doc = Jsoup.parse(html);
        Elements links = doc.select("a[href]");
        if (links.isEmpty()) {
            return null; // 或者抛出异常，或者返回一个默认值
        }
        return links.first().attr("href");
    }


    public static String extractDownloadLinkFromHtml(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.connect();
        InputStream inputStream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder htmlContent = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            htmlContent.append(line).append("\n");
        }
        connection.disconnect();
        return extractDownloadLink(htmlContent.toString());
    }

    public static String get(String url) {
        try {
            URL urlObj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Safari/537.36 Edg/93.0.961.52");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                boolean isFirstLine = true;

                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        responseBuilder.append(line);
                        isFirstLine = false;
                    } else {
                        responseBuilder.append("\n").append(line);
                    }
                }
                reader.close();

                return responseBuilder.toString();
            } else {
                throw new IOException("HTTP request failed with response code: " + responseCode);
            }
        } catch (IOException e) {
            Client.logger.error("Failed to get data from URL: {} {}", e, url);
        }
        return null;
    }
}
