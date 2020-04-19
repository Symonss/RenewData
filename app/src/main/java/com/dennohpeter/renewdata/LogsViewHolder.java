package com.dennohpeter.renewdata;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class LogsViewHolder extends RecyclerView.ViewHolder {
    TextView received_date, message_body;
    LinearLayout parent;


    public LogsViewHolder(View itemView) {
        super(itemView);
        parent = itemView.findViewById(R.id.logs_card);
        received_date = itemView.findViewById(R.id.received_date);
        message_body = itemView.findViewById(R.id.message_body);

    }
}
