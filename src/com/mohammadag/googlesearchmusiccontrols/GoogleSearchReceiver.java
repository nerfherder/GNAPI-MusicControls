package com.mohammadag.googlesearchmusiccontrols;

import java.io.DataOutputStream;
import java.io.IOException;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import java.lang.Runtime;


public class GoogleSearchReceiver extends BroadcastReceiver {
	
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent i=Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC);
		PackageManager pm = context.getPackageManager();
		final ResolveInfo mInfo = pm.resolveActivity(i, 0);
		String appName =(pm.getApplicationLabel(mInfo.activityInfo.applicationInfo)).toString();
		Log.w("playlist", appName);
		String queryText = intent.getStringExtra(GoogleSearchApi.KEY_QUERY_TEXT);
		if (queryText.contains("song")) {
			if (queryText.contains("what") && !queryText.startsWith("play")) {
				Intent shazam = new Intent("com.shazam.android.intent.actions.START_TAGGING");
				shazam.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(shazam);
			}
		}

		if (queryText.contains("listen to") || queryText.contains("play song")) {
			
			String songQuery = queryText.replace("listen to", "");
			songQuery = songQuery.replace("play song", "");
			songQuery = songQuery.replace("by", "");
			
			//Check if default music app is Google Play Music as it has functionality built in.
			if (!(appName.equals("Google Play Music"))) {
				
				Intent launchIntent = new Intent("android.media.action.MEDIA_PLAY_FROM_SEARCH");
				launchIntent.putExtra(SearchManager.QUERY, songQuery);
				launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(launchIntent);
			}
		}

		if (queryText.equals("play") || queryText.equals("play music")) {
			sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_PLAY);
		}

		if (queryText.contains("playback") || queryText.contains("music") || queryText.contains("song")) {
			if (queryText.startsWith("resume")) {
				sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_PLAY);
			}
			
			if (queryText.startsWith("pause")) {
				sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_PAUSE);
			}		
		}
		if (queryText.startsWith("stop")) {
			if (queryText.contains("playback") || queryText.contains("music")
					|| queryText.contains("playing")) {
				sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_STOP);
			}
		}

		if (queryText.contains("track") || queryText.contains("song")) {
			if (queryText.startsWith("next")) {
				sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_NEXT);
			} else if (queryText.startsWith("previous")) {
				sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
			}
		}
		
		if (queryText.startsWith("volume")) {
			AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			if (queryText.contains("up")) {
				audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
		                AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
			} else if (queryText.contains("down")) {
				audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
		                AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
			} else if (queryText.contains("max")) {
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 15, 0);
			}
		}
		if (queryText.contains("start playlist")) {

			String playlistQuery = queryText.replace("start playlist ", "");

    	    // open playlist
			Intent launchIntent = new Intent(Intent.ACTION_VIEW);
		    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    launchIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		    launchIntent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		    launchIntent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);		        
		    launchIntent.setType(MediaStore.Audio.Playlists.CONTENT_TYPE);
		    //get playlist id for intent
		    //cannot figure out how to start for playlist on google music... Can only open playlist
		    //playlist will auto start on stock player and winamp 
		    String playlist_id = getPlaylistId(playlistQuery, context, appName); 
		    launchIntent.putExtra("playlist", playlist_id);
		    context.startActivity(launchIntent);       
	
		}
	}
	
	private String getPlaylistId(String playlistName, Context context, String appName){
		String[] proj = {"", ""};
		Uri playlistUri;
		Log.w("playlist", appName);
		if (appName.equals("Google Play Music")) {
			Log.w("playlist", "google play music");
		    proj[0] = "playlist_id";
		    proj[1] = "playlist_name";
		    playlistUri = Uri.parse("content://com.google.android.music.MusicContent/playlists");
		}
		else {
			proj[0] = "_id";
			proj[1] = "name";
			playlistUri = Uri.parse("content://media/external/audio/playlists");
		}
	    ContentResolver resolver = context.getContentResolver();
	    Cursor playlistCursor = resolver.query(playlistUri, proj, null, null, null);
	    String playlist_name;
	    if (playlistCursor !=null) {
		    while (playlistCursor.moveToNext()){
		    	playlist_name = playlistCursor.getString(1);
			    if (playlistName.equalsIgnoreCase(playlist_name)) {
			    	String playlist_id = playlistCursor.getString(0);
   			    	return playlist_id;
			    }	
		    }
	    }
		return null;
		}
	
	private static void sendMediaKey(Context context, int key) {
		Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
		synchronized (context) {
			i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, key));
			context.sendOrderedBroadcast(i, null);

			i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, key));
			context.sendOrderedBroadcast(i, null);
		}
	}
}
