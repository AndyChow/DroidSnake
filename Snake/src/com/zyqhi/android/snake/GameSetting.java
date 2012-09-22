package com.zyqhi.android.snake;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class GameSetting extends ListActivity {

	public static int MoveDelay = 100;

	private String[] mSettings = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setContentView(R.layout.game_setting);
		// Get the string resource
		Resources res = getResources();
		mSettings = res.getStringArray(R.array.settings_array);

		// Use an existing ListAdapter that will map an array
		// of strings to TextViews
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, mSettings));
		getListView().setTextFilterEnabled(true);
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
	//	Context context = getApplicationContext();
		
		Dialog dlg = new Dialog(this);
		dlg.setContentView(R.layout.dlg_speed_setting);
		dlg.setTitle("Hello world!");
		dlg.show();

		
		Toast toast = Toast.makeText(this,
		// Long.toString(ids.length),
				Long.toString(id),
				// "hh",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	private void setMoveDelay(int delay) {
		MoveDelay = delay;
	}

}
