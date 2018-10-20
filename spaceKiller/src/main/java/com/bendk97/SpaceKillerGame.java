/*
 * Developed by Benjamin Lefèvre
 * Last modified 29/09/18 22:06
 * Copyright (c) 2018. All rights reserved.
 */

package com.bendk97;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.bendk97.assets.Assets;
import com.bendk97.google.PlayServices;
import com.bendk97.player.PlayerData;
import com.bendk97.screens.MenuScreen;
import com.bendk97.screens.SplashScreen;
import com.bendk97.screens.levels.Level;
import com.bendk97.screens.levels.LevelScreen;
import com.bendk97.screens.levels.utils.TransitionScreen;
import com.bendk97.share.IntentShare;

import java.io.File;

import static com.bendk97.SpaceKillerGameConstants.SKIP_SPLASH;
import static com.bendk97.screens.SocialScoreScreen.TEMP_DIRECTORY;

public class SpaceKillerGame extends Game {
    private static final String INTENT_FILES = "intent files";
    private static final String WAS_UNABLE_TO_CLEAN_TEMP_DIRECTORY = "was unable to clean temp directory";
    private final Assets assets = new Assets();
    public final PlayServices playServices;
    public Screen currentScreen;
    public PlayerData playerData;
    public final IntentShare intentShare;
    public boolean signInFailed = false;


    public SpaceKillerGame(PlayServices playServices, IntentShare intentShare) {
        this.playServices = playServices;
        this.intentShare = intentShare;
    }

    private void cleanTempDirectory() {
        if (Gdx.files.isExternalStorageAvailable()) {
            final File[] tempDirectoryFiles = Gdx.files.external(TEMP_DIRECTORY).file().listFiles();
            if (tempDirectoryFiles != null) {
                new Thread(() -> {
                    try {
                        for (File file : tempDirectoryFiles) {
                            if (!file.delete()) {
                                Gdx.app.log(INTENT_FILES, WAS_UNABLE_TO_CLEAN_TEMP_DIRECTORY);
                            }
                        }

                    } catch (Exception e) {
                        Gdx.app.log(INTENT_FILES, WAS_UNABLE_TO_CLEAN_TEMP_DIRECTORY);
                    }
                }).start();
            }
        }
    }

    @Override
    public void create() {
        Gdx.input.setCatchBackKey(true);
        cleanTempDirectory();
        if (SKIP_SPLASH) {
            goToScreen(MenuScreen.class);
        } else {
            goToScreen(SplashScreen.class);
        }
    }

    public void goToScreen(Class<? extends Screen> screen) {
        try {
            assets.loadResources(screen);
            //noinspection JavaReflectionMemberAccess
            currentScreen = screen.getConstructor(Assets.class, SpaceKillerGame.class).newInstance(assets, this);
            this.setScreen(currentScreen);
        } catch (Exception e) {
            Gdx.app.log("Guru Meditation", "error: " + e.getMessage());
            Gdx.app.exit();
        } finally {
            Runtime.getRuntime().gc();
        }
    }

    public void goToLevelScreen(Level level) {
        goToLevelScreen(level, null, null);
    }

    public void goToLevelScreen(Level level, PlayerData playerData, Sprite previousScreenSprite) {
        try {
            this.playerData = playerData;
            currentScreen = new LevelScreen(assets, this, level);
            if (previousScreenSprite != null) {
                LevelScreen nextScreen = (LevelScreen) currentScreen;
                Sprite nextScreenSprite = nextScreen.takeScreenshot(Gdx.graphics.getDeltaTime(), Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                this.setScreen(new TransitionScreen(previousScreenSprite, nextScreenSprite, nextScreen, this));
            } else {
                this.setScreen(currentScreen);
            }
        } catch (Exception e) {
            Gdx.app.log("Guru Meditation", "error: " + e.getMessage());
            Gdx.app.exit();
        } finally {
            Runtime.getRuntime().gc();
        }
    }


    @Override
    public void dispose() {
        // Nothing to dispose here ... Really ? huh
    }

    public void signInSucceeded() {
        if (currentScreen instanceof MenuScreen) {
            signInFailed = false;
            ((MenuScreen) currentScreen).signInSucceeded();
        }
    }

    public void signInFailed() {
        if (currentScreen instanceof MenuScreen) {
            signInFailed = true;
            ((MenuScreen) currentScreen).signInFailed();
        }
    }

    public void continueWithExtraLife() {
        if (currentScreen instanceof LevelScreen) {
            ((LevelScreen) currentScreen).continueWithExtraLife();
        }
    }
}
