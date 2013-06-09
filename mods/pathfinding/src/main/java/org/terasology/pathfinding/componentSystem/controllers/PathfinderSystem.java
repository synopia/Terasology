package org.terasology.pathfinding.componentSystem.controllers;

import org.terasology.componentSystem.RenderSystem;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.pathfinding.components.PathfinderComponent;

/**
 * @author synopia
 */
@RegisterComponentSystem(headedOnly = true)
public class PathfinderSystem implements UpdateSubscriberSystem, RenderSystem {
    @In
    private EntityManager entityManager;

    @Override
    public void renderOpaque() {

    }

    @Override
    public void renderTransparent() {
    }

    @Override
    public void renderOverlay() {
        for (EntityRef ref : entityManager.iteratorEntities(PathfinderComponent.class)) {
            PathfinderComponent pathfinder = ref.getComponent(PathfinderComponent.class);
            pathfinder.grid.render();
        }
    }

    @Override
    public void renderFirstPerson() {

    }

    @Override
    public void renderShadows() {

    }

    @Override
    public void update(float delta) {

    }

    @Override
    public void initialise() {

    }

    @Override
    public void shutdown() {

    }
}
