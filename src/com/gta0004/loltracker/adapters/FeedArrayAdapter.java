package com.gta0004.loltracker.adapters;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.gta0004.loltracker.events.IEvent;
import com.gta0004.loltracker.utils.Constants;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FeedArrayAdapter extends ArrayAdapter<IEvent>{
  
  private Context context;
  private List<IEvent> objects;

  public FeedArrayAdapter(Context context, List<IEvent> objects) {
    super(context, com.gta0004.lolstalker.R.layout.triple_list_item, objects);
    this.context = context;
    this.objects = objects;
  }
  
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    //make new view if old one is null
    if (convertView == null) {
      LayoutInflater inflater = (LayoutInflater) this.context
              .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = inflater.inflate(com.gta0004.lolstalker.R.layout.triple_list_item, parent, false);
    }    
    
    //get event
    IEvent event = objects.get(position);
    //populate text fields
    TextView time = (TextView) convertView.findViewById(com.gta0004.lolstalker.R.id.secondary_text_1);
    time.setText(event.getFormattedEventTime());
    TextView region = (TextView) convertView.findViewById(com.gta0004.lolstalker.R.id.secondary_text_2);
    region.setText(event.getRegion().toUpperCase(Locale.getDefault()));
    TextView main  = (TextView) convertView.findViewById(com.gta0004.lolstalker.R.id.main_text);
    main.setText(event.getMessage());
    
    //set text color to white
    main.setTextColor(0xFFFFFFFF);
    time.setTextColor(0xFFFFFFFF);
    region.setTextColor(0xFFFFFFFF);
    
    //set appropriate background color for list item
    if (event.getEventType() == Constants.EVENT_NEGATIVE) {
      convertView.setBackgroundColor(0xFFbd362f);
    }
    else if (event.getEventType() == Constants.EVENT_POSITIVE) {
      convertView.setBackgroundColor(0xFF51a351);
    }
    else if (event.getEventType() == Constants.EVENT_NEUTRAL) {
      convertView.setBackgroundColor(0xFFf89406);
    }
    
    //TODO padding and rounded colors
    //convertView.setPadding(0, 5, 0, 5);
    
    return convertView;
  }
  
  @Override
  public void notifyDataSetChanged() {
    Collections.sort(objects, new Comparator<IEvent>() {

      @Override
      public int compare(IEvent lhs, IEvent rhs) {
        return (-1) * lhs.compareTo(rhs);
      }
      
    });
    super.notifyDataSetChanged();
  }
  
}
