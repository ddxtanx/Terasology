
package org.terasology.config;

import org.terasology.config.ControllerConfig.ControllerInfo;
import org.terasology.context.Context;

public class InputDeviceConfigImpl implements InputDeviceConfig {
    private Config config;

    public InputDeviceConfigImpl(Config config) {
        this.config = config;
    }

    @Override
    public float getMouseSensitivity() {
        return config.getInput().getMouseSensitivity();
    }

    @Override
    public void setMouseSensitivity(float mouseSensitivity) {
        config.getInput().setMouseSensitivity(mouseSensitivity);
    }

    @Override
    public void reset(Context context) {
        config.getInput().reset(context);
    }

    @Override
    public boolean isMouseYAxisInverted() {
        return config.getInput().isMouseYAxisInverted();
    }

    @Override
    public void setMouseYAxisInverted(boolean mouseYAxisInverted) {
        config.getInput().setMouseYAxisInverted(mouseYAxisInverted);
    }

    @Override
    public ControllerInfo getController(String name) {
        return config.getInput().getControllers().getController(name);
    }

}
