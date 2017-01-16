package com.checkmyfac.utils;

import android.content.Context;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.checkmyfac.R;
import com.checkmyfac.activities.map.tasks.HoraireTransport;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class MyExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> headers;
    private LinkedHashMap<String, List<HoraireTransport>> headersWithChilds;

    public MyExpandableListAdapter(Context context, List<String> headers) {
        this.context = context;
        if(headers==null) this.headers = new ArrayList<>();
        else this.headers = headers;

        this.headersWithChilds = new LinkedHashMap<>();
    }

    public void setData(LinkedHashMap<String, List<HoraireTransport>> headersWithChilds){
        this.headersWithChilds = headersWithChilds;
        notifyDataSetChanged();
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.headersWithChilds.get(headers.get(groupPosition)).get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @SuppressWarnings("deprecation")
    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        HoraireTransport horaireTransport = (HoraireTransport) getChild(groupPosition, childPosition);
        String horaire = '('+context.getString(R.string.prefixe_horaire)+horaireTransport.getDestination()+')';
        horaire+= "<br><b>" + horaireTransport.getHoraire() +"</b>";
        TextView txtListChild = new TextView(context);
        txtListChild.setText(Html.fromHtml(horaire));
        txtListChild.setGravity(Gravity.END);
        txtListChild.setPadding(10, 10, 10, 10);
        return txtListChild;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        List<HoraireTransport> child = this.headersWithChilds.get(headers.get(groupPosition));
        if(child==null) return 0;
        return child.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.headers.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.headers.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        String headerTitle = (String) getGroup(groupPosition);
        TextView lblListHeader = new TextView(context);
        lblListHeader.setTextSize(lblListHeader.getTextSize()+1);
        lblListHeader.setText(headerTitle);
        lblListHeader.setPadding(70, 40, 0, 40);
        return lblListHeader;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
