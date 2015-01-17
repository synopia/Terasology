/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import org.terasology.entitySystem.Component;
import org.terasology.logic.behavior.core.ActionNode;
import org.terasology.logic.behavior.core.BehaviorNode;
import org.terasology.logic.behavior.core.DecoratorNode;
import org.terasology.logic.behavior.core.DynamicSelectorNode;
import org.terasology.logic.behavior.core.FailureNode;
import org.terasology.logic.behavior.core.ParallelNode;
import org.terasology.logic.behavior.core.RunningNode;
import org.terasology.logic.behavior.core.SelectorNode;
import org.terasology.logic.behavior.core.SequenceNode;
import org.terasology.logic.behavior.core.SuccessNode;
import org.terasology.rendering.nui.Color;

/**
 * Defines a renderable node used to display behavior trees.
 *
 * @author synopia
 */
public class BehaviorNodeComponent implements Component {
    public static final BehaviorNodeComponent DEFAULT = new BehaviorNodeComponent();

    public String action;
    public String name;
    public String displayName;
    public String category;                     // for palette
    public String shape = "diamond";            // diamond or rect
    public Color color = Color.GREY;
    public Color textColor = Color.BLACK;
    public String description = "";

    @Override
    public String toString() {
        return name;
    }
}
