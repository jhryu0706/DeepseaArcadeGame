package edu.uchicago.gerber._08final.mvc.model;

import edu.uchicago.gerber._08final.mvc.controller.CommandCenter;
import edu.uchicago.gerber._08final.mvc.controller.Sound;

import java.awt.*;
import java.util.LinkedList;

public class Coin extends Sprite {
private int x;
private int y;

public Coin(int x,int y) {
		setColor(Color.YELLOW);
		setExpiry(750);
		this.x=x;
		this.y=y;
		setCenter(new Point(x , y));
		setRadius(5);
		setTeam(Team.FLOATER);
	}
	public void draw(Graphics g) {
		g.setColor(getColor());
		g.fillOval(x,y , 20, 20);
	}
	@Override
	public void remove(LinkedList<Movable> list) {
		super.remove(list);
		Sound.playSound("coin.wav");
		//if getExpiry() > 0, then this remove was the result of a collision, rather than natural mortality
		if (getExpiry() > 0) {
			CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + (10L));
		}

	}


}
