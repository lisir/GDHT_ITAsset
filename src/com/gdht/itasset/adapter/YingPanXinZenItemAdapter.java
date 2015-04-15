package com.gdht.itasset.adapter;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gdht.itasset.CangKuSelectActivity;
import com.gdht.itasset.PanYingActivity;
import com.gdht.itasset.R;
import com.gdht.itasset.http.HttpClientUtil;
import com.gdht.itasset.pojo.YingPanXinZengItem;
import com.gdht.itasset.widget.WaitingDialog;

public class YingPanXinZenItemAdapter extends BaseAdapter {
	private Context context;
	private List<YingPanXinZengItem> items;
	private LayoutInflater inflater;
	private YingPanXinZengItem item;
	private AlertDialog nameAd;
	private WaitingDialog dialog;

	public YingPanXinZenItemAdapter(Context context,
			List<YingPanXinZengItem> items) {
		this.inflater = LayoutInflater.from(context);
		this.context = context;
		this.items = items;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int arg0) {
		return items.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup arg2) {
		ViewHolder vh;
		// if(convertView == null){
		// convertView = inflater.inflate(R.layout.item_xinzengpanying, null);
		// vh = new ViewHolder();
		// vh.rfidTv = (TextView)convertView.findViewById(R.id.rfidCode);
		// vh.nameEt = (EditText)convertView.findViewById(R.id.name);
		// vh.quyuTv = (TextView) convertView.findViewById(R.id.quyu);
		// convertView.setTag(vh);
		// }
		// vh = (ViewHolder) convertView.getTag();
		item = items.get(position);
		// vh.rfidTv.setText(item.getRfid());
		// vh.nameEt.setText(item.getName());
		// vh.quyuTv.setText(item.getQuyu());
		convertView = inflater.inflate(R.layout.item_xinzengpanying, null);
		TextView rfidTv = (TextView) convertView.findViewById(R.id.rfidCode);
		TextView nameEt = (TextView) convertView.findViewById(R.id.name);
		TextView fuzerenTv = (TextView) convertView
				.findViewById(R.id.fuzerenname);
		// TextView quyuTv = (TextView) convertView.findViewById(R.id.quyu);
		RelativeLayout nameLayout = (RelativeLayout) convertView
				.findViewById(R.id.nameLayout);
		RelativeLayout fuzerenLayout = (RelativeLayout) convertView
				.findViewById(R.id.fuzerenLayout);
		ImageView addBtn = (ImageView) convertView.findViewById(R.id.addBtn);
		nameLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showNameAd(position);
			}
		});
		fuzerenLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showFuZeRenNameAd(position);
			}
		});
		addBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				YingPanXinZengItem item = items.get(position);
				if (TextUtils.isEmpty(item.getName())) {
					Toast.makeText(context, "请填写名称!", 0).show();
				} else if (TextUtils.isEmpty(item.getKeeper())) {
					Toast.makeText(context, "请填写责任人!", 0).show();
				} else {
					// Toast.makeText(context, "item = " + item.toString(),
					// 1).show();
					Log.i("a", "item = " + item.toString());
					new isHasRfidAs(item).execute("");
				}
			}
		});
		rfidTv.setText(item.getRfid_labelnum());
		nameEt.setText(item.getName());
		fuzerenTv.setText(item.getKeeper());
		// quyuTv.setText(item.getQuyu());
		return convertView;
	}

	private class isHasRfidAs extends AsyncTask<String, Integer, String> {
		private YingPanXinZengItem asItem;

		public isHasRfidAs(YingPanXinZengItem _item) {
			asItem = _item;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new WaitingDialog(context, R.style.TRANSDIALOG);
			dialog.show();
		}

		@Override
		protected String doInBackground(String... arg0) {
			return new HttpClientUtil(context).isHasRfid(context,
					asItem.getRfid_labelnum());
		}

		@Override
		protected void onPostExecute(String result) {
			dialog.dismiss();
			if ("true".equals(result)) {
				Toast.makeText(context, "RFID码已经存在!", 0).show();
			} else {
				new pyxzAs(asItem).execute("");
			}
		}
	}

	private class pyxzAs extends AsyncTask<String, Integer, String> {
		private YingPanXinZengItem asItem;

		public pyxzAs(YingPanXinZengItem _item) {
			asItem = _item;
		}

		@Override
		protected void onPreExecute() {
			dialog = new WaitingDialog(context, R.style.TRANSDIALOG);
			dialog.show();
		}

		@Override
		protected String doInBackground(String... arg0) {
			return new HttpClientUtil(context).pyxz(context, asItem);
		}

		@Override
		protected void onPostExecute(String result) {
			dialog.dismiss();
			if ("1".equals(result)) {
				items.remove(asItem);
				notifyDataSetChanged();
			} else if ("2".equals(result)) {
				Toast.makeText(context, "新增失败!", 0).show();
			}
		}
	}

	static class ViewHolder {
		private TextView rfidTv, quyuTv;
		private EditText nameEt;
	}

	private void showNameAd(final int position) {
		View nameAdView = inflater.inflate(R.layout.dialog_yingpan_name, null);
		final EditText nameEt = (EditText) nameAdView.findViewById(R.id.name);
		TextView title = (TextView) nameAdView.findViewById(R.id.title);
		title.setText("名称");
		ImageView back = (ImageView) nameAdView.findViewById(R.id.back);
		ImageView sure = (ImageView) nameAdView.findViewById(R.id.sure);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				nameAd.dismiss();
			}
		});
		sure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (TextUtils.isEmpty(nameEt.getText())) {
					Toast.makeText(context, "请填写名称.", 0).show();
				} else {
					nameAd.dismiss();
					items.get(position).setName(nameEt.getText().toString());
					YingPanXinZenItemAdapter.this.notifyDataSetChanged();
				}
			}
		});
		nameAd = new AlertDialog.Builder(context).setView(nameAdView).create();
		nameAd.show();
	}

	private void showFuZeRenNameAd(final int position) {
		View nameAdView = inflater.inflate(R.layout.dialog_yingpan_name, null);
		final EditText nameEt = (EditText) nameAdView.findViewById(R.id.name);
		TextView title = (TextView) nameAdView.findViewById(R.id.title);
		title.setText("责任人");
		ImageView back = (ImageView) nameAdView.findViewById(R.id.back);
		ImageView sure = (ImageView) nameAdView.findViewById(R.id.sure);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				nameAd.dismiss();
			}
		});
		sure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (TextUtils.isEmpty(nameEt.getText())) {
					Toast.makeText(context, "请填写责任人.", 0).show();
				} else {
					nameAd.dismiss();
					items.get(position).setKeeper(nameEt.getText().toString());
					YingPanXinZenItemAdapter.this.notifyDataSetChanged();
				}
			}
		});
		nameAd = new AlertDialog.Builder(context).setView(nameAdView).create();
		nameAd.show();
	}
}
