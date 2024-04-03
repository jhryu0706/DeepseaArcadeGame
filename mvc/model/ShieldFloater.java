package edu.uchicago.gerber._08final.mvc.model;

import edu.uchicago.gerber._08final.mvc.controller.CommandCenter;
import edu.uchicago.gerber._08final.mvc.controller.Game;
import edu.uchicago.gerber._08final.mvc.controller.Sound;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ShieldFloater extends Sprite {
	//spawn every 25 seconds
	private int index = 0;
	public static final int MIN_RADIUS = 28;
	public static final int SPAWN_SHIELD_FLOATER = Game.FRAMES_PER_SECOND * 25;
	public ShieldFloater() {
		setTeam(Team.FLOATER);
		setExpiry(260);
		setRadius(50);
		Map<Integer, BufferedImage> rasterMap = new HashMap<>();
		rasterMap.put(0, loadGraphic("/imgs/other/plastic.png") );
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
			Sound.playSound("shieldup.wav");
		    CommandCenter.getInstance().getSub().setShield(Sub.MAX_SHIELD);
	   }

	}


}
