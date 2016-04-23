package com.pbazaar.marsfarm.extras;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.pbazaar.marsfarm.manager.ResourcesManager;
import com.pbazaar.marsfarm.scenes.GameScene;

import android.util.Log;


public class LevelCompleteWindow extends Sprite
{
	private TiledSprite star1;
	private TiledSprite star2;
	private TiledSprite star3;
	
	private GameScene motherScene;
	public enum StarsCount
	{
		ONE,
		TWO,
		THREE
	}
	
	public LevelCompleteWindow(GameScene scene, VertexBufferObjectManager pSpriteVertexBufferObject)
	{
		super(0, 0, 650, 400, ResourcesManager.getInstance().complete_window_region, pSpriteVertexBufferObject);
		motherScene = scene;
		attachStars(pSpriteVertexBufferObject);
	}
	
	private void attachStars(VertexBufferObjectManager pSpriteVertexBufferObject)
	{
		star1 = new TiledSprite(150, 150, ResourcesManager.getInstance().complete_stars_region, pSpriteVertexBufferObject);
		motherScene.registerTouchArea(star1);
		star2 = new TiledSprite(325, 150, ResourcesManager.getInstance().complete_stars_region, pSpriteVertexBufferObject);
		star3 = new TiledSprite(500, 150, ResourcesManager.getInstance().complete_stars_region, pSpriteVertexBufferObject);
		
		attachChild(star1);
		attachChild(star2);
		attachChild(star3);
	}
	
	/**
	 * Change star`s tile index, depends on stars count.
	 * @param starsCount
	 */
	public void display(StarsCount starsCount, Scene scene, Camera camera)
	{
		// Change stars tile index, based on stars count (1-3)
		switch (starsCount)
		{
			case ONE:
				star1.setCurrentTileIndex(0);
				star2.setCurrentTileIndex(1);
				star3.setCurrentTileIndex(1);
				break;
			case TWO:
				star1.setCurrentTileIndex(0);
				star2.setCurrentTileIndex(0);
				star3.setCurrentTileIndex(1);
				break;
			case THREE:
				star1.setCurrentTileIndex(0);
				star2.setCurrentTileIndex(0);
				star3.setCurrentTileIndex(0);
				break;
		}
		
		
		// Hide HUD
		camera.getHUD().setVisible(false);
		
		// Disable camera chase entity
		camera.setChaseEntity(null);
		
		// Attach our level complete panel in the middle of camera
		setPosition(camera.getCenterX(), camera.getCenterY());
		scene.attachChild(this);
	}
}