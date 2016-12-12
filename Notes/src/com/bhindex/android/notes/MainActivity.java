package com.bhindex.android.notes;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import com.bhindex.android.notes.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final String DEBUGTAG = "";
	public static final String TEXTFILE = "notesquirrel.txt";
	public static final String FILESAVED = "FileSaved";
	private Uri image;
	private static final int PHOTO_TAKEN_REQUEST = 0;
	private static final int BROWSE_GALLERY_REQUEST = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		// Listeners
		addSaveButtonListener();
		addLockButtonListener();

		SharedPreferences prefs = getPreferences(MODE_PRIVATE);

		boolean fileSaved = prefs.getBoolean(FILESAVED, false);

		if (fileSaved) {
			loadSavedFile();
		}
	}

	private void resetPasspoints(Uri image) {
		Intent i = new Intent(this, ImageActivity.class);
		i.putExtra(ImageActivity.RESET_PASSPOINTS, true);

		if (image != null) {
			i.putExtra(ImageActivity.RESET_IMAGE, image.getPath());
		}

		startActivity(i);
	}

	@Override

	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_passpoints_reset:
			resetPasspoints(null);
			return true;
		case R.id.menu_replace_image:
			replaceImage();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	private void replaceImage() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		View v = getLayoutInflater().inflate(R.layout.replace_image, null);
		builder.setTitle(R.string.replace_lock_image);
		builder.setView(v);

		final AlertDialog dlg = builder.create();
		dlg.show();

		Button takePhoto = (Button) dlg.findViewById(R.id.take_photo);
		Button browseGallery = (Button) dlg.findViewById(R.id.browse_gallery);

		takePhoto.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Kamera activity
				takePhoto();
			}
		});

		browseGallery.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// brauzuj galeriju za sliku
				browseGallery();
			}
		});
	}

	private void browseGallery() {
		Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, BROWSE_GALLERY_REQUEST);
	}

	private void takePhoto() {
		// placeanje slike
		File picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File imageFile = new File(picturesDirectory, "passpoints_image");

		Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		image = Uri.fromFile(imageFile);
		i.putExtra(MediaStore.EXTRA_OUTPUT, image);
		startActivityForResult(i, PHOTO_TAKEN_REQUEST);
	}

	@Override

	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

		if (requestCode == BROWSE_GALLERY_REQUEST) {
			String[] columns = { MediaStore.Images.Media.DATA };

			Uri imageUri = intent.getData();

			Cursor cursor = getContentResolver().query(imageUri, columns, null, null, null);

			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(columns[0]);
			String imagePath = cursor.getString(columnIndex);

			cursor.close();

			image = Uri.parse(imagePath);
		}

		// image URI
		if (image == null) {
			Toast.makeText(this, R.string.unable_to_display_image, Toast.LENGTH_LONG).show();
			return;
		}

		Log.d(DEBUGTAG, "Photo: " + image.getPath());

		// new image.
		resetPasspoints(image);
	}

	// listener za kljucanje
	private void addLockButtonListener() {
		Button lockBtn = (Button) findViewById(R.id.lock);

		lockBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(DEBUGTAG, "start image activity");
				startActivity(new Intent(MainActivity.this, ImageActivity.class));
			}
		});
	}

	// loaduj saved file

	private void loadSavedFile() {
		try {

			FileInputStream fis = openFileInput(TEXTFILE);

			BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(fis)));

			EditText editText = (EditText) findViewById(R.id.text);

			String line;

			while ((line = reader.readLine()) != null) {
				editText.append(line);
				editText.append("\n");
			}

			fis.close();
		} catch (Exception e) {
			Toast.makeText(MainActivity.this, getString(R.string.toast_cant_load), Toast.LENGTH_LONG).show();
		}
	}

	// save tekst
	private void addSaveButtonListener() {
		Button saveBtn = (Button) findViewById(R.id.save);

		saveBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				saveText();
			}
		});

	}

	@Override
	protected void onPause() {
		super.onPause();

		saveText();
	}

	private void saveText() {
		EditText editText = (EditText) findViewById(R.id.text);

		String text = editText.getText().toString();

		try {
			FileOutputStream fos = openFileOutput(TEXTFILE, Context.MODE_PRIVATE);
			fos.write(text.getBytes());
			fos.close();

			SharedPreferences prefs = getPreferences(MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean(FILESAVED, true);
			editor.commit();
		} catch (Exception e) {
			Toast.makeText(MainActivity.this, getString(R.string.toast_cant_save), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
