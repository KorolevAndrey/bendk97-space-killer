/*
 * Developed by Benjamin Lefèvre
 * Last modified 29/09/18 21:09
 * Copyright (c) 2018. All rights reserved.
 */

package com.bendk97.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.RandomXS128;
import com.bendk97.components.SquadronComponent;
import com.bendk97.components.helpers.ComponentMapperHelper;
import com.bendk97.entities.EntityFactory;
import com.bendk97.listeners.PlayerListener;
import com.bendk97.screens.levels.Level;
import com.bendk97.timer.PausableTimer;

import java.util.Random;

import static com.bendk97.screens.levels.Level.Level3;

public class SquadronSystem extends IteratingSystem {

    private static final int THRESHOLD_POWER_UP_DEFAULT = 17;
    private static final int THRESHOLD_SHIELD_DEFAULT = 21;
    private int threshold_power_up = THRESHOLD_POWER_UP_DEFAULT;
    private int threshold_shield = THRESHOLD_SHIELD_DEFAULT;

    private final EntityFactory entityFactory;
    private final Entity player;
    private final PlayerListener playerListener;
    private final Random random = new RandomXS128();

    public SquadronSystem(Level level, int priority, EntityFactory entityFactory, Entity player, PlayerListener playerListener) {
        super(Family.all(SquadronComponent.class).get(), priority);
        this.entityFactory = entityFactory;
        this.player = player;
        this.playerListener = playerListener;
        if (level.equals(Level3)) {
            threshold_power_up = 14;
            threshold_shield = 20;
        }
    }

    @Override
    public void processEntity(final Entity entity, float deltaTime) {
        SquadronComponent squadron = ComponentMapperHelper.squadron.get(entity);
        if (squadron.ships.size == 0) {
            getEngine().removeEntity(entity);
            if (squadron.toShoot == 0) {
                if (squadron.powerUpAfterDestruction) {
                    int type = random.nextInt(22);
                    if (type < threshold_power_up) {
                        entityFactory.bonusEntityFactory.createPowerUp(entity);
                    } else if (type < threshold_shield) {
                        entityFactory.bonusEntityFactory.createShieldUp(entity);
                    } else {
                        entityFactory.bonusEntityFactory.createBombUp(entity);
                    }
                }
                if (squadron.displayBonusSquadron) {
                    playerListener.updateScore(player, squadron.scoreBonus);
                    final Entity score = entityFactory.enemyEntityFactory.createScoreSquadron(entity);
                    PausableTimer.schedule(new PausableTimer.Task() {
                        @Override
                        public void run() {
                            getEngine().removeEntity(score);
                        }
                    }, 1.5f);
                }
            }
        }
    }
}