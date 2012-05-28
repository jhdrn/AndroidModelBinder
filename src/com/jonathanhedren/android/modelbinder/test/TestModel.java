package com.jonathanhedren.android.modelbinder.test;

import com.jonathanhedren.android.modelbinder.BindTo;

public class TestModel {

	@BindTo({ "checkBox1", "radioButton1" })
	private boolean primBool;
	
	@BindTo("seekBar1")
	private int primInt;

	@BindTo("ratingBar1")
	private float primFloat;
	
	@SuppressWarnings("unused")
	@BindTo("toggleButton1")
	private Boolean wrappedBool;
	
	@BindTo("editText3")
	private String text;
	
	@BindTo("editTextSigned")
	private Integer wrappedInt;
	
	@BindTo("editTextSigned")
	private Long wrappedLong;
	
	@BindTo("editTextDecimal")
	private Double wrappedDouble;
	
	@BindTo("editTextDecimal")
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
