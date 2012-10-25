package com.zyqhi.android.snake;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

public class GameSetting extends ListActivity implements
		SeekBar.OnSeekBarChangeListener {

	public static int MoveDelay = 100;
	public static int BackgroundColor = 0;

	private String[] mSettings = null;
	private SeekBar mSeekBar = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Use an existing ListAdapter that will map an array
		// of strings to TextViews
		Resources res = getResources();
		mSettings = res.getStringArray(R.array.settings_array);

		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, mSettings));
		getListView().setTextFilterEnabled(true);

	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO:
		// Setting the move speed of the snake.
		if (id == 0) {
			LayoutInflater factory = LayoutInflater.from(this);
			View DialogView = factory.inflate(R.layout.dlg_speed_setting, null);

			ViewGroup vg = (ViewGroup) DialogView;
			mSeekBar = (SeekBar) vg.getChildAt(1);
			mSeekBar.setOnSeekBarChangeListener(this);

			AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle("hello world")
					.setView(DialogView)
					.setPositiveButton(" Ok ",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									setMoveDelay(mSeekBar.getProgress());
								}
							}).setNegativeButton(" Cancle ", null).create();
			dialog.show();
		}

		// Setting color from a color picker.
		if (id == 1) {
			AmbilWarnaDialog dialog = new AmbilWarnaDialog(this,
					BackgroundColor, new OnAmbilWarnaListener() {
						public void onOk(AmbilWarnaDialog dialog, int color) {
							// color is the color selected by the user.
							BackgroundColor = color;
							showToast(Integer.toString(BackgroundColor));
						}

						public void onCancel(AmbilWarnaDialog dialog) {
							// cancel was selected by the user
						}
					});

			dialog.show();
		}

	}

	private void setMoveDelay(int delay) {
		MoveDelay = delay;
	}

	private void showToast(String s) {
		Toast toast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
		toast.show();
	}

	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		showToast(Integer.toString(progress));

	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

}
