package com.example.governanceportal.config;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    private final FrontendDevServerProperties properties;
    private Process process;

    public FrontendDevServerRunner(FrontendDevServerProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (isPortOpen(properties.getPort())) {
            log.info("Frontend dev server already appears to be running on port {}.", properties.getPort());
            return;
        }

        Optional<Path> workingDirectory = resolveWorkingDirectory();
        if (workingDirectory.isEmpty()) {
            log.warn(
                "Frontend dev server directory does not exist. Configured path: {}, current directory: {}",
                properties.getWorkingDirectory(),
                Path.of("").toAbsolutePath().normalize()
            );
            return;
        }

        List<String> command = new ArrayList<>();
        command.add(properties.getCommand());
        command.addAll(List.of(properties.getArgs()));

        ProcessBuilder processBuilder = new ProcessBuilder(command)
            .directory(workingDirectory.get().toFile())
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT);

        process = processBuilder.start();
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopProcess));

        log.info(
            "Started frontend dev server with command '{}' in {}. URL: http://127.0.0.1:{}",
            String.join(" ", command),
            workingDirectory.get(),
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

    private void stopProcess() {
        if (process != null && process.isAlive()) {
            process.destroy();
        }
    }
}
