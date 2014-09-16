package com.gta0004.lolstalker.adapters;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gta0004.lolstalker.riot.Summoner;

public class SummonerArrayAdapter extends ArrayAdapter<Summoner> {
  
  private Context context;
  private List<Summoner> objects;

  public SummonerArrayAdapter(Context context, List<Summoner> objects) {
    super(context, com.gta0004.lolstalker.R.layout.triple_list_item, objects);
    this.context = context;
    this.objects = objects;
  }
  
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      LayoutInflater inflater = (LayoutInflater) this.context
              .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = inflater.inflate(com.gta0004.lolstalker.R.layout.triple_list_item, parent, false);
    }
    Summoner summoner = objects.get(position);
    TextView level = (TextView) convertView.findViewById(com.gta0004.lolstalker.R.id.secondary_text_1);
    level.setText("Level " + summoner.summonerLevel);
    TextView region = (TextView) convertView.findViewById(com.gta0004.lolstalker.R.id.secondary_text_2);
    region.setText(summoner.region.toUpperCase(Locale.getDefault()));
    TextView main  = (TextView) convertView.findViewById(com.gta0004.lolstalker.R.id.main_text);
    main.setText(summoner.name);
    return convertView;
  }

}
