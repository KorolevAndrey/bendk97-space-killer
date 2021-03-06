/*
 * Developed by Benjamin Lefèvre
 * Last modified 29/09/18 21:09
 * Copyright (c) 2018. All rights reserved.
 */

package com.bendk97.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.RandomXS128;
import com.bendk97.components.BossComponent;
import com.bendk97.components.PauseComponent;
import com.bendk97.components.PlayerComponent;
import com.bendk97.components.helpers.ComponentMapperHelper;
import com.bendk97.entities.EntityFactory;

import java.util.Random;

public class BossAttackSystem extends IteratingSystem {

    private final EntityFactory entityFactory;
    private final Family player = Family.one(PlayerComponent.class).exclude(PauseComponent.class).get();
    private final Random random = new RandomXS128();

    public BossAttackSystem(int priority, EntityFactory entityFactory) {
        super(Family.all(BossComponent.class).get(), priority);
        this.entityFactory = entityFactory;
    }


    @Override
    protected void processEntity(final Entity entity, float deltaTime) {
        ImmutableArray<Entity> playerEntity = getEngine().getEntitiesFor(player);
        if (playerEntity.size() == 0) {
            return;
        }
        final BossComponent boss = ComponentMapperHelper.boss.get(entity);
        if (boss.pleaseFire1) {
            if (ComponentMapperHelper.enemy.get(entity).isDead()) {
                return;
            }
            entityFactory.enemyActionEntityFactory.createBossFire(entity, playerEntity.first());
            boss.pleaseFire1 = false;
            com.bendk97.timer.PausableTimer.schedule(new com.bendk97.timer.PausableTimer.Task() {
                @Override
                public void run() {
                    if (ComponentMapperHelper.enemy.get(entity) != null && !ComponentMapperHelper.enemy.get(entity).isDead()) {
                        boss.pleaseFire1 = true;
                    }
                }
            }, boss.minTriggerFire1 + random.nextFloat() * 2f);
        }
        if (ComponentMapperHelper.boss.get(entity).pleaseFire2) {
            if (ComponentMapperHelper.enemy.get(entity).isDead()) {
                return;
            }
            entityFactory.enemyActionEntityFactory.createBossFire2(entity);
            boss.pleaseFire2 = false;
            com.bendk97.timer.PausableTimer.schedule(new com.bendk97.timer.PausableTimer.Task() {
                @Override
                public void run() {
                    if (!ComponentMapperHelper.enemy.get(entity).isDead()) {
                        boss.pleaseFire2 = true;
                    }
                }
            }, boss.minTriggerFire2 + random.nextFloat() * 2f);
        }
    }
}
