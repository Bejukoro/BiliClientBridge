package local.pilinara.bridge;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import java.util.*;

/** Discover declared link handlers, excluding this bridge and generic browsers. */
final class ClientRegistry {
    private static final String[] KNOWN_CLIENTS = {
        "com.example.pilinara", "com.example.pilinara.debug", "com.example.pilinara.dev",
        "com.example.piliplus", "com.example.piliplus.debug", "com.example.piliplus.dev"
    };
    static Intent view(String url) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addCategory(Intent.CATEGORY_BROWSABLE);
    }
    static List<ResolveInfo> handlers(Context context, String url) {
        List<ResolveInfo> result = new ArrayList<>();
        Set<String> browsers = new HashSet<>();
        Set<String> biliClients = new HashSet<>();
        for (ResolveInfo r : query(context, "https://example.org/")) browsers.add(r.activityInfo.packageName);
        for (ResolveInfo r : query(context, "bilibili://video/170001")) biliClients.add(r.activityInfo.packageName);
        Set<String> seen = new HashSet<>();
        for (ResolveInfo r : query(context, url)) {
            String pkg = r.activityInfo.packageName;
            if (pkg.equals(context.getPackageName()) || !r.activityInfo.exported) continue;
            if (browsers.contains(pkg) && !biliClients.contains(pkg)) continue;
            if (seen.add(pkg)) result.add(r);
        }
        // Some OEM resolvers hide unapproved deep-link handlers. These clients
        // support the accepted Bilibili URLs, so use their exported launcher as fallback.
        for (String pkg : KNOWN_CLIENTS) {
            ResolveInfo client = knownClient(context, pkg);
            if (client != null && seen.add(pkg)) result.add(client);
        }
        result.sort((a,b) -> a.loadLabel(context.getPackageManager()).toString().compareToIgnoreCase(b.loadLabel(context.getPackageManager()).toString()));
        return result;
    }
    static List<ResolveInfo> installed(Context context) {
        Map<String, ResolveInfo> result = new LinkedHashMap<>();
        for (String url : new String[]{"bilibili://video/170001", "https://www.bilibili.com/video/BV17x411w7KC", "https://b23.tv/test", "https://live.bilibili.com/1"})
            for (ResolveInfo r : handlers(context, url)) result.putIfAbsent(r.activityInfo.packageName, r);
        return new ArrayList<>(result.values());
    }
    private static List<ResolveInfo> query(Context context, String url) {
        return context.getPackageManager().queryIntentActivities(view(url), android.content.pm.PackageManager.MATCH_DEFAULT_ONLY | android.content.pm.PackageManager.MATCH_ALL);
    }
    private static ResolveInfo knownClient(Context context, String pkg) {
        Intent launcher = context.getPackageManager().getLaunchIntentForPackage(pkg);
        if (launcher == null || launcher.getComponent() == null) return null;
        try {
            ResolveInfo result = new ResolveInfo();
            result.activityInfo = context.getPackageManager().getActivityInfo(launcher.getComponent(), 0);
            if (!result.activityInfo.exported || !result.activityInfo.enabled || !result.activityInfo.applicationInfo.enabled) return null;
            return result;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) { return null; }
    }
}
