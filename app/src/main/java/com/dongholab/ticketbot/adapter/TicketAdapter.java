package com.dongholab.ticketbot.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongholab.ticketbot.R;
import com.dongholab.ticketbot.data.Soldout;

import java.util.List;

public class TicketAdapter extends ArrayAdapter<Soldout> {

    List<Soldout> soldoutList;
    Context context;
    int resource;

    public TicketAdapter(Context context, int resource, List<Soldout> soldoutList) {
        super(context, resource, soldoutList);
        this.soldoutList = soldoutList;
        this.context = context;
        this.resource = resource; //R.layout.row
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(resource, parent, false);

        Soldout data = soldoutList.get(position);

        //ui
        TextView train_num, train_category, check_text, start_time, end_time;
        ImageView check;

        train_num = (TextView)view.findViewById(R.id.train_num);
        train_category = (TextView)view.findViewById(R.id.train_category);
        check_text = (TextView)view.findViewById(R.id.check_text);
        start_time = (TextView)view.findViewById(R.id.start_time);
        end_time = (TextView)view.findViewById(R.id.end_time);
        check = (ImageView)view.findViewById(R.id.check);

        //값 변경
        train_num.setText(String.valueOf(data.getTrain_number()));

        if(data.getTrain_number() < 400){
            //KTX
            train_category.setText("KTX");
        }else if(1000 <= data.getTrain_number() && data.getTrain_number() < 1200){
            //새마을
            train_category.setText("새마을");
        }else{
            //무궁화
            train_category.setText("무궁화");
        }

        check_text.setText(getContext().getResources().getString(R.string.check_text_on));
        start_time.setText(data.getTime_departure());
        end_time.setText(data.getTime_arrive());

        return view;
    }
}