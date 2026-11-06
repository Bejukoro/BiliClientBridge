package local.pilinara.bridge;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.pm.ResolveInfo;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText input;
    private TextView status;
    private TextView selected;
    private boolean scanning;
    private AlertDialog scanningDialog;
    @Override public void onCreate(Bundle state) {
        DynamicColors.applyToActivityIfAvailable(this);
        super.onCreate(state);
        if (state == null && forwardToPreferredClient(getIntent())) return;
        boolean dark = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightNavigationBars(!dark);
        if (android.os.Build.VERSION.SDK_INT == 26) getWindow().setNavigationBarColor(android.graphics.Color.BLACK);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        input = findViewById(R.id.link_input);
        status = findViewById(R.id.status);
        selected = findViewById(R.id.selected_client);
        refreshSelection();
        findViewById(R.id.choose_client).setOnClickListener(v -> scan(null, false));
        findViewById(R.id.open_client).setOnClickListener(v -> launch(null));
        findViewById(R.id.open_link).setOnClickListener(v -> forward(input.getText().toString()));
        findViewById(R.id.paste_link).setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip() && clipboard.getPrimaryClip().getItemCount() > 0) {
                CharSequence text = clipboard.getPrimaryClip().getItemAt(0).coerceToText(this);
                input.setText(text);
                ((TextInputLayout) findViewById(R.id.link_field)).setError(null);
            } else Toast.makeText(this, R.string.empty_clipboard, Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.link_settings).setOnClickListener(v -> {
            try { startActivity(new Intent(android.os.Build.VERSION.SDK_INT >= 31 ? Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS : Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()))); }
            catch (ActivityNotFoundException e) { startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()))); }
        });
        findViewById(R.id.help).setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.help).setMessage(R.string.help_body).setPositiveButton("知道了", null).show());
        if (state == null) receive(getIntent());
    }
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (!forwardToPreferredClient(intent)) receive(intent);
    }
    private boolean forwardToPreferredClient(Intent incoming) {
        if (scanning) return false;
        String raw = incoming.getDataString();
        if (raw == null && Intent.ACTION_SEND.equals(incoming.getAction())) {
            CharSequence shared = incoming.getCharSequenceExtra(Intent.EXTRA_TEXT);
            raw = shared == null ? null : shared.toString();
        }
        String url = LinkParser.extract(raw);
        String preferred = getPreferences(0).getString("client", "");
        if (url == null || preferred.isEmpty()) return false;
        try {
            ResolveInfo client = ClientRegistry.preferredHandler(this, preferred, url);
            if (client == null) return false;
            Intent outgoing = ClientRegistry.view(url).setClassName(
                client.activityInfo.packageName, client.activityInfo.name);
            outgoing.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(outgoing);
            closeAfterForwarding();
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
    private void closeAfterForwarding() {
        // Only remove a task rooted in this activity. A caller's task may contain us.
        if (isTaskRoot()) finishAndRemoveTask();
        else finish();
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
        if (url == null) { ((TextInputLayout) findViewById(R.id.link_field)).setError(getString(R.string.invalid_link)); return; }
        ((TextInputLayout) findViewById(R.id.link_field)).setError(null);
        launch(url);
    }
    private void launch(String url) {
        scan(url, true);
    }
    private void scan(String url, boolean openAfter) {
        if (scanning) return;
        scanning = true;
        AlertDialog progress = new MaterialAlertDialogBuilder(this).setTitle("检测客户端")
            .setMessage("正在查找已安装的兼容客户端…").setCancelable(false).create();
        progress.show();
        scanningDialog = progress;
        new Thread(() -> {
            try {
                List<ResolveInfo> clients = url == null ? ClientRegistry.installed(this) : ClientRegistry.handlers(this, url);
                runOnUiThread(() -> {
                    scanning = false;
                    if (isFinishing() || isDestroyed()) return;
                    progress.dismiss();
                    if (openAfter) selectForLaunch(clients, url); else choose(clients, url, false);
                });
            } catch (RuntimeException e) {
                runOnUiThread(() -> {
                    scanning = false;
                    if (isFinishing() || isDestroyed()) return;
                    progress.dismiss();
                    new MaterialAlertDialogBuilder(this).setTitle("检测失败")
                        .setMessage("暂时无法检测，请稍后重试。")
                        .setPositiveButton("知道了", null).show();
                });
            }
        }, "client-discovery").start();
    }
    private void selectForLaunch(List<ResolveInfo> clients, String url) {
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
        if (clients.isEmpty()) {
            String message = "请先安装 PiliNara、PiliPlus 等客户端，并确认与助手位于同一用户空间。";
            status.setText(message);
            new MaterialAlertDialogBuilder(this).setTitle("未找到客户端").setMessage(message).setPositiveButton("知道了", null).show();
            return;
        }
        new MaterialAlertDialogBuilder(this).setTitle(openAfter ? "用哪个客户端打开？" : "选择默认客户端")
            .setAdapter(new ClientListAdapter(this, clients), (dialog, index) -> {
                remember(clients.get(index));
                if (openAfter) openClient(clients.get(index), url);
            }).setNegativeButton("取消", null).show();
    }
    private void remember(ResolveInfo client) {
        getPreferences(0).edit().putString("client", client.activityInfo.packageName).putString("client_name", client.loadLabel(getPackageManager()).toString()).apply(); refreshSelection(); status.setText(R.string.client_saved);
    }
    private void refreshSelection() {
                String pkg = getPreferences(0).getString("client", "");
        String name = getPreferences(0).getString("client_name", "");
        if (!pkg.isEmpty()) {
            try { name = getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkg, 0)).toString(); }
            catch (android.content.pm.PackageManager.NameNotFoundException ignored) { name = "需要重新选择"; }
        }
        selected.setText(pkg.isEmpty() ? getString(R.string.no_client) : name);
        findViewById(R.id.open_client).setEnabled(!pkg.isEmpty());
        ((TextView) findViewById(R.id.choose_client)).setText(pkg.isEmpty() ? R.string.choose_client : R.string.change_client);
    }
    private void openClient(ResolveInfo client, String url) {
        Intent outgoing = url == null ? getPackageManager().getLaunchIntentForPackage(client.activityInfo.packageName) : ClientRegistry.view(url).setClassName(client.activityInfo.packageName, client.activityInfo.name);
        if (outgoing == null) { status.setText("这个客户端需要通过链接打开"); return; }
        outgoing.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try { startActivity(outgoing); closeAfterForwarding(); }
        catch (ActivityNotFoundException | SecurityException e) { status.setText("打开失败，请重新选择客户端"); }
    }
    @Override protected void onDestroy() {
        if (scanningDialog != null) scanningDialog.dismiss();
        super.onDestroy();
    }
}

