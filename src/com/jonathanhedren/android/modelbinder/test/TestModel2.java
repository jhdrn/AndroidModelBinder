package com.jonathanhedren.android.modelbinder.test;

import com.jonathanhedren.android.modelbinder.BindTo;
import com.jonathanhedren.android.modelbinder.R;

public class TestModel2 {
	
	@BindTo(R.id.textView1)
	private String text;

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
