package edu.berkeley.cs160.andrewfang.greymeout;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 *  To handle images in ListView
 */
public class ImageAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] imageNameList;
    private final Integer[] imageIdList;

    public ImageAdapter(Activity context, String[] imageNameList, Integer[] imageIdList) {
        super(context, R.layout.picture_list_row, imageNameList);
        this.context = context;
        this.imageNameList = imageNameList;
        this.imageIdList = imageIdList;
    }

    @Override
    public View getView(int pos, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.picture_list_row, null, true);

        TextView textView = (TextView) rowView.findViewById(R.id.list_text);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.list_image);
        textView.setText(imageNameList[pos]);
        imageView.setImageResource(imageIdList[pos]);
        return rowView;
    }
}
