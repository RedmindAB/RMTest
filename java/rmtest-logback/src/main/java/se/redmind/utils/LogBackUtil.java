package se.redmind.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;

/**
 * @author Jeremy Comte
 */
public final class LogBackUtil {

    private LogBackUtil() {
    }

    /**
     * @see se.redmind.utils.LogBackUtil.LogBackConfigurator#withLevel(ch.qos.logback.classic.Level)
     */
    public static LogBackConfigurator withLevel(Level level) {
        return new LogBackConfigurator().withLevel(level);
    }

    /**
     * @see se.redmind.utils.LogBackUtil.LogBackConfigurator#withLoggers(java.lang.String, ch.qos.logback.classic.Level)
     */
    public static LogBackConfigurator withLoggers(String pattern, Level level) {
        return new LogBackConfigurator().withLoggers(pattern, level);
    }

    /**
     * @see se.redmind.utils.LogBackUtil.LogBackConfigurator#withFormat(java.lang.String)
     */
    public static LogBackConfigurator withFormat(String format) {
        return new LogBackConfigurator().withFormat(format);
    }

    /**
     * @see se.redmind.utils.LogBackUtil.LogBackConfigurator#install()
     */
    public static void install() {
        new LogBackConfigurator().install();
    }

    /**
     * @see se.redmind.utils.LogBackUtil.LogBackConfigurator#ifNotInstalled()
     */
    public static LogBackConfigurator ifNotInstalled() {
        return new LogBackConfigurator().ifNotInstalled();
    }

    public static class LogBackConfigurator {

        private static boolean julLoggersInstalled;
        private static boolean alreadyInstalled;
        private Level defaultLevel = Level.INFO;
        private final Map<String, Level> loggers = new LinkedHashMap<>();
        private String format = "%d{HH:mm:ss.SSS} %-5p [%logger{5}] %msg %n";
        private boolean onlyIfNotInstalled;

        public static LoggerContext getLoggerContext() {
            final long startTime = System.currentTimeMillis();
            while (true) {
                ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
                if (iLoggerFactory instanceof LoggerContext) {
                    return (LoggerContext) iLoggerFactory;
                }
                if ((System.currentTimeMillis() - startTime) > 10_000) {
                    throw new IllegalStateException("Unable to acquire the logger context");
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        /**
         * Set the log level of the root logger
         *
         * @param level
         * @return the current LogBackConfigurator
         */
        public LogBackConfigurator withLevel(Level level) {
            this.defaultLevel = level;
            return this;
        }

        /**
         * Set the log level for a specific logger (class or package)
         *
         * @param pattern
         * @param level
         * @return the current LogBackConfigurator
         */
        public LogBackConfigurator withLoggers(String pattern, Level level) {
            loggers.put(pattern, level);
            return this;
        }

        /**
         * Set the format for this console appender
         *
         * @param format
         * @return the current LogBackConfigurator
         */
        public LogBackConfigurator withFormat(String format) {
            this.format = format;
            return this;
        }

        /**
         * Install the logger only if none has been installed before
         *
         * @return the current LogBackConfigurator
         */
        public LogBackConfigurator ifNotInstalled() {
            onlyIfNotInstalled = true;
            return this;
        }

        /**
         * Hijack the default java.util.Logger, remove all appenders and (re)install the new logback configuration
         */
        public synchronized void install() {
            if (!onlyIfNotInstalled || !alreadyInstalled) {
                if (!julLoggersInstalled) {
                    SLF4JBridgeHandler.removeHandlersForRootLogger();
                    SLF4JBridgeHandler.install();
                    julLoggersInstalled = true;
                }

                LoggerContext loggerContext = getLoggerContext();
                loggerContext.reset();

                Logger root = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
                root.detachAndStopAllAppenders();
                root.setLevel(defaultLevel);

                loggers.forEach((pattern, level) -> loggerContext.getLogger(pattern).setLevel(level));

                ConsoleAppender<ILoggingEvent> console = new ConsoleAppender<>();
                console.setName("STDOUT");
                console.setContext(loggerContext);
                console.setTarget("System.out");

                PatternLayout patternLayout = new PatternLayout();
                patternLayout.setContext(loggerContext);
                patternLayout.setPattern(format);
                patternLayout.start();

                console.setLayout(patternLayout);
                console.start();

                root.addAppender(console);

                loggerContext.start();
            }
        }
    }
}
