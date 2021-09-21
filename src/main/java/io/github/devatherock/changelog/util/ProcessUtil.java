package io.github.devatherock.changelog.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class to handle processes
 */
@Slf4j
public class ProcessUtil {

    /**
     * Executes a command and returns the exit code and output
     *
     * @param command
     * @return exit code
     */
    public static int executeCommand(Object command, boolean shell) {
        String[] finalCommand = null;

        if (shell && !(command instanceof String[])) {
            finalCommand = new String[] { "sh", "-c", command.toString() };
        } else if (command instanceof String) {
            finalCommand = new String[] { command.toString() };
        } else {
            finalCommand = (String[]) command;
        }
        int exitCode = 0;

        try {
            Process process = Runtime.getRuntime().exec(finalCommand);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            Thread outThread = startThread(() -> {
                try {
                    IOUtils.copy(process.getInputStream(), out);
                } catch (IOException exception) {
                    LOGGER.warn("Exception when reading std out", exception);
                }
            });
            Thread errThread = startThread(() -> {
                try {
                    IOUtils.copy(process.getErrorStream(), err);
                } catch (IOException exception) {
                    LOGGER.warn("Exception when reading std err", exception);
                }
            });

            exitCode = process.waitFor();
            outThread.join();
            errThread.join();

            if (out.size() > 0) {
                LOGGER.info(new String(out.toByteArray(), StandardCharsets.UTF_8));
            }
            if (err.size() > 0) {
                LOGGER.error(new String(err.toByteArray(), StandardCharsets.UTF_8));
            }
        } catch (IOException | InterruptedException exception) {
            LOGGER.error("Exception when executing command {}", finalCommand, exception);
        }

        return exitCode;
    }

    private static Thread startThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.start();

        return thread;
    }
}
