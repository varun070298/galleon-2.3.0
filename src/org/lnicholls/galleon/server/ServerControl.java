package org.lnicholls.galleon.server;

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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.io.File;

import org.lnicholls.galleon.app.AppContext;
import org.lnicholls.galleon.app.AppDescriptor;
import org.lnicholls.galleon.database.Video;
import org.lnicholls.galleon.downloads.Download;

public interface ServerControl extends Remote {

	public void reset() throws RemoteException;

	public ServerConfiguration getServerConfiguration() throws RemoteException;

	public void updateServerConfiguration(ServerConfiguration serverConfiguration) throws RemoteException;

	public MusicPlayerConfiguration getMusicPlayerConfiguration() throws RemoteException;

	public void updateMusicPlayerConfiguration(MusicPlayerConfiguration musicPlayerConfiguration) throws RemoteException;

	public DataConfiguration getDataConfiguration() throws RemoteException;

	public void updateDataConfiguration(DataConfiguration dataConfiguration) throws RemoteException;

	public GoBackConfiguration getGoBackConfiguration() throws RemoteException;

	public void updateGoBackConfiguration(GoBackConfiguration goBackConfiguration) throws RemoteException;

	public DownloadConfiguration getDownloadConfiguration() throws RemoteException;

	public void updateDownloadConfiguration(DownloadConfiguration downloadConfiguration) throws RemoteException;

	public List getRecordings() throws RemoteException;

	public List getAppDescriptors() throws RemoteException;

	public List getApps() throws RemoteException;

	public List getTiVos() throws RemoteException;

	public void updateTiVos(List tivos) throws RemoteException;

	public void updateApp(AppContext app) throws RemoteException;

	public void removeApp(AppContext app) throws RemoteException;

	public void updateVideo(Video video) throws RemoteException;	public Video retrieveVideo(Video video) throws RemoteException;
	public void removeVideo(Video video) throws RemoteException;

	public AppContext createAppContext(AppDescriptor appDescriptor) throws RemoteException;

	public List getRules() throws RemoteException;

	public void updateRules(List rules) throws RemoteException;

	public List getWinampSkins() throws RemoteException;

	public List getSkins() throws RemoteException;

	public int getPort() throws RemoteException;

	public int getHttpPort() throws RemoteException;

	public List getPodcasts() throws RemoteException;

	public void setPodcasts(List list) throws RemoteException;

	public List getVideocasts() throws RemoteException;

	public void setVideocasts(List list) throws RemoteException;

	public boolean isCurrentVersion() throws RemoteException;

	public List getDownloads() throws RemoteException;

	public void pauseDownload(Download download) throws RemoteException;

	public void resumeDownload(Download download) throws RemoteException;

	public void stopDownload(Download download) throws RemoteException;

	public void setDisableTimeout(boolean value) throws RemoteException;

	public boolean isFileExists(String path) throws RemoteException;

	public void deleteFile(String path) throws RemoteException;

	public List getUpcomingCountries() throws RemoteException;

	public List getUpcomingStates(String countryId) throws RemoteException;

	public List getMetros(String stateId) throws RemoteException;
}