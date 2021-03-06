/*
 * Developed by Benjamin Lefèvre
 * Last modified 29/09/18 21:09
 * Copyright (c) 2018. All rights reserved.
 */

package com.bendk97.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.bendk97.components.GameOverComponent;
import com.bendk97.components.PositionComponent;
import com.bendk97.components.SpriteComponent;
import com.bendk97.components.helpers.ComponentMapperHelper;

import static com.bendk97.SpaceKillerGameConstants.*;
import static com.bendk97.shaders.Shaders.HIGHLIGHT;

public class DynamicEntitiesRenderingSystem extends SortedIteratingSystem {
    private final SpriteBatch batcher;

    public DynamicEntitiesRenderingSystem(SpriteBatch batcher, int priority) {
        super(Family.all(SpriteComponent.class, PositionComponent.class).exclude(GameOverComponent.class).get(),
                (o1, o2) -> Integer.compare(ComponentMapperHelper.sprite.get(o1).zIndex, ComponentMapperHelper.sprite.get(o2).zIndex),
                priority);
        this.batcher = batcher;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PositionComponent position = ComponentMapperHelper.position.get(entity);
        SpriteComponent spriteComponent = ComponentMapperHelper.sprite.get(entity);
        Sprite sprite = spriteComponent.sprite;
        sprite.setPosition(position.x(), position.y());
        if ((sprite.getX() + sprite.getWidth() < -OFFSET_WIDTH)
                || sprite.getX() > SCREEN_WIDTH + OFFSET_WIDTH
                || sprite.getY() > SCREEN_HEIGHT
                || sprite.getY() + sprite.getHeight() < 0) {
            return;
        }
        preBatching(spriteComponent);
        sprite.draw(batcher, spriteComponent.alpha);
        postBatching(spriteComponent);
    }

    private void preBatching(SpriteComponent spriteComponent) {
        if (spriteComponent.flashing && HIGHLIGHT.isCompiled()) {
            batcher.setShader(HIGHLIGHT);
        }
    }

    private void postBatching(SpriteComponent spriteComponent) {
        if (spriteComponent.flashing) {
            batcher.setShader(null);
            spriteComponent.flashing = false;
        }
    }
}
