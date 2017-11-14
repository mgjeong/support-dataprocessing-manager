/*******************************************************************************
 * Copyright 2017 Samsung Electronics All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package com.sec.processing.framework.spring;

import com.sec.processing.framework.Application;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class ApplicationTest {

    @Rule
    public OutputCapture outputCapture = new OutputCapture();

    private static final String SPRING_STARTUP = "root of context hierarchy";

    @Test
    public void emptyApplicationContext() throws Exception {
        Application.main(getArgs());
        Assert.assertTrue(getOutput().contains(SPRING_STARTUP));
    }

    private String[] getArgs(String... args) {
        List<String> list = new ArrayList<>(Arrays.asList(
//                "--spring.main.webEnvironment=false", "--spring.main.showBanner=OFF",
                "--spring.main.registerShutdownHook=true", "--debug"));
        if (args.length > 0) {
            list.add("--spring.main.sources="
                    + StringUtils.arrayToCommaDelimitedString(args));
        }
        return list.toArray(new String[list.size()]);
    }

    private String getOutput() {
        return this.outputCapture.toString();
    }

}