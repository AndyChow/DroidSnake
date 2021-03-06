/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.zyqhi.android.snake;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * SnakeView: implementation of a simple game of Snake
 * 
 * 
 */
public class SnakeView extends TileView {

	private static final String TAG = "SnakeView";

	/**
	 * Current mode of application: READY to run, RUNNING, or you have already
	 * lost. static final ints are used instead of an enum for performance
	 * reasons.
	 */
	private int mMode = READY;
	public static final int PAUSE = 0;
	public static final int READY = 1;
	public static final int RUNNING = 2;
	public static final int LOSE = 3;

	/**
	 * Current direction the snake is headed.
	 */
	private int mDirection = NORTH;
	private int mNextDirection = NORTH;
	private static final int NORTH = 1;
	private static final int SOUTH = 2;
	private static final int EAST = 3;
	private static final int WEST = 4;

	/**
	 * Labels for the drawables that will be loaded into the TileView class
	 */
	private static final int RED_STAR = 1;
	private static final int YELLOW_STAR = 2;
	private static final int GREEN_STAR = 3;

	/**
	 * mScore: used to track the number of apples captured mMoveDelay: number of
	 * milliseconds between snake movements. This will decrease as apples are
	 * captured.
	 */
	private long mScore = 0;
	private long mMoveDelay = 600;
	/**
	 * mLastMove: tracks the absolute time when the snake last moved, and is
	 * used to determine if a move should be made based on mMoveDelay.
	 */
	private long mLastMove;

	/**
	 * mStatusText: text shows to the user in some run states
	 */
	private TextView mStatusText;

	/**
	 * mSnakeTrail: a list of Coordinates that make up the snake's body
	 * mAppleList: the secret location of the juicy apples the snake craves
	 * mGodFruit: a fruit falling from top screen, like in Russian square
	 */
	private ArrayList<Coordinate> mSnakeTrail = new ArrayList<Coordinate>();
	private ArrayList<Coordinate> mAppleList = new ArrayList<Coordinate>();
	private ArrayList<Coordinate> mGodFruit = new ArrayList<Coordinate>();
	private ArrayList<Coordinate> mFruitOnGround = new ArrayList<Coordinate>();

	/**
	 * A factory producing squares
	 */
	private TetrisFactory mTetriFactory = new TetrisFactory();

	/**
	 * Everyone needs a little randomness in their life
	 */
	private static final Random RNG = new Random();

	/**
	 * Create a simple handler that we can use to cause animation to happen. We
	 * set ourselves as a target and we can use the sleep() function to cause an
	 * update/invalidate to occur at a later date.
	 */
	private RefreshHandler mRedrawHandler = new RefreshHandler();

	class RefreshHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			Bundle b = msg.getData();
			String s = b.getString("msg");

			if (s == "start") {
				SnakeView.this.initNewGame();
				SnakeView.this.setMode(RUNNING);
				SnakeView.this.update();
				SnakeView.this.invalidate();
				return;
			}
			SnakeView.this.update();
			SnakeView.this.invalidate();
		}

		public void sleep(long delayMillis) {
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	};

	private SnakeViewThread mThread;

	class SnakeViewThread implements Runnable {

		public void run() {
			// we should sleep before the game start
			// 应该睡眠一段时间，不然的话，程序开启时会出问题
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// TODO Auto-generated method stub
			Message msg = new Message();
			Bundle b = new Bundle();// 存放数据
			b.putString("msg", "start");
			msg.setData(b);
			SnakeView.this.mRedrawHandler.sendMessage(msg);
		}

	}

	/**
	 * Constructs a SnakeView based on inflation from XML
	 * 
	 * @param context
	 * @param attrs
	 */
	public SnakeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initSnakeView();

		mThread = new SnakeViewThread();
		new Thread(mThread).start();
	}

	public SnakeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initSnakeView();

	}

	private void initSnakeView() {
		setFocusable(true);

		Resources r = this.getContext().getResources();

		resetTiles(4);
		loadTile(RED_STAR, r.getDrawable(R.drawable.redstar));
		loadTile(YELLOW_STAR, r.getDrawable(R.drawable.yellowstar));
		loadTile(GREEN_STAR, r.getDrawable(R.drawable.greenstar));

	}

	private void initNewGame() {
		mSnakeTrail.clear();
		mAppleList.clear();
		mFruitOnGround.clear();
		mGodFruit.clear();
		mOnGround = false;

		// For now we're just going to load up a short default eastbound snake
		// that's just turned north

		mSnakeTrail.add(new Coordinate(7, 7));
		mSnakeTrail.add(new Coordinate(6, 7));
		mSnakeTrail.add(new Coordinate(5, 7));
		mSnakeTrail.add(new Coordinate(4, 7));
		mSnakeTrail.add(new Coordinate(3, 7));
		mSnakeTrail.add(new Coordinate(2, 7));

		// fruit from God
		int loca = RNG.nextInt(mXTileCount - 3) + 1 ;
		mGodFruit = mTetriFactory.getTetris();

		for (Coordinate c : mGodFruit)
			c.x += loca;

		/*
		mGodFruit.add(new Coordinate(6, 1));
		mGodFruit.add(new Coordinate(7, 1));
		mGodFruit.add(new Coordinate(6, 2));
		mGodFruit.add(new Coordinate(7, 2));
		*/
		mNextDirection = NORTH;

		// Two apples to start with
		// Three apples
		addRandomApple();
		addRandomApple();
		addRandomApple();

		// Set the background color
		setBackgroundColor(GameSetting.BackgroundColor);

		mMoveDelay = GameSetting.MoveDelay;
		mScore = 0;
	}

	/**
	 * Given a ArrayList of coordinates, we need to flatten them into an array
	 * of ints before we can stuff them into a map for flattening and storage.
	 * 
	 * @param cvec
	 *            : a ArrayList of Coordinate objects
	 * @return : a simple array containing the x/y values of the coordinates as
	 *         [x1,y1,x2,y2,x3,y3...]
	 */
	private int[] coordArrayListToArray(ArrayList<Coordinate> cvec) {
		int count = cvec.size();
		int[] rawArray = new int[count * 2];
		for (int index = 0; index < count; index++) {
			Coordinate c = cvec.get(index);
			rawArray[2 * index] = c.x;
			rawArray[2 * index + 1] = c.y;
		}
		return rawArray;
	}

	/**
	 * Save game state so that the user does not lose anything if the game
	 * process is killed while we are in the background.
	 * 
	 * @return a Bundle with this view's state
	 */
	public Bundle saveState() {
		Bundle map = new Bundle();

		map.putIntArray("mAppleList", coordArrayListToArray(mAppleList));
		map.putInt("mDirection", Integer.valueOf(mDirection));
		map.putInt("mNextDirection", Integer.valueOf(mNextDirection));
		map.putLong("mMoveDelay", Long.valueOf(mMoveDelay));
		map.putLong("mScore", Long.valueOf(mScore));
		map.putIntArray("mSnakeTrail", coordArrayListToArray(mSnakeTrail));

		return map;
	}

	/**
	 * Given a flattened array of ordinate pairs, we reconstitute them into a
	 * ArrayList of Coordinate objects
	 * 
	 * @param rawArray
	 *            : [x1,y1,x2,y2,...]
	 * @return a ArrayList of Coordinates
	 */
	private ArrayList<Coordinate> coordArrayToArrayList(int[] rawArray) {
		ArrayList<Coordinate> coordArrayList = new ArrayList<Coordinate>();

		int coordCount = rawArray.length;
		for (int index = 0; index < coordCount; index += 2) {
			Coordinate c = new Coordinate(rawArray[index], rawArray[index + 1]);
			coordArrayList.add(c);
		}
		return coordArrayList;
	}

	/**
	 * Restore game state if our process is being relaunched
	 * 
	 * @param icicle
	 *            a Bundle containing the game state
	 */
	public void restoreState(Bundle icicle) {
		setMode(PAUSE);

		mAppleList = coordArrayToArrayList(icicle.getIntArray("mAppleList"));
		mDirection = icicle.getInt("mDirection");
		mNextDirection = icicle.getInt("mNextDirection");
		mMoveDelay = icicle.getLong("mMoveDelay");
		mScore = icicle.getLong("mScore");
		mSnakeTrail = coordArrayToArrayList(icicle.getIntArray("mSnakeTrail"));
	}

	/*
	 * handles key events in the game. Update the direction our snake is
	 * traveling based on the DPAD. Ignore events that would cause the snake to
	 * immediately turn back on itself.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onKeyDown(int, android.os.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent msg) {

		if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {

			if (mMode == READY | mMode == LOSE) {
				/*
				 * At the beginning of the game, or the end of a previous one,
				 * we should start a new game.
				 */
				initNewGame();
				setMode(RUNNING);
				update();
				return (true);
			}

			if (mMode == PAUSE) {
				/*
				 * If the game is merely paused, we should just continue where
				 * we left off.
				 */
				setMode(RUNNING);
				update();
				return (true);
			}

			if (mDirection != SOUTH) {
				mNextDirection = NORTH;
			}
			return (true);
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			if (mDirection != NORTH) {
				mNextDirection = SOUTH;
			}
			return (true);
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			if (mDirection != EAST) {
				mNextDirection = WEST;
			}
			return (true);
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (mDirection != WEST) {
				mNextDirection = EAST;
			}
			return (true);
		}

		return super.onKeyDown(keyCode, msg);
	}

	private float startX, startY;
	private float currentX, currentY;
	private float chanX, chanY;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mMode == READY || mMode == LOSE) {
			initNewGame();
			setMode(RUNNING);
			update();

			return true;
		}

		if (mMode == PAUSE) {
			/*
			 * If the game is merely paused, we should just continue where we
			 * left off.
			 */
			setMode(RUNNING);
			update();
			return (true);
		}

		if (mMode != RUNNING)
			return false;

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			startX = event.getX();
			startY = event.getY();
		}
		else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			currentX = event.getX();
			currentY = event.getY();
		}
		else if (event.getAction() == MotionEvent.ACTION_UP) {
			chanX = currentX - startX;
			chanY = currentY - startY;

			if (chanY > 0) {
				// Down SOUTH
				if (chanY / Math.abs(chanX) > 2) {
					if (mDirection != NORTH)
						mNextDirection = SOUTH;
				}
				// Right EAST
				else if (chanX > 0 && (chanX / chanY > 2)) {
					if (mDirection != WEST)
						mNextDirection = EAST;
				}
				// Left WEST
				else if (chanX < 0 && (chanX / chanY < -2)) {
					if (mDirection != EAST)
						mNextDirection = WEST;
				}
			}
			else if (chanY < 0) {
				// Up NORTH
				if (chanY / Math.abs(chanX) < -2) {
					if (mDirection != SOUTH)
						mNextDirection = NORTH;
				}
				// Right EAST
				else if (chanX > 0 && (chanX / chanY < -2)) {
					if (mDirection != WEST)
						mNextDirection = EAST;
				}
				// left WEST
				else if (chanX < 0 && (chanX / chanY > 2)) {
					if (mDirection != EAST)
						mNextDirection = WEST;
				}
			}

		}

		return (true);// 这里一定要返回true！
	}

	/**
	 * Sets the TextView that will be used to give information (such as "Game
	 * Over" to the user.
	 * 
	 * @param newView
	 */
	public void setTextView(TextView newView) {
		mStatusText = newView;
	}

	/**
	 * Sets the MoveDelay that will be used to change the speed of snake
	 * 
	 * @param mMoveDelay
	 */
	public void setMoveDelay(int delay) {
		mMoveDelay = delay;
	}

	/**
	 * Updates the current mode of the application (RUNNING or PAUSED or the
	 * like) as well as sets the visibility of textview for notification
	 * 
	 * @param newMode
	 */
	public void setMode(int newMode) {
		int oldMode = mMode;
		mMode = newMode;

		if (newMode == RUNNING & oldMode != RUNNING) {
			mStatusText.setVisibility(View.INVISIBLE);
			update();
			return;
		}

		Resources res = getContext().getResources();
		CharSequence str = "";
		if (newMode == PAUSE) {
			str = res.getText(R.string.mode_pause);
		}
		if (newMode == READY) {
			str = res.getText(R.string.mode_ready);
		}
		if (newMode == LOSE) {
			str = res.getString(R.string.mode_lose_prefix) + mScore
					+ res.getString(R.string.mode_lose_suffix);
		}

		mStatusText.setText(str);
		mStatusText.setVisibility(View.VISIBLE);
	}

	/**
	 * Selects a random location within the garden that is not currently covered
	 * by the snake. Currently _could_ go into an infinite loop if the snake
	 * currently fills the garden, but we'll leave discovery of this prize to a
	 * truly excellent snake-player.
	 * 
	 */
	private void addRandomApple() {
		Coordinate newCoord = null;
		boolean found = false;
		while (!found) {
			// Choose a new location for our apple
			int newX = 1 + RNG.nextInt(mXTileCount - 2);
			int newY = 1 + RNG.nextInt(mYTileCount - 2);
			newCoord = new Coordinate(newX, newY);

			// Make sure it's not already under the snake
			boolean collision = false;
			int snakelength = mSnakeTrail.size();
			for (int index = 0; index < snakelength; index++) {
				if (mSnakeTrail.get(index).equals(newCoord)) {
					collision = true;
				}
			}
			// if we're here and there's been no collision, then we have
			// a good location for an apple. Otherwise, we'll circle back
			// and try again
			found = !collision;
		}
		if (newCoord == null) {
			Log.e(TAG, "Somehow ended up with a null newCoord!");
		}
		mAppleList.add(newCoord);
	}

	/**
	 * Selects a random location in the top of garden
	 */
	private void addRandomFruit() {
		int loca;

		loca = RNG.nextInt(mXTileCount - 4) + 1;

		mGodFruit = mTetriFactory.getTetris();

		for (Coordinate c : mGodFruit) {
			c.x = c.x + loca;
		}

		/*
		 * mGodFruit.add(new Coordinate(loca, 1)); mGodFruit.add(new
		 * Coordinate(loca + 1, 1)); mGodFruit.add(new Coordinate(loca, 2));
		 * mGodFruit.add(new Coordinate(loca + 1, 2));
		 */

		mOnGround = false;
	}

	/**
	 * Handles the basic update loop, checking to see if we are in the running
	 * state, determining if a move should be made, updating the snake's
	 * location.
	 */
	int i = 0;

	public void update() {
		if (mMode == RUNNING) {
			long now = System.currentTimeMillis();

			if (now - mLastMove > mMoveDelay) {
				clearTiles();
				updateWalls();
				updateSnake();
				updateApples();
				updateFruit();
				updateGround();
				mLastMove = now;
			}
			mRedrawHandler.sleep(mMoveDelay);
		}

		/*
		 * mStatusText.setText("hello" + i); i++;
		 * mStatusText.setVisibility(View.VISIBLE);
		 */
	}

	/**
	 * Draws some walls.
	 * 
	 */
	private void updateWalls() {
		for (int x = 0; x < mXTileCount; x++) {
			// Colorful wall
			/*
			 * setTile(RNG.nextInt(3)+1, x, 0); setTile(RNG.nextInt(3)+1, x,
			 * mYTileCount - 1);
			 */

			setTile(RED_STAR, x, 0);
			setTile(RED_STAR, x, mYTileCount - 1);

		}
		for (int y = 1; y < mYTileCount - 1; y++) {
			/*
			 * setTile(RNG.nextInt(3)+1, 0, y); setTile(RNG.nextInt(3)+1,
			 * mXTileCount - 1, y);
			 */

			setTile(GREEN_STAR, 0, y);
			setTile(GREEN_STAR, mXTileCount - 1, y);

		}
	}

	/**
	 * Draws some apples.
	 * 
	 */
	private void updateApples() {
		for (Coordinate c : mAppleList) {
			setTile(YELLOW_STAR, c.x, c.y);
		}
	}

	/**
	 * Figure out which way the snake is going, see if he's run into anything
	 * (the walls, himself, or an apple). If he's not going to die, we then add
	 * to the front and subtract from the rear in order to simulate motion. If
	 * we want to grow him, we don't subtract from the rear.
	 * 
	 */
	private void updateSnake() {
		boolean growSnake = false;
		boolean shortSnake = false;

		// grab the snake by the head
		Coordinate head = mSnakeTrail.get(0);
		Coordinate newHead = new Coordinate(1, 1);

		mDirection = mNextDirection;

		switch (mDirection) {
		case EAST: {
			newHead = new Coordinate(head.x + 1, head.y);
			break;
		}
		case WEST: {
			newHead = new Coordinate(head.x - 1, head.y);
			break;
		}
		case NORTH: {
			newHead = new Coordinate(head.x, head.y - 1);
			break;
		}
		case SOUTH: {
			newHead = new Coordinate(head.x, head.y + 1);
			break;
		}
		}

		// Collision detection
		// For now we have a 1-square wall around the entire arena
		if ((newHead.x < 1) || (newHead.y < 1) || (newHead.x > mXTileCount - 2)
				|| (newHead.y > mYTileCount - 2)) {
			setMode(LOSE);
			return;

		}

		// Look for collisions with itself
		int snakelength = mSnakeTrail.size();
		for (int snakeindex = 0; snakeindex < snakelength; snakeindex++) {
			Coordinate c = mSnakeTrail.get(snakeindex);
			if (c.equals(newHead)) {
				setMode(LOSE);
				return;
			}
		}

		// Look for apples
		int applecount = mAppleList.size();
		for (int appleindex = 0; appleindex < applecount; appleindex++) {
			Coordinate c = mAppleList.get(appleindex);
			if (c.equals(newHead)) {
				mAppleList.remove(c);
				addRandomApple();

				mScore++;
				// mMoveDelay *= 0.9;

				growSnake = true;
			}
		}

		// Look for fruit
		int fruitcount = mGodFruit.size();

		if (fruitcount == 0) {
			// the fruit was ate up by the snake
			addRandomFruit();
		}
		else {

			for (int appleindex = 0; appleindex < fruitcount; appleindex++) {
				Coordinate c = mGodFruit.get(appleindex);
				if (c.equals(newHead)) {

					mScore += 2;
					growSnake = true;
					mGodFruit.remove(c);
					break;
				}
			}
		}

		// Look for fruit on ground, which will decrease you body length
		fruitcount = mFruitOnGround.size();

		for (int appleindex = 0; appleindex < fruitcount; appleindex++) {
			Coordinate c = mFruitOnGround.get(appleindex);
			if (c.equals(newHead)) {

				// mScore += 2;
				// growSnake = true;
				mFruitOnGround.remove(c);
				shortSnake = true;
				break;
			}
		}

		// push a new head onto the ArrayList and pull off the tail
		mSnakeTrail.add(0, newHead);
		// except if we want the snake to grow
		if (!growSnake) {
			mSnakeTrail.remove(mSnakeTrail.size() - 1);
		}

		// make the body short
		if (mSnakeTrail.size() > 2) {
			if (shortSnake) {
				mSnakeTrail.remove(mSnakeTrail.size() - 1);
			}
			mStatusText.setVisibility(View.INVISIBLE);
		}
		else {
			mStatusText
					.setText("Greedy Snake!\nGod dosen't like Snakes eating ground apples!");
			mStatusText.setVisibility(View.VISIBLE);
		}

		int index = 0;
		for (Coordinate c : mSnakeTrail) {
			if (index == 0) {
				setTile(YELLOW_STAR, c.x, c.y);
			}
			else {
				setTile(RED_STAR, c.x, c.y);
			}
			index++;
		}
	}

	/**
	 * An fruit from the God fall in to the screen.
	 */
	// private int fruitIndex;
	// low the falling speed of fruit
	private boolean mLowSpeed = false;
	// fruit is on the ground now, it should no longer falling
	private boolean mOnGround = false;

	private void updateFruit() {
		// fruit has reached ground
		for (Coordinate c : mGodFruit) {
			// setTile(RED_STAR, c.x, c.y);
			if (c.y == mYTileCount - 2)
				mOnGround = true;
		}
		// fruit hasn't reached ground, but there is other fruit under it
		for (Coordinate c1 : mGodFruit) {
			Coordinate temp = new Coordinate(c1.x, c1.y + 1);
			for (Coordinate c2 : mFruitOnGround) {
				if (temp.equals(c2)) {
					mOnGround = true;
				}
			}
		}

		if (mOnGround) {
			for (Coordinate c : mGodFruit)
				mFruitOnGround.add(c);
			mGodFruit.clear();
			// TODO: Add another fruit
			addRandomFruit();
			return;
		}

		if (mLowSpeed) {
			mLowSpeed = false;

			for (Coordinate c : mGodFruit) {
				setTile(RED_STAR, c.x, c.y);
			}
		}
		else {
			mLowSpeed = true;
			for (Coordinate c : mGodFruit) {
				setTile(RED_STAR, c.x, c.y);

				if (mOnGround != true)
					++c.y;
			}
		}
	}

	/**
	 * After the fruit fall down to the ground, it will make a ground
	 */
	private void updateGround() {
		for (Coordinate c : mFruitOnGround) {
			setTile(GREEN_STAR, c.x, c.y);
		}
	}

	/**
	 * Simple class containing two integer values and a comparison function.
	 * There's probably something I should use instead, but this was quick and
	 * easy to build.
	 * 
	 */
	private class Coordinate {
		public int x;
		public int y;

		public Coordinate(int newX, int newY) {
			x = newX;
			y = newY;
		}

		public boolean equals(Coordinate other) {
			if (x == other.x && y == other.y) {
				return true;
			}
			return false;
		}

		@Override
		public String toString() {
			return "Coordinate: [" + x + "," + y + "]";
		}
	}

	/**
	 * Class as a factory to produce a random square.
	 */
	private class TetrisFactory {
		public static final int TETRIS_O = 0;
		public static final int TETRIS_I = 1;
		public static final int TETRIS_Z = 2;
		public static final int TETRIS_J = 3;
		public static final int TETRIS_L = 4;
		public static final int TETRIS_T = 5;
		public static final int TETRIS_S = 6;

		private ArrayList<Coordinate> mSquare = new ArrayList<Coordinate>();

		public ArrayList<Coordinate> getTetris() {
			int randSquare = RNG.nextInt(7);
			createTetris(randSquare);

			return mSquare;
		}

		private void createTetris(int randSquare) {
			switch (randSquare) {
			case TETRIS_O: {
				mSquare.clear();
				mSquare.add(new Coordinate(0, 0));
				mSquare.add(new Coordinate(0, 1));
				mSquare.add(new Coordinate(1, 0));
				mSquare.add(new Coordinate(1, 1));
				break;
			}
			case TETRIS_I: {
				mSquare.clear();
				mSquare.add(new Coordinate(0, 0));
				mSquare.add(new Coordinate(0, 1));
				mSquare.add(new Coordinate(0, 2));
				mSquare.add(new Coordinate(0, 3));
				break;
			}
			case TETRIS_Z: {
				mSquare.clear();
				mSquare.add(new Coordinate(0, 0));
				mSquare.add(new Coordinate(0, 1));
				mSquare.add(new Coordinate(1, 1));
				mSquare.add(new Coordinate(1, 2));
				break;
			}
			case TETRIS_J: {
				mSquare.clear();
				mSquare.add(new Coordinate(0, 2));
				mSquare.add(new Coordinate(1, 0));
				mSquare.add(new Coordinate(1, 1));
				mSquare.add(new Coordinate(1, 2));
				break;
			}
			case TETRIS_L: {
				mSquare.clear();
				mSquare.add(new Coordinate(0, 0));
				mSquare.add(new Coordinate(1, 0));
				mSquare.add(new Coordinate(1, 1));
				mSquare.add(new Coordinate(1, 2));
				break;
			}
			case TETRIS_T: {
				mSquare.clear();
				mSquare.add(new Coordinate(0, 1));
				mSquare.add(new Coordinate(1, 0));
				mSquare.add(new Coordinate(1, 1));
				mSquare.add(new Coordinate(1, 2));
				break;
			}
			case TETRIS_S: {
				mSquare.clear();
				mSquare.add(new Coordinate(0, 1));
				mSquare.add(new Coordinate(0, 2));
				mSquare.add(new Coordinate(1, 0));
				mSquare.add(new Coordinate(1, 1));
				break;
			}
			default:
			}
		}
	}

}
