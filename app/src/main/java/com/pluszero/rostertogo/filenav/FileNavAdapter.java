package com.pluszero.rostertogo.filenav;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pluszero.rostertogo.R;

import java.io.File;
import java.util.List;

public class FileNavAdapter extends ArrayAdapter<File> {

	private Context c;

	public FileNavAdapter(Context c, int resource, List<File> objects) {
		super(c, resource, objects);
		this.c = c;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		File file = getItem(position);

		LayoutInflater mInflater = (LayoutInflater) c
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.filenav_list_row, null);
		}

		TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
		ImageView ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);

		tvName.setText(file.getName());
		if (file.isDirectory())
			ivIcon.setImageResource(R.drawable.folder_icon);
		else
			ivIcon.setImageResource(R.drawable.file_icon);

		return convertView;
	}
}
