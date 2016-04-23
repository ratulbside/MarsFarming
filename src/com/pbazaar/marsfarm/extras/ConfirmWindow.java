package com.pbazaar.marsfarm.extras;

import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.pbazaar.marsfarm.manager.ResourcesManager;
import com.pbazaar.marsfarm.scenes.GameScene;

public class ConfirmWindow extends Sprite {
	
	GameScene motherScene;
	public ConfirmWindow(GameScene scene, VertexBufferObjectManager pSpriteVertexBufferObject)
	{
		super(0, 0, 650, 400, ResourcesManager.getInstance().complete_window_region, pSpriteVertexBufferObject);
		motherScene = scene;
	}
}
