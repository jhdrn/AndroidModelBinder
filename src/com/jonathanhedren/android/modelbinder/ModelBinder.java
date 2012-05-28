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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ModelBinder {

	private static final String LOG_TAG = ModelBinder.class.getName();
	private static final String NULL_TEXT = "null";
	private static final String EMPTY_STRING = "";
	
	private OnModelUpdateListener mOnModelUpdateListener;
	private OnViewUpdateListener mOnViewUpdateListener;
	private Class<?> mResourceClass;

	public interface OnModelUpdateListener {
		/**
		 * 
		 * @param view
		 * @param field
		 * @return 
		 */
		boolean onModelUpdate(View view, Field field);
	}
	
	
	public interface OnViewUpdateListener {
		/**
		 * 
		 * @param value 
		 * @param v the view which the value will be bound to.
		 * @return the value
		 */
		Object onViewUpdate(Object value, View v);
	}
	
	private static class ModelMetadata<T> {

		private static final String LOG_TAG = ModelMetadata.class.getName();
		private static Map<Class<?>, ModelMetadata<?>> mModelMetadataMap = new HashMap<Class<?>, ModelMetadata<?>>();

		private Class<T> mModelClass;
		private List<Field> mFields;
		private Method[] mMethods;

		/**
		 * @throws NoSuchMethodException
		 * @throws SecurityException
		 * 
		 */
		@SuppressWarnings("unchecked")
		public static <T> ModelMetadata<T> getInstance(final Class<T> clazz) {
			ModelMetadata<?> modelMetadata = mModelMetadataMap.get(clazz);
			if (modelMetadata == null) {
				modelMetadata = new ModelMetadata<T>(clazz);
				mModelMetadataMap.put(clazz, modelMetadata);
			}
			return (ModelMetadata<T>) modelMetadata;
		}

		private ModelMetadata(final Class<T> clazz) {

			mFields = getAllFields(clazz);
			mMethods = clazz.getMethods();
			mModelClass = clazz;
		}

		public List<Field> getFields() {
			return mFields;
		}

		public Method[] getMethods() {
			return mMethods;
		}

		public Class<T> getModelClass() {
			return mModelClass;
		}
		
		/**
		 * Return the set of fields declared at all level of class hierarchy
		 */
		private List<Field> getAllFields(final Class<?> clazz) {
			return getAllFieldsRecursively(clazz, new ArrayList<Field>());
		}

		private List<Field> getAllFieldsRecursively(final Class<?> clazz,
				final List<Field> foundFields) {
			final Class<?> superClazz = clazz.getSuperclass();
			if (superClazz != null) {
				getAllFieldsRecursively(superClazz, foundFields);
			}
			foundFields.addAll(Arrays.asList(clazz.getDeclaredFields()));
			return foundFields;
		}

		public BindTo getAnnotation(Field field) {
			// FIXME
			return field.getAnnotation(BindTo.class);
		}

		public Class<?> getFieldType(Field field) {
			// TODO Auto-generated method stub
			return field.getType();
		}

	}
		
	public ModelBinder(final Class<?> resourceClass) {
		if (resourceClass == null) {
			Log.e(LOG_TAG, "The resourceClass parameter cannot be null. It should be R.id.class of your project");
		}
		
		mResourceClass = resourceClass;
	}
	
	/**
	 * @param onModelUpdateListener
	 */
	public void setOnModelUpdateListener(final OnModelUpdateListener onModelUpdateListener) {
		mOnModelUpdateListener = onModelUpdateListener;
	}
	
	/**
	 * 
	 * @param onViewUpdateListener
	 */
	public void setOnViewUpdateListener(final OnViewUpdateListener onViewUpdateListener) {
		mOnViewUpdateListener = onViewUpdateListener;
	}
	
	/**
	 * @param field
	 */
	private boolean onModelUpdate(final View view, final Field field) {
		if (mOnModelUpdateListener != null) {
			return mOnModelUpdateListener.onModelUpdate(view, field);
		}
		return false;
	}
	
	private Object onViewUpdate(final Object value, final View view) {
		if (mOnViewUpdateListener != null) {
			return mOnViewUpdateListener.onViewUpdate(value, view);
		}
		return value;
	}
	
	public static ModelBinder newInstance(final Class<?> resourceClass) {
		return new ModelBinder(resourceClass);
	}
	
	/**
	 * 
	 * @param model
	 * @param rootView
	 * @param resourceClass eg. R.id.class
	 */
	public void bind(final Object model, final View rootView) {
		
		if (model == null) {
			return;
		}
		
		try {
		
			final Class<?> modelType = model.getClass();
			ModelMetadata<?> modelMetadata = ModelMetadata.getInstance(modelType);
			
			if (model instanceof Collection 
				|| modelType.isAssignableFrom(Collection.class) 
				|| model instanceof Map
				|| modelType.isAssignableFrom(Map.class)
				|| modelType.isArray()) {
			
				Log.w(LOG_TAG, "There is no support for Map/Collection/array types. Use a ListView and ArrayAdapter instead.");
				return;
			} else if (modelType.isPrimitive() 
				|| isWrapperType(modelType) 
				|| String.class.isAssignableFrom(modelType)) {
				Log.w(LOG_TAG, "Primitives/wrappers can not be bound on their own.");
				return;
			} 
			
			final Method[] methods = modelMetadata.getMethods();
			
			for (final Field field : modelMetadata.getFields()) {
				
				final BindTo annotation = modelMetadata.getAnnotation(field);
				final Class<?> fieldType = modelMetadata.getFieldType(field);
			
				
				final Method getter = findGetter(methods, field.getName());
//				FIXME: modelMetatadata.getGetter(field);
				
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
				
				if (annotation == null) {
				
					// Traverse down the object hierarchy
					if (!fieldType.isPrimitive() 
							&& !isWrapperType(fieldType) 
							&& !String.class.isAssignableFrom(fieldType)) {
				
						bind(fieldValue, rootView);
					}
					continue;
				}
				
				// Get all view ids to bind to
				final String[] viewIds = annotation.value();
				
				for (String stringViewId : viewIds) {
							
					final int viewId = getResId(stringViewId);
					
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
			Log.w(LOG_TAG, "Could not bind the model to the view: " + e.getMessage());
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
	private void bindRatingBar(final Object model, final Method[] methods, final Field field, Object fieldValue,
			final RatingBar targetView, final Class<?> fieldType) 
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {


		fieldValue = onViewUpdate(fieldValue, targetView);

		if (fieldValue != null) {
			final Class<? extends Object> fieldValueType = fieldValue.getClass();
			if (fieldValueType != float.class && fieldValueType != Float.class) {
				throw new IllegalArgumentException("Views of 'RatingBar' type can only be bound to a fields of 'float' or 'Float' type.");
			}
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
				
				if (onModelUpdate(ratingBar, field)) {
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
					
					
					
				} catch (Exception e) {
					Log.e(LOG_TAG, "Could not set RatingBar value to model field.", e);
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
	private void bindSeekBar(final Object model, final Method[] methods, final Field field, Object fieldValue,
			final SeekBar targetView, final Class<?> fieldType) 
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
	
		fieldValue = onViewUpdate(fieldValue, targetView);

		if (fieldValue != null) {
			final Class<? extends Object> fieldValueType = fieldValue.getClass();
			if (fieldValueType != int.class && fieldValueType != Integer.class) {
				throw new IllegalArgumentException("Views of 'SeekBar' type can only be bound to a fields of 'int' or 'Integer' type.");
			}
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

				if (onModelUpdate(seekBar, field)) {
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


				} catch (Exception e) {
					Log.e(LOG_TAG, "Could not SeekBar value to model field.", e);
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
			final Field field, Object fieldValue, final CompoundButton targetView, final Class<?> fieldType)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		
		fieldValue = onViewUpdate(fieldValue, targetView);

		if (fieldValue != null) {
			
			final Class<? extends Object> fieldValueType = fieldValue.getClass();
			if (fieldValueType != boolean.class && fieldValueType != Boolean.class) {
				throw new IllegalArgumentException("Views of 'CompoundButton' type can only be bound to a fields of 'boolean' or 'Boolean' type.");
			}
		} else {
			fieldValue = false;
		}
		
		
		// Set the model value to the view.
		targetView.setChecked((Boolean)fieldValue);
	
		// Add a listener for changes on the view and update the model accordingly.
		targetView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				if (onModelUpdate(buttonView, field)) {
					return;
				}
				
				try {
					
					Method setter = findSetter(methods, field.getName());
					
					if (setter == null) {
						if (!field.isAccessible()) {
							field.setAccessible(true);
						}
						
						if (fieldType.isPrimitive()) {
							field.setBoolean(model, isChecked);	
						} else {
							field.set(model, isChecked);
						}
						
					} else {
						setter.invoke(model, isChecked);
					}
					
				} catch (Exception e) {
					Log.e(LOG_TAG, "Could not set CompoundButton value to model field.", e);
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
			final Field field, Object fieldValue, final TextView targetView, final Class<?> fieldType)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		fieldValue = onViewUpdate(fieldValue, targetView);
		
		// Set the model value to the view.
		if (fieldValue == null) {
			targetView.setText(null);
		} else {
			targetView.setText(String.valueOf(fieldValue));
		}
		
		// Add a listener for focus changes that will updated the model field whenever
		// focus is lost from the view.
		targetView.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				

				if (onModelUpdate(targetView, field)) {
					return;
				}
				
				String text = s.toString();
				
				if (fieldType.isPrimitive() && (text == null || text.trim().equals(EMPTY_STRING))) {
					return;
				}
				
				try {
					
					Method setter = findSetter(methods, field.getName());
					
					if (setter == null) {
						if (!field.isAccessible()) {
							field.setAccessible(true);
						}
						if (text.equals(NULL_TEXT)) {
							field.set(model, null);
						} else if (fieldType == int.class || fieldType == Integer.class) {
							if (!EMPTY_STRING.equals(text)) {
								field.setInt(model, Integer.valueOf(text));
							}
						} else if (fieldType == long.class || fieldType == Long.class) {
							if (!EMPTY_STRING.equals(text)) {
								field.setLong(model, Long.valueOf(text));
							}
						} else if (fieldType == float.class || fieldType == Float.class) {
							if (!EMPTY_STRING.equals(text)) {
								field.setFloat(model, Float.valueOf(text));
							}
						} else if (fieldType == double.class || fieldType == Double.class) {
							if (!EMPTY_STRING.equals(text)) {
								field.setDouble(model, Double.valueOf(text));
							}
						} else {
							field.set(model, text);
						}
					} else {
						Class<?>[] parameterTypes = setter.getParameterTypes();
						
						if (text.equals(NULL_TEXT)) {
							setter.invoke(model, (Object)null);
						} else if (parameterTypes[0] == int.class || parameterTypes[0] == Integer.class) {
							if (!EMPTY_STRING.equals(text)) {
								setter.invoke(model, Integer.valueOf(text));
							}
						} else if (parameterTypes[0] == long.class || parameterTypes[0] == Long.class) {
							if (!EMPTY_STRING.equals(text)) {
								setter.invoke(model, Long.valueOf(text));
							}
						} else if (parameterTypes[0] == float.class || parameterTypes[0] == Float.class) {
							if (!EMPTY_STRING.equals(text)) {
								setter.invoke(model, Float.valueOf(text));
							}
						} else if (parameterTypes[0] == double.class || parameterTypes[0] == Double.class) {
							if (!EMPTY_STRING.equals(text)) {
								setter.invoke(model, Double.valueOf(text));
							}
						} else {
							setter.invoke(model, text);
						}
					}
					
				} catch (Exception e) {
					Log.e(LOG_TAG, "Could not set text to model field.", e);
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
			if (method.getName().equals(setterName)) {
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
			if (method.getName().equals(getterName)) {
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

	private int getResId(String variableName) {

	    try {
	        Field idField = mResourceClass.getDeclaredField(variableName);
	        return idField.getInt(idField);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return -1;
	    } 
	}
}
