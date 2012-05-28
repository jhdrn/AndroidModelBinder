#Android Model Binder
A simple way to bind a value object/model to Android views.

##Features
* Views are "auto-populated" when the model is bound.
* The model is automatically updated when the bound view is changed.
* An OnModelUpdateListener interface can be implemented. It's modelUpdated(Object model, Field field) method is executed every time the bound model has been changed.

##Supported view types:
* CompoundButton (CheckBox, RadioButton, ToggleButton)
* TextView (EditText etc)
* SeekBar 
* RatingBar

##Example usage

Lets start by creating a simple layout (note the id of the TextView):

	<?xml version="1.0" encoding="utf-8"?>
	<LinearLayout
	  xmlns:android="http://schemas.android.com/apk/res/android"
	  android:orientation="vertical"
	  android:layout_width="fill_parent"
	  android:layout_height="fill_parent">
	    <TextView 
	    	android:text="TextView" 
	    	android:id="@+id/textView1" 
	    	android:layout_width="wrap_content" 
	    	android:layout_height="wrap_content"></TextView>
	    
	</LinearLayout>

Then create a value object class where we bind the "text" field to the TextView in our layout via the "BindTo" annotation. 

	public class ExampleModel {

		// The BindTo annotation binds the field to a view id.	
		@BindTo("textView1")
	    private String text;
	
		// Setters/getters will be used if they exists. 
	    public String getText() {
	        return text;
	    }
	    
	    public void setText(final String text) {
	        this.text = text;
	    }
	}



Then create an activity to execute the actual binding:

	public class ExampleActivity extends Activity {
	
		// Create an instance of the ModelBinder class.
		private ModelBinder mModelBinder = ModelBinder.newInstance(R.id.class);

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		
			// Inflate our layout
			LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Service.LAYOUT_INFLATER_SERVICE);
			View rootView = layoutInflater.inflate(R.layout.main, null);
		
			// Create an instance of our ExampleModel and set some text to it.
			ExampleModel model = new ExampleModel();
			model.setText("Example text");
			
			setContentView(rootView);
			
			// Bind the model to the view
			mModelBinder.bind(model, rootView);
			
		}
	}

The result in the simple example above will be that the TextView we've bound will be populated with the text "Example text".