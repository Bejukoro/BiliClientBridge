package local.pilinara.bridge;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

final class ClientListAdapter extends ArrayAdapter<ResolveInfo> {
    ClientListAdapter(Context context, List<ResolveInfo> clients) {
        super(context, 0, clients);
    }

    @Override public View getView(int position, View recycledView, ViewGroup parent) {
        View row = recycledView;
        if (row == null) {
            row = LayoutInflater.from(getContext()).inflate(R.layout.item_client, parent, false);
        }

        ResolveInfo client = getItem(position);
        PackageManager packageManager = getContext().getPackageManager();
        ImageView icon = row.findViewById(R.id.client_icon);
        TextView label = row.findViewById(R.id.client_label);
        TextView packageName = row.findViewById(R.id.client_package);
        icon.setImageDrawable(client.activityInfo.applicationInfo.loadIcon(packageManager));
        label.setText(client.loadLabel(packageManager));
        packageName.setText(client.activityInfo.packageName);
        return row;
    }
}
