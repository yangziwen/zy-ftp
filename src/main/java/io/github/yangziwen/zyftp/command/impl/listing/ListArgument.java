/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.github.yangziwen.zyftp.command.impl.listing;

import java.util.StringTokenizer;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * Contains the parsed argument for a list command (e.g. LIST or NLST)
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class ListArgument {

    private final String file;

    private final String pattern;

    private final char[] options;

    /**
     * @param file
     *            The file path including the directory
     * @param pattern
     *            A regular expression pattern that files must match
     * @param options
     *            List options, such as -la
     */
    public ListArgument(String file, String pattern, char[] options) {
        this.file = file;
        this.pattern = pattern;
        if (options == null) {
            this.options = new char[0];
        } else {
            this.options = options.clone();
        }
    }

    /**
     * The listing options,
     *
     * @return All options
     */
    public char[] getOptions() {
        return options.clone();
    }

    /**
     * The regular expression pattern that files must match
     *
     * @return The regular expression
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Checks if a certain option is set
     *
     * @param option
     *            The option to check
     * @return true if the option is set
     */
    public boolean hasOption(char option) {
        for (int i = 0; i < options.length; i++) {
            if (option == options[i]) {
                return true;
            }
        }

        return false;
    }

    /**
     * The file path including the directory
     *
     * @return The file path
     */
    public String getFile() {
        return file;
    }

    public static ListArgument parse(String argument) {
        String file = "./";
        String options = "";
        String pattern = "*";

        // find options and file name (may have regular expression)
        if (argument != null) {
            argument = argument.trim();
            StringBuilder optionsSb = new StringBuilder(4);
            StringBuilder fileSb = new StringBuilder(16);
            StringTokenizer st = new StringTokenizer(argument, " ", true);
            while (st.hasMoreTokens()) {
                String token = st.nextToken();

                if (fileSb.length() != 0) {
                    // file name started - append to file name buffer
                    fileSb.append(token);
                } else if (token.equals(" ")) {
                    // delimiter and file not started - ignore
                    continue;
                } else if (token.charAt(0) == '-') {
                    // token and file name not started - append to options
                    // buffer
                    if (token.length() > 1) {
                        optionsSb.append(token.substring(1));
                    }
                } else {
                    // filename - append to the filename buffer
                    fileSb.append(token);
                }
            }

            if (fileSb.length() != 0) {
                file = fileSb.toString();
            }
            options = optionsSb.toString();
        }

        int slashIndex = file.lastIndexOf('/');
        if (slashIndex == -1) {
            if (containsPattern(file)) {
                pattern = file;
                file = "./";
            }
        } else if (slashIndex != (file.length() - 1)) {
            String after = file.substring(slashIndex + 1);

            if (containsPattern(after)) {
                pattern = file.substring(slashIndex + 1);
                file = file.substring(0, slashIndex + 1);
            }

            if (containsPattern(file)) {
                throw new IllegalArgumentException(
                        "Directory path can not contain regular expression");
            }
        }

        if ("*".equals(pattern) || "".equals(pattern)) {
            pattern = null;
        }

        return new ListArgument(file, pattern, options.toCharArray());
    }

    private static boolean containsPattern(String file) {
        return file.indexOf('*') > -1 || file.indexOf('?') > -1
                || file.indexOf('[') > -1;

    }

}
