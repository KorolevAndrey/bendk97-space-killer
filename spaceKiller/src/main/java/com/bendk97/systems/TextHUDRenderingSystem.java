/*
 * Developed by Benjamin Lefèvre
 * Last modified 29/09/18 21:09
 * Copyright (c) 2018. All rights reserved.
 */

package com.bendk97.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.bendk97.assets.Assets;
import com.bendk97.components.BossAlertComponent;
import com.bendk97.components.PlayerComponent;
import com.bendk97.components.helpers.ComponentMapperHelper;

import static com.bendk97.SpaceKillerGameConstants.*;
import static com.bendk97.assets.Assets.FONT_SPACE_KILLER;
import static com.bendk97.assets.Assets.FONT_SPACE_KILLER_LARGE;

public class TextHUDRenderingSystem extends IteratingSystem {
    private static final String SCORE = "SCORE";
    private static final String LIVES = "LIVES";
    private static final String HIGH = "HIGH";
    private static final String BOSS_ALERT = "BOSS\nALERT";
    private final SpriteBatch batcher;
    private final BitmapFont bitmapFont;
    private final BitmapFont bitmapLargeFont;

    public TextHUDRenderingSystem(SpriteBatch batcher, Assets assets, int priority) {
        super(Family.one(PlayerComponent.class).get() ,priority);
        this.batcher = batcher;
        this.bitmapFont = assets.get(FONT_SPACE_KILLER);
        this.bitmapLargeFont = assets.get(FONT_SPACE_KILLER_LARGE);
    }

    @Override
    protected void processEntity(Entity player, float deltaTime) {
        bitmapFont.setColor(Color.WHITE);
        bitmapFont.draw(batcher, SCORE, SCORE_X, SCORE_Y);
        bitmapFont.draw(batcher, ComponentMapperHelper.player.get(player).getScore(), SCORE_X - 10f, SCORE_Y - 20f);
        bitmapFont.draw(batcher, LIVES, LIVES_X, LIVES_Y);
        bitmapFont.draw(batcher, HIGH, HIGH_X, HIGH_Y);
        bitmapFont.draw(batcher, ComponentMapperHelper.player.get(player).getHighScoreFormatted(), HIGH_X - 10f, HIGH_Y - 20f);
        displayEventTexts(player);
    }

    private void displayEventTexts(Entity player) {
        if(player.getComponent(BossAlertComponent.class)!=null) {
            bitmapLargeFont.draw(batcher, BOSS_ALERT, BOSS_ALERT_X, BOSS_ALERT_Y);
        }
    }
}