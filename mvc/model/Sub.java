package edu.uchicago.gerber._08final.mvc.model;

import edu.uchicago.gerber._08final.mvc.controller.CommandCenter;
import edu.uchicago.gerber._08final.mvc.controller.Game;
import edu.uchicago.gerber._08final.mvc.controller.Sound;
import lombok.Data;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Data
public class Sub extends Sprite {

	// ==============================================================
	// FIELDS 
	// ==============================================================

	//static fields

	//number of degrees the falcon will turn at each animation cycle if the turnState is LEFT or RIGHT
	public final static int TURN_STEP = 20;
	//number of frames that the falcon will be protected after a spawn
	public static final int INITIAL_SPAWN_TIME = 46;
	//number of frames falcon will be protected after consuming a NewShieldFloater
	public static final int MAX_SHIELD = 200;
	public static final int MAX_NUKE = 600;
	public static final int MIN_RADIUS = 28;


	//images states
	public enum ImageState {
		SUB_INVISIBLE, //for pre-spawning
		SUB, //normal ship
		SUB_THR, //normal ship thrusting
		SUB_PRO, //protected ship (green)
		SUB_PRO_THR, //protected ship (green) thrusting

	}


	//instance fields (getters/setters provided by Lombok @Data above)
	private int shield;

	private int nukeMeter;
	private int invisible;
	private boolean maxSpeedAttained;

	//showLevel is not germane to the Falcon. Rather, it controls whether the level is shown in the middle of the
	// screen. However, given that the Falcon reference is never null, and that a Falcon is a Movable whose move/draw
	// methods are being called every ~40ms, this is a very convenient place to store this variable.
	private int showLevel;
	private boolean thrusting;
	//enum used for turnState field
	public enum TurnState {IDLE, LEFT, RIGHT}
	private TurnState turnState = TurnState.IDLE;


	// ==============================================================
	// CONSTRUCTOR
	// ==============================================================
	
	public Sub() {

		setTeam(Team.FRIEND);
		setRadius(MIN_RADIUS);


		//We use HashMap which has a seek-time of O(1)
		//See the resources directory in the root of this project for pngs.
		//Using enums as keys is safer b/c we know the value exists when we reference the consts later in code.
    	Map<ImageState, BufferedImage> rasterMap = new HashMap<>();
		rasterMap.put(ImageState.SUB_INVISIBLE, null );
		rasterMap.put(ImageState.SUB, loadGraphic("/imgs/other/submarine.png") ); //normal ship
		rasterMap.put(ImageState.SUB_THR, loadGraphic("/imgs/other/submarine.png") ); //normal ship thrusting
		rasterMap.put(ImageState.SUB_PRO, loadGraphic("/imgs/other/submarine2.png") ); //protected ship (green)
		rasterMap.put(ImageState.SUB_PRO_THR, loadGraphic("/imgs/other/submarine2.png") ); //protected ship (green)
		setRasterMap(rasterMap);


	}


	// ==============================================================
	// METHODS 
	// ==============================================================
	@Override
	public void move() {
		super.move();

		if (invisible > 0) invisible--;
		if (shield > 0) shield--;
		if (showLevel > 0) showLevel--;

		final double THRUST = 10;
		final int MAX_VELOCITY = 39;


		//apply some thrust vectors using trig.
		if (thrusting) {
			double vectorX = Math.cos(Math.toRadians(getOrientation()))
					* THRUST;
			double vectorY = Math.sin(Math.toRadians(getOrientation()))
					* THRUST;
			setCenter(new Point(getCenter().x+(int) vectorX,getCenter().y+(int)vectorY));
			//Absolute velocity is the hypotenuse of deltaX and deltaY
			int absVelocity =
					(int) Math.sqrt(Math.pow(getDeltaX()+ vectorX, 2) + Math.pow(getDeltaY() + vectorY, 2));

			//only accelerate (or adjust radius) if we are below the maximum absVelocity.
			if (absVelocity < MAX_VELOCITY){
				//accelerate
//				setDeltaX(getDeltaX() + vectorX);
//				setDeltaY(getDeltaY() + vectorY);
				//Make the ship radius bigger when the absolute velocity increases, thereby increasing difficulty when not
				// protected, and allowing player to use the shield offensively when protected.
				setRadius(MIN_RADIUS + absVelocity / 3);
				maxSpeedAttained = false;
			} else {
				//at max speed, you will lose steerage if you attempt to accelerate in the same general direction
				//show WARNING message to player using this flag (see drawFalconStatus() of GamePanel class)
				maxSpeedAttained = true;
			}

		}

		//adjust the orientation given turnState
		int adjustOr = getOrientation();
		switch (turnState){
			case LEFT:
				adjustOr = getOrientation() <= 0 ? 360 - TURN_STEP : getOrientation() - TURN_STEP;
				break;
			case RIGHT:
				adjustOr = getOrientation() >= 360 ? TURN_STEP : getOrientation() + TURN_STEP;
				break;
			case IDLE:
			default:
				//do nothing
		}
		setOrientation(adjustOr);

	}

	//Since the superclass Spite does not provide an
	// implementation for draw() (contract method from Movable) ,we inherit that contract debt, and therefore must
	// provide an implementation. This is a raster and vector (see drawShield below) implementation of draw().
	@Override
	public void draw(Graphics g) {

		//set local image-state
		ImageState imageState;
		if (invisible > 0){
			imageState = ImageState.SUB_INVISIBLE;
		}
		else if (shield > 0){
			imageState = thrusting ? ImageState.SUB_PRO_THR : ImageState.SUB_PRO;
			//you can also combine vector elements and raster elements
		}
		else { //not protected
			imageState = thrusting ? ImageState.SUB_THR : ImageState.SUB;
		}

		//down-cast (widen the aperture of) the graphics object to gain access to methods of Graphics2D
		//and render the raster image according to the image-state
		renderRaster((Graphics2D) g, getRasterMap().get(imageState));

	}


	@Override
	public void remove(LinkedList<Movable> list) {
		//The falcon is never actually removed from the game-space; instead we decrement numFalcons
		//only execute the decrementFalconNumAndSpawn() method if shield is down.
		if ( shield == 0)  decrementFalconNumAndSpawn();
	}


	public void decrementFalconNumAndSpawn(){

		CommandCenter.getInstance().setNumFalcons(CommandCenter.getInstance().getNumFalcons() -1);
		CommandCenter.getInstance().setMTimeLeft(20);
		if (CommandCenter.getInstance().isGameOver()) return;
		Sound.playSound("shipspawn.wav");
		setShield(Sub.INITIAL_SPAWN_TIME);
		setInvisible(Sub.INITIAL_SPAWN_TIME/4);
		//put falcon in the middle of the game-space
		setCenter(new Point(Game.DIM.width / 2, Game.DIM.height / 2));
		//random number between 0-360 in steps of TURN_STEP
		setOrientation(Game.R.nextInt(360 / Sub.TURN_STEP) * Sub.TURN_STEP);
		setDeltaX(0);
		setDeltaY(0);
		setRadius(Sub.MIN_RADIUS);
		setMaxSpeedAttained(false);
		setNukeMeter(0);

	}



} //end class
