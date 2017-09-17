package com.bendk97.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.bendk97.components.SpriteComponent;

public class StaticEntitiesRenderingSystem extends IteratingSystem {
    private SpriteBatch batcher;

    public StaticEntitiesRenderingSystem(SpriteBatch batcher, int priority) {
        super(Family.all(SpriteComponent.class).exclude(com.bendk97.components.PositionComponent.class).get(), priority);
        this.batcher = batcher;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        SpriteComponent staticSpriteComponent = com.bendk97.components.Mappers.sprite.get(entity);
        staticSpriteComponent.sprite.draw(batcher, staticSpriteComponent.alpha);
    }
}