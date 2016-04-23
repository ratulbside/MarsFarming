package com.pbazaar.marsfarm.scenes;

import java.io.IOException;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.util.GLState;
import org.andengine.util.SAXUtils;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.level.EntityLoader;
import org.andengine.util.level.constants.LevelConstants;
import org.andengine.util.level.simple.SimpleLevelEntityLoaderData;
import org.andengine.util.level.simple.SimpleLevelLoader;
import org.xml.sax.Attributes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.pbazaar.marsfarm.base.BaseScene;
import com.pbazaar.marsfarm.extras.LevelCompleteWindow;
import com.pbazaar.marsfarm.extras.LevelCompleteWindow.StarsCount;
import com.pbazaar.marsfarm.manager.ResourcesManager;
import com.pbazaar.marsfarm.manager.SceneManager;
import com.pbazaar.marsfarm.manager.SceneManager.SceneType;
import com.pbazaar.marsfarm.object.Player;

/**
 * @author Mateusz Mysliwiec
 * @author www.matim-dev.com
 * @version 1.0
 */
public class GameScene extends BaseScene implements IOnSceneTouchListener
{
	private int score = 0;
	
	private HUD gameHUD;
	private Text scoreText;
	private PhysicsWorld physicsWorld;
	private LevelCompleteWindow levelCompleteWindow;
	
	private static final String TAG_ENTITY = "entity";
	private static final String TAG_ENTITY_ATTRIBUTE_X = "x";
	private static final String TAG_ENTITY_ATTRIBUTE_Y = "y";
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";
	
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1 = "platform1";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2 = "platform2";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM3 = "platform3";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_COIN = "coin";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER = "player";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_LEVEL_COMPLETE = "levelComplete";
	
	private Player player;
	
	private Text gameOverText;
	private Text FarmText;
	private Text WrongItemSelection;
	
	private boolean gameOverDisplayed = false;
	
	private boolean firstTouch = false;
	
	public enum CroppingType {
		CROP_DIRTY,
		CROP_CLEAN,
		CROP_DIGY,
		GROP_SEED,
		CROP_GREEN,
		CROP_DARKGREEN,
		CROP_YELLOW,
		CROP_HARVEST
	}
	
	CroppingType currentCropState;
	
	private Sprite attachjar;	
	private Sprite attachcleaner;
	private Sprite attachseeds;
	private Sprite attachshovel;
	private Sprite attachsickle;
	
	private Sprite selectedSprite;
	private TiledSprite farmArea;
	@Override
	public void createScene()
	{
		createBackground();
		createHUD();
		createPhysics();
		//loadLevel(1);
		createGameOverText();
		
		levelCompleteWindow = new LevelCompleteWindow(this, vbom);		
		
		FarmText = new Text(20, 420, resourcesManager.font, "Please Select Items!", new TextOptions(HorizontalAlign.LEFT), vbom);
		FarmText.setPosition(camera.getCenterX(), camera.getCenterY());
		attachChild(FarmText);
		FarmText.setVisible(false);
		
		WrongItemSelection = new Text(20, 420, resourcesManager.font, "Wrong Item Selected!", new TextOptions(HorizontalAlign.LEFT), vbom);
		WrongItemSelection.setPosition(camera.getCenterX(), camera.getCenterY());
		attachChild(WrongItemSelection);
		WrongItemSelection.setVisible(false);
		
		setOnSceneTouchListener(this); 
		AttachChildButtons();
	}
	
	private void ShowHintText(String text)
	{
		final Text hintText = new Text(20, 420, resourcesManager.font, text, new TextOptions(HorizontalAlign.LEFT), vbom);
		hintText.setPosition(camera.getCenterX(), camera.getCenterY());
		attachChild(hintText);
		engine.registerUpdateHandler(new TimerHandler(1.0f, new ITimerCallback() 
		{
            public void onTimePassed(final TimerHandler pTimerHandler) 
            {
            	hintText.detachSelf();
            	hintText.dispose();
            }
		}));
	}
	
	private void AttachChildButtons()
	{
		currentCropState = CroppingType.CROP_DIRTY;
		attachjar = new Sprite(100, 120, resourcesManager.game_item_jar, vbom) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				// TODO Auto-generated method stub
				if (selectedSprite == null) {
					setScale(1.3f);
					selectedSprite = this;
				}
				if (!selectedSprite.equals(this)) {
					setScale(1.3f);
					selectedSprite.setScale(1.0f);
					selectedSprite = this;
				}
				
				ShowHintText("water");
				
				return false;
			}
		};
		registerTouchArea(attachjar);
		attachChild(attachjar);
		attachcleaner = new Sprite(250, 60, resourcesManager.game_item_cleaner, vbom) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				// TODO Auto-generated method stub
				if (selectedSprite == null) {
					setScale(1.3f);
					selectedSprite = this;
				}
				if (!selectedSprite.equals(this)) {
					setScale(1.3f);
					selectedSprite.setScale(1.0f);
					selectedSprite = this;
				}
				
				ShowHintText("clean");
				
				return false;
			}
		};
		registerTouchArea(attachcleaner);
		attachChild(attachcleaner);
		attachseeds = new Sprite(400, 60, resourcesManager.game_item_seeds, vbom) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				// TODO Auto-generated method stub
				if (selectedSprite == null) {
					setScale(1.3f);
					selectedSprite = this;
				}
				if (!selectedSprite.equals(this)) {
					setScale(1.3f);
					selectedSprite.setScale(1.0f);
					selectedSprite = this;
				}
				
				ShowHintText("spread");
				return false;
			}
		};
		registerTouchArea(attachseeds);
		attachChild(attachseeds);
		attachshovel = new Sprite(550, 60, resourcesManager.game_item_shovel, vbom) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				// TODO Auto-generated method stub
				if (selectedSprite == null) {
					setScale(1.3f);
					selectedSprite = this;
				}
				if (!selectedSprite.equals(this)) {
					setScale(1.3f);
					selectedSprite.setScale(1.0f);
					selectedSprite = this;
				}
				
				ShowHintText("dig");
				return false;
			}
		};
		registerTouchArea(attachshovel);
		attachChild(attachshovel);
		attachsickle = new Sprite(700, 120, resourcesManager.game_item_sickle, vbom) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				// TODO Auto-generated method stub
				if (selectedSprite == null) {
					setScale(1.3f);
					selectedSprite = this;
				}
				if (!selectedSprite.equals(this)) {
					setScale(1.3f);
					selectedSprite.setScale(1.0f);
					selectedSprite = this;
				}				
				return false;
			}
		};
		registerTouchArea(attachsickle);
		attachChild(attachsickle);
		
		farmArea = new TiledSprite(400, 200, ResourcesManager.getInstance().game_farm_region, vbom) {
			int currentindex = 0;
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				// TODO Auto-generated method stub
				
				if (pSceneTouchEvent.isActionDown()) {
					if (selectedSprite == null) {
						showFarmText();
						return true;
					}
					switch (currentCropState) {
					case CROP_DIRTY:
						if (selectedSprite.equals(attachcleaner)) {
							currentindex ++;
							currentCropState = CroppingType.CROP_CLEAN;
						}							
						else {
							showWrongItemText();
							return true;
						}
						break;
					case CROP_CLEAN:
						if (selectedSprite.equals(attachshovel)) {
							currentindex ++;
							currentCropState = CroppingType.CROP_DIGY;
						}							
						else {
							showWrongItemText();
							return true;
						}
						break;
					case CROP_DIGY:
						if (selectedSprite.equals(attachseeds)) {
							currentindex ++;
							currentCropState = CroppingType.GROP_SEED;
						}							
						else {
							showWrongItemText();
							return true;
						}
						break;
					case GROP_SEED:
						if (selectedSprite.equals(attachjar)) {
							currentindex ++;
							currentCropState = CroppingType.CROP_GREEN;
						}							
						else {
							showWrongItemText();
							return true;
						}
						break;
					case CROP_GREEN:
						if (selectedSprite.equals(attachjar)) {
							currentindex ++;
							currentCropState = CroppingType.CROP_DARKGREEN;
						}							
						else {
							showWrongItemText();
							return true;
						}
						break;
					case CROP_DARKGREEN:
						if (selectedSprite.equals(attachjar)) {
							currentindex ++;
							currentCropState = CroppingType.CROP_YELLOW;
						}							
						else {
							showWrongItemText();
							return true;
						}
						break;
					case CROP_YELLOW:
						if (selectedSprite.equals(attachsickle)) {
							currentindex ++;
							currentCropState = CroppingType.CROP_HARVEST;
						}							
						else {
							showWrongItemText();
							return true;
						}
						break;
					case CROP_HARVEST:
						levelCompleteWindow.display(StarsCount.TWO, GameScene.this, camera);
						this.setVisible(false);
						this.setIgnoreUpdate(true);
						return true;
					default:
						break;
					}
					if (currentindex > 7)
						currentindex = 0;
					addToScore(10);
					setCurrentTileIndex(currentindex);
				}
				
				return true;
			}
		};
		farmArea.setScale(2.0f);
		farmArea.setCurrentTileIndex(0);
		
		registerTouchArea(farmArea);
		attachChild(farmArea);
		
	}
	
	@Override
	public void onBackKeyPressed()
	{
		SceneManager.getInstance().loadMenuScene(engine);
	}

	@Override
	public SceneType getSceneType()
	{
		return SceneType.SCENE_GAME;
	}

	private void showFarmText() {
		FarmText.setVisible(true);
		engine.registerUpdateHandler(new TimerHandler(1.0f, new ITimerCallback() 
		{
            public void onTimePassed(final TimerHandler pTimerHandler) 
            {
            	FarmText.setVisible(false);
            }
		}));
	}
	
	private void showWrongItemText() {
		WrongItemSelection.setVisible(true);
		engine.registerUpdateHandler(new TimerHandler(1.0f, new ITimerCallback() 
		{
            public void onTimePassed(final TimerHandler pTimerHandler) 
            {
            	WrongItemSelection.setVisible(false);
            }
		}));
	}
	
	@Override
	public void disposeScene()
	{
		camera.setHUD(null);
		camera.setChaseEntity(null); //TODO
		camera.setCenter(400, 240);
		
		// TODO code responsible for disposing scene
		// removing all game scene objects.
	}
	
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent)
	{
		if (pSceneTouchEvent.isActionDown())
		{
			/*if (!firstTouch)
			{
				player.setRunning();
				firstTouch = true;
			}
			else
			{
				player.jump();
			}*/
		}
		return false;
	}
	
	private void loadLevel(int levelID)
	{
		final SimpleLevelLoader levelLoader = new SimpleLevelLoader(vbom);
		
		final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(0, 0.01f, 0.5f);
		
		levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(LevelConstants.TAG_LEVEL)
		{
			public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException 
			{
				final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
				final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
				
				camera.setBounds(0, 0, width, height); // here we set camera bounds
				camera.setBoundsEnabled(true);

				return GameScene.this;
			}
		});
		
		levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(TAG_ENTITY)
		{
			public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException
			{
				final int x = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_X);
				final int y = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_Y);
				final String type = SAXUtils.getAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_TYPE);
				
				final Sprite levelObject;
				
				if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1))
				{
					levelObject = new Sprite(x, y, resourcesManager.platform1_region, vbom);
					PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("platform1");
				} 
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2))
				{
					levelObject = new Sprite(x, y, resourcesManager.platform2_region, vbom);
					final Body body = PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF);
					body.setUserData("platform2");
					physicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
				}
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM3))
				{
					levelObject = new Sprite(x, y, resourcesManager.platform3_region, vbom);
					final Body body = PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF);
					body.setUserData("platform3");
					physicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
				}
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_COIN))
				{
					levelObject = new Sprite(x, y, resourcesManager.coin_region, vbom)
					{
						@Override
						protected void onManagedUpdate(float pSecondsElapsed) 
						{
							super.onManagedUpdate(pSecondsElapsed);

							if (player.collidesWith(this))
							{
								addToScore(10);
								this.setVisible(false);
								this.setIgnoreUpdate(true);
							}
						}
					};
					levelObject.registerEntityModifier(new LoopEntityModifier(new ScaleModifier(1, 1, 1.3f)));
				}	
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER))
				{
					player = new Player(x, y, vbom, camera, physicsWorld)
					{
						@Override
						public void onDie()
						{
							if (!gameOverDisplayed)
							{
								displayGameOverText();
							}
						}
					};
					levelObject = player;
				}
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_LEVEL_COMPLETE))
				{
					levelObject = new Sprite(x, y, resourcesManager.complete_stars_region, vbom)
					{
						@Override
						protected void onManagedUpdate(float pSecondsElapsed) 
						{
							super.onManagedUpdate(pSecondsElapsed);

							if (player.collidesWith(this))
							{
								levelCompleteWindow.display(StarsCount.TWO, GameScene.this, camera);
								this.setVisible(false);
								this.setIgnoreUpdate(true);
							}
						}
					};
					levelObject.registerEntityModifier(new LoopEntityModifier(new ScaleModifier(1, 1, 1.3f)));
				}	
				else
				{
					throw new IllegalArgumentException();
				}

				levelObject.setCullingEnabled(true);

				return levelObject;
			}
		});

		levelLoader.loadLevelFromAsset(activity.getAssets(), "level/" + levelID + ".lvl");
	}
	
	private void createGameOverText()
	{
		gameOverText = new Text(0, 0, resourcesManager.font, "Game Over!", vbom);
	}
	
	private void displayGameOverText()
	{
		camera.setChaseEntity(null);
		gameOverText.setPosition(camera.getCenterX(), camera.getCenterY());
		attachChild(gameOverText);
		gameOverDisplayed = true;
	}
	
	private void createHUD()
	{
		gameHUD = new HUD();
		
		scoreText = new Text(20, 420, resourcesManager.font, "Score: 0123456789", new TextOptions(HorizontalAlign.LEFT), vbom);
		scoreText.setAnchorCenter(0, 0);	
		scoreText.setText("Score: 0");
		gameHUD.attachChild(scoreText);
		
		camera.setHUD(gameHUD);
	}
	
	private void createBackground()
	{
		attachChild(new Sprite(400, 240, resourcesManager.game_background_region, vbom)
		{
    		@Override
            protected void preDraw(GLState pGLState, Camera pCamera) 
    		{
                super.preDraw(pGLState, pCamera);
                pGLState.enableDither();
            }
		});
	}
	
	private void addToScore(int i)
	{
		score += i;
		scoreText.setText("Score: " + score);
	}
	
	private void createPhysics()
	{
		physicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, -17), false); 
		physicsWorld.setContactListener(contactListener());
		registerUpdateHandler(physicsWorld);
	}
	
	// ---------------------------------------------
	// INTERNAL CLASSES
	// ---------------------------------------------

	private ContactListener contactListener()
	{
		ContactListener contactListener = new ContactListener()
		{
			public void beginContact(Contact contact)
			{
				final Fixture x1 = contact.getFixtureA();
				final Fixture x2 = contact.getFixtureB();

				if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null)
				{
					if (x2.getBody().getUserData().equals("player"))
					{
						player.increaseFootContacts();
					}
					
					if (x1.getBody().getUserData().equals("platform2") && x2.getBody().getUserData().equals("player"))
					{
						engine.registerUpdateHandler(new TimerHandler(0.2f, new ITimerCallback()
						{									
						    public void onTimePassed(final TimerHandler pTimerHandler)
						    {
						    	pTimerHandler.reset();
						    	engine.unregisterUpdateHandler(pTimerHandler);
						    	x1.getBody().setType(BodyType.DynamicBody);
						    }
						}));
					}
					
					if (x1.getBody().getUserData().equals("platform3") && x2.getBody().getUserData().equals("player"))
					{
						x1.getBody().setType(BodyType.DynamicBody);
					}
				}
			}

			public void endContact(Contact contact)
			{
				final Fixture x1 = contact.getFixtureA();
				final Fixture x2 = contact.getFixtureB();

				if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null)
				{
					if (x2.getBody().getUserData().equals("player"))
					{
						player.decreaseFootContacts();
					}
				}
			}

			public void preSolve(Contact contact, Manifold oldManifold)
			{

			}

			public void postSolve(Contact contact, ContactImpulse impulse)
			{

			}
		};
		return contactListener;
	}
}