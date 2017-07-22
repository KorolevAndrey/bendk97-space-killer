package com.benk97.listeners.impl;

import aurelienribon.tweenengine.*;
import aurelienribon.tweenengine.equations.Linear;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.utils.Timer;
import com.benk97.assets.Assets;
import com.benk97.components.*;
import com.benk97.entities.EntityFactory;
import com.benk97.listeners.CollisionListener;
import com.benk97.listeners.PlayerListener;
import com.benk97.screens.Level1Screen;

import static com.benk97.SpaceKillerGameConstants.SCREEN_HEIGHT;
import static com.benk97.SpaceKillerGameConstants.SCREEN_WIDTH;
import static com.benk97.assets.Assets.*;
import static com.benk97.tweens.PositionComponentAccessor.POSITION_XY;
import static com.benk97.tweens.SpriteComponentAccessor.ALPHA;

public class CollisionListenerImpl extends EntitySystem implements CollisionListener {

    private Assets assets;
    private EntityFactory entityFactory;
    private PlayerListener playerListener;
    private TweenManager tweenManager;
    private Level1Screen screen;

    public CollisionListenerImpl(TweenManager tweenManager, Assets assets, EntityFactory entityFactory, PlayerListener playerListener,
                                 Level1Screen screen) {
        this.playerListener = playerListener;
        this.assets = assets;
        this.entityFactory = entityFactory;
        this.tweenManager = tweenManager;
        this.screen = screen;
    }

    @Override
    public void enemyShoot(final Entity enemy, final Entity player, Entity bullet) {
        EnemyComponent enemyComponent = Mappers.enemy.get(enemy);
        // create explosion
        assets.playSound(SOUND_EXPLOSION);
        PositionComponent explosePosition = enemyComponent.isBoss ? Mappers.position.get(bullet) : Mappers.position.get(enemy);
        entityFactory.createEntityExploding(explosePosition.x, explosePosition.y);
        getEngine().removeEntity(bullet);
        // update score
        playerListener.updateScore(player, enemy);
        // check health of ennemy
        enemyComponent.hit();
        if (enemyComponent.isDead()) {
            Mappers.player.get(player).enemiesKilled++;
            screen.checkAchievements(player);
            tweenManager.killTarget(Mappers.position.get(enemy));
            Mappers.squadron.get(Mappers.enemy.get(enemy).squadron).removeEntity(enemy);
            SpriteComponent spriteComponent = Mappers.sprite.get(enemy);
            if (Mappers.boss.get(enemy) != null) {
                assets.playSound(SOUND_BOSS_FINISHED);
                entityFactory.createBossExploding(enemy);
                Timeline.createSequence()
                        .beginParallel()
                        .push(Tween.to(Mappers.position.get(enemy), POSITION_XY, 5f).ease(Linear.INOUT)
                                .target(SCREEN_WIDTH / 2f - spriteComponent.sprite.getWidth() / 2f,
                                        SCREEN_HEIGHT - spriteComponent.sprite.getHeight() - 20f))

                        .push(Tween.to(Mappers.sprite.get(enemy), ALPHA, 0.2f).ease(Linear.INOUT)
                                .target(0.2f).repeatYoyo(25, 0f))
                        .end()
                        .setCallback(new TweenCallback() {
                            @Override
                            public void onEvent(int i, BaseTween<?> baseTween) {
                                if (i == TweenCallback.COMPLETE) {
                                    getEngine().removeEntity(enemy);
                                }
                                new Timer().scheduleTask(new Timer.Task() {
                                    @Override
                                    public void run() {
                                        player.add(((PooledEngine) getEngine()).createComponent(LeveLFinishedComponent.class));
                                        new Timer().scheduleTask(new Timer.Task() {
                                            @Override
                                            public void run() {
                                                player.remove(LeveLFinishedComponent.class);
                                                screen.restartLevel1();
                                            }
                                        }, 5f);
                                    }
                                }, 2f);
                            }
                        })
                        .start(tweenManager);
            } else {
                getEngine().removeEntity(enemy);
            }
        }
    }

    @Override
    public void playerHitByEnnemyBody(Entity player, Entity ennemy) {
        assets.playSound(SOUND_EXPLOSION);
        PositionComponent playerPosition = Mappers.position.get(player);
        entityFactory.createEntityExploding(playerPosition.x, playerPosition.y);
        playerListener.loseLive(player);
    }

    @Override
    public void playerHitByEnnemyBullet(Entity player, Entity bullet) {
        assets.playSound(SOUND_EXPLOSION);
        PositionComponent playerPosition = Mappers.position.get(player);
        entityFactory.createEntityExploding(playerPosition.x, playerPosition.y);
        getEngine().removeEntity(bullet);
        playerListener.loseLive(player);
    }

    @Override
    public void playerPowerUp(Entity player, Entity powerUp) {
        assets.playSound(SOUND_POWER_UP);
        assets.playSound(SOUND_POWER_UP_VOICE);
        PlayerComponent playerComponent = Mappers.player.get(player);
        playerComponent.powerUp();
        tweenManager.killTarget(Mappers.position.get(powerUp));
        tweenManager.killTarget(Mappers.sprite.get(powerUp));
        getEngine().removeEntity(powerUp);
    }


    @Override
    public void playerShieldUp(Entity player, Entity shieldUp) {
        assets.playSound(SOUND_SHIELD_UP);
        entityFactory.createShield(player);
        tweenManager.killTarget(Mappers.position.get(shieldUp));
        tweenManager.killTarget(Mappers.sprite.get(shieldUp));
        getEngine().removeEntity(shieldUp);
    }

    @Override
    public void bulletStoppedByShield(Entity bullet) {
        assets.playSound(SOUND_SHIELD_BULLET);
        getEngine().removeEntity(bullet);
    }

    @Override
    public void enemyShootByShield(Entity enemy, Entity shield) {
        assets.playSound(SOUND_EXPLOSION);
        PositionComponent ennemyPosition = Mappers.position.get(enemy);
        entityFactory.createEntityExploding(ennemyPosition.x, ennemyPosition.y);
        tweenManager.killTarget(Mappers.position.get(enemy));
        Mappers.squadron.get(Mappers.enemy.get(enemy).squadron).removeEntity(enemy);
        getEngine().removeEntity(enemy);
    }

}