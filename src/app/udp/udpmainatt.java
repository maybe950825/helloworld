package app.udp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Process;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;

import java.io.FileWriter;
import java.io.IOException;

public class udpmainatt extends Activity implements OnClickListener, OnItemSelectedListener, OnCheckedChangeListener 
{
	private static final String[] codes = {"UTF-8","GBK","GB2312","ISO-8895-1"};
	private EditText etremoteip;
	private EditText etremoteport;
	private EditText etlocalport;
	private EditText etrdata;
	private EditText etsdata;
	private EditText etlogfilepath;
	private CheckBox cbshex;
	private CheckBox cbrhex;
	private Spinner spscharcodes;
	private Spinner sprcharcodes;
	private Button btnconnect;
	private Button btndisconnect;
	private Button btnsend;
	
	private udpthread ut = null;
	
	private void initUI() //初始化UI
	{
		etremoteip = (EditText)findViewById(R.id.etremoteip);
		etremoteport = (EditText)findViewById(R.id.etremoteport);
		etlocalport = (EditText)findViewById(R.id.etlocalport);
		etrdata = (EditText)findViewById(R.id.etrdata); //显示数据
		etrdata.setMovementMethod(ScrollingMovementMethod.getInstance()); //设置滚动方式 获取实例
		etsdata = (EditText)findViewById(R.id.etsdata); //发送数据
		cbshex = (CheckBox)findViewById(R.id.cbshex); // 发送16进制
		cbrhex = (CheckBox)findViewById(R.id.cbrhex); //接收16进制
		cbshex.setChecked(true); //打开
		cbrhex.setChecked(true); //打开
		cbshex.setOnCheckedChangeListener(this);
		cbrhex.setOnCheckedChangeListener(this);
		btnconnect = (Button)findViewById(R.id.btnconnect); //连接
		btnconnect.setOnClickListener(this);
		btndisconnect = (Button)findViewById(R.id.btndisconnect); // 断开连接
		btndisconnect.setEnabled(false); //关闭
		btndisconnect.setOnClickListener(this);
		btnsend = (Button)findViewById(R.id.btnsend); //发送
		btnsend.setEnabled(false); //关闭
		btnsend.setOnClickListener(this);
		initCharCodes();
		
		ut = new udpthread(etrdata);
	}
	
	private void initCharCodes() //初始化字符编码
	{
		spscharcodes = (Spinner)findViewById(R.id.spscharcodes); //接收数据
		sprcharcodes = (Spinner)findViewById(R.id.sprcharcodes); //发送数据
		spscharcodes.setEnabled(false); //关闭
		sprcharcodes.setEnabled(false); //关闭
		spscharcodes.setOnItemSelectedListener(this);
		sprcharcodes.setOnItemSelectedListener(this);
		ArrayAdapter<String> itemvalues = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,codes); //数组适配器  
		itemvalues.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);   //设置下拉查看资源
		spscharcodes.setAdapter(itemvalues);
		sprcharcodes.setAdapter(itemvalues);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) //在创建选项菜单
	{
		menu.add(0, 0, 0, R.string.menuexit);
		menu.add(0, 1, 1, R.string.menusave);
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId()==0) //获取项目Id
			finish();
		if (item.getItemId()==1)
		{
			SaveToFileDlg().show();
		}
		return super.onOptionsItemSelected(item);
	}

	private void uiState(boolean state) //ui打开
	{
		btnconnect.setEnabled(state); //连接按钮打开
		btndisconnect.setEnabled(!state); //关闭断开连接按钮
		btnsend.setEnabled(!state); //关闭发送按钮
		etremoteip.setEnabled(state); //远程IP可输入
		etremoteport.setEnabled(state); //远程端口可输入
		etlocalport.setEnabled(state); //本地端口可输入
		cbshex.setEnabled(state);  //16进制发送 可勾选
		cbrhex.setEnabled(state);  //16进制接收 可勾选
	}
	
	private void SaveToFile(String FilePath)
	{
		String filevalue = etrdata.getText().toString().trim();
		if (filevalue.trim().equals("")) return;
		try
		{
			FileWriter fw = new FileWriter(FilePath,true);
			fw.write(filevalue);
			fw.close();
			fw = null;

			showMessage(getResources().getString(R.string.alertsavefiletilte),getResources().getString(R.string.alertsavefileok));
		}catch(IOException ie)
		{
			showMessage(getResources().getString(R.string.alertsavefiletilte),getResources().getString(R.string.alertsavefilefail) + ie.getMessage());
		}
	}

	private AlertDialog SaveToFileDlg()
	{
		LayoutInflater inflater = LayoutInflater.from(this);
		final View dlgView = inflater.inflate(R.layout.logfilesavedlg, null);
		Builder dlg = new AlertDialog.Builder(this);
		dlg.setTitle(R.string.alertsavefiletilte);
		dlg.setView(dlgView);
		dlg.setPositiveButton(R.string.alertok, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				etlogfilepath = (EditText)dlgView.findViewById(R.id.etsavefilepath);
				String s = etlogfilepath.getText().toString().trim();
				if (s.trim().equals("")) return;
				SaveToFile(s);
			}
		});
		dlg.setNegativeButton(R.string.alertcancel, null);
		
		return dlg.create();
	}
	 
	private void showMessage(String Title,String Info)
	{
		Builder alertdlg = new AlertDialog.Builder(this);
		alertdlg.setTitle(Title);
		alertdlg.setMessage(Info);
		alertdlg.setPositiveButton(R.string.alertok, null);
		alertdlg.show();
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        initUI();
    }

	@Override
	public void onClick(View v)
	{
		if (v.equals(btnconnect))
		{
			String RemoteIP = etremoteip.getText().toString().trim(); //获取远程IP
			int RemotePort = Integer.parseInt(etremoteport.getText().toString().trim()); //获取远程端口
			int LocalPort = Integer.parseInt(etlocalport.getText().toString().trim()); //获取本地端口
			ut.setRemoteIP(RemoteIP); //设置远程IP
			ut.setRemotePort(RemotePort); //设置远程端口
			ut.setLocalPort(LocalPort); //设置本地端口
			ut.setRHex(cbrhex.isChecked()); //设置16进制接收
			ut.setSHex(cbshex.isChecked()); //设置16进制发送
			if (ut.ConnectSocket())
				uiState(false);
		}
		if (v.equals(btndisconnect))
		{
			ut.DisConnectSocket();
			uiState(true);
		}
		if (v.equals(btnsend))
		{
			String SData = etsdata.getText().toString().trim();
			if (!SData.trim().equals(""))
				ut.SendData(SData);
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		Process.killProcess(Process.myPid());
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
	{
		Spinner tmp = (Spinner)parent;
		if (tmp.equals(sprcharcodes))
			ut.setCurrentRCodes(codes[position]);
		if (tmp.equals(spscharcodes))
			ut.setCurrentSCodes(codes[position]);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent)
	{
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (buttonView.equals(cbrhex))
			sprcharcodes.setEnabled(!cbrhex.isChecked());
		if (buttonView.equals(cbshex))
			spscharcodes.setEnabled(!cbshex.isChecked());
	}
}