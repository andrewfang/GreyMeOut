package edu.berkeley.cs160.andrewfang.greymeout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DrawActivity extends Activity {

    private DrawView dView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        setContentView(R.layout.activity_draw);

        LinearLayout board_layout = (LinearLayout) findViewById(R.id.board);

        // Make the layer upon which we can draw
        dView = new DrawView(this);
        dView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        Display display = this.getWindowManager().getDefaultDisplay();
        int display_height = display.getHeight();
        if (intent.getExtras() == null) {
            dView.setScaleImage(R.drawable.flower);
        } else {
            dView.setScaleImage(intent.getExtras().getInt("PICTURE"));
        }
        board_layout.addView(dView);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case (R.id.action_about):
                /* Shows the about message */
                new AlertDialog.Builder(this)
                        .setTitle(R.string.action_about)
                        .setMessage(R.string.about_rates)
                        .setPositiveButton(R.string.about_confirm, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // okay
                            }
                        })
                        .show();
                break;
            case (R.id.action_clear):
                dView.clear();
                break;
            case (R.id.action_save):
                File save_file = this.make_file();
                dView.save(save_file);
                Toast.makeText(this.getApplicationContext(), "File saved to gallery", Toast.LENGTH_LONG).show();
                break;
            case (R.id.action_change_shape):
                new AlertDialog.Builder(this)
                        .setTitle(R.string.change_shape)
                        .setItems(R.array.shapes_array, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dView.change_shape(which);
                            }
                        })
                        .show();
                break;
            case (R.id.action_brush_configure):
                View number_picker_view = getLayoutInflater().inflate(R.layout.number_picker_view, null);
                final NumberPicker num_picker = (NumberPicker) number_picker_view.findViewById(R.id.number_picker_widget);
                num_picker.setMinValue(1);
                num_picker.setMaxValue(200);
                num_picker.setValue(dView.get_stroke_width());
                num_picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);


                final AlertDialog numPickerDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.brush_configure)
                        .setView(number_picker_view)
                        .setPositiveButton(R.string.color,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dView.set_width(num_picker.getValue());
                                        dView.change_brush_to_color();
                                    }
                                })
                        .setNegativeButton(R.string.grey,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dView.set_width(num_picker.getValue());
                                        dView.change_brush_to_grey();
                                    }
                                }).create();

                numPickerDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                numPickerDialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Inspired from http://stackoverflow.com/questions/15662258/how-to-save-a-bitmap-on-internal-storage
     */
    private File make_file() {
        File file_directory = new File(Environment.getExternalStorageDirectory() + "/Pictures/GreyMeOut/");
        if (!file_directory.exists()) {
            if(!file_directory.mkdirs()) {
                Log.d("ANDREW", "Error occured: File is null.");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        File output_file;
        String output_file_name="GMO_"+ timeStamp +".jpg";
        output_file = new File(file_directory.getPath() + File.separator + output_file_name);
        return output_file;
    }
}
