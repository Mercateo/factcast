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

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.factcast.core.Fact;
import org.factcast.core.FactCast;
import org.factcast.core.spec.FactSpec;
import org.factcast.core.subscription.SubscriptionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercateo.content.distribution.cli.AbstractAction;
import com.mercateo.content.distribution.cli.CLI;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CatchupAndFollow extends AbstractAction {

    private static final String ACTION_NAME = "catchup-and-follow";

    private static final String DESCRIPTION = "Catchup and then follow future facts from FactCast.";

    static {
        CLI.register(ACTION_NAME);
    }

    @Autowired
    private final FactCast factCast;

    @Override
    protected void performAction() throws Exception {
        FactSpec factSpec = FactSpec.ns(commandLine.getOptionValue("n"));

        SubscriptionRequest request = SubscriptionRequest
                .follow(factSpec)
                .fromScratch();

        System.out.println("Catchup and following new facts:");
        System.out.println();

        factCast
                .subscribeToFacts(request, this::printFact)
                .awaitComplete();
    }

    private void printFact(Fact fact) {
        System.out.println("Fact " + fact.id() + ":");
        System.out.println("Header:");
        System.out.println(fact.jsonHeader());
        System.out.println("Payload:");
        System.out.println(fact.jsonPayload());
        System.out.println();
        System.out.println();
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
                        .builder("n")
                        .longOpt("namespace")
                        .desc("The namespace that should be retrieved.")
                        .hasArg()
                        .required()
                        .build());

    }

}
