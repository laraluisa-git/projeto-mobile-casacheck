package com.vistoria.app.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.vistoria.app.R;
import com.vistoria.app.ui.lista.ListaVistoriasActivity;

/**
 * Utilitário para criação e exibição de notificações locais.
 * Demonstra uso do recurso de notificações nativas do Android.
 */
public class NotificacaoHelper {

    private static final String CHANNEL_ID = "vistoria_channel";
    private static final String CHANNEL_NAME = "Vistorias";
    private static final String CHANNEL_DESC = "Notificações do App de Vistoria";
    private static int notifId = 1000;

    /**
     * Cria o canal de notificação (necessário para Android 8.0+).
     * Deve ser chamado na inicialização do app.
     */
    public static void criarCanal(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            canal.setDescription(CHANNEL_DESC);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(canal);
            }
        }
    }

    /**
     * Exibe notificação ao concluir uma vistoria.
     *
     * @param context  contexto da aplicação
     * @param nomeImovel nome do imóvel vistoriado
     * @param protocolo  número do protocolo gerado
     */
    public static void notificarVistoriaConcluida(Context context,
                                                   String nomeImovel,
                                                   String protocolo) {
        // Intent para abrir o app ao clicar na notificação
        Intent intent = new Intent(context, ListaVistoriasActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_home)
                .setContentTitle("Vistoria Concluída!")
                .setContentText(nomeImovel + " – Protocolo: " + protocolo)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("A vistoria do imóvel \"" + nomeImovel + "\" foi concluída.\n"
                                + "Protocolo: " + protocolo + "\n"
                                + "Laudo disponível no app."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            manager.notify(notifId++, builder.build());
        } catch (SecurityException e) {
            // Permissão não concedida — trate silenciosamente
            e.printStackTrace();
        }
    }
}
