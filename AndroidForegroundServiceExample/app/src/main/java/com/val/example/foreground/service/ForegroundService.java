package com.val.example.foreground.service;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class ForegroundService extends Service {


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(getClass().getCanonicalName(), "Creando ForeGroundService");
    }

    /**
     * Tanto este atributo (MediaPLayer, como los métodos relativos a eĺ
     * deberían ir en otra clase. Por motivos didácticos (no disgregar el código y
     * facilitar su seguimiento, se declaran aquí)
     */

    private static MediaPlayer mediaPlayer;

    private static void play(Context context) {

        mediaPlayer = MediaPlayer.create(context, R.raw.audio);
        mediaPlayer.start();
    }

    private static void stop() {

        mediaPlayer.stop();

    }



    //Usaremos este PendingIntent cuando el usuario Clique en la Notificacion (en ningún icono a la escucha)
    private PendingIntent obtenerNotificationIntent ()
    {
        PendingIntent notificationPendingIntent = null;

            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction(Constantes.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);


        return notificationPendingIntent;
    }


    public PendingIntent obtenerPendingIntentActivity ()
    {
        PendingIntent intentActivity = null;

            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction(Constantes.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intentActivity = PendingIntent.getActivity(this, 0, notificationIntent, 0);


        return  intentActivity;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(getClass().getCanonicalName(), "Entrando en foreground service ");

        String intent_action = intent.getAction();

        if (intent_action.equals(Constantes.STARTFOREGROUND_ACTION))
        {
            Log.i(getClass().getCanonicalName(), "Han llamado al servio para lanzarlo ");


            //Intent notificationIntent = new Intent(this, MainActivity.class);
            //notificationIntent.setAction(Constantes.MAIN_ACTION);
            //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent =  obtenerPendingIntentActivity();//PendingIntent.getActivity(this, 0, notificationIntent, 0);


           // PendingIntent intentActivity = obtenerIntentActivity();
            /**
             * Obtengo e objeto Remote View, que conformora el Layout de la notificación
             * Debo emplear esta clase para que la notificación pueda ser mostrada por otro proceso
             * (y no el hilo principal)
             *
             * Cuando ejecuto un foregroundService, no hay seguridad de que el servicio se ejecute
             * en un hilo separado o no. Por ello, debo emplear una RemoteView, para que pueda ser
             * mostrada aún por otro hilo
             *
             */
            RemoteViews notificationView = new RemoteViews(this.getPackageName(),R.layout.notification);


            /** El hecho de emplear RemoteViews, varía la forma de gestión de eventos:
             *
             * Para cada elemento visual al que quiera asociarle una acción al ser tocado en la pantalla,
             * no me vale el onClikListener - por ser una RemoteView -; sino que debo usar un PendingIntent
             * que a su vez (lo más cómodo) se invoque a un Reciever
             *
             */

            // Creo el PendingIntent para cuando se toque el boton PLAY y lo asocio a la correspondiente vista
            Intent buttonPlayIntent = new Intent(this, NotificationPlayButtonHandler.class);
            PendingIntent buttonPlayPendingIntent = PendingIntent.getBroadcast(this, 0, buttonPlayIntent, 0);
            notificationView.setOnClickPendingIntent(R.id.notification_button_play, buttonPlayPendingIntent);

            // Creo el PendingIntent para cuando se toque el boton Skip (siguiente) y lo asocio a la correspondiente vista
            Intent buttonSkipIntent = new Intent(this, NotificationSkipButtonHandler.class);
            PendingIntent buttonSkipPendingIntent = pendingIntent.getBroadcast(this, 0, buttonSkipIntent, 0);
            notificationView.setOnClickPendingIntent(R.id.notification_button_skip, buttonSkipPendingIntent);

            // Creo el PendingIntent para cuando se toque el boton Prev (anterior) y lo asocio a la correspondiente vista
            Intent buttonPrevIntent = new Intent(this, NotificationPrevButtonHandler.class);
            PendingIntent buttonPrevPendingIntent = pendingIntent.getBroadcast(this, 0, buttonPrevIntent, 0);
            notificationView.setOnClickPendingIntent(R.id.notification_button_prev, buttonPrevPendingIntent);

            // Creo el PendingIntent para cuando se toque el boton Close (cierre) y lo asocio a la correspondiente vista
            Intent buttonCloseIntent = new Intent(this, NotificationCloseButtonHandler.class);
            PendingIntent buttonClosePendingIntent = pendingIntent.getBroadcast(this, 0, buttonCloseIntent, 0);
            notificationView.setOnClickPendingIntent(R.id.notification_button_close, buttonClosePendingIntent);

            //Obtengo el icono de la Notificación
            Bitmap icon = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);




            //Genero la Notificación
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Player segundo plano")
                    .setTicker("Player segundo plano")
                    .setContentText("Música maestro")
                    .setSmallIcon(R.mipmap.ic_launcher)//icono peque : not plegada
                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false)) //icono grande : not desplegada
                    .setContent(notificationView)//la vista personalizada, con sus PendingIntentAsociados
                    .setContentIntent(pendingIntent)//la actividad a la que llamaremos si tocan la notificación
                    .build();// y se hace

            //lanzo el servicio haciendo visible la notificación
            //y la actividad de reproducción
            startForeground(Constantes.FOREGROUND_SERVICE, notification);
            play(this);
        }


        else if (intent_action.equals(Constantes.STOPFOREGROUND_ACTION))
        {
            Toast.makeText(this,"Parando servicio",Toast.LENGTH_SHORT).show();
            Log.i(getClass().getCanonicalName(), "Petición de parada recibida");

            //elimino el servicio del "foreground"
            stopForeground(true);
            //lo detengo
            stopSelf();
            //paro la música
            ForegroundService.stop();
        }


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(getClass().getCanonicalName(), "Destruyendo el Servicio");
    }

    @Override
    public IBinder onBind(Intent intent) {
        //devolvemos nul, ya que estamos implmentando un ForeGroundService (No un bounded)
        return null;
    }

    /**
     * Los siguientes Recievers están aquí, pero igualmente deberían estar aparte.
     *
     */

    /**
     * Reciever Invocado al tocar el boton play
     */
    public static class NotificationPlayButtonHandler extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context,"Play Seleccionado",Toast.LENGTH_SHORT).show();
            ForegroundService.play(context);

        }
    }

    /**
     * Reciever Invocado al tocar el boton SKIP
     */
    public static class NotificationSkipButtonHandler extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context,"Siguiente Seleccionado",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Reciever Invocado al tocar el boton atrás (prev)
     */
    public static class NotificationPrevButtonHandler extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context,"Prev Seleccionado",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Reciever Invocado al tocar el boton cerrar (prev)
     */
    public static class NotificationCloseButtonHandler extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context,"Close Seleccionado",Toast.LENGTH_SHORT).show();

            //Lanzo el intent para que se cierre el servicio
            Intent stopIntent = new Intent(context, ForegroundService.class);
            stopIntent.setAction(Constantes.STOPFOREGROUND_ACTION);
            context.startService(stopIntent);

        }
    }

}