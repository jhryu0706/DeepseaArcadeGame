package edu.uchicago.gerber._08final.mvc.model;

import edu.uchicago.gerber._08final.mvc.controller.CommandCenter;
import edu.uchicago.gerber._08final.mvc.controller.Game;
import edu.uchicago.gerber._08final.mvc.controller.GameOp;
import edu.uchicago.gerber._08final.mvc.controller.Sound;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class EnemyStarFish extends Sprite {

	private int index = 0;
	//spawn every 40 seconds
	public static final int SPAWN_NEW_WALL_FLOATER = Game.FRAMES_PER_SECOND * 40;
	public EnemyStarFish() {
		setTeam(Team.FOE);
		setExpiry(260);
		setRadius(50);
		Map<Integer, BufferedImage> rasterMap = new HashMap<>();
		rasterMap.put(0, loadGraphic("/imgs/other/starfish.png") );
		setRasterMap(rasterMap);
		setSpin(somePosNegValue(10));
		setDeltaX(somePosNegValue(10));
		setDeltaY(somePosNegValue(10));
	}

	@Override
	public void draw(Graphics g) {
		renderRaster((Graphics2D) g, getRasterMap().get(index));
	}

	@Override
	public void remove(LinkedList<Movable> list) {
		super.remove(list);
		//if getExpiry() > 0, then this remove was the result of a collision, rather than natural mortality
		if (getExpiry() > 0) {
			Sound.playSound("scream.wav");
			buildWall();
		}
	}

	private void buildWall() {
		final int BRICK_SIZE = Game.DIM.width / 30, ROWS = 2, COLS = 20, X_OFFSET = BRICK_SIZE * 5, Y_OFFSET = 50;

		for (int nCol = 0; nCol < COLS; nCol++) {
			for (int nRow = 0; nRow < ROWS; nRow++) {
				CommandCenter.getInstance().getOpsQueue().enqueue(
						new Brick(
								new Point(nCol * BRICK_SIZE + X_OFFSET, nRow * BRICK_SIZE + Y_OFFSET),
								BRICK_SIZE),
						GameOp.Action.ADD);

			}
		}
	}

}
