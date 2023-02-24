package org.lnicholls.galleon.apps.audioScrobbler;

/*
 * Copyright (C) 2005 Leon Nicholls
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * 
 * See the file "COPYING" for more details.
 */

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.lnicholls.galleon.app.AppContext;
import org.lnicholls.galleon.app.AppFactory;
import org.lnicholls.galleon.database.Audio;
import org.lnicholls.galleon.database.PersistentValue;
import org.lnicholls.galleon.database.PersistentValueManager;
import org.lnicholls.galleon.server.Server;
import org.lnicholls.galleon.util.ReloadCallback;
import org.lnicholls.galleon.util.ReloadTask;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.widget.DefaultApplication;
import org.lnicholls.galleon.widget.DefaultScreen;

import com.tivo.hme.bananas.BText;
import com.tivo.hme.sdk.IHmeProtocol;
import com.tivo.hme.sdk.Resource;
import com.tivo.hme.interfaces.IContext;
import com.tivo.hme.interfaces.IArgumentList;

import de.nava.informa.core.ChannelBuilderIF;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.parsers.FeedParser;

public class AudioScrobbler extends DefaultApplication {

    private static Logger log = Logger.getLogger(AudioScrobbler.class.getName());

    public final static String TITLE = "Audio Scrobbler";

    private Resource mMenuBackground;

    private Resource mItemIcon;

    // AudioScrobbler client ID: Do not use this ID for your project
    // Get your own ID from http://www.audioscrobbler.com/development/protocol.php
    private static String CLIENT_ID = "tst";

    public void init(IContext context) throws Exception {
        super.init(context);

        mMenuBackground = getSkinImage("menu", "background");
        mItemIcon = getSkinImage(null, "icon");

        AudioScrobblerConfiguration audioScrobblerConfiguration = (AudioScrobblerConfiguration) ((AudioScrobblerFactory)getFactory()).getAppContext().getConfiguration();

        push(new AudioScrobblerScreen(this), TRANSITION_NONE);
    }

    public class AudioScrobblerScreen extends DefaultScreen {

        public AudioScrobblerScreen(AudioScrobbler app) {
            super(app, "Recent Tracks", false);

            getBelow().setResource(mMenuBackground);

            int start = TOP;

            BText submitText = new BText(getNormal(), BORDER_LEFT, TOP - 20, BODY_WIDTH, 20);
            submitText.setFlags(IHmeProtocol.RSRC_HALIGN_CENTER);
            submitText.setFont("default-18.font");
            submitText.setColor(Color.GREEN);
            submitText.setShadow(true);
            PersistentValue submitted = PersistentValueManager.loadPersistentValue(AudioScrobblerFactory.class
                    .getName()
                    + ".submitted");
            if (submitted == null)
                submitText.setValue("Number of tracks submitted: 0");
            else
                submitText.setValue("Number of tracks submitted: " + submitted.getValue());

            int max = mTracks.size() > 5 ? 5 : mTracks.size();
            for (int i = 0; i < max; i++) {
                ItemIF item = (ItemIF) mTracks.get(i);

                BText titleText = new BText(getNormal(), BORDER_LEFT, start, BODY_WIDTH, 60);
                titleText.setFlags(RSRC_HALIGN_LEFT | RSRC_TEXT_WRAP | RSRC_VALIGN_TOP);
                titleText.setFont("system-20-bold.font");
                titleText.setShadow(true);

                titleText.setValue(String.valueOf(i + 1) + ". " + item.getTitle());

                start += 60;
            }
        }
    }

    public static class AudioScrobblerFactory extends AppFactory implements ApplicationEventListener {

        public void updateAppContext(AppContext appContext) {
            super.updateAppContext(appContext);

            updateData();
        }

        private void updateData() {
            final AudioScrobblerConfiguration audioScrobblerConfiguration = (AudioScrobblerConfiguration) getAppContext()
                    .getConfiguration();

            new Thread() {
                public void run() {
                    try {
                        PersistentValue persistentValue = PersistentValueManager
                                .loadPersistentValue(AudioScrobblerFactory.this.getClass().getName());
                        if (PersistentValueManager.isAged(persistentValue)) {
                            String content = persistentValue == null ? null : persistentValue.getValue();
                            try {
                                String page = Tools.getPage(new URL("http://ws.audioscrobbler.com/rss/recent.php?user="
                                        + Tools.decrypt(audioScrobblerConfiguration.getUsername())));
                                if (page != null && page.length()>0)
                                    content = page;
                            } catch (Exception ex) {
                                log.error("Could get audio scrobbler data");
                            }

                            if (content != null) {
                                ChannelBuilderIF builder = new ChannelBuilder();
                                ChannelIF channel = FeedParser.parse(builder, new ByteArrayInputStream((content
                                        .getBytes())));

                                if (channel.getItems().size() > 0) {
                                    mTracks.clear();

                                    int count = 0;
                                    Iterator chs = channel.getItems().iterator();
                                    while (chs.hasNext()) {
                                        ItemIF item = (ItemIF) chs.next();
                                        mTracks.add(item);
                                    }

                                    int ttl = channel.getTtl();
                                    if (ttl < 10)
                						ttl = 60;
                					else
                						ttl = 60 * 6;

                                    PersistentValueManager.savePersistentValue(AudioScrobblerFactory.this.getClass()
                                            .getName(), content, ttl * 60);
                                    return;
                                }
                            }
                        }

                        if (persistentValue != null) {
                            ChannelBuilderIF builder = new ChannelBuilder();
                            ChannelIF channel = FeedParser.parse(builder, new ByteArrayInputStream((persistentValue
                                    .getValue().getBytes())));

                            mTracks.clear();

                            int count = 0;
                            Iterator chs = channel.getItems().iterator();
                            while (chs.hasNext()) {
                                ItemIF item = (ItemIF) chs.next();
                                mTracks.add(item);
                            }
                        }
                    } catch (Exception ex) {
                        Tools.logException(AudioScrobbler.class, ex, "Could not reload audio scrobbler data");
                    }
                }
            }.start();
        }

        public void handleApplicationEvent(ApplicationEvent appEvent) {
            System.out.println("handleApplicationEvent");

            Audio audio = appEvent.getAudio();
            if (audio != null) {
                // Dont submit streaming stations
                if (!audio.getPath().startsWith("http")) {
                    // Dont submit if it is less than 30 seconds
                    if (audio.getDuration() / 1000 >= 30) {
                        if (mCurrentAudio == null || !mCurrentAudio.getId().equals(audio.getId())) {
                            mCurrentAudio = audio;
                            mSubmitted = false;
                            mStarted = new Date();
                        }

                        int percentage = (int) (appEvent.getCurrentPosition() / (double) audio.getDuration() * 100);
                        System.out.println("percentage: " + percentage);
                        if (!mSubmitted && (appEvent.getCurrentPosition() / 1000 >= 240 || percentage >= 50)) {
                            System.out.println("Time: " + (appEvent.getCurrentPosition() / 1000) + "/"
                                    + (audio.getDuration() / 1000));
                            submit();
                        }
                    }
                }
            }
        }

        private void submit() {
            final AudioScrobblerConfiguration audioScrobblerConfiguration = (AudioScrobblerConfiguration) getAppContext()
                    .getConfiguration();

            new Thread() {
                public void run() {
                    HttpClient httpclient = new HttpClient();
                    httpclient.getParams().setParameter("http.socket.timeout", new Integer(30000));
                    httpclient.getParams().setParameter("http.useragent", System.getProperty("http.agent"));

                    //http://post.audioscrobbler.com/?hs=true&p=1.2&c=<clientid>&v=<clientversion>&u=<username>&t=<unix_timestamp>&a=<passcode>

                    GetMethod get = new GetMethod("http://post.audioscrobbler.com/");
                    get.setFollowRedirects(true);
                    NameValuePair param1 = new NameValuePair("hs", "true");
                    NameValuePair param2 = new NameValuePair("p", "1.2");
                    NameValuePair clientid = new NameValuePair("c", CLIENT_ID);
                    NameValuePair clientversion = new NameValuePair("v", "1.0"); // TODO
                    NameValuePair username = new NameValuePair("username", "1.0");

                    GregorianCalendar gregorianCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
                    long utc = gregorianCalendar.getTime().getTime();
                    NameValuePair timestamp = new NameValuePair("t", String.valueOf(utc));
                    NameValuePair passcode = new NameValuePair("a", Tools.md5(Tools.md5(audioScrobblerConfiguration
                            .getPassword()))
                            + String.valueOf(utc));
                    get.setQueryString(new NameValuePair[] { param1, param2, clientid, clientversion, username,
                            timestamp, passcode });

                    String radarurl = "";

                    try {
                        int iGetResultCode = httpclient.executeMethod(get);
                        final String strGetResponseBody = get.getResponseBodyAsString();

                        mSubmitted = true;
                        int count = 0;
                        PersistentValue persistentValue = PersistentValueManager
                                .loadPersistentValue(AudioScrobblerFactory.this.getClass().getName() + ".submitted");
                        if (persistentValue != null) {
                            count = Integer.parseInt(persistentValue.getValue());
                        }
                        PersistentValueManager.savePersistentValue(AudioScrobblerFactory.this.getClass().getName()
                                + ".submitted", String.valueOf(count));
                    } catch (Exception ex) {
                        Tools.logException(AudioScrobbler.class, ex);
                    }
                }
            }.start();
        }

        private void handshake() {
            AudioScrobblerConfiguration audioScrobblerConfiguration = (AudioScrobblerConfiguration) getAppContext()
                    .getConfiguration();

            HttpClient httpclient = new HttpClient();
            httpclient.getParams().setParameter("http.socket.timeout", new Integer(30000));
            httpclient.getParams().setParameter("http.useragent", System.getProperty("http.agent"));

            //http://post.audioscrobbler.com/?hs=true&p=1.2&c=<clientid>&v=<clientversion>&u=<username>&t=<unix_timestamp>&a=<passcode>

            GetMethod get = new GetMethod("http://post.audioscrobbler.com/");
            get.setFollowRedirects(true);
            NameValuePair param1 = new NameValuePair("hs", "true");
            NameValuePair param2 = new NameValuePair("p", "1.2");
            NameValuePair clientid = new NameValuePair("c", CLIENT_ID);
            NameValuePair clientversion = new NameValuePair("v", "1.0"); // TODO
            NameValuePair username = new NameValuePair("username", "1.0");

            GregorianCalendar gregorianCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            long utc = gregorianCalendar.getTime().getTime();
            NameValuePair timestamp = new NameValuePair("t", String.valueOf(utc));
            NameValuePair passcode = new NameValuePair("a", Tools.md5(Tools.md5(audioScrobblerConfiguration
                    .getPassword()))
                    + String.valueOf(utc));
            get.setQueryString(new NameValuePair[] { param1, param2, clientid, clientversion, username, timestamp,
                    passcode });

            String radarurl = "";

            try {
                int iGetResultCode = httpclient.executeMethod(get);
                String strGetResponseBody = get.getResponseBodyAsString();
                System.out.println(strGetResponseBody);

                mSubmitted = true;
            } catch (Exception ex) {
                Tools.logException(AudioScrobbler.class, ex);
            }
        }

        public void initialize()
        {
            DefaultApplication.addApplicationEventListener(this);

            Server.getServer().scheduleShortTerm(new ReloadTask(new ReloadCallback() {
                public void reload() {
                    try {
                        log.debug("Audioscrobbler");
                    	updateData();
                    } catch (Exception ex) {
                        log.error("Could not download data", ex);
                    }
                }
            }), 4);            

            AudioScrobblerConfiguration audioScrobblerConfiguration = (AudioScrobblerConfiguration) getAppContext()
                    .getConfiguration();
        }

        private Audio mCurrentAudio;

        private boolean mSubmitted;

        private Date mStarted;

        private List mNotSubmitted = new ArrayList();
    }

    private static List mTracks = new ArrayList();
}