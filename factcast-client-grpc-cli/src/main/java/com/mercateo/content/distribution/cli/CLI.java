/**
 * Copyright © 2018 Mercateo AG (http://www.mercateo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mercateo.content.distribution.cli;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * In order to add more commands, simply create a new class that is annotated with
 * <p>
 * &#64;{@link Component}, extends {@link AbstractAction} and registers with this
 * (via method {@link #register(String)}) in a static block.
 */
@RequiredArgsConstructor
@Component
@SpringBootApplication
public class CLI {

    private static final String HOST_SYSPROP_NAME = "grpc.client.factstore.host";

    private static final String PORT_SYSPROP_NAME = "grpc.client.factstore.port";

    /** Map command line arg to description of corresponding action */
    private static final Set<String> cliActions = new HashSet<>();

    private static String actionFromArgs = null;

    static String getActionFromArgs() {
        return actionFromArgs;
    }

    static {
        // triggers that their static {} blocks are executed
        loadActionClasses();
    }

    /**
     * Actions can register their names here.
     *
     * @param actionName
     */
    public static void register(String actionName) {
        cliActions.add(actionName);
    }

    public static void main(String[] args) {

        Options options = new Options();

        addGlobalOptions(options);
        CommandLine commandLine = getCommandLine(args, options);

        // values can be specified either with command line option, environment variable or
        // system property.
        // make sure the system property is set from one of the two other sources, if needed.
        overwriteSystemProperties(commandLine, "host", "FC_HOST", HOST_SYSPROP_NAME);
        overwriteSystemProperties(commandLine, "port", "FC_PORT", PORT_SYSPROP_NAME);

        // Exits with error code 1 in case global arguments are not valid.
        validateArguments(options, commandLine);

        actionFromArgs = commandLine.getOptionValue("a");

        // start spring app, the runners themselves will evaluate their parameters then.
        SpringApplication.run(CLI.class, args);
    }

    /**
     * If setting is given as command-line argument, use this.
     * <p>
     * Otherwise, if system property is set, keep that one.
     * <p>
     * If system property is not set, use value from environment variable.
     * <p>
     * Exit if value is not set anywhere.
     *
     * @param commandLine
     * @param commandLineOption
     * @param envVarName
     * @param sysPropName
     */
    private static void overwriteSystemProperties(CommandLine commandLine,
            String commandLineOption,
            String envVarName,
            String sysPropName) {

        if (isNotBlank(commandLine.getOptionValue(commandLineOption))) {
            System.setProperty(sysPropName, commandLine.getOptionValue(commandLineOption));
        }

        if (isBlank(System.getProperty(sysPropName)) && isNotBlank(System.getenv(envVarName))) {
            System.setProperty(sysPropName, System.getenv(envVarName));
        }

    }

    private static CommandLine getCommandLine(String[] args, Options options) {

        CommandLine commandLine = null;

        try {
            commandLine = new DefaultParser()
                    .parse(options, args, true);

        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.err.flush();

            printHelp(options);

            System.exit(1);
        }

        return commandLine;
    }

    private static void validateArguments(Options options, CommandLine args) {

        String action = args.getOptionValue("a");

        if (!cliActions.contains(action)) {

            System.err.println("Unknown action " + action);
            System.err.flush();

            printHelp(options);

            System.exit(1);
        }

        validate("host", "host", "FC_HOST", HOST_SYSPROP_NAME);
        validate("port", "port", "FC_PORT", PORT_SYSPROP_NAME);

    }

    private static void validate(String name,
            String optionName,
            String envVarName,
            String systemPropertyName) {

        // value must be set either given as command-line option,
        // environment variable or system property.
        // At startup, made sure the value, wherever specified,
        // ended up in the system property.
        // Now check if the value was given at all, and explain how
        // it can be set.
        if (isNotBlank(System.getProperty(systemPropertyName))) {
            return;
        }

        System.err.println("Value for " + name + " not specified.");
        System.err.println("You must either specify via the command-line option --" + optionName
                + ",");
        System.err.println("or via system property " + systemPropertyName + ",");
        System.err.println("or via environment variable " + envVarName + ".");
        System.err.flush();

        System.exit(1);
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();

        formatter.setOptPrefix("");
        formatter.printHelp("fc -a <action> [further options]", options);
    }

    static void addGlobalOptions(Options options) {

        options.addOption(
                Option.builder("a")
                        .longOpt("action")
                        .desc("The action to perform, one of " + cliActions)
                        .hasArg()
                        .required()
                        .build());

        options.addOption(
                Option.builder()
                        .longOpt("host")
                        .hasArg()
                        .desc("The host URL of the fact cast instance you want to connect to. Required, "
                                +
                                "unless the FC_HOST environment variable is set.")
                        .build());

        options.addOption(
                Option.builder()
                        .longOpt("port")
                        .hasArg()
                        .desc("The port of the fact cast instance you want to connect to. Required, "
                                +
                                "unless the FC_PORT environment variable is set.")
                        .build());

    }

    private static void loadActionClasses() {
        BeanDefinitionRegistry bdr = new SimpleBeanDefinitionRegistry();
        ClassPathBeanDefinitionScanner s = new ClassPathBeanDefinitionScanner(bdr);

        TypeFilter tf = new AssignableTypeFilter(AbstractAction.class);
        s.addIncludeFilter(tf);
        s.scan(AbstractAction.class.getPackage().getName());

        asList(bdr.getBeanDefinitionNames())
                .stream()
                .map(name -> bdr.getBeanDefinition(name))
                .map(BeanDefinition::getBeanClassName)
                // this loads some more classes than we need, but it's fine
                .forEach(CLI::loadClass);
    }

    private static void loadClass(String className) {
        try {
            Class.forName(className);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
