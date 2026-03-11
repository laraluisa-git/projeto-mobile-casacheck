package com.vistoria.app.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Utilitário centralizado para verificação e solicitação de permissões em runtime.
 * Segue as boas práticas do Android para permissões perigosas.
 */
public class PermissaoHelper {

    // Códigos de requisição de permissão
    public static final int REQUEST_CAMERA         = 101;
    public static final int REQUEST_LOCALIZACAO    = 102;
    public static final int REQUEST_ARMAZENAMENTO  = 103;
    public static final int REQUEST_NOTIFICACAO    = 104;

    // ── Câmera ────────────────────────────────────────────
    public static boolean temPermissaoCamera(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void solicitarPermissaoCamera(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA);
    }

    // ── Localização ───────────────────────────────────────
    public static boolean temPermissaoLocalizacao(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void solicitarPermissaoLocalizacao(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                REQUEST_LOCALIZACAO);
    }

    // ── Armazenamento ─────────────────────────────────────
    public static boolean temPermissaoArmazenamento(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+: READ_MEDIA_IMAGES
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static void solicitarPermissaoArmazenamento(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    REQUEST_ARMAZENAMENTO);
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_ARMAZENAMENTO);
        }
    }

    // ── Notificações (Android 13+) ────────────────────────
    public static boolean temPermissaoNotificacao(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Android < 13 não precisa de permissão em runtime
    }

    public static void solicitarPermissaoNotificacao(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICACAO);
        }
    }
}
