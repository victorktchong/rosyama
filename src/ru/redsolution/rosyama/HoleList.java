package ru.redsolution.rosyama;

import ru.redsolution.rosyama.data.Hole;
import ru.redsolution.rosyama.data.Rosyama;
import ru.redsolution.rosyama.data.Status;
import ru.redsolution.rosyama.data.UpdateListener;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;
import android.widget.ListView;

public class HoleList extends Activity implements OnItemClickListener,
		UpdateListener, OnCancelListener, YesNoDialogClickListener,
		OnItemSelectedListener {
	/**
	 * Диалог с сообщением перед созданием дефекта.
	 */
	private static final int DIALOG_CREATE_ID = 0;

	/**
	 * Диалог с выбором источника фотографии.
	 */
	private static final int DIALOG_PHOTO_ID = 1;

	/**
	 * Добавить дефект.
	 */
	private static final int OPTION_MENU_ADD_ID = 0;

	/**
	 * Приложение.
	 */
	private Rosyama rosyama;

	/**
	 * Диалог выполнения задания.
	 */
	private ProgressDialog progressDialog;

	/**
	 * Адаптер для отображения дефектов.
	 */
	private HoleAdapter holeAdapter;

	/**
	 * Помошник создания фотографии.
	 */
	private PhotoDialogHelper photoDialogHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hole_list);
		rosyama = (Rosyama) getApplication();

		photoDialogHelper = new PhotoDialogHelper(this, DIALOG_PHOTO_ID,
				savedInstanceState);

		holeAdapter = new HoleAdapter(this, Status.fixed);

		ListView listView = (ListView) findViewById(android.R.id.list);
		listView.setAdapter(holeAdapter);
		listView.setOnItemClickListener(this);

		Gallery gallery = (Gallery) findViewById(R.id.gallery);
		gallery.setAdapter(new StatusAdapter(this));
		gallery.setOnItemSelectedListener(this);

		if (savedInstanceState == null) {
			rosyama.getListOperation().execute();
		}

		progressDialog = new ProgressDialog(this);
		progressDialog.setIndeterminate(true);
		progressDialog.setOnCancelListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		((Rosyama) getApplication()).setUpdateListener(this);
		holeAdapter.setStatus((Status) ((Gallery) findViewById(R.id.gallery))
				.getSelectedItem());
	}

	@Override
	protected void onPause() {
		super.onPause();
		((Rosyama) getApplication()).setUpdateListener(null);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		photoDialogHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Uri uri = photoDialogHelper.getResultUri(requestCode, resultCode, data);
		if (uri != null) {
			rosyama.createHole(uri);
			Intent intent = new Intent(this, HoleEdit.class);
			startActivity(intent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, OPTION_MENU_ADD_ID, 0,
				getString(R.string.hole_create_label)).setIcon(
				android.R.drawable.ic_menu_add);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case OPTION_MENU_ADD_ID:
			showDialog(DIALOG_CREATE_ID);
			return true;
		}
		return false;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		switch (id) {
		case DIALOG_CREATE_ID:
			return new YesNoDialogBuilder(this, this, DIALOG_CREATE_ID,
					getString(R.string.hole_create_dialog)).create();
		case DIALOG_PHOTO_ID:
			return photoDialogHelper.create();
		default:
			return null;
		}
	}

	@Override
	public void onAccept(YesNoDialogBuilder builder) {
		switch (builder.getDialogId()) {
		case DIALOG_CREATE_ID:
			showDialog(DIALOG_PHOTO_ID);
			break;
		}
	}

	@Override
	public void onDecline(YesNoDialogBuilder builder) {
	}

	@Override
	public void onCancel(YesNoDialogBuilder builder) {
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Hole hole = (Hole) parent.getAdapter().getItem(position);
		if (hole == null)
			// Footer
			return;
		Intent intent = new Intent(this, HoleDetail.class);
		intent.putExtra(HoleEdit.EXTRA_ID, hole.getId());
		startActivity(intent);
	}

	@Override
	public void onUpdate() {
		holeAdapter.notifyDataSetChanged();

		if (rosyama.getListOperation().isInProgress()) {
			progressDialog.setMessage(getString(R.string.list_request));
			progressDialog.show();
		} else
			progressDialog.dismiss();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		finish();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		Status status = (Status) parent.getAdapter().getItem(position);
		holeAdapter.setStatus(status);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}
}
