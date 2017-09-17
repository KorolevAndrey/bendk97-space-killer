package com.bendk97.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.bendk97.components.Mappers;

public class StateSystem extends IteratingSystem {

    public StateSystem(int priority) {
        super(Family.all(com.bendk97.components.StateComponent.class).get(), priority);
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        Mappers.state.get(entity).time += deltaTime;
    }
}