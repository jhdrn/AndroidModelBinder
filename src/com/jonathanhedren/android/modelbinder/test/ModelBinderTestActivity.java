package com.jonathanhedren.android.modelbinder.test;

import android.app.Activity;
import android.app.Service;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.jonathanhedren.android.modelbinder.ModelBinder;
import com.jonathanhedren.android.modelbinder.R;

public class ModelBinderTestActivity extends Activity {

	private ModelBinder mModelBinder = ModelBinder.newInstance(R.id.class);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Service.LAYOUT_INFLATER_SERVICE);
		View rootView = layoutInflater.inflate(R.layout.main, null);
		
		TestModel model = new TestModel();
		model.setPrimInt(8);
		model.setPrimFloat(6.4f);
		model.setText("Some text");
		model.setPrimBool(true);
		
		TestModel2 model2 = new TestModel2();
		model2.setText("Some test text");
		
		model.setTestModel2(model2);
		
		setContentView(rootView);
		
		mModelBinder.bind(model, rootView);
	}
	
}
