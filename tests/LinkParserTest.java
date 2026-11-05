import local.pilinara.bridge.LinkParser;

public class LinkParserTest {
    private static int count;
    private static void check(String raw, String expected) {
        String actual = LinkParser.extract(raw);
        if (!java.util.Objects.equals(actual, expected)) throw new AssertionError(raw + " -> " + actual);
        count++;
    }
    public static void main(String[] args) {
        check("bilibili://video/123?p=2", "bilibili://video/123?p=2");
        check("bilibili://bangumi/play/ep123", "bilibili://bangumi/play/ep123");
        check("https://www.bilibili.com/video/BV123?p=2&t=30", "https://www.bilibili.com/video/BV123?p=2&t=30");
        check("分享：https://b23.tv/abc。", "https://b23.tv/abc");
        check("https://live.bilibili.com/123", "https://live.bilibili.com/123");
        check("intent://video/123#Intent;scheme=bilibili;package=tv.danmaku.bili;end", "bilibili://video/123");
        check("intent://video/123#Intent;scheme=evil;end", null);
        check("https://bilibili.com.evil.test/video/123", null);
        check("https://bilibili.com@evil.test/", null);
        check("https://evil.test/ bilibili://live/123", "bilibili://live/123");
        check("file:///secret", null);
        check("content://secret", null);
        check("not a link", null);
        check(null, null);
        System.out.println("Passed " + count + " link routing checks");
    }
}
