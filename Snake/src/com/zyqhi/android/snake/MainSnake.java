package com.zyqhi.android.snake;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainSnake extends Activity {

	private Button mBtnStartGame = null;
	private Button mBtnGmaeSetting = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_snake);

		mBtnStartGame = (Button) findViewById(R.id.start_game);
		mBtnStartGame.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click
				Intent intent = new Intent(MainSnake.this,
						com.zyqhi.android.snake.Snake.class);
				startActivity(intent);
			}
		});

		mBtnGmaeSetting = (Button) findViewById(R.id.game_setting);
		mBtnGmaeSetting.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainSnake.this,
						com.zyqhi.android.snake.GameSetting.class);
				startActivity(intent);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main_snake, menu);
		return true;
	}

}
