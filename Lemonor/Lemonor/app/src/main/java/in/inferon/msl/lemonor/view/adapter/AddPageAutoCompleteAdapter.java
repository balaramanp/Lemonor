package in.inferon.msl.lemonor.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import in.inferon.msl.lemonor.R;

import java.util.ArrayList;
import java.util.List;

public class AddPageAutoCompleteAdapter<T> extends ArrayAdapter<T> implements Filterable {
    private List<T> listObjects;
    private List<T> suggestions = new ArrayList<>();
    private Context context;

    public AddPageAutoCompleteAdapter(Context context, List<T> listObjects) {
        super(context, R.layout.autocomplete_tv, listObjects);
        this.listObjects = new ArrayList<>(listObjects);
        this.context = context;
    }

    private Filter mFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint != null) {
                suggestions.clear();
                for (T object : listObjects) {
                    if (object.toString().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        suggestions.add(object);
                    }
                }

                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results == null) {
                return;
            }
            List<T> filteredList = (List<T>) results.values;
            if (results.count > 0) {
                clear();
                for (T filteredObject : filteredList) {
                    add(filteredObject);
                }
                notifyDataSetChanged();
            }
        }
    };

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private static class ViewHolder {
        TextView title;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Object listObject = getItem(position);
        final ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.addpageautocomplete_tv, parent, false);
            viewHolder.title = (TextView) convertView.findViewById(R.id.textView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.title.setText(listObject.toString());
        return convertView;
    }
}

