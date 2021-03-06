package com.gdht.itasset;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gdht.itasset.R.id;
import com.gdht.itasset.adapter.RfidPanDianAdapter;
import com.gdht.itasset.db.service.LocalPandianService;
import com.gdht.itasset.db.service.ScanCheckRFIDSDBService;
import com.gdht.itasset.eventbus.GetPandianStrListener;
import com.gdht.itasset.eventbus.RefreshDatas;
import com.gdht.itasset.eventbus.RefreshNumberListener;
import com.gdht.itasset.http.HttpClientUtil;
import com.gdht.itasset.utils.AppSharedPreferences;
import com.gdht.itasset.utils.GlobalParams;
import com.gdht.itasset.widget.WaitingDialog;
import com.gdht.itasset.xintong.Accompaniment;
import com.gdht.itasset.xintong.App;
import com.gdht.itasset.xintong.DataTransfer;
import com.google.common.eventbus.EventBus;
import com.senter.support.openapi.StUhf.UII;
import com.senter.support.openapi.StUhf.InterrogatorModelDs.InterrogatorModelD2;
import com.senter.support.openapi.StUhf.InterrogatorModelDs.UmdErrorCode;
import com.senter.support.openapi.StUhf.InterrogatorModelDs.UmdFrequencyPoint;
import com.senter.support.openapi.StUhf.InterrogatorModelDs.UmdOnIso18k6cRealTimeInventory;
import com.senter.support.openapi.StUhf.InterrogatorModelDs.UmdRssi;

public class ScanPandianActivity extends Activity {
	private RfidPanDianAdapter adapter;
	private ArrayList<String> rfids = new ArrayList<String>();
	private ArrayList<String> selectRifds = new ArrayList<String>();
	private ListView listView;
	private final Accompaniment accompaniment = new Accompaniment(this,
			R.raw.tag_inventoried);
	private Handler accompainimentsHandler;
	private Timer mTimer;
	private TimerTask mTask;
	private String uii;
	private UII uii_change;
	private LinearLayout finishBtn, stopBtn, startBtn;
	private TextView num1;
	private WaitingDialog wd;
	private SharedPreferences loginSettings = null;
	private String planId;
	private String userid;
	private StringBuffer sb;
	private Handler saveHandler = new Handler();
	private ScanCheckRFIDSDBService checkRFIDSDBService;
	private final Runnable accompainimentRunnable = new Runnable() {
		@Override
		public void run() {
			accompaniment.start();
			accompainimentsHandler.removeCallbacks(this);
			// 截取rfid编号
			uii = DataTransfer.xGetString(mUii.getBytes()).substring(6, 41)
					.replace(" ", "");
			if (!rfids.contains(uii)) {
				rfids.add(uii);
				selectRifds.add(uii);
				Log.i("a", "uii = " + uii);
				adapter.notifyDataSetChanged();
			}
			if (selectRifds.size() > 0) {
				num1.setText(selectRifds.size() + "");
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_scan_pandian_new);
		de.greenrobot.event.EventBus.getDefault().register(this);
		wd = new WaitingDialog(this);
		checkRFIDSDBService = new ScanCheckRFIDSDBService(this);
		loginSettings = this.getSharedPreferences("GDHT_ITASSET_SETTINGS", Context.MODE_PRIVATE);
		userid = loginSettings.getString("LOGIN_NAME", "");
		planId = this.getIntent().getStringExtra("planId");
		
		findViews();

		if (App.getRfid() == null) {
			Toast.makeText(this, R.string.MakeSurePDAexitRfid,
					Toast.LENGTH_LONG).show();
			finish();
		} else {
			switch (App.getRfid().getInterrogatorModel()) {

			case InterrogatorModelD2: {
				AppSharedPreferences asp = new AppSharedPreferences(
						ScanPandianActivity.this, "gdht");
				App.getRfid().setPower(asp.getGongLv());
				// Toast.makeText(ScanPandianActivity.this, "当前功率是  = " +
				// App.getRfid().getPower(), 0).show();
				// findViews();
				// setOnClicks();
				break;
			}

			default:
				break;
			}
		}
		HandlerThread htHandlerThread = new HandlerThread("");
		htHandlerThread.start();
		accompainimentsHandler = new Handler(htHandlerThread.getLooper());
		accompaniment.init();

		startBtn.setVisibility(View.GONE);
		rfids.addAll(checkRFIDSDBService.loadDatas(userid));
		selectRifds.addAll(rfids);
		if (selectRifds.size() > 0) {
			num1.setText(selectRifds.size() + "");
		}
		start();
		saveHandler.postDelayed(saveRunnable, 300000);
	}
	
	Runnable saveRunnable = new Runnable() {
		
		@Override
		public void run() {
			Log.i("a", "save !!!!!!!!!!!!!!");
			checkRFIDSDBService.deleteAll(userid);
			for(String s : rfids) {
				checkRFIDSDBService.saveRFID(s, userid);
			}
			saveHandler.postDelayed(this, 300000);
		}
	};

	private void findViews() {
		listView = (ListView) this.findViewById(R.id.listView);
		adapter = new RfidPanDianAdapter(this, rfids, selectRifds, planId);
		listView.setAdapter(adapter);
		finishBtn = (LinearLayout) this.findViewById(R.id.finish);
		startBtn = (LinearLayout) this.findViewById(R.id.start);
		stopBtn = (LinearLayout) this.findViewById(R.id.stop);
		num1 = (TextView) this.findViewById(R.id.num1);
	}

	public void onEvent(RefreshNumberListener event) {
		num1.setText(selectRifds.size() + "");
	}

	public void btnClick(View view) {
		switch (view.getId()) {
		case R.id.back:
			this.finish();
			break;
		case R.id.finish:
			
			if(selectRifds.size() <= 0) {
				Toast.makeText(this, "请先选择要盘点的资产!", 0).show();
			}else {
				sb = new StringBuffer();
				for(String s : selectRifds) {
					sb.append("'").append(s).append("'").append(",");
				}
				sb.deleteCharAt(sb.length() - 1);
				if(GlobalParams.LOGIN_TYPE == 1) {
					new PanDianAt().execute("");
				}else {
					GlobalParams.pandian_str = sb.toString();
					ScanPandianActivity.this.finish();
				}
			}
			
			
			break;
		case R.id.stop:
			stopBtn.setVisibility(View.GONE);
			startBtn.setVisibility(View.VISIBLE);
			stop();
			break;
		case R.id.start:
			stopBtn.setVisibility(View.VISIBLE);
			startBtn.setVisibility(View.GONE);
			start();
			break;
		case R.id.ewscanBtn:
			Intent intent = new Intent(this, ErWeiScanCaptureActivity.class);
			startActivityForResult(intent, 100);
			break;
		case R.id.clear:
			rfids.clear();
			selectRifds.clear();
			num1.setText(selectRifds.size() + "");
			checkRFIDSDBService.deleteAll(userid);
			adapter.notifyDataSetChanged();
			break;
		}
	}

	// 启动
	private void start() {
		if (mTimer == null) {
			mTimer = new Timer();
		}
		mTask = new TimerTask() {
			@Override
			public void run() {
				startInventory();
			}
		};
		mTimer.schedule(mTask, 1000, 10);

	}

	// 停止
	private void stop() {
		App.stop();
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		if (mTask != null) {
			mTask.cancel();
			mTask = null;
		}
	}

	private UII mUii;

	// 开启RFID扫描器
	protected void startInventory() {
		App.getRfid()
				.getInterrogatorAs(InterrogatorModelD2.class)
				.iso18k6cRealTimeInventory(1,
						new UmdOnIso18k6cRealTimeInventory() {

							@Override
							public void onTagInventory(UII uii,
									UmdFrequencyPoint frequencyPoint,
									Integer antennaId, UmdRssi rssi) {
								mUii = uii;

							}

							@Override
							public void onFinishedSuccessfully(Integer arg0,
									int arg1, int arg2) {
								if (mUii != null) {
									addToplay(mUii);
								}

							}

							@Override
							public void onFinishedWithError(UmdErrorCode arg0) {

							}
						});

	}

	protected void addToplay(com.senter.support.openapi.StUhf.UII uii2) {
		uii_change = uii2;
		tagAccompainiment();

	}

	private void tagAccompainiment() {
		this.runOnUiThread(accompainimentRunnable);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case 100:
			Bundle bundle = data.getExtras();
			String code = bundle.getString("result") + "0000000";
			if (rfids.contains(code)) {
				Toast.makeText(ScanPandianActivity.this, "已经存在!", 0).show();
			} else {

				rfids.add(code);
				selectRifds.add(code);
				adapter.notifyDataSetChanged();
				if (selectRifds.size() > 0) {
					num1.setText(selectRifds.size() + "");
				}
			}
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		de.greenrobot.event.EventBus.getDefault().unregister(this);
		stop();
		saveHandler.removeCallbacks(saveRunnable);
		checkRFIDSDBService.closeDB();
	}
	
	private class PanDianAt extends AsyncTask<String, Integer, Boolean> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			wd.show();
		}
		
		@Override
		protected Boolean doInBackground(String... arg0) {
			return new HttpClientUtil(ScanPandianActivity.this).updateManyRfidToYp(ScanPandianActivity.this, planId, sb.toString(), userid);
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			wd.dismiss();
			if(result) {
				de.greenrobot.event.EventBus.getDefault().post(new RefreshDatas());
				ScanPandianActivity.this.finish();
			}else {
				Toast.makeText(ScanPandianActivity.this, "资产盘点失败，请稍后再试!", 0).show();
			}
		}
	
	}
}
