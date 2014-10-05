/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior;

import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by synopia on 01.02.14.
 */
public class Doc {
    private static Pattern javadocStart = Pattern.compile("/\\*\\*");
    private static Pattern javadocEnd = Pattern.compile(" \\**/");
    private static Pattern headLine = Pattern.compile("#.*");
    private static Pattern nodePattern = Pattern.compile("### (.*)");
    private static Pattern prefabPattern = Pattern.compile(".*\"description\".*:.*\"(.*)\",");

    public static void main(String[] args) throws IOException {
        final List<Docu> docus = Lists.newArrayList();
        readMarkup("modules/Pathfinding/README.markdown", docus);

        Files.walkFileTree(Paths.get("."), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                for (Docu docu : docus) {
                    if (file.toString().toLowerCase().contains(docu.name().toLowerCase() + "node.java")) {
                        docu.sourceFile = file;
                    } else if (file.toString().toLowerCase().contains(docu.name().toLowerCase() + ".prefab")) {
                        docu.prefabFile = file;
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        for (Docu d : docus) {
            if (d.valid()) {
                writePrefab(d);
                writeJavaDoc(d);
            }
        }
    }

    private static void writeJavaDoc(Docu docu) throws IOException {
        FileReader reader = new FileReader(docu.sourceFile.toFile());
        BufferedReader bufferedReader = new BufferedReader(reader);
        Path tmpPath = Files.createTempFile(docu.name(), "");
        FileWriter tmp = new FileWriter(tmpPath.toFile());
        BufferedWriter tmpWriter = new BufferedWriter(tmp);
        boolean inJavadoc = false;
        boolean written = false;
        while (bufferedReader.ready()) {
            String line = bufferedReader.readLine();
            if (!written && javadocStart.matcher(line).matches()) {
                inJavadoc = true;
                tmpWriter.write(docu.toJavaDoc());
                written = true;
            } else if (inJavadoc && javadocEnd.matcher(line).matches()) {
                inJavadoc = false;
                tmpWriter.append('\n');
            } else if (!inJavadoc) {
                tmpWriter.write(line);
                tmpWriter.append('\n');
            }
        }
        reader.close();
        tmpWriter.close();
        Files.delete(docu.sourceFile);
        Files.move(tmpPath, docu.sourceFile);
    }

    private static void writePrefab(Docu docu) throws IOException {
        FileReader reader = new FileReader(docu.prefabFile.toFile());
        BufferedReader bufferedReader = new BufferedReader(reader);
        Path tmpPath = Files.createTempFile(docu.name(), "");
        FileWriter tmp = new FileWriter(tmpPath.toFile());
        BufferedWriter tmpWriter = new BufferedWriter(tmp);
        while (bufferedReader.ready()) {
            String line = bufferedReader.readLine();
            if (prefabPattern.matcher(line).matches()) {
                tmpWriter.write("        \"description\": \"" + docu.toPrefabDescription() + "\",");
            } else {
                tmpWriter.write(line);
            }
            tmpWriter.append('\n');
        }
        reader.close();
        tmpWriter.close();
        Files.delete(docu.prefabFile);
        Files.move(tmpPath, docu.prefabFile);
    }

    private static void readMarkup(String fileName, List<Docu> docus) throws IOException {
        FileReader reader = new FileReader(fileName);
        BufferedReader bufferedReader = new BufferedReader(reader);
        Docu docu = null;
        while (bufferedReader.ready()) {
            String line = bufferedReader.readLine();
            Matcher matcher = nodePattern.matcher(line);
            if (matcher.matches()) {
                if (docu != null && docu.name() != null) {
                    docus.add(docu);
                }
                docu = new Docu();
                docu.headline = matcher.group(1);
            } else {
                matcher = headLine.matcher(line);
                if (matcher.matches()) {
                    if (docu != null) {
                        if (docu.name() != null) {
                            docus.add(docu);
                        }
                        docu = null;
                    }
                } else {
                    if (docu != null) {
                        docu.lines.add(line);
                    }
                }
            }
        }
    }

    private static class Docu {
        private String headline;
        private Path sourceFile;
        private Path prefabFile;

        private List<String> lines = Lists.newArrayList();

        public boolean valid() {
            return name() != null && sourceFile != null && prefabFile != null;
        }

        public String name() {
            String[] split = this.headline.split("`");
            if (split.length >= 2) {
                return split[1];
            }
            return null;
        }

        public String type() {
            String[] split = this.headline.split("\\*");
            if (split.length >= 2) {
                return split[1];
            }
            return null;
        }

        @Override
        public String toString() {
            String res = headline + "\n";
            for (String line : lines) {
                res += line + "\n";
            }
            return res;
        }

        public String toPrefabDescription() {
            String s = null;
            for (String line : lines) {
                line = line.replace("`", "");
                line = line.replace("*", "");
                if (s == null) {
                    s = (type() != null ? type() + "\\n" : "") + line;
                } else if (line.length() > 0) {
                    s += "\\n" + line;
                }
            }
            return s;
        }

        public String toJavaDoc() {
            String s = "/**";
            for (String line : lines) {
                line = line.replaceAll("`(.*?)`", "<b>$1</b>");
                line = line.replaceAll("\\*(.*?)\\*", "<b>$1</b>");
                s += "\n * " + line + "<br/>";
            }
            s += "\n * Auto generated javadoc - modify README.markdown instead!";
            s += "\n */";
            return s;
        }
    }
}
