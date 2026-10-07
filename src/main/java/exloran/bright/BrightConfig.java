package exloran.bright;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * BrightConfig — basit JSON tabanli ayar dosyasi.
 * Cloth Config / AutoConfig kullanilmiyor; menu kendi arayuzunu ciziyor,
 * bu dosya sadece degerleri disk uzerinde kalici hale getiriyor.
 */
public class BrightConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("bright.json");

    // ── Modul acik/kapali ────────────────────────────────────
    public boolean hitboxActive  = false;
    public boolean triggerActive = false;
    public boolean espActive     = false;

    // ── Hitbox ───────────────────────────────────────────────
    public float xzExpand = 1.4f;
    public float yExpand  = 1.2f;

    // ── TriggerBot ───────────────────────────────────────────
    public int     triggerDelay     = 50;
    public boolean trigBlockShield  = true;
    public boolean trigBlockEating  = true;

    // ── ESP ──────────────────────────────────────────────────
    public float   espR           = 0.53f;
    public float   espG           = 0.20f;
    public float   espB           = 0.90f;
    public float   espLineWidth   = 2.0f;
    public boolean espThroughWalls = true;

    // ── GUI konumu (surukleme sonrasi hatirlanir) ──────────────
    public int guiX = Integer.MIN_VALUE;
    public int guiY = Integer.MIN_VALUE;

    public static BrightConfig load() {
        if (Files.exists(PATH)) {
            try (Reader r = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
                BrightConfig cfg = GSON.fromJson(r, BrightConfig.class);
                if (cfg != null) return cfg;
            } catch (IOException ignored) {
                // dosya bozuksa varsayilanlarla devam et
            }
        }
        return new BrightConfig();
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer w = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(this, w);
            }
        } catch (IOException ignored) {
            // kaydedilemedi, oturum icinde ayarlar yine de gecerli kalir
        }
    }
}
