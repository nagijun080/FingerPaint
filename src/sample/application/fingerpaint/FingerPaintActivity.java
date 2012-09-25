package sample.application.fingerpaint;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;

public class FingerPaintActivity extends Activity implements OnTouchListener{
	
	public Canvas canvas;
	public Paint paint;
	public Path path;
	public Bitmap bitmap;
	public Float x1,y1;
	public Integer w,h;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自動生成されたメソッド・スタブ
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fingerpaint);
		
		ImageView iv = (ImageView) this.findViewById(R.id.imageView1);
		WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		Display disp = wm.getDefaultDisplay();
		this.w = disp.getWidth();
		this.h = disp.getHeight();
		this.bitmap = Bitmap.createBitmap(this.w, this.h, Bitmap.Config.ARGB_8888);
		this.paint = new Paint();
		this.path = new Path();
		this.canvas = new Canvas(this.bitmap);
		
		this.paint.setStrokeWidth(5);
		this.paint.setStyle(Paint.Style.STROKE);
		this.paint.setStrokeJoin(Paint.Join.ROUND);
		this.paint.setStrokeCap(Paint.Cap.ROUND);
		this.canvas.drawColor(Color.WHITE);
		iv.setOnTouchListener(this);
	}

	
	public boolean onTouch (View v, MotionEvent event){
		float x = event.getX();
		float y = event.getY();
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			this.path.reset();
			this.path.moveTo(x, y);
			break;
		case MotionEvent.ACTION_MOVE:
			this.path.quadTo(x1, y1, x, y);
			this.x1 = x;
			this.y1 = y;
			this.canvas.drawPath(path, paint);
			this.path.reset();
			this.path.moveTo(x, y);
			break;
		case MotionEvent.ACTION_UP:
			if (x == this.x1 && y == this.y1 ) {
				this.y1 = this.y1 + 1;
			}
			this.path.quadTo(x1, y1, x, y);
			this.canvas.drawPath(path, paint);
			this.path.reset();
			break;
		}
		ImageView iv = (ImageView) this.findViewById(R.id.imageView1);
		iv.setImageBitmap(this.bitmap);
		
		return true;
	}
	
	public void save() {
		
		SharedPreferences prefs = this.getSharedPreferences(
				"FingerPaintPreferences", MODE_PRIVATE);
		Integer imageNumber = prefs.getInt("imageNumber", 1);
		File file = null;
		
		if (externalMediaChecker()) {
			DecimalFormat form = new DecimalFormat("0000");
			String path = Environment.getExternalStorageDirectory() + "/mypaint/";
			File outDir = new File(path);
			if (!outDir.exists()) {
				outDir.mkdir();
			}
			do {
				file = new File(path + "img" + form.format(imageNumber) + ".png");
				imageNumber++;
			} while (file.exists());
			if (writeImage(file)) {
				scanMedia(file.getPath());
				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("imageNumber", imageNumber);
				editor.commit();
			}
		}
	}


	private boolean writeImage(File file) {
		// TODO 自動生成されたメソッド・スタブ
		try {
			FileOutputStream fo = new FileOutputStream(file);
			this.bitmap.compress(CompressFormat.PNG, 100, fo);
			fo.flush();
			fo.close();
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			return false;
		}
		return true;
	}


	private boolean externalMediaChecker() {
		// TODO 自動生成されたメソッド・スタブ
		boolean result = false;
		String status = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(status)) {
			result = true;
		}
		return result;
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO 自動生成されたメソッド・スタブ
		MenuInflater mi = this.getMenuInflater();
		mi.inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO 自動生成されたメソッド・スタブ
		switch (item.getItemId()) {
		case R.id.menu_save:
			this.save();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	MediaScannerConnection mc;
	public void scanMedia(final String fp) {
		this.mc = new MediaScannerConnection(this, new MediaScannerConnection.MediaScannerConnectionClient() {
			public void onScanCompleted(String path, Uri uri) {
				disconnect();
			}
			public void onMediaScannerConnected() {
				scanFile(fp);
			}
		});
		mc.connect();
	}
	
	public void scanFile(String fp) {
		mc.scanFile(fp, "image/png");
	}
	
	public void disconnect() {
		mc.disconnect();
	}
}
