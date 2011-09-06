package com.jonathanhedren.android.modelbinder.test;

import com.jonathanhedren.android.modelbinder.BindTo;
import com.jonathanhedren.android.modelbinder.R;

public class TestModel {

	@BindTo({ R.id.checkBox1, R.id.radioButton1 })
	private boolean primBool;
	
	@BindTo(R.id.seekBar1)
	private int primInt;

	@BindTo(R.id.ratingBar1)
	private float primFloat;
	
	@SuppressWarnings("unused")
	@BindTo(R.id.toggleButton1)
	private Boolean wrappedBool;
	
	@BindTo(R.id.editText3)
	private String text;
	
	@BindTo(R.id.editTextSigned)
	private Integer wrappedInt;
	
	@BindTo(R.id.editTextSigned)
	private Long wrappedLong;
	
	@BindTo(R.id.editTextDecimal)
	private Double wrappedDouble;
	
	@BindTo(R.id.editTextDecimal)
	private Float wrappedFloat;
	
	private TestModel2 testModel2;
	
	public int getPrimInt() {
		return primInt;
	}
	public void setPrimInt(int primInt) {
		this.primInt = primInt;
	}
	public float getPrimFloat() {
		return primFloat;
	}
	public void setPrimFloat(float primFloat) {
		this.primFloat = primFloat;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Integer getWrappedInt() {
		return wrappedInt;
	}
	public void setWrappedInt(Integer wrappedInt) {
		this.wrappedInt = wrappedInt;
	}
	public Long getWrappedLong() {
		return wrappedLong;
	}
	public void setWrappedLong(Long wrappedLong) {
		this.wrappedLong = wrappedLong;
	}
	public Double getWrappedDouble() {
		return wrappedDouble;
	}
	public void setWrappedDouble(Double wrappedDouble) {
		this.wrappedDouble = wrappedDouble;
	}
	public Float getWrappedFloat() {
		return wrappedFloat;
	}
	public void setWrappedFloat(Float wrappedFloat) {
		this.wrappedFloat = wrappedFloat;
	}
	public void setPrimBool(boolean primBool) {
		this.primBool = primBool;
	}
	public boolean isPrimBool() {
		return primBool;
	}
	public void setTestModel2(TestModel2 testModel2) {
		this.testModel2 = testModel2;
	}
	public TestModel2 getTestModel2() {
		return testModel2;
	}
}
