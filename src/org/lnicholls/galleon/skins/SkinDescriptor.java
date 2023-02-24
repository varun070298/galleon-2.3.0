package org.lnicholls.galleon.skins;

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

import java.util.ArrayList;

import org.apache.commons.lang.builder.ToStringBuilder;

public class SkinDescriptor extends Descriptor {
    public SkinDescriptor() {
        mApps = new ArrayList();
    }

    public String getVersion() {
        return mVersion;
    }

    public void setVersion(String value) {
        mVersion = value;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String value) {
        mTitle = value;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public void setReleaseDate(String value) {
        mReleaseDate = value;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String value) {
        mDescription = value;
    }

    public String getAuthorName() {
        return mAuthorName;
    }

    public void setAuthorName(String value) {
        mAuthorName = value;
    }

    public String getAuthorEmail() {
        return mAuthorEmail;
    }

    public void setAuthorEmail(String value) {
        mAuthorEmail = value;
    }

    public String getAuthorHomepage() {
        return mAuthorHomepage;
    }

    public void setAuthorHomepage(String value) {
        mAuthorHomepage = value;
    }

    public void getApps(java.util.List value) {
        mApps = value;
    }

    public java.util.List getApps() {
        return mApps;
    }

    public void addApp(App value) {
        mApps.add(value);
    }

    public App getApp(String id) {
        for (int i = 0; i < mApps.size(); i++) {
            App app = (App) mApps.get(i);
            if (app.getId().toLowerCase().equals(id.toLowerCase())) {
                return app;
            }
        }
        return null;
    }

    public String getImage(String appId, String screenId, String id) {
        if (appId != null) {
            App app = getApp(appId);
            if (app != null) {
                if (screenId != null) {
                    App.Screen screen = app.getScreen(screenId);
                    if (screen != null) {
                        if (id != null) {
                            Image image = screen.getImage(id);
                            if (image != null)
                                return image.getSource();
                        }
                    }
                }
                Image image = app.getImage(id);
                if (image != null)
                    return image.getSource();
            }
        }

        if (id != null) {
            Image image = getImage(id);
            if (image != null)
                return image.getSource();
        }
        return null;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public static class App extends Descriptor {
        public App() {
            mScreens = new ArrayList();
        }

        public String getId() {
            return mId;
        }

        public void setId(String value) {
            mId = value;
        }

        public Screen getScreen(String id) {
            for (int i = 0; i < mScreens.size(); i++) {
                Screen screen = (Screen) mScreens.get(i);
                if (screen.getId().toLowerCase().equals(id.toLowerCase()))
                    return screen;
            }
            return null;
        }

        public java.util.List getScreens() {
            return mScreens;
        }

        public void addScreen(Screen value) {
            mScreens.add(value);
        }

        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }

        public static class Screen extends Descriptor {
            public Screen() {
            }

            public String getId() {
                return mId;
            }

            public void setId(String value) {
                mId = value;
            }

            public String toString() {
                return ToStringBuilder.reflectionToString(this);
            }

            private String mId;
        }

        private String mId;

        private java.util.List mScreens;
    }

    private String mVersion;

    private String mTitle;

    private String mReleaseDate;

    private String mDescription;

    private String mAuthorName;

    private String mAuthorEmail;

    private String mAuthorHomepage;

    private java.util.List mApps;
}