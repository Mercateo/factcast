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

import static com.mercateo.content.distribution.cli.CLI.addGlobalOptions;
import static com.mercateo.content.distribution.cli.CLI.getActionFromArgs;
import static org.apache.commons.lang.ObjectUtils.notEqual;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.boot.CommandLineRunner;

/**
 * Actions must implement this.
 * <p>
 * They also need to register with the CLI in a static block:
 *
 * <pre>
 *
 * static {
 *     CLI.register(ACTION_NAME, DESCRIPTION);
 * }
 *
 * </pre>
 */
public abstract class AbstractAction implements CommandLineRunner {

    private Options options = new Options();

    protected CommandLine commandLine;

    @Override
    public void run(String... args) throws Exception {

        if (notEqual(getActionFromArgs(), getActionName())) {
            // not for us...
            return;
        }

        initAndParseOptions(args);

        try {
            performAction();

        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

    }

    private void initAndParseOptions(String[] args) throws ParseException {
        try {

            addGlobalOptions(options);
            addLocalOptions(options);

            parseCommandLine(args);

        } catch (MissingOptionException e) {

            HelpFormatter formatter = new HelpFormatter();

            System.out.println("FactCast CLI / " + getActionName());
            System.out.println(getDescription());
            System.out.println();
            System.out.flush();

            formatter.printHelp("fc -a " + getActionName() + " [further options]", options);
            System.out.flush();

            System.err.println("Missing options: " + e.getMissingOptions());
            System.err.println();
            System.err.flush();

            System.exit(1);
        }
    }

    protected abstract void performAction() throws Exception;

    protected abstract String getActionName();

    protected abstract String getDescription();

    private void parseCommandLine(String[] args) throws ParseException {
        addLocalOptions(options);

        commandLine = new DefaultParser()
                .parse(options, args, true);
    }

    protected abstract void addLocalOptions(Options options);

}
