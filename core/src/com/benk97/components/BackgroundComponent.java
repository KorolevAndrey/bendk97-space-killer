package com.benk97.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Pool;

import static com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat;

public class BackgroundComponent implements Component, Pool.Poolable {

    public Texture texture;

    public void setTexture(Texture texture) {
        texture.setWrap(Repeat, Repeat);
        this.texture = texture;
    }

    @Override
    public void reset() {

    }
}
