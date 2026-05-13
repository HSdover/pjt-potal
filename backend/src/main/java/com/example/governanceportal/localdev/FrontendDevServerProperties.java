package com.example.governanceportal.localdev;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.frontend-dev-server")
public class FrontendDevServerProperties {

    private boolean enabled;
    private boolean restart;
    private boolean buildBeforeStart;
    private int port = 5173;
    private String workingDirectory = "../frontend";
    private String command = "npm.cmd";
    private String[] args = { "run", "dev" };
    private String buildCommand = "npm.cmd";
    private String[] buildArgs = { "run", "build" };

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRestart() {
        return restart;
    }

    public void setRestart(boolean restart) {
        this.restart = restart;
    }

    public boolean isBuildBeforeStart() {
        return buildBeforeStart;
    }

    public void setBuildBeforeStart(boolean buildBeforeStart) {
        this.buildBeforeStart = buildBeforeStart;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public String getBuildCommand() {
        return buildCommand;
    }

    public void setBuildCommand(String buildCommand) {
        this.buildCommand = buildCommand;
    }

    public String[] getBuildArgs() {
        return buildArgs;
    }

    public void setBuildArgs(String[] buildArgs) {
        this.buildArgs = buildArgs;
    }
}
