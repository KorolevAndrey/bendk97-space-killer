/*
 * Developed by Benjamin Lefèvre
 * Last modified 01/11/18 14:07
 * Copyright (c) 2018. All rights reserved.
 */

package com.bendk97.screens.levels.scripting;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Linear;
import aurelienribon.tweenengine.equations.Quad;
import box2dLight.ConeLight;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.bendk97.assets.GameAssets;
import com.bendk97.components.helpers.ComponentMapperHelper;
import com.bendk97.entities.EntityFactory;
import com.bendk97.entities.enemies.SquadronFactory;
import com.bendk97.screens.levels.LevelScreen;
import com.bendk97.screens.levels.utils.ScriptItem;
import com.bendk97.screens.levels.utils.ScriptItemBuilder;
import com.bendk97.screens.levels.utils.ScriptItemExecutor;
import com.bendk97.systems.FollowPlayerSystem;
import com.bendk97.tweens.ConeLightTweenAccessor;
import com.bendk97.tweens.VelocityComponentTweenAccessor;

import java.util.LinkedList;
import java.util.List;

import static com.bendk97.SpaceKillerGameConstants.*;
import static com.bendk97.assets.GameAssets.*;
import static com.bendk97.components.helpers.ComponentMapperHelper.gameOver;
import static com.bendk97.entities.EntityFactoryIds.BOSS_LEVEL_2;
import static com.bendk97.screens.levels.Level.Level2;
import static com.bendk97.screens.levels.Level.SoundEffect.GO;

public class Level2Script extends LevelScript {

    private LinkedList<ScriptItem> scriptItemsEasy;
    private LinkedList<ScriptItem> scriptItemsMediumLeft;
    private LinkedList<ScriptItem> scriptItemsMediumRight;
    private LinkedList<ScriptItem> scriptItemsHardLeft;
    private LinkedList<ScriptItem> scriptItemsHardRight;
    private ConeLight coneLight;

    private final Array<Entity> backgrounds = new Array<>();

    public Level2Script(final LevelScreen levelScreen, final GameAssets assets, EntityFactory entityFactory, TweenManager tweenManager, Entity player,
                        PooledEngine engine) {
        super(levelScreen, Level2, assets, entityFactory, tweenManager, player);
        initLevel2(assets, entityFactory, engine);
        if (levelScreen.isFxLightEnabled()) {
            initAmbiantLights(entityFactory);
        }
    }

    private void initAmbiantLights(EntityFactory entityFactory) {
        coneLight = new ConeLight(entityFactory.rayHandler, 10, Color.WHITE, 0,
                -SCREEN_WIDTH / 3, SCREEN_HEIGHT / 2,
                0, 60);
        coneLight.setActive(false);
        Timeline timeline = Timeline.createSequence();
        timeline.push(
                Tween.to(coneLight, ConeLightTweenAccessor.DISTANCE, 10f)
                        .setCallbackTriggers(TweenCallback.START)
                        .setCallback((type, source) -> {
                            if (type == TweenCallback.START) {
                                coneLight.setActive(true);
                            }
                        })
                        .delay(5f)
                        .ease(Linear.INOUT).target(1200f)
                        .delay(3f)
        );
        timeline.push(
                Tween.to(coneLight, ConeLightTweenAccessor.DISTANCE, 5f)
                        .ease(Linear.INOUT)
                        .setCallback((type, source) -> {
                            if (type == TweenCallback.COMPLETE) {
                                coneLight.setActive(false);
                            }
                        })
                        .target(0f)
        );
        timeline.repeat(Tween.INFINITY, 5f).start(tweenManager);
    }

    /*
      for test purposes only
    */
    protected Level2Script(final LevelScreen levelScreen, GameAssets assets, EntityFactory entityFactory, TweenManager tweenManager, Entity player,
                           ScriptItemExecutor scriptItemExecutor, PooledEngine engine) {
        super(levelScreen, Level2, assets, entityFactory, tweenManager, player, scriptItemExecutor);
        initLevel2(assets, entityFactory, engine);
    }

    private void initLevel2(GameAssets assets, EntityFactory entityFactory, PooledEngine engine) {
        backgrounds.add(entityFactory.stageSetEntityFactory.createBackground(assets.get(GFX_BGD_LEVEL2), 0, -500f));
        backgrounds.add(entityFactory.stageSetEntityFactory.createBackground(assets.get(GFX_BGD_STARS_LEVEL2), 1, -300f));
        backgrounds.add(entityFactory.stageSetEntityFactory.createBackground(assets.get(GFX_BGD_BIG_PLANET), 4, -250f));
        backgrounds.add(entityFactory.stageSetEntityFactory.createBackground(assets.get(GFX_BGD_FAR_PLANETS), 2, -275f));
        backgrounds.add(entityFactory.stageSetEntityFactory.createBackground(assets.get(GFX_BGD_RISING_PLANETS), 3, -325f));
        engine.addSystem(new FollowPlayerSystem(2));
    }


    @Override
    public void initSpawns() {
        scriptItemsEasy = new LinkedList<>(randomEasySpawnEnemies(30));
        scriptItemsMediumLeft = new LinkedList<>(randomMediumSpawnEnemiesComingFromLeft(30));
        scriptItemsMediumRight = new LinkedList<>(randomMediumSpawnEnemiesComingFromRight(30));
        scriptItemsHardLeft = new LinkedList<>(randomHardSpawnEnemiesComingFromLeft(30));
        scriptItemsHardRight = new LinkedList<>(randomHardSpawnEnemiesComingFromRight(30));
        boss = new ScriptItemBuilder().typeShip(BOSS_LEVEL_2).typeSquadron(SquadronFactory.BOSS_LEVEL2_MOVE)
                .velocity(100f).number(1).powerUp(false).displayBonus(true).withBonus(15000)
                .bulletVelocity(ENEMY_BULLET_EASY_VELOCITY).createScriptItem();
    }

    @Override
    public void script(int second) {
        if (second < 0) {
            return;
        }
        super.script(second);
        scriptAsteroids(second);
        scriptMists(second);
        scriptFirstSoloEnemies(second);

        if (second < 20) {
            return;
        }
        if (second <= 90) {
            scriptEasyPart(second);
            return;
        }
        if (second <= 160) {
            scriptMediumPart(second);
            return;
        }
        if (second <= 250) {
            scriptDifficultPart(second);
            return;
        }
        if (second >= 255) {
            scriptBoss(second);
        }
    }

    private void scriptAsteroids(int second) {
        if (second % 2 == 0 || second % 9 == 0 || second % 7 == 0) {
            scriptItemExecutor.execute(
                    new ScriptItemBuilder().typeShip(getRandomAsteroidType()).typeSquadron(SquadronFactory.LINEAR_Y).velocity(40f + random.nextFloat() * 260f).number(1).powerUp(random.nextInt() % 4 == 0).displayBonus(false).withBonus(0).bulletVelocity(0f).withParams(random.nextFloat() * (SCREEN_WIDTH - 36f), SCREEN_HEIGHT).createScriptItem());
        }
    }

    private void scriptMists(int second) {
        if (second % 10 == 0) {
            entityFactory.stageSetEntityFactory.createForeground(getRandomMist(), 350f);
        }
    }

    private void scriptFirstSoloEnemies(int second) {
        if (second == 1) {
            entityFactory.enemyEntityFactory.soloEnemyFactory
                    .createSoloEnemy(STATIC_ENEMY_LEVEL2_VELOCITY, STATIC_ENEMY_LEVEL2_BULLET_VELOCITY, STATIC_ENEMY_LEVEL2_RATE_SHOOT, 10, 100);
        }
        if (second == 5) {
            entityFactory.enemyEntityFactory.soloEnemyFactory
                    .createSoloEnemy(STATIC_ENEMY_LEVEL2_VELOCITY, STATIC_ENEMY_LEVEL2_BULLET_VELOCITY, STATIC_ENEMY_LEVEL2_RATE_SHOOT, 10, 100);
        }
        if (second == 9) {
            entityFactory.enemyEntityFactory.soloEnemyFactory
                    .createSoloEnemy(STATIC_ENEMY_LEVEL2_VELOCITY, STATIC_ENEMY_LEVEL2_BULLET_VELOCITY, STATIC_ENEMY_LEVEL2_RATE_SHOOT, 10, 100);
        }
        if (second == 13) {
            entityFactory.enemyEntityFactory.soloEnemyFactory
                    .createSoloEnemy(STATIC_ENEMY_LEVEL2_VELOCITY, STATIC_ENEMY_LEVEL2_BULLET_VELOCITY, STATIC_ENEMY_LEVEL2_RATE_SHOOT, 10, 100);
        }
        if (second == 17) {
            entityFactory.enemyEntityFactory.soloEnemyFactory
                    .createSoloEnemy(STATIC_ENEMY_LEVEL2_VELOCITY, STATIC_ENEMY_LEVEL2_BULLET_VELOCITY, STATIC_ENEMY_LEVEL2_RATE_SHOOT, 10, 100);
        }
    }

    private void scriptEasyPart(int second) {
        if (second % 5 == 0 || second % 7 == 0) {
            executeScriptFromList(scriptItemsEasy);
        }
        if (second == 55) {
            entityFactory.enemyEntityFactory.soloEnemyFactory
                    .createSoloEnemy(STATIC_ENEMY_LEVEL2_VELOCITY, STATIC_ENEMY_LEVEL2_BULLET_VELOCITY, STATIC_ENEMY_LEVEL2_RATE_SHOOT, 10, 100);
        }
    }

    private void scriptMediumPart(int second) {
        if (second == 95) {
            playSound(GO);
        }
        boolean left = random.nextBoolean();
        if (second % 5 == 0 || second % 7 == 0) {
            executeScriptFromList(left ? scriptItemsMediumLeft : scriptItemsMediumRight);
        }
        if (second % 10 == 0) {
            executeScriptFromList(left ? scriptItemsMediumRight : scriptItemsMediumLeft);
        }
        if (second % 30 == 0) {
            entityFactory.enemyEntityFactory.soloEnemyFactory
                    .createSoloEnemy(STATIC_ENEMY_LEVEL2_VELOCITY, STATIC_ENEMY_LEVEL2_BULLET_VELOCITY, STATIC_ENEMY_LEVEL2_RATE_SHOOT, 10, 100);
        }
    }

    private void scriptDifficultPart(int second) {
        if (second == 165) {
            playSound(GO);
        }
        if (second == 190) {
            entityFactory.enemyEntityFactory.soloEnemyFactory
                    .createSoloEnemy(STATIC_ENEMY_LEVEL2_VELOCITY, STATIC_ENEMY_LEVEL2_BULLET_VELOCITY, STATIC_ENEMY_LEVEL2_RATE_SHOOT_WHEN_TWICE, 10, 200, false);
        }
        if (second == 220) {
            entityFactory.enemyEntityFactory.soloEnemyFactory
                    .createSoloEnemy(STATIC_ENEMY_LEVEL2_VELOCITY, STATIC_ENEMY_LEVEL2_BULLET_VELOCITY, STATIC_ENEMY_LEVEL2_RATE_SHOOT_WHEN_TWICE, 10, 200, false);
            entityFactory.enemyEntityFactory.soloEnemyFactory
                    .createSoloEnemy(STATIC_ENEMY_LEVEL2_VELOCITY, STATIC_ENEMY_LEVEL2_BULLET_VELOCITY, STATIC_ENEMY_LEVEL2_RATE_SHOOT_WHEN_TWICE, 10, 200, true);
        }
        boolean left = random.nextBoolean();
        if (second % 5 == 0 || second % 8 == 0) {
            executeScriptFromList(left ? scriptItemsHardLeft : scriptItemsHardRight);
        }
        if (second % 8 == 0) {
            executeScriptFromList(left ? scriptItemsHardRight : scriptItemsHardLeft);
        }
    }

    private void scriptBoss(int second) {
        if (gameOver.get(player) != null) {
            return;
        }
        if (second == 255) {
            bossIsComing();
            for (Entity background : new Array.ArrayIterator<>(backgrounds)) {
                Tween.to(ComponentMapperHelper.velocity.get(background), VelocityComponentTweenAccessor.VELOCITY_Y, 4).ease(Quad.IN)
                        .target(-ComponentMapperHelper.velocity.get(background).y / 10f).start(tweenManager);
            }
            return;
        }
        if (second == 259) {
            bossIsHere();
        }
    }

    @Override
    protected Texture getRandomMist() {
        int randomMist = random.nextInt(8);
        return getMist(randomMist);
    }

    @Override
    protected Texture getMist(int mistType) {
        Texture texture = super.getMist(mistType);
        if (texture == null) {
            texture = assets.get(GFX_BGD_CLOUDS);
        }
        return texture;
    }

    @Override
    public int getRandomShipType() {
        return 1 + random.nextInt(5);
    }

    @Override
    protected int getRandomMoveType() {
        return random.nextInt(9);
    }

    private List<ScriptItem> randomEasySpawnEnemies(int nbSpawns) {
        return randomSpawnEnemies(nbSpawns, ENEMY_LEVEL2_VELOCITY_EASY, STANDARD_RATE_SHOOT, ENEMY_LEVEL2_BULLET_EASY_VELOCITY, BONUS_LEVEL2_SQUADRON_EASY, 5, 6, null);

    }

    private List<ScriptItem> randomMediumSpawnEnemiesComingFromLeft(int nbSpawns) {
        return randomSpawnEnemies(nbSpawns, ENEMY_LEVEL2_VELOCITY_MEDIUM, STANDARD_RATE_SHOOT, ENEMY_LEVEL2_BULLET_MEDIUM_VELOCITY, BONUS_LEVEL2_SQUADRON_MEDIUM, 5, 8, true);

    }

    private List<ScriptItem> randomMediumSpawnEnemiesComingFromRight(int nbSpawns) {
        return randomSpawnEnemies(nbSpawns, ENEMY_LEVEL2_VELOCITY_MEDIUM, STANDARD_RATE_SHOOT, ENEMY_LEVEL2_BULLET_MEDIUM_VELOCITY, BONUS_LEVEL2_SQUADRON_MEDIUM, 5, 10, false);

    }

    private List<ScriptItem> randomHardSpawnEnemiesComingFromLeft(int nbSpawns) {
        return randomSpawnEnemies(nbSpawns, ENEMY_LEVEL2_VELOCITY_HARD, STANDARD_RATE_SHOOT, ENEMY_LEVEL2_BULLET_HARD_VELOCITY, BONUS_LEVEL2_SQUADRON_HARD, 6, 12, true);

    }

    private List<ScriptItem> randomHardSpawnEnemiesComingFromRight(int nbSpawns) {
        return randomSpawnEnemies(nbSpawns, ENEMY_LEVEL2_VELOCITY_HARD, STANDARD_RATE_SHOOT, ENEMY_LEVEL2_BULLET_HARD_VELOCITY, BONUS_LEVEL2_SQUADRON_HARD, 6, 12, false);

    }

    @Override
    public void dispose() {
        if (coneLight != null) {
            coneLight.dispose();
        }
        scriptItemsEasy.clear();
        scriptItemsMediumLeft.clear();
        scriptItemsMediumRight.clear();
        scriptItemsHardLeft.clear();
        scriptItemsHardRight.clear();
    }
}

