package sample.application.fingerpaint;

import java.io.File;
import java.io.FileFilter;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class FilePicker extends ListActivity {

	String dir,externalStorageDir;
	FileFilter fFilter;
	Comparator<Object> comparator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自動生成されたメソッド・スタブ
		setContentView(R.layout.filelist);
		externalStorageDir = Environment.getExternalStorageDirectory().toString();
		SharedPreferences pref = this.getSharedPreferences("FilePickerPrefs", MODE_PRIVATE);
		dir = pref.getString("Folder", externalStorageDir+"/mypaint");
		makeFileFilter();
		makeComparator();
		showList();
		
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onStop() {
		// TODO 自動生成されたメソッド・スタブ
		SharedPreferences pref = this.getSharedPreferences("FilePickerPrefs", MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("Folder", dir);
		editor.commit();
		super.onStop();
	}

	public FilePicker() {
		// TODO 自動生成されたコンストラクター・スタブ
	}
	
	public void makeFileFilter() {
		fFilter = new FileFilter () {
			public boolean accept(File file) {
				Pattern p = Pattern.compile("\\.png$|\\.jpg$|\\.gif$|\\.jpeg$|\\.bmp$",Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(file.getName());
				boolean shown = (m.find() || file.isDirectory()) && ! file.isHidden();
				return shown;
			}
		};
	}
	
	public void makeComparator() {
		comparator = new Comparator<Object>() {//インナークラス
			
			public int compare(Object object1, Object object2) {
				int pad1 = 0;
				int pad2 = 0;
				File file1 = (File)object1;
				File file2 = (File)object2;
				if (file1.isDirectory()) {
					pad1 = -65536;
				}
				if (file2.isDirectory()) {
					pad2 = -65536;
				}
				return pad1-pad2+file1.getName().compareToIgnoreCase(file2.getName());
			}
		};
	}
	
	public void showList() {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			finish();
		}
		File file = new File(dir);
		if (!file.exists()) {
			dir = externalStorageDir;
			file = new File(dir);
		}
		this.setTitle(dir);
		File[] fc = file.listFiles(fFilter);
		final FileListAdapter adapter = new FileListAdapter(this,fc);
		adapter.sort(comparator);
		ListView lv = (ListView) findViewById(android.R.id.list);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
		
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (((File) adapter.getItem(position)).isDirectory()) {
					dir = ((File) adapter.getItem(position)).getPath();
					showList();
				} else {
					Intent i = new Intent();
					i.putExtra("fn", ((File) adapter.getItem(position)).getPath());
					setResult(RESULT_OK,i);
					finish();
				}
			}
		});
		
		if(dir.equals(Environment.getExternalStorageDirectory().toString())) {
			findViewById(R.id.button1).setEnabled(false);
		} else {
			findViewById(R.id.button1).setEnabled(true);
		}
	}
	
	public void upButtonClick(View v) {
		dir = new File(dir).getParent();
		showList();
	}

}
