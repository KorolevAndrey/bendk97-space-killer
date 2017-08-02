package com.benk97.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.benk97.components.*;

public class FollowPlayerSystem extends IteratingSystem {

    private Family player = Family.one(PlayerComponent.class).get();

    public FollowPlayerSystem(int priority) {
        super(Family.all(FollowPlayerComponent.class).get(), priority);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        FollowPlayerComponent followPlayerComponent = Mappers.follow.get(entity);
        followPlayerComponent.lastMove += deltaTime;
        if (followPlayerComponent.lastMove > 0.5) {
            followPlayerComponent.lastMove = 0;
            PositionComponent playerPosition = Mappers.position.get(getEngine().getEntitiesFor(player).first());
            PositionComponent entityPosition = Mappers.position.get(entity);
            VelocityComponent velocityComponent = Mappers.velocity.get(entity);
            float diff = entityPosition.x - playerPosition.x;
            if (Math.abs(diff) < 1) {
                velocityComponent.x = 0;
            } else {
                velocityComponent.x = -Math.signum(diff) * Mappers.follow.get(entity).velocity;
            }
        }
    }

}