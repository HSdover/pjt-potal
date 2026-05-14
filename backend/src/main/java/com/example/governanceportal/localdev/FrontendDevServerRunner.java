package com.example.governanceportal.localdev;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FrontendDevServerProperties.class)
@ConditionalOnProperty(prefix = "app.frontend-dev-server", name = "enabled", havingValue = "true")
public class FrontendDevServerRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FrontendDevServerRunner.class);
    private static final Duration STOP_TIMEOUT = Duration.ofSeconds(5);

    private final FrontendDevServerProperties properties;
    private Process process;

    public FrontendDevServerRunner(FrontendDevServerProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Optional<Path> workingDirectory = resolveWorkingDirectory();
        if (workingDirectory.isEmpty()) {
            log.warn(
                "Frontend dev server directory does not exist. Configured path: {}, current directory: {}",
                properties.getWorkingDirectory(),
                Path.of("").toAbsolutePath().normalize()
            );
            return;
        }

        if (isPortOpen(properties.getPort())) {
            if (!properties.isRestart()) {
                log.info("Frontend dev server already appears to be running on port {}.", properties.getPort());
                return;
            }

            log.info("Stopping existing frontend dev server on port {} before restart.", properties.getPort());
            stopProcessesOnPort(properties.getPort());
            waitUntilPortClosed(properties.getPort(), Duration.ofSeconds(10));
        }

        if (properties.isBuildBeforeStart()) {
            List<String> buildCommand = createCommand(properties.getBuildCommand(), properties.getBuildArgs());
            runForegroundCommand(workingDirectory.get(), buildCommand, "frontend build");
        }

        startFrontendDevServer(workingDirectory.get());
    }

    private void startFrontendDevServer(Path workingDirectory) throws IOException, InterruptedException {
        List<String> command = createCommand(properties.getCommand(), properties.getArgs());

        ProcessBuilder processBuilder = new ProcessBuilder(command)
            .directory(workingDirectory.toFile())
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT);

        process = processBuilder.start();
        waitUntilPortOpen(properties.getPort(), Duration.ofSeconds(20));

        log.info(
            "Started frontend dev server with command '{}' in {}. URL: http://127.0.0.1:{}",
            String.join(" ", command),
            workingDirectory,
            properties.getPort()
        );
    }

    private Optional<Path> resolveWorkingDirectory() {
        List<Path> candidates = List.of(
            Path.of(properties.getWorkingDirectory()),
            Path.of("frontend"),
            Path.of("..", "frontend")
        );

        return candidates.stream()
            .map(path -> path.toAbsolutePath().normalize())
            .filter(Files::isDirectory)
            .findFirst();
    }

    private boolean isPortOpen(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), (int) Duration.ofMillis(500).toMillis());
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private void runForegroundCommand(Path workingDirectory, List<String> command, String label)
        throws IOException, InterruptedException {
        log.info("Running {} with command '{}' in {}.", label, String.join(" ", command), workingDirectory);

        Process foregroundProcess = new ProcessBuilder(command)
            .directory(workingDirectory.toFile())
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start();

        int exitCode = foregroundProcess.waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException("%s failed with exit code %d.".formatted(label, exitCode));
        }
    }

    private List<String> createCommand(String command, String[] args) {
        List<String> result = new ArrayList<>();
        result.add(command);
        result.addAll(List.of(args));
        return result;
    }

    private void stopProcessesOnPort(int port) throws IOException, InterruptedException {
        Set<Long> processIds = isWindows()
            ? findWindowsListeningProcessIds(port)
            : findUnixListeningProcessIds(port);

        if (processIds.isEmpty()) {
            log.warn("No listening frontend process was found for port {}.", port);
            return;
        }

        for (Long processId : processIds) {
            if (isWindows()) {
                runStopCommand(List.of("taskkill", "/PID", processId.toString(), "/T", "/F"), processId);
            } else {
                runStopCommand(List.of("kill", "-TERM", processId.toString()), processId);
            }
        }
    }

    private Set<Long> findWindowsListeningProcessIds(int port) throws IOException, InterruptedException {
        Process netstatProcess = new ProcessBuilder("cmd.exe", "/c", "netstat -ano -p tcp")
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start();

        Set<Long> processIds = new LinkedHashSet<>();
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(netstatProcess.getInputStream(), StandardCharsets.UTF_8)
        )) {
            String line;
            while ((line = reader.readLine()) != null) {
                Optional<Long> processId = parseWindowsNetstatLine(line, port);
                processId.ifPresent(processIds::add);
            }
        }

        netstatProcess.waitFor(5, TimeUnit.SECONDS);
        return processIds;
    }

    private Optional<Long> parseWindowsNetstatLine(String line, int port) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length < 5 || !"TCP".equalsIgnoreCase(parts[0]) || !"LISTENING".equalsIgnoreCase(parts[3])) {
            return Optional.empty();
        }

        if (!parts[1].endsWith(":" + port)) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.parseLong(parts[4]));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    private Set<Long> findUnixListeningProcessIds(int port) throws IOException, InterruptedException {
        Process lsofProcess = new ProcessBuilder("sh", "-c", "lsof -ti tcp:" + port)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start();

        Set<Long> processIds = new LinkedHashSet<>();
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(lsofProcess.getInputStream(), StandardCharsets.UTF_8)
        )) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    processIds.add(Long.parseLong(line.trim()));
                } catch (NumberFormatException ignored) {
                    log.debug("Ignoring unexpected lsof output line: {}", line);
                }
            }
        }

        lsofProcess.waitFor(5, TimeUnit.SECONDS);
        return processIds;
    }

    private void runStopCommand(List<String> command, Long processId) throws IOException, InterruptedException {
        log.info("Stopping frontend process {}.", processId);
        Process stopProcess = new ProcessBuilder(command)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .start();
        boolean completed = stopProcess.waitFor(STOP_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        if (!completed) {
            stopProcess.destroyForcibly();
            log.warn("Timed out while stopping frontend process {}.", processId);
            return;
        }

        if (stopProcess.exitValue() == 0) {
            log.info("Stopped frontend process {}.", processId);
        } else {
            log.warn("Stop command for frontend process {} exited with code {}.", processId, stopProcess.exitValue());
        }
    }

    private void waitUntilPortClosed(int port, Duration timeout) throws InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (!isPortOpen(port)) {
                return;
            }
            Thread.sleep(250);
        }

        throw new IllegalStateException("Frontend dev server port %d is still open after restart stop timeout.".formatted(port));
    }

    private void waitUntilPortOpen(int port, Duration timeout) throws InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (isPortOpen(port)) {
                return;
            }

            if (process != null && !process.isAlive()) {
                throw new IllegalStateException("Frontend dev server process exited before opening port %d.".formatted(port));
            }

            Thread.sleep(250);
        }

        throw new IllegalStateException("Frontend dev server port %d did not open within startup timeout.".formatted(port));
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    @PreDestroy
    void stopProcess() {
        if (process == null || !process.isAlive()) {
            return;
        }

        long processId = process.pid();
        log.info("Stopping managed frontend dev server process {}.", processId);

        try {
            if (isWindows()) {
                runStopCommand(List.of("taskkill", "/PID", Long.toString(processId), "/T", "/F"), processId);
            } else {
                process.destroy();
                boolean completed = process.waitFor(STOP_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
                if (!completed) {
                    process.destroyForcibly();
                    log.warn("Forced frontend dev server process {} to stop.", processId);
                }
            }
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            log.warn("Interrupted while stopping frontend dev server process {}.", processId);
        } catch (IOException exception) {
            process.destroyForcibly();
            log.warn("Failed to stop frontend dev server process {} with platform command.", processId, exception);
        }
    }
}
