/*
 * Copyright 2011 Jonathan Hedrén
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jonathanhedren.android.modelbinder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import android.R.bool;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ModelBinder {

	//private Map<Field, View> mFieldViewMap = new HashMap<Field, View>();
	
	private OnModelUpdateListener mOnModelUpdateListener;

	public interface OnModelUpdateListener {
		void modelUpdated(Object model, Field field);
	}

	
	/**
	 * @param onModelUpdateListener
	 */
	public void setOnModelUpdateListener(final OnModelUpdateListener onModelUpdateListener) {
		mOnModelUpdateListener = onModelUpdateListener;
	}
	
	/**
	 * @param field
	 */
	private void modelUpdated(final Object model, final Field field) {
		if (mOnModelUpdateListener != null) {
			mOnModelUpdateListener.modelUpdated(model, field);
		}
	}
	
	/**
	 * 
	 * @param model
	 * @param rootView
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public void bind(final Object model, final View rootView) throws Exception {

		if (model == null) {
			return;
		}
		
		try {
		
			Class<?> modelType = model.getClass();
			
			if (!modelType.isArray() 
					&& (modelType.isPrimitive() 
					|| isWrapperType(modelType) 
					|| String.class.isAssignableFrom(modelType))) {
				
				throw new IllegalArgumentException("Primitives/wrappers can not be bound on their own.");
				
			} else if (modelType.isAssignableFrom(Collection.class) || modelType.isArray()) {
				
				if (rootView instanceof ListView) {
				
					ListAdapter listAdapter = ((ListView)rootView).getAdapter();
					
					Object[] modelArray;
					if (modelType.isArray()) {
						modelArray = (Object[])model;
					} else {
						modelArray = ((Collection)model).toArray();
					}
					
					int i = 0;
					for (Object item : modelArray) {
						bind(item, listAdapter.getView(i, null, null));
						i++;
					}
				}
				
				return;
			} 
			
			// TODO: Handle Map's
			
			final Field[] fields = model.getClass().getDeclaredFields();
			final Method[] methods = model.getClass().getMethods();
			
			for (final Field field : fields) {
				
				BindTo annotation = field.getAnnotation(BindTo.class);

				final Class<?> fieldType = field.getType();
				
				Method getter = findGetter(methods, field.getName());
				
				// Get the value of the field either by a getter if it exist
				// or by using reflection to set the field accessible.
				Object fieldValue;
				if (getter == null) {
					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					fieldValue = field.get(model);
				} else {
					fieldValue = getter.invoke(model, new Object[] { });
				}
				
				// Traverse down the object hierarchy
				if (annotation == null 
						&& !fieldType.isPrimitive() 
						&& !isWrapperType(fieldType) 
						&& !String.class.isAssignableFrom(fieldType)) {
					
					bind(fieldValue, rootView);
					continue;
				}
				
				// Get all view ids to bind to
				int[] viewIds = annotation.value();
				
				for (int viewId : viewIds) {
			
					final View targetView = rootView.findViewById(viewId);
					
					if (targetView == null) {
						continue;
					}
					
					if (targetView instanceof CompoundButton) {
						bindCompoundButton(model, methods, field, fieldValue, (CompoundButton)targetView, fieldType);
					} else if (targetView instanceof TextView) {
						bindTextView(model, methods, field, fieldValue, (TextView)targetView, fieldType);
					} else if (targetView instanceof SeekBar) {
						bindSeekBar(model, methods, field, fieldValue, (SeekBar)targetView, fieldType);
					} else if (targetView instanceof RatingBar) {
						bindRatingBar(model, methods, field, fieldValue, (RatingBar)targetView, fieldType);
					} else {
						bind(fieldValue, targetView);
					}
				}
			}
		} catch (Exception e) {
			throw new Exception("Could not bind the model to the view.", e);
		}
		
	}

	/**
	 * 
	 * @param model
	 * @param methods
	 * @param field
	 * @param fieldValue
	 * @param targetView
	 * @param fieldType
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private void bindRatingBar(final Object model, final Method[] methods, final Field field, final Object fieldValue,
			final RatingBar targetView, final Class<?> fieldType) 
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		if (fieldType != float.class) {
			throw new IllegalArgumentException("Views of 'RatingBar' type can only be bound to a fields of 'float' type.");
		}
		
		// Set the model value to the view.
		targetView.setRating((Float)fieldValue);

		// Add a listener for changes on the view and update the model accordingly.
		targetView.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
			
			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating,
					boolean fromUser) {

				if (!fromUser) {
					return;
				}
				
				try {
					
					Method setter = findSetter(methods, field.getName());
					
					if (setter == null) {
						if (!field.isAccessible()) {
							field.setAccessible(true);
						}
						field.setFloat(model, rating);
					} else {
						setter.invoke(model, new Object[] { rating });
					}
					
					modelUpdated(model, field);
					
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}	
			}
		});
		
	}

	/**
	 * 
	 * @param model
	 * @param methods
	 * @param field
	 * @param targetView
	 * @param fieldType
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private void bindSeekBar(final Object model, final Method[] methods, final Field field, final Object fieldValue,
			final SeekBar targetView, final Class<?> fieldType) 
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		if (fieldType != int.class) {
			throw new IllegalArgumentException("Views of 'ProgressBar' type can only be bound to a fields of 'int' type.");
		}
		
		// Set the model value to the view.
		targetView.setProgress((Integer)fieldValue);
		
		// Add a listener for changes on the view and update the model accordingly.
		targetView.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				
				if (!fromUser) {
					return;
				}
				
				try {
					
					Method setter = findSetter(methods, field.getName());
					
					if (setter == null) {
						if (!field.isAccessible()) {
							field.setAccessible(true);
						}
						field.setInt(model, progress);
					} else {
						setter.invoke(model, new Object[] { progress });
					}

					modelUpdated(model, field);

				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}	
			}
		});
	}

	/**
	 * 
	 * @param model
	 * @param methods
	 * @param field
	 * @param targetView
	 * @param fieldType
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private void bindCompoundButton(final Object model, final Method[] methods,
			final Field field, final Object fieldValue, final CompoundButton targetView, final Class<?> fieldType)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		if (fieldType != bool.class) {
			throw new IllegalArgumentException("Views of 'CompoundButton' type can only be bound to a fields of 'boolean' type.");
		}
		
		// Set the model value to the view.
		targetView.setChecked((Boolean)fieldValue);
	
		// Add a listener for changes on the view and update the model accordingly.
		targetView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				try {
					Method setter = findSetter(methods, field.getName());
						
					if (setter == null) {
						if (!field.isAccessible()) {
							field.setAccessible(true);
						}
						field.setBoolean(model, isChecked);
					} else {
						setter.invoke(model, isChecked);
					}
				
					modelUpdated(model, field);
					
				} catch (Exception e) {
					// TODO handle exception
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 
	 * @param model
	 * @param methods
	 * @param field
	 * @param targetView
	 * @param fieldType
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private void bindTextView(final Object model, final Method[] methods,
			final Field field, final Object fieldValue, final TextView targetView, final Class<?> fieldType)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		// Set the model value to the view.
		targetView.setText(String.valueOf(fieldValue));
		
		// Add a listener for focus changes that will updated the model field whenever
		// focus is lost from the view.
		targetView.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
				String text = s.toString();
				try {
					
					Method setter = findSetter(methods, field.getName());
					
					if (setter == null) {
						if (!field.isAccessible()) {
							field.setAccessible(true);
						}
						
						if (fieldType == int.class) {
							field.setInt(model, Integer.valueOf(text));
						} else if (fieldType == long.class || fieldType == Long.class) {
							field.setLong(model, Long.valueOf(text));
						} else if (fieldType == float.class) {
							field.setFloat(model, Float.valueOf(text));
						} else if (fieldType == double.class || fieldType == Double.class) {
							field.setDouble(model, Double.valueOf(text));
						} else {
							field.set(model, text);
						}
					} else {
						Class<?>[] parameterTypes = setter.getParameterTypes();
						Object[] args;
						if (parameterTypes[0] == int.class) {
							args = new Object[] { Integer.valueOf(text) };
						} else if (parameterTypes[0] == long.class) {
							args = new Object[] { Long.valueOf(text) };
						} else if (parameterTypes[0] == float.class) {
							args = new Object[] { Float.valueOf(text) };
						} else if (parameterTypes[0] == double.class) {
							args = new Object[] { Double.valueOf(text) };
						} else {
							args = new Object[] { text };
						}
						
						setter.invoke(model, args);
					}
					
					modelUpdated(model, field);
					
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}
	
	
	
	/**
	 * @param methods
	 * @param fieldName
	 * @return
	 */
	private static Method findSetter(final Method[] methods, final String fieldName) {
		final String setterName = String.format("set%s", uppercaseFirst(fieldName)); 
		for (Method method : methods) {
			if (method.getName() == setterName) {
				return method;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param methods
	 * @param fieldName
	 * @return
	 */
	private static Method findGetter(final Method[] methods, final String fieldName) {
		final String getterName = String.format("get%s", uppercaseFirst(fieldName)); 
		for (Method method : methods) {
			if (method.getName() == getterName) {
				return method;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param string
	 * @return
	 */
	private static String uppercaseFirst(String string) {
		return Character.toUpperCase(string.charAt(0)) + string.substring(1);
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	private static boolean isWrapperType(Class<?> type) {
        return type.equals(Boolean.class) || 
               type.equals(Integer.class) ||
               type.equals(Character.class) ||
               type.equals(Byte.class) ||
               type.equals(Short.class) ||
               type.equals(Double.class) ||
               type.equals(Long.class) ||
               type.equals(Float.class);
	}
	

}
