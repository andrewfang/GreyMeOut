package edu.berkeley.cs160.andrewfang.greymeout;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * To handle drawing
 */
public class DrawView extends View {


    private int background_image;
    private Bitmap dirty_bitmap;
    private Bitmap clean_color_bitmap;
    private Bitmap clean_grey_bitmap;
    private WindowManager win_manager;
    private Path path;
    private Paint paint;
    private Canvas backup_canvas;
    private int stroke_width;
    private boolean mode_is_making_grey;
    private ShapeDrawable shape;
    private boolean drawing_shape;

    private ArrayList<Path> path_history;
    private ArrayList<Boolean> color_history;


    public DrawView(Context context) {
        super(context);
        if (Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        this.win_manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        // Default background in case something goes wrong
        this.stroke_width = 50;
        this.mode_is_making_grey = true;
        this.path = new Path();
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.preparePaint();

        this.path_history = new ArrayList<Path>();
        this.color_history = new ArrayList<Boolean>();
    }

    private void preparePaint(){
        this.paint.setStrokeWidth(this.stroke_width);
        this.paint.setAntiAlias(true);
        this.paint.setDither(true);
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        this.paint.setColor(Color.TRANSPARENT);
        this.paint.setStrokeJoin(Paint.Join.ROUND);
        this.paint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mode_is_making_grey) {
            canvas.drawBitmap(this.clean_grey_bitmap, 0, 0, null);
        } else {
            canvas.drawBitmap(this.clean_color_bitmap, 0, 0, null);
        }
        if (this.drawing_shape) {
            this.shape.setBounds((int) this.current_x, (int) this.current_y, ((int) this.current_x) + this.stroke_width, ((int) this.current_y) + this.stroke_width);
            this.shape.getPaint().setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            this.shape.getPaint().setColor(Color.TRANSPARENT);
            this.shape.draw(this.backup_canvas);
        } else {
            this.backup_canvas.drawPath(this.path, this.paint);
        }

        canvas.drawBitmap(this.dirty_bitmap, 0, 0, null);
    }

    private float current_x;
    private float current_y;

    private void touch_down(float x, float y) {
        this.path = new Path();
        this.path.moveTo(x,y);
        this.current_x = x;
        this.current_y = y;
    }

    private void touch_move(float x, float y) {
        this.path.lineTo(x, y);
        this.current_x = x;
        this.current_y = y;
    }

    private void touch_up(float x, float y) {
        this.path.lineTo(x, y);
        this.path_history.add(this.path);
        this.color_history.add(this.mode_is_making_grey);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motion_event) {
        float x = motion_event.getX();
        float y = motion_event.getY();

        switch (motion_event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.touch_down(x, y);
                this.invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                this.touch_move(x, y);
                this.invalidate();
                break;
            case MotionEvent.ACTION_UP:
                this.touch_up(x, y);
                this.invalidate();
                break;
        }
        return true;
    }

    public void set_width(int width) {
        this.stroke_width = width;
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.preparePaint();
    }

    public void clear() {
        this.path = new Path();
        Bitmap result = this.overlay(this.clean_color_bitmap, this.clean_color_bitmap);
        this.dirty_bitmap = result;
        this.backup_canvas.setBitmap(result);
        invalidate();
    }

    public void change_shape(int shape_index) {
        switch (shape_index) {
            case 0:
                this.drawing_shape = false;
                break;
            case 1:
                this.drawing_shape = true;
                this.shape = new ShapeDrawable(new RectShape());
                break;
            case 2:
                this.drawing_shape = true;
                this.shape = new ShapeDrawable(new OvalShape());
                break;
            case 3:
                this.drawing_shape = true;
                this.shape = new ShapeDrawable(new ArcShape(35, 290));
                break;
            case 4:
                this.drawing_shape = true;
                this.shape = new ShapeDrawable(new ArcShape(55, 70));
                break;
            case 5:
                this.drawing_shape = true;
                this.shape = new ShapeDrawable(new ArcShape(235, 70));
                break;
            default:
                this.drawing_shape = false;
                break;
        }

    }

    public void change_brush_to_grey() {
        if (!this.mode_is_making_grey) {
            this.mode_is_making_grey = true;
            Bitmap result = this.overlay(this.clean_color_bitmap, this.dirty_bitmap);
            this.dirty_bitmap = result;
            this.backup_canvas = new Canvas(result);

        }
    }

    public void change_brush_to_color() {
        if (this.mode_is_making_grey) {
            this.mode_is_making_grey = false;
            Bitmap result = this.overlay(this.clean_grey_bitmap, this.dirty_bitmap);
            this.dirty_bitmap = result;
            this.backup_canvas.setBitmap(result);
        }
    }


    /**
     * Inspired from http://stackoverflow.com/questions/8112715/how-to-crop-bitmap-center-like-imageview
     */
    public Bitmap scaleCenterCrop(Bitmap original, int deviceHeight, int deviceWidth) {
        int old_width = original.getWidth();
        int old_height = original.getHeight();

        float scale = Math.max((float) deviceHeight / old_height, (float) deviceWidth / old_width);
        float newWidth = scale * old_width;
        float newHeight = scale * old_height;
        float left = (deviceWidth - newWidth) / 2;
        float top = (deviceHeight - newHeight) / 2;

        RectF rectF = new RectF(left, top, left + newWidth, top + newHeight);
        Bitmap scaled = Bitmap.createBitmap(deviceWidth, deviceHeight, original.getConfig());
        Canvas canvas = new Canvas(scaled);
        canvas.drawBitmap(original, null, rectF, null);
        return scaled;
    }

    public void setScaleImage(int id) {
        this.background_image = id;
        Display display = win_manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);

        Bitmap raw_background_bitmap = BitmapFactory.decodeResource(getResources(), this.background_image);
        this.dirty_bitmap = scaleCenterCrop(raw_background_bitmap, point.y, point.x);
        this.clean_grey_bitmap = scaleCenterCrop(this.removeColor(raw_background_bitmap), point.y, point.x);
        this.clean_color_bitmap = scaleCenterCrop(raw_background_bitmap, point.y, point.x);
        this.backup_canvas = new Canvas(this.dirty_bitmap);
    }

    /**
     * Inspired from http://stackoverflow.com/questions/1540272/android-how-to-overlay-a-bitmap-draw-over-a-bitmap
     */
    private Bitmap overlay(Bitmap bm_bottom, Bitmap bm_top) {
        Bitmap result = Bitmap.createBitmap(bm_bottom.getWidth(), bm_bottom.getHeight(), bm_bottom.getConfig());
        Canvas c = new Canvas(result);
        c.drawBitmap(bm_bottom, new Matrix(), null);
        c.drawBitmap(bm_top, new Matrix(), null);
        return result;
    }

    /**
     * Inspired from http://stackoverflow.com/questions/3373860/convert-a-bitmap-to-grayscale-in-android/3391061#3391061
     */
    private Bitmap removeColor(Bitmap original) {
        Bitmap greyscale = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(greyscale);
        Paint grey_paint = new Paint();
        ColorMatrix color_matrix = new ColorMatrix();
        color_matrix.setSaturation(0);
        grey_paint.setColorFilter(new ColorMatrixColorFilter(color_matrix));
        c.drawBitmap(original, 0, 0, grey_paint);
        return greyscale;
    }

    /**
     * Returns the stroke width (for number picker default setting)
     */
    public int get_stroke_width() {
        return this.stroke_width;
    }

    /**
     * Saves the file
     */
    public void save(File output_file) {
        Bitmap result_image;
        if (this.mode_is_making_grey) {
            result_image = this.overlay(this.clean_grey_bitmap, this.dirty_bitmap);
        } else {
            result_image = this.overlay(this.clean_color_bitmap, this.dirty_bitmap);
        }

        try {
            FileOutputStream output_stream = new FileOutputStream(output_file);
            result_image.compress(Bitmap.CompressFormat.PNG, 90, output_stream);
            output_stream.close();
        } catch (FileNotFoundException e) {
            Log.d("ANDREW", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("ANDREW", "Error accessing file: " + e.getMessage());
        } catch (Exception e) {
            Log.d("ANDREW", "Other:" + e.getMessage());
        }

    }
}
