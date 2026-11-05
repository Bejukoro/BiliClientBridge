package local.pilinara.bridge;

import android.app.Activity;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.*;
import android.app.AlertDialog;
import android.content.pm.ResolveInfo;
import java.util.List;

public class MainActivity extends Activity {
    private EditText input;
    private TextView status;
    private TextView selected;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int)(24 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad * 2, pad, pad);
        TextView title = new TextView(this);
        title.setText("B站客户端跳转助手"); title.setTextSize(26);
        layout.addView(title);
        status = new TextView(this);
        status.setText("接收 B 站链接后自动转交所选客户端，支持 PiliNara、PiliPlus 等声明兼容链接的应用。\n这是第三方跳转工具，不是哔哩哔哩官方客户端。\n\n如网页链接未唤起，请在系统设置中允许本应用打开支持的链接。部分 QQ / 微信入口会校验官方签名，同包名也无法通过。\n");
        layout.addView(status);
        selected = new TextView(this); layout.addView(selected); refreshSelection();
        Button choose = new Button(this); choose.setText("选择 / 更换默认客户端");
        choose.setOnClickListener(v -> choose(ClientRegistry.installed(this), null, false)); layout.addView(choose);
        input = new EditText(this); input.setHint("粘贴 bilibili://、B 站网址或分享文本");
        input.setMinLines(3); layout.addView(input);
        Button open = new Button(this); open.setText("打开对应内容");
        open.setOnClickListener(v -> forward(input.getText().toString())); layout.addView(open);
        Button home = new Button(this); home.setText("打开所选客户端");
        home.setOnClickListener(v -> launch(null)); layout.addView(home);
        Button settings = new Button(this); settings.setText("设置支持的链接");
        settings.setOnClickListener(v -> {
            try { startActivity(new Intent(android.os.Build.VERSION.SDK_INT >= 31 ? Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS : Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()))); }
            catch (ActivityNotFoundException e) { startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()))); }
        }); layout.addView(settings);
        ScrollView scroll = new ScrollView(this); scroll.addView(layout); setContentView(scroll);
        if (state == null) receive(getIntent());
    }
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent); setIntent(intent); receive(intent);
    }
    private void receive(Intent intent) {
        String raw = intent.getDataString();
        if (raw == null && Intent.ACTION_SEND.equals(intent.getAction())) {
            CharSequence text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
            raw = text == null ? null : text.toString();
        }
        if (raw != null) { input.setText(raw); forward(raw); }
    }
    private void forward(String raw) {
        String url = LinkParser.extract(raw);
        if (url == null) { status.setText("未找到支持的 B 站链接，请粘贴完整链接后重试。"); return; }
        launch(url);
    }
    private void launch(String url) {
        List<ResolveInfo> clients = url == null ? ClientRegistry.installed(this) : ClientRegistry.handlers(this, url);
        String preferred = getPreferences(0).getString("client", "");
        for (ResolveInfo client : clients) {
            if (client.activityInfo.packageName.equals(preferred)) { openClient(client, url); return; }
        }
        if (clients.size() == 1 && preferred.isEmpty()) {
            remember(clients.get(0)); openClient(clients.get(0), url); return;
        }
        choose(clients, url, true);
    }
    private void choose(List<ResolveInfo> clients, String url, boolean openAfter) {
        if (clients.isEmpty()) { status.setText("未找到声明支持此类链接的客户端。请先安装 PiliNara、PiliPlus 等兼容应用。链接保留在下方。"); return; }
        String[] labels = new String[clients.size()];
        for (int i = 0; i < labels.length; i++) {
            ResolveInfo r = clients.get(i);
            labels[i] = r.loadLabel(getPackageManager()) + "\n" + r.activityInfo.packageName;
        }
        new AlertDialog.Builder(this).setTitle(openAfter ? "选择客户端（将记住选择）" : "选择默认客户端")
            .setItems(labels, (dialog, index) -> {
                remember(clients.get(index));
                if (openAfter) openClient(clients.get(index), url);
            }).setNegativeButton("取消", null).show();
    }
    private void remember(ResolveInfo client) {
        getPreferences(0).edit().putString("client", client.activityInfo.packageName).apply(); refreshSelection();
    }
    private void refreshSelection() {
        selected.setText("默认客户端：" + getPreferences(0).getString("client", "未选择（收到链接时选择）"));
    }
    private void openClient(ResolveInfo client, String url) {
        Intent outgoing = url == null ? getPackageManager().getLaunchIntentForPackage(client.activityInfo.packageName) : ClientRegistry.view(url).setClassName(client.activityInfo.packageName, client.activityInfo.name);
        if (outgoing == null) { status.setText("此客户端没有可用的主页入口，请使用内容链接打开。"); return; }
        outgoing.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try { startActivity(outgoing); finish(); }
        catch (ActivityNotFoundException | SecurityException e) { status.setText("客户端启动失败，请重新选择。链接保留在下方。"); }
    }
}
