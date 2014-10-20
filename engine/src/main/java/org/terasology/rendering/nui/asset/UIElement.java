/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui.asset;

import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;
import org.terasology.module.sandbox.API;
import org.terasology.rendering.nui.UIWidget;

/**
 * @author Immortius
 */
@API
public class UIElement extends AbstractAsset<UIData> {

    private UIWidget rootWidget;

    public UIElement(AssetUri uri, UIData data) {
        super(uri);
        onReload(data);
    }

    @Override
    protected void onReload(UIData data) {
        rootWidget = data.getRootWidget();
    }

    @Override
    protected void onDispose() {
        rootWidget = null;
    }

    public UIWidget getRootWidget() {
        return rootWidget;
    }
}
