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
	
	private void initUI() //��ʼ��UI
	{
		etremoteip = (EditText)findViewById(R.id.etremoteip);
		etremoteport = (EditText)findViewById(R.id.etremoteport);
		etlocalport = (EditText)findViewById(R.id.etlocalport);
		etrdata = (EditText)findViewById(R.id.etrdata); //��ʾ����
		etrdata.setMovementMethod(ScrollingMovementMethod.getInstance()); //���ù�����ʽ ��ȡʵ��
		etsdata = (EditText)findViewById(R.id.etsdata); //��������
		cbshex = (CheckBox)findViewById(R.id.cbshex); // ����16����
		cbrhex = (CheckBox)findViewById(R.id.cbrhex); //����16����
		cbshex.setChecked(true); //��
		cbrhex.setChecked(true); //��
		cbshex.setOnCheckedChangeListener(this);
		cbrhex.setOnCheckedChangeListener(this);
		btnconnect = (Button)findViewById(R.id.btnconnect); //����
		btnconnect.setOnClickListener(this);
		btndisconnect = (Button)findViewById(R.id.btndisconnect); // �Ͽ�����
		btndisconnect.setEnabled(false); //�ر�
		btndisconnect.setOnClickListener(this);
		btnsend = (Button)findViewById(R.id.btnsend); //����
		btnsend.setEnabled(false); //�ر�
		btnsend.setOnClickListener(this);
		initCharCodes();
		
		ut = new udpthread(etrdata);
	}
	
	private void initCharCodes() //��ʼ���ַ�����
	{
		spscharcodes = (Spinner)findViewById(R.id.spscharcodes); //��������
		sprcharcodes = (Spinner)findViewById(R.id.sprcharcodes); //��������
		spscharcodes.setEnabled(false); //�ر�
		sprcharcodes.setEnabled(false); //�ر�
		spscharcodes.setOnItemSelectedListener(this);
		sprcharcodes.setOnItemSelectedListener(this);
		ArrayAdapter<String> itemvalues = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,codes); //����������  
		itemvalues.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);   //���������鿴��Դ
		spscharcodes.setAdapter(itemvalues);
		sprcharcodes.setAdapter(itemvalues);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) //�ڴ���ѡ��˵�
	{
		menu.add(0, 0, 0, R.string.menuexit);
		menu.add(0, 1, 1, R.string.menusave);
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId()==0) //��ȡ��ĿId
			finish();
		if (item.getItemId()==1)
		{
			SaveToFileDlg().show();
		}
		return super.onOptionsItemSelected(item);
	}

	private void uiState(boolean state) //ui��
	{
		btnconnect.setEnabled(state); //���Ӱ�ť��
		btndisconnect.setEnabled(!state); //�رնϿ����Ӱ�ť
		btnsend.setEnabled(!state); //�رշ��Ͱ�ť
		etremoteip.setEnabled(state); //Զ��IP������
		etremoteport.setEnabled(state); //Զ�̶˿ڿ�����
		etlocalport.setEnabled(state); //���ض˿ڿ�����
		cbshex.setEnabled(state);  //16���Ʒ��� �ɹ�ѡ
		cbrhex.setEnabled(state);  //16���ƽ��� �ɹ�ѡ
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
			String RemoteIP = etremoteip.getText().toString().trim(); //��ȡԶ��IP
			int RemotePort = Integer.parseInt(etremoteport.getText().toString().trim()); //��ȡԶ�̶˿�
			int LocalPort = Integer.parseInt(etlocalport.getText().toString().trim()); //��ȡ���ض˿�
			ut.setRemoteIP(RemoteIP); //����Զ��IP
			ut.setRemotePort(RemotePort); //����Զ�̶˿�
			ut.setLocalPort(LocalPort); //���ñ��ض˿�
			ut.setRHex(cbrhex.isChecked()); //����16���ƽ���
			ut.setSHex(cbshex.isChecked()); //����16���Ʒ���
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