package com.benk97.tweens;

import aurelienribon.tweenengine.TweenAccessor;
import com.benk97.components.VelocityComponent;

public class VelocityComponentAccessor implements TweenAccessor<VelocityComponent> {
    public final static int VELOCITY_X = 0;
    public final static int VELOCITY_Y = 1;

    @Override
    public int getValues(VelocityComponent target, int type, float[] returnValues) {
        switch (type) {
            case VELOCITY_X:
                returnValues[0] = target.x;
                return 1;
            case VELOCITY_Y:
                returnValues[0] = target.y;
                return 1;
            default:
                return -1;
        }
    }

    @Override
    public void setValues(VelocityComponent target, int type, float[] newValues) {
        switch (type) {
            case VELOCITY_X:
                target.x = newValues[0];
                break;
            case VELOCITY_Y:
                target.y = newValues[0];
                break;
            default:
                break;
        }
    }
}