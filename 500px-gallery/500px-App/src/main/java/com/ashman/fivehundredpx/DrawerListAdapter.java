package com.ashman.fivehundredpx;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ashman.fivehundredpx.enums.GalleryCategory;

public class DrawerListAdapter extends ArrayAdapter<GalleryCategory> {
    private GalleryCategory currentCategory = FiveHundredApplication.DEFAULT_CATEGORY;

    public DrawerListAdapter(BaseFragmentActivity activity) {
        super(activity, R.layout.drawer_textview, GalleryCategory.values());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(
                            R.layout.drawer_textview, null, false);
        GalleryCategory item = getItem(position);
        ((TextView)convertView.findViewById(R.id.drawer_textView)).setText(item.getName());

        return convertView;
    }
}
