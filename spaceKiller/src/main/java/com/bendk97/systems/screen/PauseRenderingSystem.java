/*
 * Developed by Benjamin Lefèvre
 * Last modified 07/10/18 18:34
 * Copyright (c) 2018. All rights reserved.
 */

package com.bendk97.systems.screen;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.bendk97.assets.Assets;
import com.bendk97.components.PauseComponent;

import static com.bendk97.SpaceKillerGameConstants.SCREEN_HEIGHT;

public class PauseRenderingSystem extends GLDarkRenderingSystem {
    private static final String RESUME = "resume";
    private static final String QUIT = "quit";
    private final BitmapFont mediumFont;


    public PauseRenderingSystem(SpriteBatch batcher, Camera camera, Assets assets, int priority) {
        super(Family.all(PauseComponent.class).get(), batcher, camera, priority);
        this.mediumFont = assets.get(Assets.FONT_SPACE_KILLER_MEDIUM);
        this.mediumFont.setColor(Color.WHITE);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        super.processEntity(entity, deltaTime);
        mediumFont.draw(batcher, RESUME, 50f, SCREEN_HEIGHT * 2f / 3f);
        mediumFont.draw(batcher, QUIT, 100f, SCREEN_HEIGHT * 1f / 2f);
    }
}