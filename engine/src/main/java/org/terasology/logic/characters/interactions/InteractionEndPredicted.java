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
package org.terasology.logic.characters.interactions;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.module.sandbox.API;

/**
 *
 * Sent to the client by itself at the end of an interaction between a character and a target.
 *
 * THe event is sent to the target entity.
 *
 * This event should not be sent manually by modules: Modules that want to end an interaction should use the utility
 * class {@link InteractionUtil} to do so.
 *
 * When event handler runs, the  predictedInteractionTarget field of the instigator's
 * CharacterComponent will already be updated to the new value.
 *
 * @author Florian <florian@fkoeberle.de>
 */
@API
public class InteractionEndPredicted implements Event {
    private EntityRef instigator;

    protected InteractionEndPredicted() {
    }

    public InteractionEndPredicted(EntityRef instigator) {
        this.instigator = instigator;
    }

    /**
     * @return the character which stopped the interaction.
     */
    public EntityRef getInstigator() {
        return instigator;
    }
}
