/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.logic.players;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.i18n.TranslationSystem;
import org.terasology.i18n.TranslationSystemImpl;
import org.terasology.input.Keyboard;
import org.terasology.input.binds.general.HideHUDButton;
import org.terasology.input.events.KeyDownEvent;
import org.terasology.input.events.KeyEvent;
import org.terasology.input.events.MouseAxisEvent;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.debug.DebugProperties;
import org.terasology.logic.notifications.NotificationMessageEvent;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.ingame.metrics.DebugOverlay;
import org.terasology.rendering.world.viewDistance.ViewDistance;
import org.terasology.world.WorldProvider;

/**
 */
@RegisterSystem(RegisterMode.CLIENT)
public class DebugControlSystem extends BaseComponentSystem {

    @In
    private WorldProvider world;
    
    @In
    private Config config;
    
    @In
    private Context context;

    @In
    private NUIManager nuiManager;
    
    private DebugOverlay overlay;
    
    private TranslationSystem translationSystem;
    
    private boolean mouseGrabbed = true;

    @Override
    public void initialise() {
        overlay = nuiManager.addOverlay("engine:debugOverlay", DebugOverlay.class);
        translationSystem = new TranslationSystemImpl(context);
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onHideHUD(HideHUDButton event, EntityRef entity) {
        if (event.isDown()) {
            // Make sure both are either visible or hidden
            final boolean hide = !(config.getRendering().getDebug().isHudHidden() && config.getRendering().getDebug().isFirstPersonElementsHidden());

            config.getRendering().getDebug().setFirstPersonElementsHidden(hide);
            config.getRendering().getDebug().setHudHidden(hide);

            event.consume();
        }
    }

    /**
     * Increases view distance upon receiving an increase view distance event.
     * @param button The button or key pressed to increase view distance.
     * @param entity The player entity that triggered the view distance increase.
     */
    @ReceiveEvent(components = ClientComponent.class)
    public void onIncreaseViewDistance(IncreaseViewDistanceButton button, EntityRef entity) {
        int viewDistance = config.getRendering().getViewDistance().getIndex();
        int maxViewDistance = ViewDistance.values().length - 1;
        
        //Ensuring that the view distance does not exceed its maximum value.
        if (viewDistance != maxViewDistance) {
            ViewDistance greaterViewDistance = ViewDistance.forIndex(viewDistance + 1);
            String greaterViewDistanceStr = translationSystem.translate(greaterViewDistance.toString());
            fireChangeEvent("Increasing view distance to " + greaterViewDistanceStr + ".", Arrays.asList(entity));
            //Presenting user with a warning if the view distance is set higher than recommended.
            if (greaterViewDistance == ViewDistance.MEGA || greaterViewDistance == ViewDistance.EXTREME) {
                fireChangeEvent("Warning: Increasing view distance to " + greaterViewDistanceStr
            	                    + " may result in performance issues.", Arrays.asList(entity));
            }
            config.getRendering().setViewDistance(greaterViewDistance);
        }
        button.consume();
    }

    /**
     * Decreases view distance upon receiving a decrease view distance event.
     * @param button The button or key pressed to decrease view distance.
     * @param entity The player entity that triggered the view distance decrease.
     */
    @ReceiveEvent(components = ClientComponent.class)
    public void onDecreaseViewDistance(DecreaseViewDistanceButton button, EntityRef entity) {
        int viewDistance = config.getRendering().getViewDistance().getIndex();
        int minViewDistance = 0;
        
        //Ensuring that the view distance does not fall below its minimum value.
        if (viewDistance != minViewDistance) {
            ViewDistance lesserViewDistance = ViewDistance.forIndex(viewDistance - 1);
            String lesserViewDistanceStr = translationSystem.translate(lesserViewDistance.toString());
            fireChangeEvent("Decreasing view distance to " + lesserViewDistanceStr + ".", Arrays.asList(entity));
            config.getRendering().setViewDistance(lesserViewDistance);
        }
        button.consume();
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onKeyEvent(KeyEvent event, EntityRef entity) {
        boolean debugEnabled = config.getSystem().isDebugEnabled();
        // Features for debug mode only
        if (debugEnabled && event.isDown()) {
            switch (event.getKey().getId()) {
                case Keyboard.KeyId.UP:
                    world.getTime().setDays(world.getTime().getDays() + 0.005f);
                    event.consume();
                    break;
                case Keyboard.KeyId.DOWN:
                    world.getTime().setDays(world.getTime().getDays() - 0.005f);
                    event.consume();
                    break;
                case Keyboard.KeyId.RIGHT:
                    world.getTime().setDays(world.getTime().getDays() + 0.02f);
                    event.consume();
                    break;
                case Keyboard.KeyId.LEFT:
                    world.getTime().setDays(world.getTime().getDays() - 0.02f);
                    event.consume();
                    break;
                default:
                    break;
            }
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onKeyDown(KeyDownEvent event, EntityRef entity) {
        boolean debugEnabled = config.getSystem().isDebugEnabled();
        // Features for debug mode only
        if (debugEnabled) {
            switch (event.getKey().getId()) {
                case Keyboard.KeyId.F6:
                    config.getRendering().getDebug().setEnabled(!config.getRendering().getDebug().isEnabled());
                    event.consume();
                    break;
                case Keyboard.KeyId.F8:
                    config.getRendering().getDebug().setRenderChunkBoundingBoxes(!config.getRendering().getDebug().isRenderChunkBoundingBoxes());
                    event.consume();
                    break;
                case Keyboard.KeyId.F9:
                    config.getRendering().getDebug().setWireframe(!config.getRendering().getDebug().isWireframe());
                    event.consume();
                    break;
                default:
                    break;
            }
        }

        switch (event.getKey().getId()) {
            case Keyboard.KeyId.F2:
                mouseGrabbed = !mouseGrabbed;
                DebugProperties debugProperties = (DebugProperties) nuiManager.getHUD().getHUDElement("engine:DebugProperties");
                debugProperties.setVisible(!mouseGrabbed);
                Mouse.setGrabbed(mouseGrabbed);
                event.consume();
                break;
            case Keyboard.KeyId.F3:
                config.getSystem().setDebugEnabled(!config.getSystem().isDebugEnabled());
                event.consume();
                break;
            case Keyboard.KeyId.F4:
                overlay.toggleMetricsMode();
                event.consume();
                break;
            default:
                break;

        }
    }

    @ReceiveEvent(components = CharacterComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void onMouseX(MouseAxisEvent event, EntityRef entity) {
        if (!mouseGrabbed) {
            event.consume();
        }
    }   
    
    /**
     * Fires notification events upon changes to debug parameters.
     * @param message Notification event message.
     * @param entity  Entities which will receive the notification event.
     */
	private void fireChangeEvent(String message, List<EntityRef> entities) {
    	for (EntityRef client : entities) {
    		client.send(new NotificationMessageEvent(message, client));
    	}
    }

}
