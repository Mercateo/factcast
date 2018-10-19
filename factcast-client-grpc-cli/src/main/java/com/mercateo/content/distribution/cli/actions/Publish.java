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
package com.mercateo.content.distribution.cli.actions;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.factcast.core.Fact;
import org.factcast.core.FactCast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.io.Files;
import com.mercateo.content.distribution.cli.AbstractAction;
import com.mercateo.content.distribution.cli.CLI;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class Publish extends AbstractAction {

    private static final String ACTION_NAME = "publish";

    private static final String DESCRIPTION = "Publish facts to FactCast.";

    static {
        CLI.register(ACTION_NAME);
    }

    @Autowired
    private final FactCast factCast;

    @Override
    protected void performAction() throws Exception {

        Fact fact = createFact();

        factCast.publish(fact);

        System.out.println("Successfully published fact with id " + fact.id());
    }

    private Fact createFact() throws IOException {
        String headerFilename = commandLine.getOptionValue("h");
        String payloadFilename = commandLine.getOptionValue("p");

        String header = String.join("", Files.readLines(new File(headerFilename), UTF_8));
        String payload = String.join("", Files.readLines(new File(payloadFilename), UTF_8));

        return Fact.of(header, payload);
    }

    @Override
    protected String getActionName() {
        return ACTION_NAME;
    }

    @Override
    protected String getDescription() {
        return DESCRIPTION;
    }

    @Override
    protected void addLocalOptions(Options options) {

        options.addOption(
                Option
                        .builder("h")
                        .longOpt("header")
                        .desc("File with header section of the event to be published. File should be "
                                +
                                "stored in UTF-8 as JSON document.")
                        .numberOfArgs(1)
                        .required()
                        .build());

        options.addOption(
                Option
                        .builder("p")
                        .longOpt("payload")
                        .desc("File with payload of the event to be published. File should be stored in "
                                +
                                "UTF-8 as JSON document.")
                        .numberOfArgs(1)
                        .required()
                        .build());
    }

}
