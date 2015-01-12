package org.terasology.logic.behavior.core;

import org.terasology.logic.behavior.core.compiler.ClassGenerator;
import org.terasology.logic.behavior.core.compiler.MethodGenerator;

/**
 * Created by synopia on 11.01.2015.
 */
public interface AssemblingBehaviorNode {
    void assembleSetup(ClassGenerator gen);

    void assembleTeardown(ClassGenerator gen);

    void assembleConstruct(MethodGenerator gen);

    void assembleExecute(MethodGenerator gen);

    void assembleDestruct(MethodGenerator gen);
}
