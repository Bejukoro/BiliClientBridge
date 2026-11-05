package local.pilinara.bridge;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Extracts only a URL; never executes intent:// components, extras or flags. */
public final class LinkParser {
    private static final Pattern URL = Pattern.compile("(?i)(?:https?://|bilibili://|intent://)[^\\s<>\"“”]+");
    public static String extract(String input) {
        if (input == null) return null;
        Matcher matcher = URL.matcher(input);
        while (matcher.find()) {
            String url = matcher.group();
            if (url.startsWith("intent://")) {
                int marker = url.indexOf("#Intent;");
                if (marker < 0 || !url.substring(marker).contains("scheme=bilibili;")) continue;
                url = "bilibili://" + url.substring(9, marker);
            }
            while (!url.isEmpty() && ".,，。！!）)]】".indexOf(url.charAt(url.length()-1)) >= 0)
                url = url.substring(0, url.length()-1);
            try {
                URI uri = new URI(url);
                String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
                String host = uri.getHost();
                if (host == null || uri.getRawUserInfo() != null) continue;
                host = host.toLowerCase(Locale.ROOT);
                if (scheme.equals("bilibili")) return url;
                if ((scheme.equals("https") || scheme.equals("http")) &&
                    (host.equals("b23.tv") || host.equals("bilibili.com") || host.endsWith(".bilibili.com") ||
                     host.equals("bilibili.cn") || host.endsWith(".bilibili.cn") ||
                     host.equals("bilibili.tv") || host.endsWith(".bilibili.tv"))) return url;
            } catch (Exception ignored) { }
        }
        return null;
    }
    private LinkParser() { }
}
